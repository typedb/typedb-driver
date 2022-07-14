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
use std::pin::Pin;
use std::sync::{Arc, Mutex};
use std::sync::atomic::{AtomicBool, AtomicU16, AtomicU8, Ordering};
use std::task::{Context, Poll};
use std::thread::sleep;
use std::time::{Duration, Instant, SystemTime};
use futures::{SinkExt, Stream, StreamExt, TryStreamExt};
use futures::channel::{mpsc, oneshot};
use futures::task::Spawn;
use grpc::{ClientRequestSink, GrpcStream, StreamingResponse};
use typedb_protocol::transaction::{Transaction_Client, Transaction_Req, Transaction_Res, Transaction_ResPart, Transaction_ResPart_oneof_res, Transaction_Server, Transaction_Server_oneof_server, Transaction_Stream_State};
use typedb_protocol::transaction::Transaction_Stream_State::{CONTINUE, DONE};
use uuid::Uuid;

use crate::common::error::{Error, ERRORS};
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
        Ok(
            TransactionRpc {
                sender: Sender::new(req_sink, Arc::clone(&rpc_client.executor)),
                receiver: Receiver::new(streaming_res.drop_metadata(), &rpc_client.executor).await,
            }
        )
    }

    pub(crate) async fn single(&mut self, mut req: Transaction_Req) -> Result<Transaction_Res> {
        req.req_id = Self::new_req_id();
        let (res_sink, res_receiver) = oneshot::channel::<Transaction_Res>();
        self.receiver.add_single(&req.req_id, res_sink);
        Sender::submit_message(req, Arc::clone(&self.sender.state));
        res_receiver.await.map_err(|err| Error::new(err.to_string()))
    }

    pub(crate) fn stream(&mut self, mut req: Transaction_Req) -> impl Stream<Item = Transaction_ResPart> {
        let req_id = Self::new_req_id();
        req.req_id = req_id.clone();
        const BUFFER_SIZE: usize = 256;
        let (res_part_sink, res_part_receiver) = mpsc::channel::<Transaction_ResPart>(BUFFER_SIZE);
        let (stream_req_sink, mut stream_req_receiver) = mpsc::channel::<Transaction_Req>(1);
        self.receiver.add_stream(&req_id, res_part_sink);
        Sender::add_message_provider(stream_req_receiver, Arc::clone(&self.sender.executor), Arc::clone(&self.sender.state));
        Sender::submit_message(req, Arc::clone(&self.sender.state));
        ResPartStream::new(res_part_receiver, stream_req_sink, req_id)
    }

    fn new_req_id() -> ReqId {
        Uuid::new_v4().as_bytes().to_vec()
    }

    // TODO: move receiver code from these commented methods to Receiver
    // pub(crate) async fn single_rpc(&self, mut req: Transaction_Req) -> Result<Transaction_Res> {
    //     self.req_sink.lock().await.send_data(client_msg(vec![req])).map_err(|err| Error::from_grpc(err))?;
    //     match self.res_stream.lock().await.next().await {
    //         Some(Ok(message)) => { /*println!("{:?}", message.clone());*/ Ok(message.clone().take_res()) },
    //         Some(Err(err)) => { println!("{:?}", err); Err(Error::from_grpc(err)) },
    //         None => { println!("Response stream is empty"); Err(ERRORS.client.transaction_closed.to_err(vec![])) }
    //     }
    // }

    // pub(crate) async fn streaming_rpc(&mut self, mut req: Transaction_Req) -> Result<Vec<Transaction_ResPart>> {
    //     let req_id = Uuid::new_v4().as_bytes().to_vec();
    //     req.req_id = req_id.clone();
    //     self.req_sink.lock().await.send_data(client_msg(vec![req])).map_err(|err| Error::from_grpc(err))?;
    //     let mut res_parts: Vec<Transaction_ResPart> = vec![];
    //     while let res = self.res_stream.lock().await.next().await {
    //         match res {
    //             Some(Ok(message)) => {
    //                 // println!("{:?}", message.clone());
    //                 let server = message.server
    //                     .ok_or_else(|| ERRORS.client.missing_response_field.to_err(vec!["server"]))?;
    //                 match server {
    //                     Transaction_Server_oneof_server::res_part(mut res_part) => {
    //                         if res_part.has_stream_res_part() {
    //                             match res_part.take_stream_res_part().state {
    //                                 CONTINUE => {
    //                                     self.req_sink.lock().await.send_data(client_msg(vec![stream_req(req_id.clone())]));
    //                                 }
    //                                 DONE => { break }
    //                             }
    //                         }
    //                         res_parts.push(res_part);
    //                     }
    //                     _ => {
    //                         return Err(ERRORS.client.missing_response_field.to_err(vec!["server.res_part"]))
    //                     }
    //                 }
    //             }
    //             Some(Err(err)) => { println!("{:?}", err); return Err(Error::from_grpc(err)); }
    //             // TODO: this probably occurs when the server closes the stream - test this
    //             None => { println!("Response stream is empty"); return Err(ERRORS.client.transaction_closed.to_err(vec![])); }
    //         }
    //     };
    //     Ok(res_parts)
    // }
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
    // last_dispatch_at: Mutex<Option<Instant>>,
    is_open: AtomicBool
}

