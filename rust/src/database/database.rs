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
    fmt,
    sync::{Arc, RwLock},
    thread::sleep,
    time::Duration,
};

use itertools::Itertools;
use log::{debug, error};

use crate::{
    common::{
        address::Address,
        error::ConnectionError,
        info::{DatabaseInfo, ReplicaInfo},
        Error, Result,
    },
    connection::server_connection::ServerConnection,
    error::InternalError,
};

/// A TypeDB database
pub struct Database {
    name: String,
    replicas: RwLock<Vec<Replica>>,
    server_connections: HashMap<Address, ServerConnection>,
}

impl Database {
    const PRIMARY_REPLICA_TASK_MAX_RETRIES: usize = 10;
    const FETCH_REPLICAS_MAX_RETRIES: usize = 10;
    const WAIT_FOR_PRIMARY_REPLICA_SELECTION: Duration = Duration::from_secs(2);

    pub(super) fn new(
        database_info: DatabaseInfo,
        server_connections: HashMap<Address, ServerConnection>,
    ) -> Result<Self> {
        let name = database_info.name.clone();
        let replicas = RwLock::new(Replica::try_from_info(database_info, &server_connections)?);
        Ok(Self { name, replicas, server_connections })
    }

    /// Retrieves the database name as a string.
    pub fn name(&self) -> &str {
        self.name.as_str()
    }

    /// Returns the `Replica` instances for this database.
    /// _Only works in TypeDB Cloud_
    ///
    /// # Examples
    ///
    /// ```rust
    /// database.replicas_info()
    /// ```
    pub fn replicas_info(&self) -> Vec<ReplicaInfo> {
        self.replicas.read().unwrap().iter().map(Replica::to_info).collect()
    }

    /// Returns the primary replica for this database.
    /// _Only works in TypeDB Cloud_
    ///
    /// # Examples
    ///
    /// ```rust
    /// database.primary_replica_info()
    /// ```
    pub fn primary_replica_info(&self) -> Option<ReplicaInfo> {
        self.primary_replica().map(|replica| replica.to_info())
    }

