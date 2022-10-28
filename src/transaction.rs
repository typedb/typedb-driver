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

use std::fmt::Debug;
use std::sync::{Arc, Mutex};
use std::time::Duration;
use futures::{SinkExt, Stream, StreamExt};
use tonic::Status;
use typedb_protocol::transaction as transaction_proto;
use typedb_protocol::transaction::res::Res;
use typedb_protocol::transaction::Server;

use crate::common::{Error, Result};
use crate::rpc::builder::transaction::{open_req, commit_req, rollback_req, client_msg};
use crate::rpc::client::RpcClient;
// use crate::query::QueryManager;
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

// #[derive(Clone, Debug)]
#[derive(Debug)]
pub struct Transaction {
    pub transaction_type: Type,
    // pub query: QueryManager,
    rpc: TransactionRpc,
}

impl Transaction {
    // TODO: check if this model lets us open transactions in parallel
    pub(crate) async fn new(session_id: &Vec<u8>, transaction_type: Type, network_latency: Duration, rpc_client: &RpcClient) -> Result<Self> {
        let open_req = open_req(
            session_id.clone(), transaction_type.to_proto(), network_latency.as_millis() as i32
        );
        let rpc = TransactionRpc::new(rpc_client, open_req).await?;
        // rpc_client.clone().transaction(session_id.clone(), transaction_type.to_proto(), network_latency.as_millis() as i32).await;
        // rpc.req_sink.send(client_msg(vec![open_req])).await.map_err(|err| Error::from(err))?;
        // rpc.req_sink.send(client_msg(vec![commit_req()])).await.map_err(|err| Error::from(err))?;
        // while let Some(result) = rpc.streaming_res.next().await {
        //     match result {
        //         Ok(srv) => {
        //             match srv.server.unwrap() {
        //                 transaction_proto::server::Server::Res(res) => {
        //                     match res.res.unwrap() {
        //                         Res::OpenRes(_) => { println!("got open res") }
        //                         Res::CommitRes(_) => {
        //                             println!("got commit res");
        //                             rpc.req_sink.close().await.map_err(|err| Error::from(err))?;
        //                         }
        //                         _ => { panic!() }
        //                     }
        //                 }
        //                 transaction_proto::server::Server::ResPart(_) => { panic!() }
        //             }
        //         }
        //         Err(_) => { rpc.req_sink.close().await.map_err(|err| Error::from(err))?; }
        //     }
        // }
        // rpc.single(open_req).await.unwrap();
        Ok(Transaction { transaction_type, rpc })
    }

    pub fn get_type(&self) -> Type {
        self.transaction_type
    }

    // pub fn query(&self) -> &QueryManager {
    //     &self.state.query
    // }

    pub async fn commit(&mut self) -> Result {
        self.single_rpc(commit_req()).await.map(|_| ())
    }

    pub async fn rollback(&mut self) -> Result {
        self.single_rpc(rollback_req()).await.map(|_| ())
    }

    pub(crate) async fn single_rpc(&mut self, req: transaction_proto::Req) -> Result<transaction_proto::Res> {
        self.rpc.single(req).await
    }

    // pub(crate) fn streaming_rpc(&self, req: transaction_proto::Req) -> impl Stream<Item = Result<transaction_proto::ResPart>> {
    //     self.state.rpc.lock().unwrap().stream(req)
    // }

    // TODO: refactor to delegate work to a background process so we can implement Drop
    pub async fn close(&self) {
        self.rpc.close().await;
    }
}
