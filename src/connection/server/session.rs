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

use std::time::{Duration, Instant};

use crossbeam::atomic::AtomicCell;
use futures::executor;
use log::warn;

use crate::{
    common::{
        error::ClientError,
        rpc::builder::session::{close_req, open_req},
        Result, ServerRPC, SessionType, TransactionType,
    },
    connection::{core, server::Transaction},
};

pub(crate) type SessionId = Vec<u8>;

#[derive(Debug)]
pub struct Session {
    pub db_name: String,
    pub session_type: SessionType,
    pub(crate) id: SessionId,
    pub(crate) server_rpc: ServerRPC,
    is_open_atomic: AtomicCell<bool>,
    network_latency: Duration,
}

impl Session {
    pub(crate) async fn new(
        db_name: &str,
        session_type: SessionType,
        options: core::Options,
        mut server_rpc: ServerRPC,
    ) -> Result<Self> {
        let start_time = Instant::now();
        let open_req = open_req(db_name, session_type.to_proto(), options.to_proto());
        let res = server_rpc.session_open(open_req).await?;
        // TODO: pulse task
        Ok(Session {
            db_name: String::from(db_name),
            session_type,
            network_latency: Self::compute_network_latency(start_time, res.server_duration_millis),
            id: res.session_id,
            server_rpc,
            is_open_atomic: AtomicCell::new(true),
        })
    }

    pub async fn transaction(&self, transaction_type: TransactionType) -> Result<Transaction> {
        self.transaction_with_options(transaction_type, core::Options::default()).await
    }

    pub async fn transaction_with_options(
        &self,
        transaction_type: TransactionType,
        options: core::Options,
    ) -> Result<Transaction> {
        match self.is_open() {
            true => {
                Transaction::new(
                    &self.id,
                    transaction_type,
                    options,
                    self.network_latency,
                    &self.server_rpc,
                )
                .await
            }
            false => Err(ClientError::SessionIsClosed())?,
        }
    }

    pub fn is_open(&self) -> bool {
        self.is_open_atomic.load()
    }

    pub async fn close(&mut self) {
        if let Ok(true) = self.is_open_atomic.compare_exchange(true, false) {
            // let res = self.session_close_sink.send(self.id.clone());
            let res = self.server_rpc.session_close(close_req(self.id.clone())).await;
            // TODO: the request errors harmlessly if the session is already closed. Protocol should
            //       expose the cause of the error and we can use that to decide whether to warn here.
            if res.is_err() {
                warn!("{}", ClientError::SessionCloseFailed())
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
