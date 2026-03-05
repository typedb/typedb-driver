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

use std::{
    io::{BufWriter, Write},
    path::Path,
    sync::Arc,
};

use prost::Message;

use crate::{
    common::{info::DatabaseInfo, Error, Result},
    connection::server::{server_manager::ServerManager, server_routing::ServerRouting},
    database::migration::{try_create_export_file, try_open_existing_export_file, DatabaseExportAnswer},
    error::MigrationError,
    resolve
};

/// A TypeDB database.
#[derive(Debug, Clone)]
pub struct Database {
    name: String,
    server_manager: Arc<ServerManager>,
}

impl Database {
    pub(super) fn new(database_info: DatabaseInfo, server_manager: Arc<ServerManager>) -> Result<Self> {
        Ok(Self { name: database_info.name, server_manager })
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(super) async fn get(name: String, server_manager: Arc<ServerManager>) -> Result<Self> {
        Ok(Self { name, server_manager })
    }

    /// Retrieves the database name as a string.
    pub fn name(&self) -> &str {
        self.name.as_str()
    }

    /// Deletes this database.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "database.delete();")]
    #[cfg_attr(not(feature = "sync"), doc = "database.delete().await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn delete(self: Arc<Self>) -> Result {
        self.server_manager
            .execute(ServerRouting::Auto, |server_connection| {
                let name = self.name.clone();
                async move { server_connection.delete_database(name).await }
            })
            .await
    }

    /// Returns a full schema text as a valid TypeQL define query string.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "database.schema();")]
    #[cfg_attr(not(feature = "sync"), doc = "database.schema().await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn schema(&self) -> Result<String> {
        self.server_manager
            .execute(ServerRouting::Auto, |server_connection| {
                let name = self.name.clone();
                async move { server_connection.database_schema(name).await }
            })
            .await
    }

    /// Returns the types in the schema as a valid TypeQL define query string.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "database.type_schema();")]
    #[cfg_attr(not(feature = "sync"), doc = "database.type_schema().await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn type_schema(&self) -> Result<String> {
        self.server_manager
            .execute(ServerRouting::Auto, |server_connection| {
                let name = self.name.clone();
                async move { server_connection.database_type_schema(name).await }
            })
            .await
    }

    /// Export a database into a schema definition and a data files saved to the disk.
    /// This is a blocking operation and may take a significant amount of time depending on the database size.
    ///
    /// # Arguments
    ///
    /// * `schema_file_path` — The path to the schema definition file to be created
    /// * `data_file_path` — The path to the data file to be created
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "database.export_to_file(schema_path, data_path);")]
    #[cfg_attr(not(feature = "sync"), doc = "database.export_to_file(schema_path, data_path).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn export_to_file(&self, schema_file_path: impl AsRef<Path>, data_file_path: impl AsRef<Path>) -> Result {
        let schema_file_path = schema_file_path.as_ref();
        let data_file_path = data_file_path.as_ref();
        if schema_file_path == data_file_path {
            return Err(Error::Migration(MigrationError::CannotExportToTheSameFile));
        }

        let _ = try_create_export_file(schema_file_path)?;
        if let Err(err) = try_create_export_file(data_file_path) {
            let _ = std::fs::remove_file(schema_file_path);
            return Err(err);
        }

        let result = self
            .server_manager
            .execute(ServerRouting::Auto, |server_connection| {
                let name = self.name.clone();
                async move {
                    // File opening should be idempotent for multiple function invocations
                    let mut schema_file = try_open_existing_export_file(schema_file_path)?;
                    let data_file = try_open_existing_export_file(data_file_path)?;
                    let mut export_stream = server_connection.database_export(name).await?;
                    let mut data_writer = BufWriter::new(data_file);

                    loop {
                        match resolve!(export_stream.next())? {
                            DatabaseExportAnswer::Done => break,
                            DatabaseExportAnswer::Schema(schema) => {
                                schema_file.write_all(schema.as_bytes())?;
                                schema_file.flush()?;
                            }
                            DatabaseExportAnswer::Items(items) => {
                                for item in items {
                                    let mut buf = Vec::new();
                                    item.encode_length_delimited(&mut buf)
                                        .map_err(|_| Error::Migration(MigrationError::CannotEncodeExportedConcept))?;
                                    data_writer.write_all(&buf)?;
                                }
                            }
                        }
                    }

                    data_writer.flush()?;
                    Ok(())
                }
            })
            .await;

        if result.is_err() {
            let _ = std::fs::remove_file(schema_file_path);
            let _ = std::fs::remove_file(data_file_path);
        }
        result
    }
}
