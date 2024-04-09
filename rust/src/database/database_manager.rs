/*
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

#[cfg(not(feature = "sync"))]
use std::future::Future;

use super::Database;
use crate::{
    common::{error::ConnectionError, Result},
    connection::ServerConnection,
    Connection, Error,
};

/// Provides access to all database management methods.
#[derive(Clone, Debug)]
pub struct DatabaseManager {
    connection: Connection,
}

/// Provides access to all database management methods.
impl DatabaseManager {
    pub fn new(connection: Connection) -> Self {
        Self { connection }
    }

    /// Retrieve the database with the given name.
    ///
    /// # Arguments
    ///
    /// * `name` -- The name of the database to retrieve
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.databases().get(name);")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.databases().get(name).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn get(&self, name: impl Into<String>) -> Result<Database> {
        let name = name.into();
        if !self.contains(name.clone()).await? {
            return Err(ConnectionError::DatabaseDoesNotExist { name }.into());
        }
        Database::get(name, self.connection.clone()).await
    }

    /// Checks if a database with the given name exists
    ///
    /// # Arguments
    ///
    /// * `name` -- The database name to be checked
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.databases().contains(name);")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.databases().contains(name).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn contains(&self, name: impl Into<String>) -> Result<bool> {
        let name = name.into();
        self.run_failsafe(name, |server_connection, name| async move { server_connection.database_exists(name).await })
            .await
    }

    /// Create a database with the given name
    ///
    /// # Arguments
    ///
    /// * `name` -- The name of the database to be created
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.databases().create(name);")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.databases().create(name).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn create(&self, name: impl Into<String>) -> Result {
        let name = name.into();
        self.run_failsafe(name, |server_connection, name| async move { server_connection.create_database(name).await })
            .await
    }

    /// Retrieves all databases present on the TypeDB server
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.databases().all();")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.databases().all().await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn all(&self) -> Result<Vec<Database>> {
        let mut error_buffer = Vec::with_capacity(self.connection.server_count());
        for (server_id, server_connection) in self.connection.connections() {
            match server_connection.all_databases().await {
                Ok(list) => {
                    return list.into_iter().map(|db_info| Database::new(db_info, self.connection.clone())).collect()
                }
                Err(err) => error_buffer.push(format!("- {}: {}", server_id, err)),
            }
        }
        Err(ConnectionError::ServerConnectionFailedWithError { error: error_buffer.join("\n") })?
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn run_failsafe<F, P, R>(&self, name: String, task: F) -> Result<R>
    where
        F: Fn(ServerConnection, String) -> P,
        P: Future<Output = Result<R>>,
    {
        let mut error_buffer = Vec::with_capacity(self.connection.server_count());
        for (server_id, server_connection) in self.connection.connections() {
            match task(server_connection.clone(), name.clone()).await {
                Ok(res) => return Ok(res),
                Err(Error::Connection(ConnectionError::CloudReplicaNotPrimary)) => {
                    return Database::get(name, self.connection.clone())
                        .await?
                        .run_on_primary_replica(|database| {
                            let task = &task;
                            async move { task(database.connection().clone(), database.name().to_owned()).await }
                        })
                        .await
                }
                err @ Err(Error::Connection(ConnectionError::ConnectionIsClosed)) => return err,
                Err(err) => error_buffer.push(format!("- {}: {}", server_id, err)),
            }
        }
        Err(ConnectionError::ServerConnectionFailedWithError { error: error_buffer.join("\n") })?
    }
}
