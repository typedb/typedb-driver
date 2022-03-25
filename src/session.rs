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

use futures::executor;
use std::sync::Arc;
use std::time::Instant;
use typedb_protocol::options::Options;
use typedb_protocol::session::Session_Type;

use crate::common::Result;
use crate::database::Database;
use crate::rpc::builder::session::{close_req, open_req};
use crate::rpc::client::RpcClient;
use crate::transaction;
use crate::transaction::Transaction;

#[derive(Copy, Clone, Debug)]
pub enum Type {
    Data = 0,
    Schema = 1
}

impl From<Type> for Session_Type {
    fn from(session_type: Type) -> Self {
        match session_type {
            Type::Data => Session_Type::DATA,
            Type::Schema => Session_Type::SCHEMA
        }
    }
}

pub struct Session {
    pub database: Database,
    pub session_type: Type,
    pub network_latency_millis: u32,
    pub(crate) session_id: Vec<u8>,
    pub(crate) rpc_client: Arc<RpcClient>
}

impl Session {
    pub(crate) async fn new(database: &str, session_type: Type, rpc_client: Arc<RpcClient>) -> Result<Self> {
        let start_time = Instant::now();
        let open_req = open_req(database, Session_Type::from(session_type), Options::new());
        let res = rpc_client.session_open(open_req).await?;
        Ok(Session {
            database: Database::new(String::from(database), Arc::clone(&rpc_client)),
            session_type,
            network_latency_millis: Session::compute_network_latency(start_time, res.server_duration_millis as u32),
            session_id: res.session_id,
            rpc_client
        })
    }

    pub async fn transaction(&self, transaction_type: transaction::Type) -> Result<Transaction> {
        Transaction::new(&self.session_id, transaction_type, self.network_latency_millis, &self.rpc_client).await
    }

    fn compute_network_latency(start_time: Instant, server_duration_millis: u32) -> u32 {
        ((Instant::now() - start_time).as_millis() as u32) - server_duration_millis
    }
}

impl Drop for Session {
    #[allow(unused_must_use)] /* we can safely ignore the result of the session_close request */
    fn drop(&mut self) {
        executor::block_on(self.rpc_client.session_close(close_req(self.session_id.clone())));
    }
}
