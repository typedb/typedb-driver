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
use typedb_protocol::transaction::{Transaction_Client, Transaction_Req, Transaction_Res, Transaction_Server, Transaction_Type};
use uuid::Uuid;

use crate::common::error::ERRORS;
use crate::common::error::Error;
use crate::common::Result;
use crate::rpc::builder::transaction::{client_msg, open_req, commit_req, rollback_req};
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
        Transaction::send_and_receive_in_stream(&mut req_sink, &mut res_stream, open_req).await?;
        Ok(Transaction { transaction_type, req_sink, res_stream })
    }

    pub(crate) async fn send_and_receive_in_stream(req_sink: &mut ClientRequestSink<Transaction_Client>, res_stream: &mut GrpcStream<Transaction_Server>, mut req: Transaction_Req) -> Result<Transaction_Res> {
        req.req_id = Uuid::new_v4().as_bytes().to_vec();
        req_sink.send_data(client_msg(vec![req])).map_err(|err| Error::from_grpc(err))?;
        match res_stream.next().await {
            Some(Ok(message)) => { println!("{:?}", message.clone()); Ok((message as Transaction_Server).get_res().clone()) },
            Some(Err(err)) => { println!("{:?}", err); Err(Error::from_grpc(err)) },
            None => { println!("Response stream is empty"); Err(Error::new(ERRORS.client.transaction_closed)) }
        }
    }

    pub(crate) async fn send_and_receive(&mut self, req: Transaction_Req) -> Result<Transaction_Res> {
        Transaction::send_and_receive_in_stream(&mut self.req_sink, &mut self.res_stream, req).await
    }

    pub async fn commit(&mut self) -> Result<Transaction_Res> {
        self.send_and_receive(commit_req()).await
    }

    pub async fn rollback(&mut self) -> Result<Transaction_Res> {
        self.send_and_receive(rollback_req()).await
    }

    pub async fn query_match(&mut self, query: &str) {
        // TODO
    }
}

impl Drop for Transaction {
    fn drop(&mut self) {
        self.req_sink.finish().unwrap()
    }
}
