/*
 * Copyright (C) 2021 Vaticle
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

use std::collections::{HashMap, VecDeque};
use std::{mem, sync};
use std::future::Future;
use std::pin::Pin;
use std::sync::{Arc, Mutex};
use std::sync::atomic::{AtomicBool, AtomicU16, AtomicU8, Ordering};
use std::task::{Context, Poll};
use std::thread::sleep;
use std::time::{Duration, Instant, SystemTime};
use futures::{SinkExt, Stream, StreamExt, TryStreamExt};
use futures::channel::{mpsc, oneshot};
use futures::channel::oneshot::Canceled;
use futures::future::err;
use futures::task::Spawn;
use grpc::{ClientRequestSink, GrpcStream, StreamingResponse};
use typedb_protocol::transaction::{Transaction_Client, Transaction_Req, Transaction_Res, Transaction_ResPart, Transaction_ResPart_oneof_res, Transaction_Server, Transaction_Server_oneof_server, Transaction_Stream_State};
use typedb_protocol::transaction::Transaction_Stream_State::{CONTINUE, DONE};
use uuid::Uuid;

use crate::common::error::{Error, MESSAGES};
use crate::common::{Executor, Result};
use crate::rpc::builder::transaction::{client_msg, stream_req};
use crate::rpc::client::RpcClient;

pub(crate) struct TransactionRpc {
    sender: Sender,
    receiver: Receiver,
}

impl TransactionRpc {
    pub(crate) async fn new(rpc_client: &RpcClient) -> Result<Self> {
        let (req_sink, streaming_res): (
            ClientRequestSink<Transaction_Client>, StreamingResponse<Transaction_Server>
        ) = rpc_client.transaction().await?;
        let (close_signal_sink, close_signal_receiver) = oneshot::channel::<Option<Error>>();
        Ok(
            TransactionRpc {
                sender: Sender::new(req_sink, Arc::clone(&rpc_client.executor), close_signal_receiver),
                receiver: Receiver::new(streaming_res.drop_metadata(), &rpc_client.executor, close_signal_sink).await,
            }
        )
    }

    pub(crate) async fn single(&mut self, mut req: Transaction_Req) -> Result<Transaction_Res> {
        if !self.is_open() {  }
        req.req_id = Self::new_req_id();
        let (res_sink, res_receiver) = oneshot::channel::<Result<Transaction_Res>>();
        self.receiver.add_single(&req.req_id, res_sink);
        Sender::submit_message(req, Arc::clone(&self.sender.state));
        match res_receiver.await {
            Ok(result) => { result }
            Err(err) => { Err(Error::new(err.to_string())) }
        }
    }

    pub(crate) fn stream(&mut self, mut req: Transaction_Req) -> impl Stream<Item = Result<Transaction_ResPart>> {
        let req_id = Self::new_req_id();
        req.req_id = req_id.clone();
        const BUFFER_SIZE: usize = 256;
        let (res_part_sink, res_part_receiver) = mpsc::channel::<Result<Transaction_ResPart>>(BUFFER_SIZE);
        let (stream_req_sink, stream_req_receiver) = std::sync::mpsc::channel::<Transaction_Req>();
        self.receiver.add_stream(&req_id, res_part_sink);
        Sender::add_message_provider(stream_req_receiver, Arc::clone(&self.sender.executor), Arc::clone(&self.sender.state));
        Sender::submit_message(req, Arc::clone(&self.sender.state));
        ResPartStream::new(res_part_receiver, stream_req_sink, req_id)
    }

    pub(crate) fn is_open(&self) -> bool {
        self.sender.state.is_open.load(Ordering::Relaxed)
    }

    fn err_transaction_is_closed(&self) -> Result::Err {

    }

    fn new_req_id() -> ReqId {
        Uuid::new_v4().as_bytes().to_vec()
    }
}

struct Sender {
    state: Arc<SenderState>,
    executor: Arc<Executor>
}

struct SenderState {
    req_sink: Mutex<ClientRequestSink<Transaction_Client>>,
    queued_messages: Mutex<Vec<Transaction_Req>>,
    dispatch_is_scheduled: AtomicBool,
    ongoing_task_count: AtomicU8,
    is_open: AtomicBool
}

impl SenderState {
    fn new(req_sink: ClientRequestSink<Transaction_Client>) -> Self {
        SenderState {
            req_sink: Mutex::new(req_sink),
            queued_messages: Mutex::new(vec![]),
            dispatch_is_scheduled: AtomicBool::new(false),
            ongoing_task_count: AtomicU8::new(0),
            is_open: AtomicBool::new(true),
        }
    }
}

type ReqId = Vec<u8>;

impl Sender {
    pub(super) fn new(req_sink: ClientRequestSink<Transaction_Client>, executor: Arc<Executor>, close_signal_receiver: CloseSignalReceiver) -> Self {
        let state = Arc::new(SenderState::new(req_sink));
        let sender = Sender {
            state: Arc::clone(&state),
            executor: Arc::clone(&executor)
        };
        let state2 = Arc::clone(&state);
        executor.spawn_ok(async move {
            Self::await_close_signal(close_signal_receiver, state2).await;
        });
        executor.spawn_ok(async move { Self::dispatch_loop(state) });
        sender
    }

    fn submit_message(req: Transaction_Req, state: Arc<SenderState>) {
        state.queued_messages.lock().unwrap().push(req.clone());
        println!("Submitted request message: {:?}", req);
    }

    fn add_message_provider(provider: std::sync::mpsc::Receiver<Transaction_Req>, executor: Arc<Executor>, state: Arc<SenderState>) {
        executor.spawn_ok(async move {
            let mut msg_iterator = provider.iter();
            while let Some(req) = msg_iterator.next() {
                Self::submit_message(req, Arc::clone(&state));
            }
        });
    }

    fn dispatch_loop(state: Arc<SenderState>) {
        while state.is_open.load(Ordering::Relaxed) {
            const DISPATCH_INTERVAL: Duration = Duration::from_millis(3);
            sleep(DISPATCH_INTERVAL);
            Self::dispatch_messages(Arc::clone(&state));
        }
        println!("Transaction rpc was closed; message dispatch loop has been shut down")
    }

    fn dispatch_messages(state: Arc<SenderState>) {
        state.ongoing_task_count.fetch_add(1, Ordering::Relaxed);
        state.dispatch_is_scheduled.store(false, Ordering::Relaxed);
        let msgs = mem::take(&mut *state.queued_messages.lock().unwrap());
        if !msgs.is_empty() {
            let len = msgs.len();
            state.req_sink.lock().unwrap().send_data(client_msg(msgs)).unwrap();
            println!("Dispatched {} message(s)", len);
        }
        state.ongoing_task_count.fetch_sub(1, Ordering::Relaxed);
    }

    async fn await_close_signal(close_signal_receiver: CloseSignalReceiver, state: Arc<SenderState>) {
        match close_signal_receiver.await {
            Ok(close_signal) => { Self::close(state, close_signal) }
            Err(err) => { Self::close(state, Some(Error::new(err.to_string()))) }
        }
    }

    fn close(state: Arc<SenderState>, error: Option<Error>) {
        if let Ok(true) = state.is_open.compare_exchange(
            true, false, Ordering::Acquire, Ordering::Relaxed
        ) {
            if let None = error {
                Self::dispatch_messages(Arc::clone(&state));
            }
            // TODO: refactor to non-busy wait?
            // TODO: this loop should have a timeout
            loop {
                if state.ongoing_task_count.load(Ordering::Relaxed) == 0 {
                    state.req_sink.lock().unwrap().finish().unwrap();
                    break;
                }
            }
        }
    }
}

impl Drop for Sender {
    fn drop(&mut self) {
        Sender::close(Arc::clone(&self.state), None)
    }
}

struct Receiver {
    state: Arc<ReceiverState>,
}

struct ReceiverState {
    res_collectors: Mutex<HashMap<ReqId, ResCollector>>,
    res_part_collectors: Mutex<HashMap<ReqId, ResPartCollector>>
}

impl ReceiverState {
    fn new() -> Self {
        ReceiverState {
            res_collectors: Mutex::new(HashMap::new()),
            res_part_collectors: Mutex::new(HashMap::new())
        }
    }
}

impl Receiver {
    async fn new(grpc_stream: GrpcStream<Transaction_Server>, executor: &Executor, close_signal_sink: CloseSignalSink) -> Self {
        let state = Arc::new(ReceiverState::new());
        let receiver = Receiver { state: Arc::clone(&state) };
        executor.spawn_ok(async move { Self::listen(grpc_stream, state, close_signal_sink).await; });
        receiver
    }

    fn add_single(&mut self, req_id: &ReqId, res_collector: ResCollector) {
        self.state.res_collectors.lock().unwrap().insert(req_id.clone(), res_collector);
    }

    fn add_stream(&mut self, req_id: &ReqId, res_part_sink: ResPartCollector) {
        self.state.res_part_collectors.lock().unwrap().insert(req_id.clone(), res_part_sink);
    }

    fn collect_res(res: Transaction_Res, state: Arc<ReceiverState>) {
        match state.res_collectors.lock().unwrap().remove(res.get_req_id()) {
            Some(collector) => {
                println!("Received '{:?}': posting to the SINGLE collector for req ID {:?}", res.clone(), res.get_req_id());
                collector.send(Ok(res)).unwrap()
            }
            None => {
                println!("{}", MESSAGES.client.unknown_request_id.to_err(
                    vec![std::str::from_utf8(res.get_req_id()).unwrap()])
                )
            }
        }
    }

    async fn collect_res_part(res_part: Transaction_ResPart, state: Arc<ReceiverState>) {
        let value = state.res_part_collectors.lock().unwrap().remove(res_part.get_req_id());
        match value {
            Some(mut collector) => {
                let req_id = res_part.req_id.clone();
                collector.send(Ok(res_part)).await.unwrap();
                state.res_part_collectors.lock().unwrap().insert(req_id, collector);
            }
            None => {
                println!("{}", MESSAGES.client.unknown_request_id.to_err(
                    vec![std::str::from_utf8(res_part.get_req_id()).unwrap()])
                )
            }
        }
    }

    async fn listen(mut grpc_stream: GrpcStream<Transaction_Server>, state: Arc<ReceiverState>, close_signal_sink: CloseSignalSink) {
        loop {
            match grpc_stream.next().await {
                Some(Ok(message)) => { Self::on_receive(message, Arc::clone(&state)).await; }
                Some(Err(err)) => {
                    Self::close(state, Some(Error::from_grpc(err)), close_signal_sink).await;
                    break;
                }
                None => {
                    Self::close(state, None, close_signal_sink).await;
                    break;
                }
            }
        }
    }

    async fn on_receive(message: Transaction_Server, state: Arc<ReceiverState>) {
        match message.server {
            Some(Transaction_Server_oneof_server::res(res)) => {
                Self::collect_res(res, state)
            }
            Some(Transaction_Server_oneof_server::res_part(res_part)) => {
                Self::collect_res_part(res_part, state).await;
            }
            None => { println!("{}", MESSAGES.client.missing_response_field.to_err(vec!["server"]).to_string()) }
        }
    }

    // fn on_error(&self, err: grpc::Error) -> Result {
    //
    // }

    async fn close(state: Arc<ReceiverState>, error: Option<Error>, close_signal_sink: CloseSignalSink) {
        let error_str = error.map(|err| err.to_string());
        for (_, collector) in state.res_collectors.lock().unwrap().drain() {
            collector.send(Err(Self::close_reason(&error_str))).unwrap();
        }
        let mut res_part_collectors: Vec<ResPartCollector> = vec![];
        for (_, res_part_collector) in state.res_part_collectors.lock().unwrap().drain() {
            res_part_collectors.push(res_part_collector)
        }
        for mut collector in res_part_collectors {
            collector.send(Err(Self::close_reason(&error_str))).await.unwrap();
        }
        close_signal_sink.send(Some(Self::close_reason(&error_str))).unwrap();
    }

    fn close_reason(error_str: &Option<String>) -> Error {
        match error_str {
            None => { MESSAGES.client.transaction_is_closed.to_err(vec![]) }
            Some(value) => { MESSAGES.client.transaction_is_closed_with_errors.to_err(vec![value.as_str()]) }
        }
    }
}

type ResCollector = oneshot::Sender<Result<Transaction_Res>>;
type ResPartCollector = mpsc::Sender<Result<Transaction_ResPart>>;
type CloseSignalSink = oneshot::Sender<Option<Error>>;
type CloseSignalReceiver = oneshot::Receiver<Option<Error>>;

struct ResPartStream {
    source: mpsc::Receiver<Result<Transaction_ResPart>>,
    stream_req_sink: std::sync::mpsc::Sender<Transaction_Req>,
    req_id: ReqId,
}

impl ResPartStream {
    fn new(source: mpsc::Receiver<Result<Transaction_ResPart>>, stream_req_sink: std::sync::mpsc::Sender<Transaction_Req>, req_id: ReqId) -> Self {
        ResPartStream { source, stream_req_sink, req_id }
    }
}

impl Stream for ResPartStream {
    type Item = Result<Transaction_ResPart>;

    fn poll_next(mut self: Pin<&mut Self>, ctx: &mut Context<'_>) -> Poll<Option<Self::Item>> {
        let poll = Pin::new(&mut self.source).poll_next(ctx);
        match poll {
            Poll::Ready(Some(Ok(ref res_part))) => {
                match &res_part.res {
                    Some(Transaction_ResPart_oneof_res::stream_res_part(stream_res_part)) => {
                        match stream_res_part.state {
                            DONE => Poll::Ready(None),
                            CONTINUE => {
                                let req_id = self.req_id.clone();
                                self.stream_req_sink.send(stream_req(req_id)).unwrap();
                                Poll::Pending
                            }
                        }
                    }
                    Some(_other) => { poll }
                    None => {
                        panic!("{}", MESSAGES.client.missing_response_field.to_err(vec!["res_part.res"]).to_string())
                    }
                }
            }
            Poll::Ready(Some(Err(_))) => { poll }
            Poll::Ready(None) => { poll }
            Poll::Pending => { poll }
        }
    }
}
