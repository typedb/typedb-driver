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

use crossbeam::atomic::AtomicCell;
use futures::executor;
use log::warn;
use std::{
    sync::{mpsc, Arc},
    time::{Duration, Instant},
};
use typedb_protocol::session as session_proto;

use crate::{
    common::{error::MESSAGES, Result},
    rpc::{
        builder::session::{close_req, open_req},
        client::RpcClient,
    },
    transaction,
    transaction::Transaction,
    Options,
};

#[derive(Copy, Clone, Debug)]
pub enum Type {
    Data = 0,
    Schema = 1,
}

impl Type {
    fn to_proto(&self) -> session_proto::Type {
        match self {
            Type::Data => session_proto::Type::Data,
            Type::Schema => session_proto::Type::Schema,
        }
    }
}

pub(super) type SessionId = Vec<u8>;

#[derive(Debug)]
pub struct Session {
    pub db_name: String,
    pub type_: Type,
    pub(crate) id: SessionId,
    pub(crate) rpc_client: RpcClient,
    is_open_atomic: AtomicCell<bool>,
    network_latency: Duration,
}

impl Session {
    pub(crate) async fn new(
        db_name: &str,
        type_: Type,
        options: Options,
        rpc_client: &RpcClient,
    ) -> Result<Self> {
        let start_time = Instant::now();
        let open_req = open_req(db_name, type_.to_proto(), options.to_proto());
        let mut rpc_client_clone = rpc_client.clone();
        let res = rpc_client_clone.session_open(open_req).await?;
        // TODO: pulse task
        Ok(Session {
            db_name: String::from(db_name),
            type_,
            network_latency: Self::compute_network_latency(start_time, res.server_duration_millis),
            id: res.session_id,
            rpc_client: rpc_client_clone,
            is_open_atomic: AtomicCell::new(true),
        })
    }

    pub async fn transaction(&self, type_: transaction::Type) -> Result<Transaction> {
        self.transaction_with_options(type_, Options::default()).await
    }

    pub async fn transaction_with_options(
        &self,
        type_: transaction::Type,
        options: Options,
    ) -> Result<Transaction> {
        match self.is_open() {
            true => {
                Transaction::new(&self.id, type_, options, self.network_latency, &self.rpc_client)
                    .await
            }
            false => Err(MESSAGES.client.session_is_closed.to_err(vec![])),
        }
    }

    pub fn is_open(&self) -> bool {
        self.is_open_atomic.load()
    }

    pub async fn close(&mut self) {
        if let Ok(true) = self.is_open_atomic.compare_exchange(true, false) {
            // let res = self.session_close_sink.send(self.id.clone());
            let res = self.rpc_client.session_close(close_req(self.id.clone())).await;
            // TODO: the request errors harmlessly if the session is already closed. Protocol should
            //       expose the cause of the error and we can use that to decide whether to warn here.
            if res.is_err() {
                warn!("{}", MESSAGES.client.session_close_failed.to_err(vec![]))
            }
        }
    }

    fn compute_network_latency(start_time: Instant, server_duration_millis: i32) -> Duration {
        Duration::from_millis(
            (Instant::now() - start_time).as_millis() as u64 - server_duration_millis as u64,
        )
    }
}

impl Drop for Session {
    fn drop(&mut self) {
        // TODO: this will stall in a single-threaded environment
        executor::block_on(self.close());
    }
}
