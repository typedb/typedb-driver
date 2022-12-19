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

use std::{fmt::Debug, time::Duration};

use futures::Stream;
use typedb_protocol::transaction as transaction_proto;

use crate::{
    common::{
        rpc::builder::transaction::{commit_req, open_req, rollback_req},
        Result, ServerRPC, TransactionRPC, TransactionType,
    },
    connection::core,
    query::QueryManager,
};

#[derive(Clone, Debug)]
pub struct Transaction {
    pub type_: TransactionType,
    pub options: core::Options,
    pub query: QueryManager,
    rpc: TransactionRPC,
}

impl Transaction {
    pub(crate) async fn new(
        session_id: &[u8],
        transaction_type: TransactionType,
        options: core::Options,
        network_latency: Duration,
        server_rpc: &ServerRPC,
    ) -> Result<Self> {
        let open_req = open_req(
            session_id.to_vec(),
            transaction_type.to_proto(),
            options.to_proto(),
            network_latency.as_millis() as i32,
        );
        let rpc = TransactionRPC::new(server_rpc, open_req).await?;
        Ok(Transaction { type_: transaction_type, options, query: QueryManager::new(&rpc), rpc })
    }

    pub async fn commit(&mut self) -> Result {
        self.single_rpc(commit_req()).await.map(|_| ())
    }

    pub async fn rollback(&mut self) -> Result {
        self.single_rpc(rollback_req()).await.map(|_| ())
    }

    pub(crate) async fn single_rpc(
        &mut self,
        req: transaction_proto::Req,
    ) -> Result<transaction_proto::Res> {
        self.rpc.single(req).await
    }

    pub(crate) fn streaming_rpc(
        &mut self,
        req: transaction_proto::Req,
    ) -> impl Stream<Item = Result<transaction_proto::ResPart>> {
        self.rpc.stream(req)
    }

    // TODO: refactor to delegate work to a background process
    pub async fn close(&self) {
        self.rpc.close().await;
    }
}
