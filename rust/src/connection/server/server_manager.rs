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
    sync::{Arc, RwLock, RwLockReadGuard, RwLockWriteGuard},
    thread::sleep,
    time::Duration,
};

use itertools::Itertools;
use log::debug;

use crate::{
    common::address::Address,
    connection::{
        runtime::BackgroundRuntime,
        server::{server_connection::ServerConnection, server_replica::ServerReplica, Addresses},
    },
    error::{ConnectionError, InternalError},
    Credentials, DriverOptions, Error, Result,
};

pub(crate) struct ServerManager {
    // TODO: Merge ServerConnection with ServerReplica? Probably should not as they can be different
    initial_addresses: Addresses,
    replicas: RwLock<Vec<ServerReplica>>,
    server_connections: RwLock<HashMap<Address, ServerConnection>>,
    address_translation: HashMap<Address, Address>,

    background_runtime: Arc<BackgroundRuntime>,
    credentials: Credentials,
    driver_options: DriverOptions,
    driver_lang: String,
    driver_version: String,
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
        let replicas = Self::fetch_replicas_from_addresses(
            background_runtime.clone(),
            &addresses,
            credentials.clone(),
            driver_options.clone(),
            driver_lang.as_ref(),
            driver_version.as_ref(),
        )
        .await?;
        let address_translation = addresses.address_translation();

        let server_manager = Self {
            initial_addresses: addresses,
            replicas: RwLock::new(replicas),
            server_connections: RwLock::new(HashMap::new()),
            address_translation,
            background_runtime,
            credentials,
            driver_options,
            driver_lang: driver_lang.as_ref().to_string(),
            driver_version: driver_version.as_ref().to_string(),
        };
        server_manager.update_server_connections().await?;
        Ok(server_manager)
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn update_server_connections(&self) -> Result {
        let replicas = self.replicas.read().expect("Expected replicas read access");
        let replica_addresses: HashSet<Address> =
            replicas.iter().map(|replica| replica.private_address().clone()).collect();
        let mut connection_errors = Vec::with_capacity(replicas.len());
        let mut server_connections = self.write_server_connections();
        server_connections.retain(|address, _| replica_addresses.contains(address));
        for replica in replicas.iter() {
            let private_address = replica.private_address().clone();
            if !server_connections.contains_key(&private_address) {
                match ServerConnection::new(
                    self.background_runtime.clone(),
                    replica.address().clone(),
                    self.credentials.clone(),
                    self.driver_options.clone(),
                    self.driver_lang.as_ref(),
                    self.driver_version.as_ref(),
                )
                .await
                {
                    Ok((server_connection, _)) => {
                        server_connections.insert(private_address, server_connection);
                    }
                    Err(err) => {
                        connection_errors.push(err);
                    }
                }
            }
        }

        if server_connections.is_empty() {
            // TODO: use connection_errors (convert to string or what??)
            Err(ConnectionError::ServerConnectionFailed { addresses: self.initial_addresses.clone() }.into())
        } else {
            Ok(())
        }
    }

