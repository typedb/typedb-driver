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
    io::{BufReader, BufWriter, Cursor, Read},
    path::Path,
    sync::{Arc, RwLock},
};

use prost::{bytes::Buf, Message};
use typedb_protocol::migration::Item;

use super::Database;
use crate::{common::{address::Address, error::ConnectionError, Result}, connection::server_connection::ServerConnection, database::migration::{
    read_and_print_import_file, try_creating_export_file, try_opening_import_file, DatabaseExportAnswer,
}, error::MigrationError, info::DatabaseInfo, resolve, Error};

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

    /// Retrieve the database with the given name.
    ///
    /// # Arguments
    ///
    /// * `name` — The name of the database to retrieve
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
            return Err(ConnectionError::DatabaseNotFound { name: name.to_owned() }.into());
        }

        if let Some(cached_database) = self.try_get_cached(name) {
            return Ok(cached_database);
        }

        self.cache_insert(Database::get(name.to_owned(), self.server_connections.clone()).await?);
        Ok(self.try_get_cached(name).unwrap())
    }

    /// Checks if a database with the given name exists
    ///
    /// # Arguments
    ///
    /// * `name` — The database name to be checked
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
        self.run_failsafe(
            name,
            |server_connection, name| async move { server_connection.contains_database(name).await },
        )
        .await
    }

    /// Create a database with the given name
    ///
    /// # Arguments
    ///
    /// * `name` — The name of the database to be created
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
            .run_failsafe(name, |server_connection, name| async move { server_connection.create_database(name).await }) // TODO: run_failsafe produces additiona Connection error if the database name is incorrect. Is it ok?
            .await?;
        self.cache_insert(Database::new(database_info, self.server_connections.clone())?);
        Ok(())
    }

    /// Create a database with the given name based on previously exported another database's data
    ///
    /// # Arguments
    ///
    /// * `name` — The name of the database to be created
    /// * `schema` — The schema definition query string for the database
    /// * `data_file_path` — The exported database file to import the data from
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.databases().import(name, schema, path);")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.databases().import(name, schema, path).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn import(&self, name: impl Into<String>, schema: String, data_file_path: impl AsRef<Path>) -> Result {
        let name = name.into();
        let schema = schema.as_str();
        let data_file_path = data_file_path.as_ref();
        self.run_failsafe(name, |server_connection, name| async move {
            let data_file = try_opening_import_file(data_file_path)?;
            let mut data_reader = BufReader::new(data_file);
            let mut data_buffer = Vec::new();
            data_reader.read_to_end(&mut data_buffer)?; // TODO: Read only a part

            // TODO: Don't clone schema......
            let import_stream = server_connection.import_database(name, schema.to_string()).await?;
            let mut items = Vec::new();

            let mut cursor = Cursor::new(data_buffer);
            while cursor.has_remaining() {
                let item = Item::decode_length_delimited(&mut cursor)
                    .map_err(|_| Error::Migration(MigrationError::CannotDecodeImportedConcept))?;
                items.push(item);
            }
            println!("Collected import items: {items:?}");
            resolve!(import_stream.items(items))?; // TODO: Do it in parts

            resolve!(import_stream.done())?;
            Ok(())
        })
        .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn get_cached_or_fetch(&self, name: &str) -> Result<Arc<Database>> {
        match self.try_get_cached(name) {
            Some(cached_database) => Ok(cached_database),
            None => self.get(name).await,
        }
    }

    fn try_get_cached(&self, name: &str) -> Option<Arc<Database>> {
        self.databases_cache.read().unwrap().get(name).cloned()
    }

    fn cache_insert(&self, database: Database) {
        self.databases_cache.write().unwrap().insert(database.name().to_owned(), Arc::new(database));
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

                // Err(Error::Connection(ConnectionError::ClusterReplicaNotPrimary)) => {
                //     return Database::get(name, self.connection.clone())
                //         .await?
                //         .run_on_primary_replica(|database| {
                //             let task = &task;
                //             async move { task(database.connection().clone(), database.name().to_owned()).await }
                //         })
                //         .await
                // }
                err @ Err(Error::Connection(ConnectionError::ServerConnectionIsClosed)) => return err,
                Err(err) => error_buffer.push(format!("- {}: {}", server_id, err)),
            }
        }
        Err(ConnectionError::ServerConnectionFailedWithError { error: error_buffer.join("\n") })?
    }
}
