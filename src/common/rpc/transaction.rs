/*
 * Copyright (C) 2022 Vaticle
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

use std::{
    collections::HashMap,
    mem,
    pin::Pin,
    sync::{Arc, Mutex},
    task::{Context, Poll},
    thread::sleep,
    time::Duration,
};

use crossbeam::atomic::AtomicCell;
use futures::{
    channel::{mpsc, oneshot},
    SinkExt, Stream, StreamExt,
};
use tonic::Streaming;
use typedb_protocol::{
    transaction,
    transaction::{res::Res, res_part, server::Server, stream::State},
};

use crate::common::{
    error::{ClientError, Error},
    rpc::{
        builder::transaction::{client_msg, stream_req},
        ServerRPC,
    },
    Executor, Result,
};

// TODO: This structure has become pretty messy - review
#[derive(Clone, Debug)]
pub(crate) struct TransactionRPC {
    rpc_client: ServerRPC,
    sender: Sender,
    receiver: Receiver,
}

impl TransactionRPC {
    pub(crate) async fn new(rpc_client: &ServerRPC, open_req: transaction::Req) -> Result<Self> {
        let mut rpc_client_clone = rpc_client.clone();
        let (req_sink, streaming_res): (
            mpsc::Sender<transaction::Client>,
            Streaming<transaction::Server>,
        ) = rpc_client_clone.transaction(open_req).await?;
        let (close_signal_sink, close_signal_receiver) = oneshot::channel::<Option<Error>>();
        Ok(TransactionRPC {
            rpc_client: rpc_client_clone.clone(),
            sender: Sender::new(
                req_sink,
                rpc_client_clone.executor().clone(),
                close_signal_receiver,
            ),
            receiver: Receiver::new(streaming_res, rpc_client_clone.executor(), close_signal_sink)
                .await,
        })
    }

    pub(crate) async fn single(&mut self, req: transaction::Req) -> Result<transaction::Res> {
        if !self.is_open() {
            todo!()
        }
        let (res_sink, res_receiver) = oneshot::channel::<Result<transaction::Res>>();
        self.receiver.add_single(&req.req_id, res_sink);
        self.sender.submit_message(req);
        match res_receiver.await {
            Ok(result) => result,
            Err(err) => Err(Error::new(err.to_string())),
        }
    }

    pub(crate) fn stream(&mut self, req: transaction::Req) -> ResPartStream {
        const BUFFER_SIZE: usize = 1024;
        let (res_part_sink, res_part_receiver) =
            mpsc::channel::<Result<transaction::ResPart>>(BUFFER_SIZE);
        let (stream_req_sink, stream_req_receiver) = std::sync::mpsc::channel::<transaction::Req>();
        self.receiver.add_stream(&req.req_id, res_part_sink);
        let res_part_stream =
            ResPartStream::new(res_part_receiver, stream_req_sink, req.req_id.clone());
        self.sender.add_message_provider(stream_req_receiver);
        self.sender.submit_message(req);
        res_part_stream
    }

    pub(crate) fn is_open(&self) -> bool {
        self.sender.is_open()
    }

    pub(crate) async fn close(&self) {
        self.sender.close(None).await;
    }
}

#[derive(Clone, Debug)]
struct Sender {
    state: Arc<SenderState>,
    executor: Arc<Executor>,
}

#[derive(Debug)]
struct SenderState {
    req_sink: mpsc::Sender<transaction::Client>,
    // TODO: refactor to crossbeam_queue::ArrayQueue?
    queued_messages: Mutex<Vec<transaction::Req>>,
    // TODO: refactor to message passing for these atomics
    ongoing_task_count: AtomicCell<u8>,
    is_open: AtomicCell<bool>,
}

type ReqId = Vec<u8>;

impl SenderState {
    fn new(req_sink: mpsc::Sender<transaction::Client>) -> Self {
        SenderState {
            req_sink,
            queued_messages: Mutex::new(Vec::new()),
            ongoing_task_count: AtomicCell::new(0),
            is_open: AtomicCell::new(true),
        }
    }

    fn submit_message(&self, req: transaction::Req) {
        self.queued_messages.lock().unwrap().push(req);
    }

    async fn dispatch_loop(&self) {
        while self.is_open.load() {
            const DISPATCH_INTERVAL: Duration = Duration::from_millis(3);
            sleep(DISPATCH_INTERVAL);
            self.dispatch_messages().await;
        }
    }

    async fn dispatch_messages(&self) {
        self.ongoing_task_count.fetch_add(1);
        let messages = mem::take(&mut *self.queued_messages.lock().unwrap());
        if !messages.is_empty() {
            self.req_sink.clone().send(client_msg(messages)).await.unwrap();
        }
        self.ongoing_task_count.fetch_sub(1);
    }

    async fn await_close_signal(&self, close_signal_receiver: CloseSignalReceiver) {
        match close_signal_receiver.await {
            Ok(close_signal) => {
                self.close(close_signal).await;
            }
            Err(err) => {
                self.close(Some(Error::new(err.to_string()))).await;
            }
        }
    }

    async fn close(&self, error: Option<Error>) {
        if let Ok(true) = self.is_open.compare_exchange(true, false) {
            if error.is_none() {
                self.dispatch_messages().await;
            }
            // TODO: refactor to non-busy wait?
            // TODO: this loop should have a timeout
            loop {
                if self.ongoing_task_count.load() == 0 {
                    self.req_sink.clone().close().await.unwrap();
                    break;
                }
            }
        }
    }
}

impl Sender {
    pub(crate) fn new(
        req_sink: mpsc::Sender<transaction::Client>,
        executor: Arc<Executor>,
        close_signal_receiver: CloseSignalReceiver,
    ) -> Self {
        let state = Arc::new(SenderState::new(req_sink));
        // // TODO: clarify lifetimes of these threads
        executor.spawn_ok({
            let state = state.clone();
            async move {
                state.await_close_signal(close_signal_receiver).await;
            }
        });

        executor.spawn_ok({
            let state = state.clone();
            async move {
                state.dispatch_loop().await;
            }
        });

        Sender { state, executor }
    }

    fn submit_message(&self, req: transaction::Req) {
        self.state.submit_message(req);
    }

    fn add_message_provider(&self, provider: std::sync::mpsc::Receiver<transaction::Req>) {
        let cloned_state = self.state.clone();
        self.executor.spawn_ok(async move {
            for req in provider.iter() {
                cloned_state.submit_message(req);
            }
        });
    }

    fn is_open(&self) -> bool {
        self.state.is_open.load()
    }

    async fn close(&self, error: Option<Error>) {
        self.state.close(error).await
    }
}

#[derive(Clone, Debug)]
struct Receiver {
    state: Arc<ReceiverState>,
}

#[derive(Debug)]
struct ReceiverState {
    res_collectors: Mutex<HashMap<ReqId, ResCollector>>,
    res_part_collectors: Mutex<HashMap<ReqId, ResPartCollector>>,
    is_open: AtomicCell<bool>,
}

impl ReceiverState {
    fn new() -> Self {
        ReceiverState {
            res_collectors: Mutex::new(HashMap::new()),
            res_part_collectors: Mutex::new(HashMap::new()),
            is_open: AtomicCell::new(true),
        }
    }

    async fn listen(
        self: &Arc<Self>,
        mut grpc_stream: Streaming<transaction::Server>,
        close_signal_sink: CloseSignalSink,
    ) {
        loop {
            match grpc_stream.next().await {
                Some(Ok(message)) => {
                    self.clone().on_receive(message).await;
                }
                Some(Err(err)) => {
                    self.close(Some(err.into()), close_signal_sink).await;
                    break;
                }
                None => {
                    self.close(None, close_signal_sink).await;
                    break;
                }
            }
        }
    }

    async fn on_receive(&self, message: transaction::Server) {
        // TODO: If an error occurs here (or in some other background process), resources are not
        //  properly cleaned up, and the application may hang.
        match message.server {
            Some(Server::Res(res)) => self.collect_res(res),
            Some(Server::ResPart(res_part)) => {
                self.collect_res_part(res_part).await;
            }
            None => println!("{}", ClientError::MissingResponseField("server")),
        }
    }

    fn collect_res(&self, res: transaction::Res) {
        match self.res_collectors.lock().unwrap().remove(&res.req_id) {
            Some(collector) => collector.send(Ok(res)).unwrap(),
            None => {
                if let Res::OpenRes(_) = res.res.unwrap() {
                    // ignore open_res
                } else {
                    println!("{}", ClientError::UnknownRequestId(format!("{:?}", &res.req_id)))
                    // println!("{}", MESSAGES.client.unknown_request_id.to_err(
                    //     vec![std::str::from_utf8(&res.req_id).unwrap()])
                    // )
                }
            }
        }
    }

    async fn collect_res_part(&self, res_part: transaction::ResPart) {
        let value = self.res_part_collectors.lock().unwrap().remove(&res_part.req_id);
        match value {
            Some(mut collector) => {
                let req_id = res_part.req_id.clone();
                if collector.send(Ok(res_part)).await.is_ok() {
                    self.res_part_collectors.lock().unwrap().insert(req_id, collector);
                }
            }
            None => {
                let req_id_str = hex_string(&res_part.req_id);
                println!("{}", ClientError::UnknownRequestId(req_id_str));
            }
        }
    }

    async fn close(&self, error: Option<Error>, close_signal_sink: CloseSignalSink) {
        if let Ok(true) = self.is_open.compare_exchange(true, false) {
            let error_str = error.map(|err| err.to_string());
            for (_, collector) in self.res_collectors.lock().unwrap().drain() {
                collector.send(Err(close_reason(&error_str))).ok();
            }
            let mut res_part_collectors = Vec::new();
            for (_, res_part_collector) in self.res_part_collectors.lock().unwrap().drain() {
                res_part_collectors.push(res_part_collector)
            }
            for mut collector in res_part_collectors {
                collector.send(Err(close_reason(&error_str))).await.ok();
            }
            close_signal_sink.send(Some(close_reason(&error_str))).unwrap();
        }
    }
}

fn hex_string(v: &[u8]) -> String {
    v.iter().map(|b| format!("{:02X}", b)).collect::<String>()
}

fn close_reason(error_str: &Option<String>) -> Error {
    match error_str {
        None => ClientError::TransactionIsClosed(),
        Some(value) => ClientError::TransactionIsClosedWithErrors(value.clone()),
    }
    .into()
}

impl Receiver {
    async fn new(
        grpc_stream: Streaming<transaction::Server>,
        executor: &Executor,
        close_signal_sink: CloseSignalSink,
    ) -> Self {
        let state = Arc::new(ReceiverState::new());
        executor.spawn_ok({
            let state = state.clone();
            async move {
                state.listen(grpc_stream, close_signal_sink).await;
            }
        });
        Receiver { state }
    }

    fn add_single(&mut self, req_id: &ReqId, res_collector: ResCollector) {
        self.state.res_collectors.lock().unwrap().insert(req_id.clone(), res_collector);
    }

    fn add_stream(&mut self, req_id: &ReqId, res_part_sink: ResPartCollector) {
        self.state.res_part_collectors.lock().unwrap().insert(req_id.clone(), res_part_sink);
    }
}

type ResCollector = oneshot::Sender<Result<transaction::Res>>;
type ResPartCollector = mpsc::Sender<Result<transaction::ResPart>>;
type CloseSignalSink = oneshot::Sender<Option<Error>>;
type CloseSignalReceiver = oneshot::Receiver<Option<Error>>;

#[derive(Debug)]
pub(crate) struct ResPartStream {
    source: mpsc::Receiver<Result<transaction::ResPart>>,
    stream_req_sink: std::sync::mpsc::Sender<transaction::Req>,
    req_id: ReqId,
}

impl ResPartStream {
    fn new(
        source: mpsc::Receiver<Result<transaction::ResPart>>,
        stream_req_sink: std::sync::mpsc::Sender<transaction::Req>,
        req_id: ReqId,
    ) -> Self {
        ResPartStream { source, stream_req_sink, req_id }
    }
}

impl Stream for ResPartStream {
    type Item = Result<transaction::ResPart>;

    fn poll_next(mut self: Pin<&mut Self>, ctx: &mut Context<'_>) -> Poll<Option<Self::Item>> {
        let poll = Pin::new(&mut self.source).poll_next(ctx);
        match poll {
            Poll::Ready(Some(Ok(ref res_part))) => {
                match &res_part.res {
                    Some(res_part::Res::StreamResPart(stream_res_part)) => {
                        // TODO: unwrap -> expect("enum out of range")
                        match State::from_i32(stream_res_part.state).unwrap() {
                            State::Done => Poll::Ready(None),
                            State::Continue => {
                                let req_id = self.req_id.clone();
                                self.stream_req_sink.send(stream_req(req_id)).unwrap();
                                ctx.waker().wake_by_ref();
                                Poll::Pending
                            }
                        }
                    }
                    Some(_other) => poll,
                    None => panic!("{}", ClientError::MissingResponseField("res_part.res")),
                }
            }
            poll => poll,
        }
    }
}