    fn read_server_connections(&self) -> RwLockReadGuard<'_, HashMap<Address, ServerConnection>> {
        self.server_connections.read().expect("Expected server connections read access")
    }

    fn write_server_connections(&self) -> RwLockWriteGuard<'_, HashMap<Address, ServerConnection>> {
        self.server_connections.write().expect("Expected server connections write access")
    }

    pub(crate) fn force_close(&self) -> Result {
        self.read_server_connections().values().map(ServerConnection::force_close).try_collect().map_err(Into::into)
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn servers_all(&self) -> Result<Vec<ServerReplica>> {
        self.run_read_operation(|server_connection| async move { server_connection.servers_all().await }).await
    }

    pub(crate) fn server_count(&self) -> usize {
        self.read_server_connections().len()
    }

    pub(crate) fn username(&self) -> Result<String> {
        match self.read_server_connections().iter().next() {
            Some((_, server_connection)) => Ok(server_connection.username().to_string()),
            None => Err(ConnectionError::ServerConnectionIsClosed {}.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn run_read_operation<F, P, R>(&self, task: F) -> Result<R>
    where
        F: Fn(ServerConnection) -> P,
        P: Future<Output = Result<R>>,
    {
        // TODO: Add retries?
        // TODO: Sort randomly? Remember the last working replica to try it first?
        for (address, server_connection) in self.read_server_connections().iter() {
            match task(server_connection.clone()).await {
                // TODO: refactor errors
                Err(Error::Connection(
                    ConnectionError::ServerConnectionFailedStatusError { .. } | ConnectionError::ConnectionFailed,
                )) => {
                    // TODO: Expose public instead
                    debug!("Unable to connect to {} (private). Attempting next server.", address);
                }
                res => return res,
            }
        }
        Err(ConnectionError::ServerConnectionFailed {
            addresses: Addresses::from_addresses(self.read_server_connections().keys().map(Address::clone)),
        }
        .into())
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn run_write_operation<F, P, R>(&self, task: F) -> Result<R>
    where
        F: Fn(ServerConnection) -> P,
        P: Future<Output = Result<R>>,
    {
        let mut primary_replica = match self.primary_replica() {
            Some(replica) => replica,
            None => self.seek_primary_replica().await?,
        };
        let server_connections = self.read_server_connections();

        for _ in 0..Self::PRIMARY_REPLICA_TASK_MAX_RETRIES {
            if let Some(server_connection) = server_connections.get(primary_replica.private_address()) {
                match task(server_connection.clone()).await {
                    // TODO: Refactor errors
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
            } else {
                // TODO: Refactor
                debug!("Could not connect to the primary replica, waiting...");
                Self::wait_for_primary_replica_selection().await;
                primary_replica = self.seek_primary_replica().await?;
            }
        }
        Err(ConnectionError::ServerConnectionFailed {
            addresses: Addresses::from_addresses(self.read_server_connections().keys().map(Address::clone)),
        }
        .into())
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn seek_primary_replica(&self) -> Result<ServerReplica> {
        for _ in 0..Self::FETCH_REPLICAS_MAX_RETRIES {
            let replicas = self.fetch_replicas().await?;
            *self.replicas.write().expect("Expected replicas write lock") = replicas;
            if let Some(replica) = self.primary_replica() {
                self.update_server_connections().await?;
                return Ok(replica);
            }
            Self::wait_for_primary_replica_selection().await;
        }
        self.update_server_connections().await?;
        Err(ConnectionError::ServerConnectionFailed {
            addresses: Addresses::from_addresses(self.read_server_connections().keys().map(Address::clone)),
        }
        .into())
    }

    fn primary_replica(&self) -> Option<ServerReplica> {
        self.replicas
            .read()
            .expect("Expected a read replica lock")
            .iter()
            .filter(|replica| replica.is_primary())
            .max_by_key(|replica| replica.term())
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
    async fn fetch_replicas_from_addresses(
        background_runtime: Arc<BackgroundRuntime>,
        addresses: &Addresses,
        credentials: Credentials,
        driver_options: DriverOptions,
        driver_lang: impl AsRef<str>,
        driver_version: impl AsRef<str>,
    ) -> Result<Vec<ServerReplica>> {
        let address_translation = addresses.address_translation();
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
                Ok((_, replicas)) => return Ok(Self::translate_replicas(replicas, &address_translation)),
                // TODO: Rework connection errors
                Err(Error::Connection(
                    ConnectionError::ServerConnectionFailedStatusError { .. } | ConnectionError::ConnectionFailed,
                )) => (),
                Err(err) => return Err(err),
            }
        }
        Err(ConnectionError::ServerConnectionFailed { addresses: addresses.clone() }.into())
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn fetch_replicas(&self) -> Result<Vec<ServerReplica>> {
        for (_, server_connection) in self.read_server_connections().iter() {
            match server_connection.servers_all().await {
                Ok(replicas) => {
                    return Ok(Self::translate_replicas(replicas, &self.address_translation));
                } // TODO: Rework connection errors
                Err(Error::Connection(
                    ConnectionError::ServerConnectionFailedStatusError { .. } | ConnectionError::ConnectionFailed,
                )) => (),
                Err(err) => return Err(err),
            }
        }
        // TODO: This addresses are the initial addresses. They can be changed. What to return here?
        Err(ConnectionError::ServerConnectionFailed { addresses: self.initial_addresses.clone() }.into())
    }

    fn translate_replicas(
        replicas: Vec<ServerReplica>,
        address_translation: &HashMap<Address, Address>,
    ) -> Vec<ServerReplica> {
        replicas.into_iter().map(|replica| replica.translated(address_translation)).collect()
    }
}

impl fmt::Debug for ServerManager {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("ServerConnection").field("replicas", &self.replicas).finish()
    }
}
