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
use futures::StreamExt;
use grpc::{ClientRequestSink, GrpcStream, StreamingResponse};
use typedb_protocol::query::{QueryManager_Match_Req, QueryManager_Match_ResPart, QueryManager_Req, QueryManager_ResPart_oneof_res};
use typedb_protocol::transaction::{Transaction_Client, Transaction_Req, Transaction_Res, Transaction_ResPart, Transaction_ResPart_oneof_res, Transaction_Server, Transaction_Stream_State, Transaction_Type};
use uuid::Uuid;

use crate::common::error::ERRORS;
use crate::common::error::Error;
use crate::common::Result;
use crate::rpc::builder::transaction::{client_msg, open_req, commit_req, stream_req, rollback_req};
use crate::rpc::client::RpcClient;

#[derive(Copy, Clone)]
pub enum Type {
    Read = 0,
    Write = 1
}

impl From<Type> for Transaction_Type {
    fn from(transaction_type: Type) -> Self {
        match transaction_type {
            Type::Read => Transaction_Type::READ,
            Type::Write => Transaction_Type::WRITE
        }
    }
}

pub struct Transaction {
    pub transaction_type: Type,
    req_sink: ClientRequestSink<Transaction_Client>,
    res_stream: GrpcStream<Transaction_Server>
}

impl Transaction {
    // TODO: check if these borrows hamper ability to open transactions in parallel
    pub(crate) async fn new(session_id: &Vec<u8>, transaction_type: Type, network_latency_millis: u32, rpc_client: &RpcClient) -> Result<Self> {
        let open_req = open_req(session_id.clone(), Transaction_Type::from(transaction_type), network_latency_millis);
        let (mut req_sink, streaming_res): (ClientRequestSink<Transaction_Client>, StreamingResponse<Transaction_Server>) = rpc_client.transaction().await?;
        let mut res_stream = streaming_res.drop_metadata();
        Transaction::send_single_receive_single_in_stream(&mut req_sink, &mut res_stream, open_req).await?;
        Ok(Transaction { transaction_type, req_sink, res_stream })
    }

    pub(crate) async fn send_single_receive_single_in_stream(req_sink: &mut ClientRequestSink<Transaction_Client>, res_stream: &mut GrpcStream<Transaction_Server>, mut req: Transaction_Req) -> Result<Transaction_Res> {
        req.req_id = Uuid::new_v4().as_bytes().to_vec();
        req_sink.send_data(client_msg(vec![req])).map_err(|err| Error::from_grpc(err))?;
        match res_stream.next().await {
            Some(Ok(message)) => { println!("{:?}", message.clone()); Ok((message as Transaction_Server).take_res()) },
            Some(Err(err)) => { println!("{:?}", err); Err(Error::from_grpc(err)) },
            None => { println!("Response stream is empty"); Err(Error::new(ERRORS.client.transaction_closed)) }
        }
    }

    pub(crate) async fn send_single_receive_single(&mut self, req: Transaction_Req) -> Result<Transaction_Res> {
        Transaction::send_single_receive_single_in_stream(&mut self.req_sink, &mut self.res_stream, req).await
    }

    pub async fn query_match(&mut self, query: &str) -> Result<Vec<QueryManager_Match_ResPart>> {
        let mut req = Transaction_Req::new();
        let req_id = Uuid::new_v4().as_bytes().to_vec();
        req.req_id = req_id.clone();
        let mut query_manager_req = QueryManager_Req::new();
        let mut match_req = QueryManager_Match_Req::new();
        match_req.query = String::from(query);
        query_manager_req.set_match_req(match_req);
        req.set_query_manager_req(query_manager_req);

        self.req_sink.send_data(client_msg(vec![req])).map_err(|err| Error::from_grpc(err))?;
        let mut res_vec: Vec<QueryManager_Match_ResPart> = vec![];
        while let res = self.res_stream.next().await {
            match res {
                Some(Ok(ref message)) => {
                    println!("{:?}", message.clone());
                    match (message.clone() as Transaction_Server).take_res_part().res {
                        None => { return Err(Error::format(ERRORS.client.missing_response_field, vec!["res_part.res", format!("{:?}", res).as_str()])) }
                        Some(tx_res_part) => {
                            match tx_res_part {
                                Transaction_ResPart_oneof_res::query_manager_res_part(query_manager_res_part) => {
                                    match query_manager_res_part.res {
                                        None => { return Err(Error::format(ERRORS.client.missing_response_field, vec!["res_part.query_manager_res_part.res", format!("{:?}", res).as_str()])) }
                                        Some(query_manager_res_part) => {
                                            match query_manager_res_part {
                                                QueryManager_ResPart_oneof_res::match_res_part(match_res_part) => {
                                                    res_vec.push(match_res_part);
                                                }
                                                other => {
                                                    return Err(Error::format(ERRORS.client.unexpected_response, vec![format!("query_manager_res_part.{:?}", other).as_str(), "query_manager_req.match_req"]))
                                                }
                                            }
                                        }
                                    }
                                }
                                Transaction_ResPart_oneof_res::stream_res_part(stream_res_part) => {
                                    match stream_res_part.state {
                                        Transaction_Stream_State::CONTINUE => {
                                            self.req_sink.send_data(client_msg(vec![stream_req(req_id.clone())]));
                                        }
                                        Transaction_Stream_State::DONE => {
                                            break;
                                        }
                                    }
                                }
                                other => {
                                    return Err(Error::format(ERRORS.client.unexpected_response, vec![format!("{:?}", other).as_str(), "query_manager_req"]))
                                }
                            }
                        }
                    }
                }
                Some(Err(err)) => { println!("{:?}", err); return Err(Error::from_grpc(err)); }
                None => { println!("Response stream is empty"); return Err(Error::new(ERRORS.client.transaction_closed)); }
            }
        };
        Ok(res_vec)
    }

    pub async fn commit(&mut self) -> Result<Transaction_Res> {
        self.send_single_receive_single(commit_req()).await
    }

    pub async fn rollback(&mut self) -> Result<Transaction_Res> {
        self.send_single_receive_single(rollback_req()).await
    }
}

impl Drop for Transaction {
    fn drop(&mut self) {
        self.req_sink.finish().unwrap()
    }
}
