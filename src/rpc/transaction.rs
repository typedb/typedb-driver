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
use std::sync::{Arc, mpsc, Mutex};
use std::sync::atomic::{AtomicBool, AtomicU16, AtomicU8, Ordering};
use std::thread::sleep;
use std::time::Duration;
use futures::StreamExt;
use futures::task::Spawn;
use grpc::{ClientRequestSink, GrpcStream, StreamingResponse};
use tokio::sync::oneshot;
use typedb_protocol::transaction::{Transaction_Client, Transaction_Req, Transaction_Res, Transaction_ResPart, Transaction_Server, Transaction_Server_oneof_server};
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
        req.req_id = Uuid::new_v4().as_bytes().to_vec();
        let (res_sink, res_receiver) = oneshot::channel::<Transaction_Res>();
        self.receiver.single(req.req_id.clone(), res_sink);
        self.sender.push_message(req);
        res_receiver.await.map_err(|err| Error::new(err.to_string()))
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
    dispatcher_count: AtomicU8,
}

type ReqId = Vec<u8>;

impl Sender {
    pub(super) fn new(req_sink: ClientRequestSink<Transaction_Client>, executor: Arc<Executor>) -> Sender {
        Sender {
            state: Arc::new(
                SenderState {
                    req_sink: Mutex::new(req_sink),
                    queued_messages: Mutex::new(vec![]),
                    dispatch_is_scheduled: AtomicBool::new(false),
                    dispatcher_count: AtomicU8::new(0),
                }
            ),
            executor
        }
    }

    fn push_message(&mut self, req: Transaction_Req) {
        let state = Arc::clone(&self.state);
        state.queued_messages.lock().unwrap().push(req.clone());
        println!("Pushed request message: {:?}", req);
        match state.dispatch_is_scheduled.compare_exchange(
            false, true, Ordering::Acquire, Ordering::Relaxed
        ) {
            Ok(false) => {
                println!("Spawning dispatch scheduler...");
                self.executor.spawn_ok(async { Self::schedule_dispatch(state); })
            }
            _ => {}
        }
    }

    fn dispatch_messages(state: Arc<SenderState>) {
        state.dispatcher_count.fetch_add(1, Ordering::Relaxed);
        state.dispatch_is_scheduled.store(false, Ordering::Relaxed);
        let msgs = mem::take(&mut *state.queued_messages.lock().unwrap());
        state.req_sink.lock().unwrap().send_data(client_msg(msgs));
        state.dispatcher_count.fetch_sub(1, Ordering::Relaxed);
    }

    fn schedule_dispatch(state: Arc<SenderState>) {
        println!("Scheduling dispatch...");
        sleep(Duration::from_millis(3));
        Self::dispatch_messages(state);
    }
}

impl Drop for Sender {
    fn drop(&mut self) {
        Self::dispatch_messages(Arc::clone(&self.state));
        // TODO: refactor to non-busy wait
        // TODO: this loop should have a timeout
        loop {
            if self.state.dispatcher_count.load(Ordering::Relaxed) == 0 {
                self.state.req_sink.lock().unwrap().finish();
                break;
            }
        }
    }
}

struct Receiver {
    res_collectors: Arc<Mutex<HashMap<ReqId, oneshot::Sender<Transaction_Res>>>>
}

impl Receiver {
    async fn new(res_stream: GrpcStream<Transaction_Server>, executor: &Executor) -> Receiver {
        let res_collectors = Arc::new(Mutex::new(HashMap::new()));
        let receiver = Receiver {
            res_collectors: Arc::clone(&res_collectors)
        };
        executor.spawn_ok(async move { Self::listen(res_stream, Arc::clone(&res_collectors)).await; });
        receiver
    }

    fn single(&mut self, req_id: ReqId, res_sink: oneshot::Sender<Transaction_Res>) {
        self.res_collectors.lock().unwrap().insert(req_id.clone(), res_sink);
        println!("Created single receiver for req_id {:?}", req_id);
    }

    fn collect_res(res: Transaction_Res, res_collectors: Arc<Mutex<HashMap<ReqId, oneshot::Sender<Transaction_Res>>>>) {
        match res_collectors.lock().unwrap().remove(res.get_req_id()) {
            Some(collector) => {
                println!("Sending {:?} to the SINGLE collector for req ID {:?}", res.clone(), res.get_req_id());
                collector.send(res).unwrap()
            }
            None => {
                println!("{}", ERRORS.client.unknown_request_id.to_err(
                    vec![std::str::from_utf8(res.get_req_id()).unwrap()])
                )
            }
        }
    }

    async fn listen(mut grpc_stream: GrpcStream<Transaction_Server>, res_collectors: Arc<Mutex<HashMap<ReqId, oneshot::Sender<Transaction_Res>>>>) {
        while let result = grpc_stream.next().await {
            match result {
                Some(Ok(message)) => { Self::on_receive(message, Arc::clone(&res_collectors)) }
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

    fn on_receive(message: Transaction_Server, res_collectors: Arc<Mutex<HashMap<ReqId, oneshot::Sender<Transaction_Res>>>>) {
        match message.server {
            Some(server) => {
                match server {
                    Transaction_Server_oneof_server::res(res) => { Self::collect_res(res, res_collectors) }
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