impl SenderState {
    fn new(req_sink: ClientRequestSink<Transaction_Client>) -> Self {
        SenderState {
            req_sink: Mutex::new(req_sink),
            queued_messages: Mutex::new(vec![]),
            dispatch_is_scheduled: AtomicBool::new(false),
            ongoing_task_count: AtomicU8::new(0),
            // last_dispatch_at: Mutex::new(None),
            is_open: AtomicBool::new(true),
        }
    }
}

type ReqId = Vec<u8>;

impl Sender {
    pub(super) fn new(req_sink: ClientRequestSink<Transaction_Client>, executor: Arc<Executor>) -> Self {
        let state = Arc::new(SenderState::new(req_sink));
        let sender = Sender {
            state: Arc::clone(&state),
            executor: Arc::clone(&executor)
        };
        executor.spawn_ok(async move { Self::dispatch_loop(state) });
        sender
    }

    fn submit_message(req: Transaction_Req, state: Arc<SenderState>) {
        state.queued_messages.lock().unwrap().push(req.clone());
        println!("Submitted request message: {:?}", req);
        // match state.dispatch_is_scheduled.compare_exchange(
        //     false, true, Ordering::Acquire, Ordering::Relaxed
        // ) {
        //     Ok(false) => {
        //         println!("Spawning dispatch scheduler...");
        //         self.executor.spawn_ok(async { Self::schedule_dispatch(state); })
        //     }
        //     _ => {}
        // }
    }

