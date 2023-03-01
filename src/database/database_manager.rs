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

use std::future::Future;

use super::{database::ServerDatabase, Database};
use crate::{
    common::{error::ConnectionError, Result},
    connection::ServerConnection,
    Connection,
};

#[derive(Clone, Debug)]
pub struct DatabaseManager {
    connection: Connection,
}

impl DatabaseManager {
    pub fn new(connection: Connection) -> Self {
        Self { connection }
    }

    pub async fn get(&self, name: impl Into<String>) -> Result<Database> {
        Database::get(name.into(), self.connection.clone()).await
    }

    pub async fn contains(&self, name: impl Into<String>) -> Result<bool> {
        self.run_failsafe(name.into(), move |database, server_connection, _| async move {
            server_connection.database_exists(database.name().to_owned()).await
        })
        .await
    }

    pub async fn create(&self, name: impl Into<String>) -> Result {
        self.run_failsafe(name.into(), |database, server_connection, _| async move {
            server_connection.create_database(database.name().to_owned()).await
        })
        .await
    }

    pub async fn all(&self) -> Result<Vec<Database>> {
        let mut error_buffer = Vec::with_capacity(self.connection.server_count());
        for server_connection in self.connection.connections() {
            match server_connection.all_databases().await {
                Ok(list) => {
                    return list.into_iter().map(|db_info| Database::new(db_info, self.connection.clone())).collect()
                }
                Err(err) => error_buffer.push(format!("- {}: {}", server_connection.address(), err)),
            }
        }
        Err(ConnectionError::ClusterAllNodesFailed(error_buffer.join("\n")))?
    }

    async fn run_failsafe<F, P, R>(&self, name: String, task: F) -> Result<R>
    where
        F: Fn(ServerDatabase, ServerConnection, bool) -> P,
        P: Future<Output = Result<R>>,
    {
        Database::get(name, self.connection.clone()).await?.run_failsafe(&task).await
    }
}
