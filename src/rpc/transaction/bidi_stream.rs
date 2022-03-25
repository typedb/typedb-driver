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

use std::sync::Arc;
// TODO: Maybe we should standardise and use 'tokio' everywhere instead of 'futures'?
use futures::lock::Mutex;
use futures::{executor, StreamExt};
use grpc::{ClientRequestSink, GrpcStream, StreamingResponse};
use typedb_protocol::transaction::{Transaction_Client, Transaction_Req, Transaction_Res, Transaction_ResPart, Transaction_Server, Transaction_Server_oneof_server, Transaction_Stream_State};
use typedb_protocol::transaction::Transaction_Stream_State::{CONTINUE, DONE};
use uuid::Uuid;

use crate::common::error::{Error, ERRORS};
use crate::common::Result;
use crate::rpc::builder::transaction::{client_msg, stream_req};
use crate::rpc::client::RpcClient;

pub(crate) struct BidiStream {
    req_sink: Arc<Mutex<ClientRequestSink<Transaction_Client>>>,
    res_stream: Arc<Mutex<GrpcStream<Transaction_Server>>>,
}

impl BidiStream {
    pub(crate) async fn new(rpc_client: &RpcClient) -> Result<Self> {
        let (req_sink, streaming_res): (ClientRequestSink<Transaction_Client>, StreamingResponse<Transaction_Server>) = rpc_client.transaction().await?;
        let res_stream = streaming_res.drop_metadata();
        Ok(BidiStream { req_sink: Arc::new(Mutex::new(req_sink)), res_stream: Arc::new(Mutex::new(res_stream)) })
    }

    pub(crate) async fn single_rpc(&self, mut req: Transaction_Req) -> Result<Transaction_Res> {
        req.req_id = Uuid::new_v4().as_bytes().to_vec();
        self.req_sink.lock().await.send_data(client_msg(vec![req])).map_err(|err| Error::from_grpc(err))?;
        match self.res_stream.lock().await.next().await {
            Some(Ok(message)) => { /*println!("{:?}", message.clone());*/ Ok(message.clone().take_res()) },
            Some(Err(err)) => { println!("{:?}", err); Err(Error::from_grpc(err)) },
            None => { println!("Response stream is empty"); Err(ERRORS.client.transaction_closed.to_err(vec![])) }
        }
    }

    pub(crate) async fn streaming_rpc(&mut self, mut req: Transaction_Req) -> Result<Vec<Transaction_ResPart>> {
        let req_id = Uuid::new_v4().as_bytes().to_vec();
        req.req_id = req_id.clone();
        self.req_sink.lock().await.send_data(client_msg(vec![req])).map_err(|err| Error::from_grpc(err))?;
        let mut res_parts: Vec<Transaction_ResPart> = vec![];
        while let res = self.res_stream.lock().await.next().await {
            match res {
                Some(Ok(message)) => {
                    // println!("{:?}", message.clone());
                    let server = message.server
                        .ok_or_else(|| ERRORS.client.missing_response_field.to_err(vec!["server"]))?;
                    match server {
                        Transaction_Server_oneof_server::res_part(mut res_part) => {
                            if res_part.has_stream_res_part() {
                                match res_part.take_stream_res_part().state {
                                    CONTINUE => {
                                        self.req_sink.lock().await.send_data(client_msg(vec![stream_req(req_id.clone())]));
                                    }
                                    DONE => { break }
                                }
                            }
                            res_parts.push(res_part);
                        }
                        _ => {
                            return Err(ERRORS.client.missing_response_field.to_err(vec!["server.res_part"]))
                        }
                    }
                }
                Some(Err(err)) => { println!("{:?}", err); return Err(Error::from_grpc(err)); }
                // TODO: this probably occurs when the server closes the stream - test this
                None => { println!("Response stream is empty"); return Err(ERRORS.client.transaction_closed.to_err(vec![])); }
            }
        };
        Ok(res_parts)
    }
}

impl Drop for BidiStream {
    fn drop(&mut self) {
        executor::block_on(self.req_sink.lock()).finish().unwrap()
    }
}
