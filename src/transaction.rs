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

use std::fmt::Debug;
use std::time::Duration;
use futures::Stream;
use typedb_protocol::transaction as transaction_proto;

use crate::common::Result;
use crate::Options;
use crate::query::QueryManager;
use crate::rpc::builder::transaction::{open_req, commit_req, rollback_req};
use crate::rpc::client::RpcClient;
use crate::rpc::transaction::TransactionRpc;

#[derive(Copy, Clone, Debug)]
pub enum Type {
    Read = 0,
    Write = 1
}

impl Type {
    fn to_proto(&self) -> transaction_proto::Type {
        match self {
            Type::Read => transaction_proto::Type::Read,
            Type::Write => transaction_proto::Type::Write
        }
    }
}

#[derive(Clone, Debug)]
pub struct Transaction {
    pub type_: Type,
    pub options: Options,
    pub query: QueryManager,
    rpc: TransactionRpc,
}

impl Transaction {
    pub(crate) async fn new(session_id: &Vec<u8>, type_: Type, options: Options, network_latency: Duration, rpc_client: &RpcClient) -> Result<Self> {
        let open_req = open_req(
            session_id.clone(), type_.to_proto(), options.to_proto(), network_latency.as_millis() as i32
        );
        let rpc = TransactionRpc::new(rpc_client, open_req).await?;
        Ok(Transaction {
            type_,
            options,
            query: QueryManager::new(&rpc),
            rpc
        })
    }

    pub async fn commit(&mut self) -> Result {
        self.single_rpc(commit_req()).await.map(|_| ())
    }

    pub async fn rollback(&mut self) -> Result {
        self.single_rpc(rollback_req()).await.map(|_| ())
    }

    pub(crate) async fn single_rpc(&mut self, req: transaction_proto::Req) -> Result<transaction_proto::Res> {
        self.rpc.single(req).await
    }

    pub(crate) fn streaming_rpc(&mut self, req: transaction_proto::Req) -> impl Stream<Item = Result<transaction_proto::ResPart>> {
        self.rpc.stream(req)
    }

    // TODO: refactor to delegate work to a background process
    pub async fn close(&self) {
        self.rpc.close().await;
    }
}
