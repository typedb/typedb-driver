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

use crossbeam::atomic::AtomicCell;
use futures::{
    channel::{mpsc, oneshot},
    executor, SinkExt, Stream, StreamExt,
};
use std::{
    collections::HashMap,
    mem,
    pin::Pin,
    sync::{Arc, Mutex},
    task::{Context, Poll},
    thread::sleep,
    time::Duration,
};
use tonic::Streaming;
use typedb_protocol::{
    transaction,
    transaction::{res::Res, res_part, server::Server, stream::State},
};
// use uuid::Uuid;

use crate::{
    common::{
        error::{Error, MESSAGES},
        Executor, Result,
    },
    rpc::{
        builder::transaction::{client_msg, stream_req},
        client::RpcClient,
    },
};

// TODO: This structure has become pretty messy - review
#[derive(Clone, Debug)]
pub(crate) struct TransactionRpc {
    rpc_client: RpcClient,
    sender: Sender,
    receiver: Receiver,
}

impl TransactionRpc {
    pub(crate) async fn new(rpc_client: &RpcClient, open_req: transaction::Req) -> Result<Self> {
        let mut rpc_client_clone = rpc_client.clone();
        let (req_sink, streaming_res): (
            mpsc::Sender<transaction::Client>,
            Streaming<transaction::Server>,
        ) = rpc_client_clone.transaction(open_req).await?;
        let (close_signal_sink, close_signal_receiver) = oneshot::channel::<Option<Error>>();
        Ok(TransactionRpc {
            rpc_client: rpc_client_clone.clone(),
            sender: Sender::new(
                req_sink,
                Arc::clone(&rpc_client_clone.executor),
                close_signal_receiver,
            ),
            receiver: Receiver::new(streaming_res, &rpc_client_clone.executor, close_signal_sink)
                .await,
        })
    }

    pub(crate) async fn single(&mut self, req: transaction::Req) -> Result<transaction::Res> {
        if !self.is_open() {
            todo!()
        }
        let (res_sink, res_receiver) = oneshot::channel::<Result<transaction::Res>>();
        self.receiver.add_single(&req.req_id, res_sink);
        Sender::submit_message(req, self.sender.state.clone());
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
        Sender::add_message_provider(
            stream_req_receiver,
            self.sender.executor.clone(),
            self.sender.state.clone(),
        );
        Sender::submit_message(req, self.sender.state.clone());
        res_part_stream
    }

    pub(crate) fn is_open(&self) -> bool {
        self.sender.state.is_open.load()
    }

    pub(crate) async fn close(&self) {
        Sender::close(self.sender.state.clone(), None).await;
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
            req_sink: req_sink.clone(),
            queued_messages: Mutex::new(vec![]),
            ongoing_task_count: AtomicCell::new(0),
            is_open: AtomicCell::new(true),
        }
    }
}

impl Sender {
    pub(super) fn new(
        req_sink: mpsc::Sender<transaction::Client>,
        executor: Arc<Executor>,
        close_signal_receiver: CloseSignalReceiver,
    ) -> Self {
        let state = Arc::new(SenderState::new(req_sink));
        // // TODO: clarify lifetimes of these threads
        let state2 = state.clone();
        executor.spawn_ok(async move {
            Self::await_close_signal(close_signal_receiver, state2).await;
        });
        let state3 = state.clone();
        executor.spawn_ok(async move {
            Self::dispatch_loop(state3).await;
        });
        Sender { state, executor: executor.clone() }
    }

    fn submit_message(req: transaction::Req, sender: Arc<SenderState>) {
        sender.queued_messages.lock().unwrap().push(req.clone());
    }

    fn add_message_provider(
        provider: std::sync::mpsc::Receiver<transaction::Req>,
        executor: Arc<Executor>,
        sender: Arc<SenderState>,
    ) {
        executor.spawn_ok(async move {
            let mut msg_iterator = provider.iter();
            while let Some(req) = msg_iterator.next() {
                Self::submit_message(req, sender.clone());
            }
        });
    }

    async fn dispatch_loop(sender: Arc<SenderState>) {
        while sender.is_open.load() {
            const DISPATCH_INTERVAL: Duration = Duration::from_millis(3);
            sleep(DISPATCH_INTERVAL);
            Self::dispatch_messages(sender.clone()).await;
        }
    }

    async fn dispatch_messages(sender: Arc<SenderState>) {
        sender.ongoing_task_count.fetch_add(1);
        let msgs = mem::take(&mut *sender.queued_messages.lock().unwrap());
        if !msgs.is_empty() {
            // let len = msgs.len();
            sender.req_sink.clone().send(client_msg(msgs)).await.unwrap();
        }
        sender.ongoing_task_count.fetch_sub(1);
    }

    async fn await_close_signal(
        close_signal_receiver: CloseSignalReceiver,
        sender: Arc<SenderState>,
    ) {
        match close_signal_receiver.await {
            Ok(close_signal) => {
                Self::close(sender, close_signal).await;
            }
            Err(err) => {
                Self::close(sender, Some(Error::new(err.to_string()))).await;
            }
        }
    }

    async fn close(sender: Arc<SenderState>, error: Option<Error>) {
        if let Ok(true) = sender.is_open.compare_exchange(true, false) {
            if let None = error {
                Self::dispatch_messages(sender.clone()).await;
            }
            // TODO: refactor to non-busy wait?
            // TODO: this loop should have a timeout
            loop {
                if sender.ongoing_task_count.load() == 0 {
                    sender.req_sink.clone().close().await.unwrap();
                    break;
                }
            }
        }
    }
}