    /// Returns the preferred replica for this database.
    /// Operations which can be run on any replica will prefer to use this replica.
    /// _Only works in TypeDB Cloud_
    ///
    /// # Examples
    ///
    /// ```rust
    /// database.preferred_replica_info();
    /// ```
    pub fn preferred_replica_info(&self) -> Option<ReplicaInfo> {
        self.preferred_replica().map(|replica| replica.to_info())
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
        self.run_on_primary_replica(|database| database.delete()).await
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
        self.run_failsafe(|database| async move { database.schema().await }).await
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
        self.run_failsafe(|database| async move { database.type_schema().await }).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn run_failsafe<F, P, R>(&self, task: F) -> Result<R>
    where
        F: Fn(ServerDatabase) -> P,
        P: Future<Output = Result<R>>,
    {
        match self.run_on_any_replica(&task).await {
            Err(Error::Connection(ConnectionError::CloudReplicaNotPrimary)) => {
                debug!("Attempted to run on a non-primary replica, retrying on primary...");
                self.run_on_primary_replica(&task).await
            }
            res => res,
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(super) async fn run_on_any_replica<F, P, R>(&self, task: F) -> Result<R>
    where
        F: Fn(ServerDatabase) -> P,
        P: Future<Output = Result<R>>,
    {
        let replicas = self.replicas.read().unwrap().clone();
        for replica in replicas {
            match task(replica.database.clone()).await {
                Err(Error::Connection(
                    ConnectionError::ServerConnectionFailedStatusError { .. } | ConnectionError::ConnectionFailed,
                )) => {
                    debug!("Unable to connect to {}. Attempting next server.", replica.server);
                }
                res => return res,
            }
        }
        Err(Self::unable_to_connect_error(&self.server_connections))
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(super) async fn run_on_primary_replica<F, P, R>(&self, task: F) -> Result<R>
    where
        F: Fn(ServerDatabase) -> P,
        P: Future<Output = Result<R>>,
    {
        let mut primary_replica =
            if let Some(replica) = self.primary_replica() { replica } else { self.seek_primary_replica().await? };

        for _ in 0..Self::PRIMARY_REPLICA_TASK_MAX_RETRIES {
            match task(primary_replica.database.clone()).await {
                Err(Error::Connection(
                    ConnectionError::CloudReplicaNotPrimary
                    | ConnectionError::ServerConnectionFailedStatusError { .. }
                    | ConnectionError::ConnectionFailed,
                )) => {
                    debug!("Primary replica error, waiting...");
                    Self::wait_for_primary_replica_selection().await;
                    primary_replica = self.seek_primary_replica().await?;
                }
                res => return res,
            }
        }
        Err(Self::unable_to_connect_error(&self.server_connections))
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn seek_primary_replica(&self) -> Result<Replica> {
        for _ in 0..Self::FETCH_REPLICAS_MAX_RETRIES {
            let replicas = Replica::fetch_all(self.name.clone(), &self.server_connections).await?;
            *self.replicas.write().unwrap() = replicas;
            if let Some(replica) = self.primary_replica() {
                return Ok(replica);
            }
            Self::wait_for_primary_replica_selection().await;
        }
        Err(Self::unable_to_connect_error(&self.server_connections))
    }

    fn unable_to_connect_error(server_connections: &HashMap<Address, ServerConnection>) -> Error {
        Error::Connection(ConnectionError::ServerConnectionFailed {
            addresses: server_connections.keys().map(Address::clone).collect_vec(),
        })
    }

    fn primary_replica(&self) -> Option<Replica> {
        self.replicas.read().unwrap().iter().filter(|r| r.is_primary).max_by_key(|r| r.term).cloned()
    }

    fn preferred_replica(&self) -> Option<Replica> {
        self.replicas.read().unwrap().iter().filter(|r| r.is_preferred).max_by_key(|r| r.term).cloned()
    }

    #[cfg(feature = "sync")]
    fn wait_for_primary_replica_selection() {
        sleep(Self::WAIT_FOR_PRIMARY_REPLICA_SELECTION);
    }

    #[cfg(not(feature = "sync"))]
    async fn wait_for_primary_replica_selection() {
        tokio::time::sleep(Self::WAIT_FOR_PRIMARY_REPLICA_SELECTION).await
    }
}

impl fmt::Debug for Database {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("Database").field("name", &self.name).field("replicas", &self.replicas).finish()
    }
}

/// The metadata and state of an individual raft replica of a database.
#[derive(Clone)]
pub(super) struct Replica {
    /// The server hosting this replica
    server: Address,
    /// Retrieves the database name for which this is a replica
    database_name: String,
    /// Checks whether this is the primary replica of the raft cluster.
    is_primary: bool,
    /// The raft protocol ‘term’ of this replica.
    term: i64,
    /// Checks whether this is the preferred replica of the raft cluster. If true, Operations which can be run on any replica will prefer to use this replica.
    is_preferred: bool,
    /// Retrieves the database for which this is a replica
    database: ServerDatabase,
}

impl Replica {
    fn new(name: String, metadata: ReplicaInfo, server_connection: ServerConnection) -> Self {
        Self {
            server: metadata.server,
            database_name: name.clone(),
            is_primary: metadata.is_primary,
            term: metadata.term,
            is_preferred: metadata.is_preferred,
            database: ServerDatabase::new(name, server_connection),
        }
    }

    fn try_from_info(
        database_info: DatabaseInfo,
        server_connections: &HashMap<Address, ServerConnection>,
    ) -> Result<Vec<Self>> {
        database_info
            .replicas
            .into_iter()
            .map(|replica| {
                if server_connections.len() == 1 {
                    Ok(Self::new(
                        database_info.name.clone(),
                        replica,
                        server_connections.values().next().unwrap().clone(),
                    ))
                } else {
                    // TODO: actually check the advertised == provided, if that is the strategy we want
                    let server_connection = server_connections
                        .get(&replica.server)
                        .ok_or_else(|| InternalError::UnknownServer { server: replica.server.clone() })?;
                    Ok(Self::new(database_info.name.clone(), replica, server_connection.clone()))
                }
            })
            .collect()
    }

    fn to_info(&self) -> ReplicaInfo {
        ReplicaInfo {
            server: self.server.clone(),
            is_primary: self.is_primary,
            is_preferred: self.is_preferred,
            term: self.term,
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn fetch_all(name: String, server_connections: &HashMap<Address, ServerConnection>) -> Result<Vec<Self>> {
        for (server, server_connection) in server_connections {
            let res = server_connection.get_database_replicas(name.clone()).await;
            match res {
                Ok(info) => {
                    return Self::try_from_info(info, server_connections);
                }
                Err(Error::Connection(
                    ConnectionError::DatabaseDoesNotExist { .. }
                    | ConnectionError::ServerConnectionFailedStatusError { .. }
                    | ConnectionError::ConnectionFailed,
                )) => {
                    error!(
                        "Failed to fetch replica info for database '{}' from {}. Attempting next server.",
                        name, server
                    );
                }
                Err(err) => return Err(err),
            }
        }
        Err(Database::unable_to_connect_error(server_connections))
    }
}

impl fmt::Debug for Replica {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("Replica")
            .field("server", &self.server)
            .field("database_name", &self.database_name)
            .field("is_primary", &self.is_primary)
            .field("term", &self.term)
            .field("is_preferred", &self.is_preferred)
            .finish()
    }
}

#[derive(Clone, Debug)]
pub(crate) struct ServerDatabase {
    name: String,
    connection: ServerConnection,
}

impl ServerDatabase {
    fn new(name: String, connection: ServerConnection) -> Self {
        Self { name, connection }
    }

    pub fn name(&self) -> &str {
        self.name.as_str()
    }

    pub(crate) fn connection(&self) -> &ServerConnection {
        &self.connection
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn delete(self) -> Result {
        self.connection.delete_database(self.name).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn schema(&self) -> Result<String> {
        self.connection.database_schema(self.name.clone()).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn type_schema(&self) -> Result<String> {
        self.connection.database_type_schema(self.name.clone()).await
    }
}

impl fmt::Display for ServerDatabase {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.name)
    }
}
