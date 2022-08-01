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

use crate::common::Result;
use crate::rpc::builder::transaction::{open_req, commit_req, rollback_req};
use crate::rpc::client::RpcClient;
use crate::query::QueryManager;
use crate::rpc::transaction::TransactionRpc;

type TypeProto = typedb_protocol::transaction::Transaction_Type;

#[derive(Copy, Clone, Debug)]
pub enum Type {
    Read = 0,
    Write = 1
}

impl Type {
    fn to_proto(&self) -> TypeProto {
        match self {
            Type::Read => TypeProto::READ,
            Type::Write => TypeProto::WRITE
        }
    }
}

#[derive(Clone, Debug)]
pub struct Transaction {
    state: Arc<TransactionState>
}

#[derive(Debug)]
struct TransactionState {
    pub transaction_type: Type,
    pub query: QueryManager,
    rpc: Arc<Mutex<TransactionRpc>>,
}

impl TransactionState {
    fn new(transaction_type: Type, rpc: Arc<Mutex<TransactionRpc>>) -> TransactionState {
        TransactionState {
            transaction_type,
            rpc: Arc::clone(&rpc),
            query: QueryManager::new(rpc),
        }
    }
}

impl Transaction {
    // TODO: check if these borrows hamper ability to open transactions in parallel
    pub(crate) async fn new(session_id: &Vec<u8>, transaction_type: Type, network_latency: Duration, rpc_client: &RpcClient) -> Result<Self> {
        let open_req = open_req(
            session_id.clone(), transaction_type.to_proto(), network_latency.as_millis() as u32
        );
        let rpc: Arc<Mutex<TransactionRpc>> = Arc::new(Mutex::new(TransactionRpc::new(rpc_client).await?));
        rpc.lock().unwrap().single(open_req).await.unwrap();
        Ok(Transaction { state: Arc::new(TransactionState::new(transaction_type, rpc)) })
    }

    pub fn get_type(&self) -> Type {
        self.state.transaction_type
    }

    pub fn query(&self) -> &QueryManager {
        &self.state.query
    }

    pub async fn commit(&self) -> Result {
        self.state.rpc.lock().unwrap().single(commit_req()).await.map(|_| ())
    }

    pub async fn rollback(&self) -> Result {
        self.state.rpc.lock().unwrap().single(rollback_req()).await.map(|_| ())
    }
}