    fn add_message_provider(mut provider: mpsc::Receiver<Transaction_Req>, executor: Arc<Executor>, state: Arc<SenderState>) {
        executor.spawn_ok(async move {
            while let Some(req) = provider.next().await {
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
        println!("Transaction rpc was closed; shutting down message dispatch loop")
    }

    fn dispatch_messages(state: Arc<SenderState>) {
        state.ongoing_task_count.fetch_add(1, Ordering::Relaxed);
        state.dispatch_is_scheduled.store(false, Ordering::Relaxed);
        let msgs = mem::take(&mut *state.queued_messages.lock().unwrap());
        if !msgs.is_empty() {
            let len = msgs.len();
            state.req_sink.lock().unwrap().send_data(client_msg(msgs));
            println!("Dispatched {} message(s)", len);
        }
        // state.last_dispatch_at.lock().unwrap().replace(Instant::now());
        state.ongoing_task_count.fetch_sub(1, Ordering::Relaxed);
    }

    // fn schedule_dispatch(state: Arc<SenderState>) {
    //     println!("Scheduling dispatch...");
    //     const DISPATCH_INTERVAL: Duration = Duration::from_millis(3);
    //     sleep(DISPATCH_INTERVAL);
    //     Self::dispatch_messages(state);
    // }
}

impl Drop for Sender {
    fn drop(&mut self) {
        Arc::clone(&self.state).is_open.store(false, Ordering::Relaxed);
        Self::dispatch_messages(Arc::clone(&self.state));
        // TODO: refactor to non-busy wait?
        // TODO: this loop should have a timeout
        loop {
            if self.state.ongoing_task_count.load(Ordering::Relaxed) == 0 {
                self.state.req_sink.lock().unwrap().finish();
                break;
            }
        }
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
    async fn new(grpc_stream: GrpcStream<Transaction_Server>, executor: &Executor) -> Self {
        let state = Arc::new(ReceiverState::new());
        let receiver = Receiver { state: Arc::clone(&state) };
        executor.spawn_ok(async move { Self::listen(grpc_stream, state).await; });
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
                collector.send(res).unwrap()
            }
            None => {
                println!("{}", ERRORS.client.unknown_request_id.to_err(
                    vec![std::str::from_utf8(res.get_req_id()).unwrap()])
                )
            }
        }
    }

    async fn listen(mut grpc_stream: GrpcStream<Transaction_Server>, state: Arc<ReceiverState>) {
        while let result = grpc_stream.next().await {
            match result {
                Some(Ok(message)) => { Self::on_receive(message, Arc::clone(&state)) }
                Some(Err(err)) => {
                    println!("{:?}", err);
                    // TODO: shutdown TransactionRpc
                    break;
                }
                // TODO: this probably occurs when the server closes the stream - test this
                None => {
                    println!("gRPC response stream closed");
                    break;
                }
            }
        }
    }

    fn on_receive(message: Transaction_Server, state: Arc<ReceiverState>) {
        match message.server {
            Some(server) => {
                match server {
                    Transaction_Server_oneof_server::res(res) => { Self::collect_res(res, state) }
                    Transaction_Server_oneof_server::res_part(res_part) => {
                        println!("{:?}", res_part)
                    }
                }
            }
            None => { println!("{}", ERRORS.client.missing_response_field.to_err(vec!["server"]).to_string()) }
        }
    }

    // fn on_error(&self, err: grpc::Error) -> Result {
    //
    // }
}

type ResCollector = oneshot::Sender<Transaction_Res>;

type ResPartCollector = mpsc::Sender<Transaction_ResPart>;

struct ResPartStream {
    source: mpsc::Receiver<Transaction_ResPart>,
    stream_req_sink: mpsc::Sender<Transaction_Req>,
    req_id: ReqId,
}

impl ResPartStream {
    fn new(source: mpsc::Receiver<Transaction_ResPart>, stream_req_sink: mpsc::Sender<Transaction_Req>, req_id: ReqId) -> Self {
        ResPartStream { source, stream_req_sink, req_id }
    }
}

impl Stream for ResPartStream {
    type Item = Transaction_ResPart;

    fn poll_next(mut self: Pin<&mut Self>, ctx: &mut Context<'_>) -> Poll<Option<Self::Item>> {
        let poll = Pin::new(&mut self.source).poll_next(ctx);
        match poll {
            Poll::Ready(Some(ref res_part)) => {
                match &res_part.res {
                    Some(Transaction_ResPart_oneof_res::stream_res_part(stream_res_part)) => {
                        match stream_res_part.state {
                            DONE => Poll::Ready(None),
                            CONTINUE => {
                                let req_id = self.req_id.clone();
                                self.stream_req_sink.send(stream_req(req_id));
                                Poll::Pending
                            }
                        }
                    }
                    Some(_other) => { poll }
                    None => {
                        panic!("{}", ERRORS.client.missing_response_field.to_err(vec!["res_part.res"]).to_string())
                    }
                }
            }
            Poll::Ready(None) => { poll }
            Poll::Pending => { poll }
        }
    }
}
