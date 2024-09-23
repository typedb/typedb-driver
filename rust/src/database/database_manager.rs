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
use std::{
    collections::HashMap,
    sync::{Arc, RwLock},
};

use super::Database;
use crate::{
    common::{address::Address, error::ConnectionError, Result},
    connection::server_connection::ServerConnection,
    info::DatabaseInfo,
    Error,
};

/// Provides access to all database management methods.
#[derive(Debug)]
pub struct DatabaseManager {
    server_connections: HashMap<Address, ServerConnection>,
    databases_cache: RwLock<HashMap<String, Arc<Database>>>,
}

/// Provides access to all database management methods.
impl DatabaseManager {
    pub(crate) fn new(
        server_connections: HashMap<Address, ServerConnection>,
        database_info: Vec<DatabaseInfo>,
    ) -> Result<Self> {
        let mut databases = HashMap::new();
        for info in database_info {
            let database = Database::new(info, server_connections.clone())?;
            databases.insert(database.name().to_owned(), Arc::new(database));
        }
        Ok(Self { server_connections, databases_cache: RwLock::new(databases) })
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
    pub async fn get(&self, name: impl AsRef<str>) -> Result<Arc<Database>> {
        let name = name.as_ref();
        if !self.contains(name.to_owned()).await? {
            self.databases_cache.write().unwrap().remove(name);
            return Err(ConnectionError::DatabaseDoesNotExist { name: name.to_owned() }.into());
        }
        Ok(self.databases_cache.read().unwrap().get(name).unwrap().clone())
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
        let database_info = self
            .run_failsafe(name, |server_connection, name| async move { server_connection.create_database(name).await })
            .await?;
        let database = Database::new(database_info, self.server_connections.clone())?;
        self.databases_cache.write().unwrap().insert(database.name().to_owned(), Arc::new(database));
        Ok(())
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
    pub async fn all(&self) -> Result<Vec<Arc<Database>>> {
        let mut error_buffer = Vec::with_capacity(self.server_connections.len());
        for (server_id, server_connection) in self.server_connections.iter() {
            match server_connection.all_databases().await {
                Ok(list) => {
                    let mut new_databases: Vec<Arc<Database>> = Vec::new();
                    for db_info in list {
                        new_databases.push(Arc::new(Database::new(db_info, self.server_connections.clone())?));
                    }
                    let mut databases = self.databases_cache.write().unwrap();
                    databases.clear();
                    databases
                        .extend(new_databases.iter().map(|database| (database.name().to_owned(), database.clone())));
                    return Ok(new_databases);
                }
                Err(err) => error_buffer.push(format!("- {}: {}", server_id, err)),
            }
        }
        Err(ConnectionError::ServerConnectionFailedWithError { error: error_buffer.join("\n") })?
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn get_cached_or_fetch(&self, name: &str) -> Result<Arc<Database>> {
        let databases = self.databases_cache.read().unwrap();
        if databases.contains_key(name) {
            return Ok(databases.get(name).unwrap().clone());
        } else {
            self.get(name).await
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn run_failsafe<F, P, R>(&self, name: String, task: F) -> Result<R>
    where
        F: Fn(ServerConnection, String) -> P,
        P: Future<Output = Result<R>>,
    {
        let mut error_buffer = Vec::with_capacity(self.server_connections.len());
        for (server_id, server_connection) in self.server_connections.iter() {
            match task(server_connection.clone(), name.clone()).await {
                Ok(res) => return Ok(res),
                // TODO: database manager should never encounter NOT PRIMARY errors since we are failing over server connections, not replicas

                // Err(Error::Connection(ConnectionError::CloudReplicaNotPrimary)) => {
                //     return Database::get(name, self.connection.clone())
                //         .await?
                //         .run_on_primary_replica(|database| {
                //             let task = &task;
                //             async move { task(database.connection().clone(), database.name().to_owned()).await }
                //         })
                //         .await
                // }
                err @ Err(Error::Connection(ConnectionError::ConnectionIsClosed)) => return err,
                Err(err) => error_buffer.push(format!("- {}: {}", server_id, err)),
            }
        }
        Err(ConnectionError::ServerConnectionFailedWithError { error: error_buffer.join("\n") })?
    }
}
