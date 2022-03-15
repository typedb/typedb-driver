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

extern crate grpc;
extern crate protocol;

mod common;
mod database;
mod rpc;
mod session;

use std::sync::Arc;
use crate::common::Result;
use crate::database::DatabaseManager;
use crate::rpc::client::RpcClient;

pub const DEFAULT_HOST: &str = "0.0.0.0";
pub const DEFAULT_PORT: u16 = 1729;

pub struct CoreClient {
    pub(crate) rpc_client: Arc<RpcClient>,
    pub databases: DatabaseManager,
}

impl CoreClient {
    pub fn new(host: &str, port: u16) -> Result<CoreClient> {
        let rpc_client = Arc::new(RpcClient::new(host, port)?);
        Ok(CoreClient {
            rpc_client: Arc::clone(&rpc_client),
            databases: DatabaseManager::new(Arc::clone(&rpc_client))
        })
    }

    fn close(&self) -> () {
        // TODO: close all sessions? or would they be dropped automatically?
    }
}

impl Drop for CoreClient {
    fn drop(&mut self) {
        self.close()
    }
}