impl Drop for Sender {
    fn drop(&mut self) {
        executor::block_on(Sender::close(self.state.clone(), None));
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
}

impl Receiver {
    async fn new(
        grpc_stream: Streaming<transaction::Server>,
        executor: &Executor,
        close_signal_sink: CloseSignalSink,
    ) -> Self {
        let state = Arc::new(ReceiverState::new());
        let receiver = Receiver { state: Arc::clone(&state) };
        executor.spawn_ok(async move {
            Self::listen(grpc_stream, state, close_signal_sink).await;
        });
        receiver
    }

    fn add_single(&mut self, req_id: &ReqId, res_collector: ResCollector) {
        self.state.res_collectors.lock().unwrap().insert(req_id.clone(), res_collector);
    }

    fn add_stream(&mut self, req_id: &ReqId, res_part_sink: ResPartCollector) {
        self.state.res_part_collectors.lock().unwrap().insert(req_id.clone(), res_part_sink);
    }

    fn collect_res(res: transaction::Res, state: Arc<ReceiverState>) {
        match state.res_collectors.lock().unwrap().remove(&res.req_id) {
            Some(collector) => collector.send(Ok(res)).unwrap(),
            None => {
                if let Res::OpenRes(_) = res.res.unwrap() {
                    // ignore open_res
                } else {
                    println!(
                        "{}",
                        MESSAGES.client.unknown_request_id.to_err(vec![format!(
                            "{:?}",
                            &res.req_id
                        )
                        .as_str()])
                    )
                    // println!("{}", MESSAGES.client.unknown_request_id.to_err(
                    //     vec![std::str::from_utf8(&res.req_id).unwrap()])
                    // )
                }
            }
        }
    }

    async fn collect_res_part(res_part: transaction::ResPart, state: Arc<ReceiverState>) {
        let value = state.res_part_collectors.lock().unwrap().remove(&res_part.req_id);
        match value {
            Some(mut collector) => {
                let req_id = res_part.req_id.clone();
                if let Ok(_) = collector.send(Ok(res_part)).await {
                    state.res_part_collectors.lock().unwrap().insert(req_id, collector);
                }
            }
            None => {
                // TODO: why does str::from_utf8 always fail here?
                // println!("{}", MESSAGES.client.unknown_request_id.to_err(
                //     vec![std::str::from_utf8(res_part.get_req_id()).unwrap()])
                // )
                let req_id_str = format!("{:?}", res_part.req_id);
                println!("{}", MESSAGES.client.unknown_request_id.to_err(vec![req_id_str.as_str()]))
            }
        }
    }

    async fn listen(
        mut grpc_stream: Streaming<transaction::Server>,
        state: Arc<ReceiverState>,
        close_signal_sink: CloseSignalSink,
    ) {
        loop {
            match grpc_stream.next().await {
                Some(Ok(message)) => {
                    Self::on_receive(message, Arc::clone(&state)).await;
                }
                Some(Err(err)) => {
                    Self::close(state, Some(err.into()), close_signal_sink).await;
                    break;
                }
                None => {
                    Self::close(state, None, close_signal_sink).await;
                    break;
                }
            }
        }
    }

    async fn on_receive(message: transaction::Server, state: Arc<ReceiverState>) {
        // TODO: If an error occurs here (or in some other background process), resources are not
        //  properly cleaned up, and the application may hang.
        match message.server {
            Some(Server::Res(res)) => Self::collect_res(res, state),
            Some(Server::ResPart(res_part)) => {
                Self::collect_res_part(res_part, state).await;
            }
            None => {
                println!(
                    "{}",
                    MESSAGES.client.missing_response_field.to_err(vec!["server"]).to_string()
                )
            }
        }
    }

    // fn on_error(&self, err: grpc::Error) -> Result {
    //
    // }

    async fn close(
        state: Arc<ReceiverState>,
        error: Option<Error>,
        close_signal_sink: CloseSignalSink,
    ) {
        if let Ok(true) = state.is_open.compare_exchange(true, false) {
            let error_str = error.map(|err| err.to_string());
            for (_, collector) in state.res_collectors.lock().unwrap().drain() {
                collector.send(Err(Self::close_reason(&error_str))).ok();
            }
            let mut res_part_collectors: Vec<ResPartCollector> = vec![];
            for (_, res_part_collector) in state.res_part_collectors.lock().unwrap().drain() {
                res_part_collectors.push(res_part_collector)
            }
            for mut collector in res_part_collectors {
                collector.send(Err(Self::close_reason(&error_str))).await.ok();
            }
            close_signal_sink.send(Some(Self::close_reason(&error_str))).unwrap();
        }
    }

    fn close_reason(error_str: &Option<String>) -> Error {
        match error_str {
            None => MESSAGES.client.transaction_is_closed.to_err(vec![]),
            Some(value) => {
                MESSAGES.client.transaction_is_closed_with_errors.to_err(vec![value.as_str()])
            }
        }
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
                    None => panic!(
                        "{}",
                        MESSAGES
                            .client
                            .missing_response_field
                            .to_err(vec!["res_part.res"])
                            .to_string()
                    ),
                }
            }
            Poll::Ready(Some(Err(_))) => poll,
            Poll::Ready(None) => poll,
            Poll::Pending => poll,
        }
    }
}
