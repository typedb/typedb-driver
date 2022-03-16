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

use std::sync::Arc;
use crate::common::Result;
use crate::rpc::client::RpcClient;
use crate::rpc::builder::core::database::{delete_req, schema_req};
use crate::rpc::builder::core::database_manager::{all_req, contains_req, create_req};

pub struct DatabaseManager {
    pub(crate) rpc_client: Arc<RpcClient>
}

impl DatabaseManager {
    pub(crate) fn new(rpc_client: Arc<RpcClient>) -> Self {
        DatabaseManager { rpc_client }
    }

    pub fn contains(&self, name: &str) -> Result<bool> {
        self.rpc_client.databases_contains(contains_req(name)).map(|res| res.contains)
    }

    pub fn create(&self, name: &str) -> Result {
        self.rpc_client.databases_create(create_req(name)).map(|_| ())
    }

    pub fn all(&self) -> Result<Vec<Database>> {
        self.rpc_client.databases_all(all_req()).map(|res| res.names.iter()
            .map(|name| Database::new(String::from(name), Arc::clone(&self.rpc_client))).collect())
    }
}

pub struct Database {
    pub name: String,
    rpc_client: Arc<RpcClient>
}

impl Database {
    pub(crate) fn new(name: String, rpc_client: Arc<RpcClient>) -> Self {
        Database { name, rpc_client }
    }

    pub fn schema(&self) -> Result<String> {
        self.rpc_client.database_schema(schema_req(self.name.clone())).map(|res| res.schema)
    }

    pub fn delete(&self) -> Result {
        self.rpc_client.database_delete(delete_req(self.name.clone())).map(|_| ())
    }
}
