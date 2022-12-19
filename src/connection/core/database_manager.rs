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

use crate::{
    common::{
        error::ClientError,
        rpc::builder::core::database_manager::{all_req, contains_req, create_req},
        CoreRPC, Result,
    },
    connection::server,
};

/// An interface for performing database-level operations against the connected server.
/// These operations include:
///
/// - Listing [all databases][DatabaseManager::all]
/// - Creating a [new database][DatabaseManager::create]
/// - Checking if a database [exists][DatabaseManager::contains]
/// - Retrieving a [specific database][DatabaseManager::get] in order to perform further operations on it
///
/// These operations all connect to the server to retrieve results. In the event of a connection
/// failure or other problem executing the operation, they will return an [`Err`][Err] result.
#[derive(Clone, Debug)]
pub struct DatabaseManager {
    pub(crate) core_rpc: CoreRPC,
}

impl DatabaseManager {
    pub(crate) fn new(core_rpc: CoreRPC) -> Self {
        DatabaseManager { core_rpc }
    }

    /// Retrieves a single [`Database`][Database] by name. Returns an [`Err`][Err] if there does not
    /// exist a database with the provided name.
    pub async fn get(&mut self, name: &str) -> Result<server::Database> {
        match self.contains(name).await? {
            true => Ok(server::Database::new(name, self.core_rpc.clone().into())),
            false => Err(ClientError::DatabaseDoesNotExist(name.to_string()))?,
        }
    }

    pub async fn contains(&mut self, name: &str) -> Result<bool> {
        self.core_rpc.databases_contains(contains_req(name)).await.map(|res| res.contains)
    }

    pub async fn create(&mut self, name: &str) -> Result {
        self.core_rpc.databases_create(create_req(name)).await.map(|_| ())
    }

    pub async fn all(&mut self) -> Result<Vec<server::Database>> {
        self.core_rpc.databases_all(all_req()).await.map(|res| {
            res.names
                .iter()
                .map(|name| server::Database::new(name, self.core_rpc.clone().into()))
                .collect()
        })
    }
}
