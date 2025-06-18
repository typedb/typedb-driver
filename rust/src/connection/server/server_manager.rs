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
    collections::{HashMap, HashSet},
    fmt,
    future::Future,
    sync::{Arc, RwLock},
    time::Duration,
};

use itertools::Itertools;
use log::debug;

use crate::{
    common::address::Address,
    connection::{
        message::{Request, Response},
        runtime::BackgroundRuntime,
        server::{server_connection::ServerConnection, server_replica::ServerReplica, Addresses},
    },
    error::{ConnectionError, InternalError},
    Credentials, DriverOptions, Error, Result,
};

pub(crate) struct ServerManager {
    // TODO: Merge ServerConnection with ServerReplica?
    server_connections: HashMap<Address, ServerConnection>,
    replicas: RwLock<HashSet<ServerReplica>>,
}

impl ServerManager {
    const PRIMARY_REPLICA_TASK_MAX_RETRIES: usize = 10;
    const FETCH_REPLICAS_MAX_RETRIES: usize = 10;
    const WAIT_FOR_PRIMARY_REPLICA_SELECTION: Duration = Duration::from_secs(2);

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn new(
        background_runtime: Arc<BackgroundRuntime>,
        addresses: Addresses,
        credentials: Credentials,
        driver_options: DriverOptions,
        driver_lang: impl AsRef<str>,
        driver_version: impl AsRef<str>,
    ) -> Result<Self> {
        let replicas = Self::fetch_server_list(
            background_runtime.clone(),
            &addresses,
            credentials.clone(),
            driver_options.clone(),
            driver_lang.as_ref(),
            driver_version.as_ref(),
        )
        .await?;
        let mut server_connections = HashMap::new();
        for replica in &replicas {
            let (server_connection, _) = ServerConnection::new(
                background_runtime.clone(),
                replica.address().clone(),
                credentials.clone(),
                driver_options.clone(),
                driver_lang.as_ref(),
                driver_version.as_ref(),
            )
            .await?;
            server_connections.insert(replica.address().clone(), server_connection);
        }

        if server_connections.is_empty() {
            return Err(ConnectionError::ServerConnectionFailed { addresses }.into());
        }

        Ok(Self { server_connections, replicas: RwLock::new(replicas) })
    }

    pub(crate) fn force_close(&self) -> Result {
        self.server_connections.values().map(ServerConnection::force_close).try_collect().map_err(Into::into)
    }

    pub(crate) async fn servers_all(&self) -> Result<Vec<ServerReplica>> {
        self.run_failsafe(|server_connection| async move {
            server_connection.servers_all()
        }).await
    }

    pub(crate) fn server_count(&self) -> usize {
        self.server_connections.len()
    }

    pub(crate) fn servers(&self) -> impl Iterator<Item = &Address> {
        self.server_connections.keys()
    }

    pub(crate) fn connection(&self, address: &Address) -> Option<&ServerConnection> {
        self.server_connections.get(address)
    }

    pub(crate) fn connections(&self) -> impl Iterator<Item = (&Address, &ServerConnection)> + '_ {
        self.server_connections.iter()
    }

    // TODO: Implement everything below

    // #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    // pub(crate) async fn run_failsafe<F, P, R>(&self, task: F) -> Result<R>
    // where
    //     F: Fn(crate::database::database::ServerDatabase) -> P,
    //     P: Future<Output = Result<R>>,
    // {
    //     match self.run_on_any_replica(&task).await {
    //         Err(Error::Connection(ConnectionError::ClusterReplicaNotPrimary)) => {
    //             debug!("Attempted to run on a non-primary replica, retrying on primary...");
    //             self.run_on_primary_replica(&task).await
    //         }
    //         res => res,
    //     }
    // }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn run_failsafe<F, P, R>(&self, task: F) -> Result<R>
    where
        F: Fn(ServerConnection) -> P,
        P: Future<Output = Result<R>>,
    {
        let mut error_buffer = Vec::with_capacity(self.server_connections.len());
        for (server_id, server_connection) in self.server_connections.iter() {
            match task(server_connection.clone()).await {
                Ok(res) => return Ok(res),
                // TODO: Refactor errors
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
        // TODO: With this, every operation fails with
        // [CXN03] Connection Error: Unable to connect to TypeDB server(s), received errors: .... <stacktrace>
        // Which is quite confusing as it's not really connected to connection.
        Err(ConnectionError::ServerConnectionFailedWithError { error: error_buffer.join("\n") })?
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(super) async fn run_on_any_replica<F, P, R>(&self, task: F) -> Result<R>
    where
        F: Fn(crate::database::database::ServerDatabase) -> P,
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
        F: Fn(crate::database::database::ServerDatabase) -> P,
        P: Future<Output = Result<R>>,
    {
        let mut primary_replica =
            if let Some(replica) = self.primary_replica() { replica } else { self.seek_primary_replica().await? };

        for _ in 0..Self::PRIMARY_REPLICA_TASK_MAX_RETRIES {
            match task(primary_replica.database.clone()).await {
                Err(Error::Connection(
                    ConnectionError::ClusterReplicaNotPrimary
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
    async fn seek_primary_replica(&self) -> Result<ServerReplica> {
        for _ in 0..Self::FETCH_REPLICAS_MAX_RETRIES {
            let replicas = ServerReplica::fetch_all(self.name.clone(), &self.server_connections).await?;
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
            addresses: Addresses::from_addresses(server_connections.keys().map(Address::clone)),
        })
    }

    fn primary_replica(&self) -> Option<ServerReplica> {
        self.replicas
            .read()
            .expect("Expected a read replica lock")
            .iter()
            .filter(|replica| replica.is_primary())
            .max_by_key(|replica| replica.term)
            .cloned()
    }

    #[cfg(feature = "sync")]
    fn wait_for_primary_replica_selection() {
        sleep(Self::WAIT_FOR_PRIMARY_REPLICA_SELECTION);
    }

    #[cfg(not(feature = "sync"))]
    async fn wait_for_primary_replica_selection() {
        tokio::time::sleep(Self::WAIT_FOR_PRIMARY_REPLICA_SELECTION).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn fetch_server_list(
        background_runtime: Arc<BackgroundRuntime>,
        addresses: &Addresses,
        credentials: Credentials,
        driver_options: DriverOptions,
        driver_lang: impl AsRef<str>,
        driver_version: impl AsRef<str>,
    ) -> Result<HashSet<ServerReplica>> {
        for address in addresses.addresses() {
            let server_connection = ServerConnection::new(
                background_runtime.clone(),
                address.clone(),
                credentials.clone(),
                driver_options.clone(),
                driver_lang.as_ref(),
                driver_version.as_ref(),
            )
            .await;
            match server_connection {
                // TODO: Don't we need to close the connection?
                Ok((_, servers)) => return Ok(servers.into_iter().collect()),
                // TODO: Rework connection errors
                Err(Error::Connection(
                    ConnectionError::ServerConnectionFailedStatusError { .. } | ConnectionError::ConnectionFailed,
                )) => (),
                Err(err) => return Err(err),
            }
        }
        Err(ConnectionError::ServerConnectionFailed { addresses: addresses.clone() }.into())
    }
}

impl fmt::Debug for ServerManager {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("ServerConnection").field("replicas", &self.replicas).finish()
    }
}
