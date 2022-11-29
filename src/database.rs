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
    common::{error::MESSAGES, Result},
    rpc::{
        builder::core::{
            database::{delete_req, rule_schema_req, schema_req, type_schema_req},
            database_manager::{all_req, contains_req, create_req},
        },
        client::RpcClient,
    },
};
use std::fmt::{Display, Formatter};

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
#[derive(Clone)]
pub struct DatabaseManager {
    pub(crate) rpc_client: RpcClient,
}

impl DatabaseManager {
    pub(crate) fn new(rpc_client: &RpcClient) -> Self {
        DatabaseManager { rpc_client: rpc_client.clone() }
    }

    /// Retrieves a single [`Database`][Database] by name. Returns an [`Err`][Err] if there does not
    /// exist a database with the provided name.
    pub async fn get(&mut self, name: &str) -> Result<Database> {
        match self.contains(name).await? {
            true => Ok(Database::new(name, &self.rpc_client)),
            false => Err(MESSAGES.client.db_does_not_exist.to_err(vec![name])),
        }
    }

    pub async fn contains(&mut self, name: &str) -> Result<bool> {
        self.rpc_client.databases_contains(contains_req(name)).await.map(|res| res.contains)
    }

    pub async fn create(&mut self, name: &str) -> Result {
        self.rpc_client.databases_create(create_req(name)).await.map(|_| ())
    }

    pub async fn all(&mut self) -> Result<Vec<Database>> {
        self.rpc_client
            .databases_all(all_req())
            .await
            .map(|res| res.names.iter().map(|name| Database::new(name, &self.rpc_client)).collect())
    }
}

#[derive(Debug)]
pub struct Database {
    pub name: String,
    rpc_client: RpcClient,
}

impl Database {
    pub(crate) fn new(name: &str, rpc_client: &RpcClient) -> Self {
        Database { name: name.into(), rpc_client: rpc_client.clone() }
    }

    pub async fn delete(&mut self) -> Result {
        self.rpc_client.database_delete(delete_req(self.name.as_str())).await.map(|_| ())
    }

    pub async fn rule_schema(&mut self) -> Result<String> {
        self.rpc_client
            .database_rule_schema(rule_schema_req(self.name.as_str()))
            .await
            .map(|res| res.schema)
    }

    pub async fn schema(&mut self) -> Result<String> {
        self.rpc_client.database_schema(schema_req(self.name.as_str())).await.map(|res| res.schema)
    }

    pub async fn type_schema(&mut self) -> Result<String> {
        self.rpc_client
            .database_type_schema(type_schema_req(self.name.as_str()))
            .await
            .map(|res| res.schema)
    }
}

impl Display for Database {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.name)
    }
}
