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

use std::future::Future;
use std::sync::Arc;
use grpc::GrpcStream;
use typedb_protocol::transaction::{Transaction_Client, Transaction_Req, Transaction_Server, Transaction_Type};

use crate::common::Result;
use crate::rpc::builder::transaction::{client_msg, open_req};
use crate::rpc::client::RpcClient;
use crate::session::Session;

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

pub struct Transaction<'a> {
    pub transaction_type: Type,
    req_stream: &'a GrpcStream<Transaction_Client>,
    res_stream: GrpcStream<Transaction_Server>
}

impl Transaction<'_> {
    // pub(crate) fn new(session_id: Vec<u8>, transaction_type: Type, network_latency_millis: u32, rpc_client: Arc<RpcClient>) -> Self {
    //     let open_req = client_msg(vec![open_req(session_id, Transaction_Type::from(transaction_type), network_latency_millis)]);
    //     let req_stream_wrapper = grpc::StreamingRequest::single(open_req);
    //     let req_stream = &req_stream_wrapper.0;
    //     let res_stream = rpc_client.transaction(req_stream_wrapper);
    //     Transaction { transaction_type, req_stream, res_stream }
    // }

    pub(crate) async fn execute(req: Transaction_Req) {

    }
}
