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
    iter,
    sync::{Arc, RwLock, RwLockReadGuard, RwLockWriteGuard},
    time::Duration,
};

use itertools::Itertools;
use log::debug;

use crate::{
    common::{address::Address, consistency_level::ConsistencyLevel},
    connection::{
        runtime::BackgroundRuntime,
        server::{server_connection::ServerConnection, server_replica::ServerReplica, Addresses},
    },
    error::ConnectionError,
    Credentials, DriverOptions, Error, Result,
};

pub(crate) struct ServerManager {
    // TODO: Merge ServerConnection with ServerReplica? Probably should not as they can be different
    configured_addresses: Addresses,
    replicas: RwLock<Vec<ServerReplica>>,
    server_connections: RwLock<HashMap<Address, ServerConnection>>,

    // public - private
    address_translation: HashMap<Address, Address>,

    background_runtime: Arc<BackgroundRuntime>,
    credentials: Credentials,
    driver_options: DriverOptions,
    driver_lang: String,
    driver_version: String,
}

impl ServerManager {
    // TODO: Introduce a timer-based connections update
    const PRIMARY_REPLICA_TASK_MAX_RETRIES: usize = 1;

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn new(
        background_runtime: Arc<BackgroundRuntime>,
        addresses: Addresses,
        credentials: Credentials,
        driver_options: DriverOptions,
        driver_lang: impl AsRef<str>,
        driver_version: impl AsRef<str>,
    ) -> Result<Self> {
        let (source_connections, replicas) = Self::fetch_replicas_from_addresses(
            background_runtime.clone(),
            &addresses,
            credentials.clone(),
            driver_options.clone(),
            driver_lang.as_ref(),
            driver_version.as_ref(),
            driver_options.use_replication,
        )
        .await?;
        let address_translation = addresses.address_translation();

        let server_manager = Self {
            configured_addresses: addresses,
            replicas: RwLock::new(replicas),
            server_connections: RwLock::new(source_connections),
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
        let replicas = self.read_replicas().clone();
        let mut connection_errors = Vec::with_capacity(replicas.len());
        let mut new_server_connections = HashMap::new();
        let connection_addresses: HashSet<Address> = self.read_server_connections().keys().cloned().collect();
        for replica in &replicas {
            let private_address = replica.private_address().clone();
            if !connection_addresses.contains(&private_address) {
                match self.new_server_connection(replica.address().clone()).await {
                    Ok(server_connection) => {
                        new_server_connections.insert(private_address, server_connection);
                    }
                    Err(err) => {
                        connection_errors.push(err);
                    }
                }
            }
        }

        let replica_addresses: HashSet<Address> =
            replicas.into_iter().map(|replica| replica.private_address().clone()).collect();
        let mut server_connections = self.write_server_connections();
        server_connections.retain(|address, _| replica_addresses.contains(address));
        server_connections.extend(new_server_connections);

        if server_connections.is_empty() {
            Err(self.server_connection_failed_err(None))
        } else {
            Ok(())
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn new_server_connection(&self, address: Address) -> Result<ServerConnection> {
        ServerConnection::new(
            self.background_runtime.clone(),
            address,
            self.credentials.clone(),
            self.driver_options.clone(),
            self.driver_lang.as_ref(),
            self.driver_version.as_ref(),
        )
        .await
        .map(|(server_connection, _)| server_connection)
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn record_new_server_connection(
        &self,
        public_address: Address,
        private_address: Address,
    ) -> Result<ServerConnection> {
        let server_connection = self.new_server_connection(public_address).await?;
        let mut server_connections = self.write_server_connections();
        server_connections.insert(private_address, server_connection.clone());
        Ok(server_connection)
    }

    fn read_server_connections(&self) -> RwLockReadGuard<'_, HashMap<Address, ServerConnection>> {
        self.server_connections.read().expect("Expected server connections read access")
    }

    fn write_server_connections(&self) -> RwLockWriteGuard<'_, HashMap<Address, ServerConnection>> {
        self.server_connections.write().expect("Expected server connections write access")
    }

    fn read_replicas(&self) -> RwLockReadGuard<'_, Vec<ServerReplica>> {
        self.replicas.read().expect("Expected a read replica lock")
    }

    pub(crate) fn force_close(&self) -> Result {
        self.read_server_connections().values().map(ServerConnection::force_close).try_collect().map_err(Into::into)
    }

    pub(crate) fn replicas(&self) -> HashSet<ServerReplica> {
        self.read_replicas().iter().cloned().collect()
    }

    pub(crate) fn primary_replica(&self) -> Option<ServerReplica> {
        self.read_replicas().iter().filter(|replica| replica.is_primary()).max_by_key(|replica| replica.term()).cloned()
    }

    pub(crate) fn username(&self) -> Result<String> {
        match self.read_server_connections().iter().next() {
            Some((_, server_connection)) => Ok(server_connection.username().to_string()),
            None => Err(ConnectionError::ServerConnectionIsClosed {}.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn execute<F, P, R>(&self, consistency_level: ConsistencyLevel, task: F) -> Result<R>
    where
        F: Fn(ServerConnection) -> P,
        P: Future<Output = Result<R>>,
    {
        match consistency_level {
            ConsistencyLevel::Strong => self.execute_strongly_consistent(task).await,
            ConsistencyLevel::Eventual => self.execute_eventually_consistent(task).await,
            ConsistencyLevel::ReplicaDependant { address } => {
                if self.read_replicas().iter().find(|replica| replica.address() == &address).is_none() {
                    return Err(ConnectionError::UnknownDirectReplica {
                        address,
                        configured_addresses: self.configured_addresses.clone(),
                    }
                    .into());
                }
                let private_address = self.address_translation.get(&address).unwrap_or_else(|| &address);
                self.execute_on(&address, private_address, false, &task).await
            }
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn execute_strongly_consistent<F, P, R>(&self, task: F) -> Result<R>
    where
        F: Fn(ServerConnection) -> P,
        P: Future<Output = Result<R>>,
    {
        let mut primary_replica = match self.primary_replica() {
            Some(replica) => replica,
            None => self.seek_primary_replica_in(self.replicas()).await?,
        };

        for _ in 0..=Self::PRIMARY_REPLICA_TASK_MAX_RETRIES {
            let private_address = primary_replica.private_address().clone();
            match self.execute_on(primary_replica.address(), &private_address, false, &task).await {
                Err(Error::Connection(connection_error)) => {
                    let replicas_without_old_primary =
                        self.replicas().into_iter().filter(|replica| replica.private_address() != &private_address);

                    primary_replica = match connection_error {
                        ConnectionError::ClusterReplicaNotPrimary => {
                            debug!("Could not connect to the primary replica: no longer primary. Retrying...");
                            let replicas = iter::once(primary_replica).chain(replicas_without_old_primary);
                            self.seek_primary_replica_in(replicas).await?
                        }
                        err => {
                            debug!("Could not connect to the primary replica: {err:?}. Retrying...");
                            self.seek_primary_replica_in(replicas_without_old_primary).await?
                        }
                    };

                    if primary_replica.private_address() == &private_address {
                        break;
                    }
                }
                res => return res,
            }
        }
        Err(self.server_connection_failed_err(None))
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn execute_eventually_consistent<F, P, R>(&self, task: F) -> Result<R>
    where
        F: Fn(ServerConnection) -> P,
        P: Future<Output = Result<R>>,
    {
        let replicas = self.replicas();
        self.execute_on_any(replicas, task).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn execute_on_any<F, P, R>(&self, replicas: impl IntoIterator<Item = ServerReplica>, task: F) -> Result<R>
    where
        F: Fn(ServerConnection) -> P,
        P: Future<Output = Result<R>>,
    {
        for replica in replicas.into_iter() {
            match self.execute_on(replica.address(), replica.private_address(), true, &task).await {
                Err(Error::Connection(ConnectionError::ClusterReplicaNotPrimary)) => {
                    return Err(ConnectionError::NotPrimaryOnReadOnly { address: replica.address().clone() }.into());
                }
                Err(Error::Connection(err)) => {
                    debug!("Unable to connect to {}: {err:?}. Attempting next server.", replica.address());
                }
                res => return res,
            }
        }
        Err(self.server_connection_failed_err(None))
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn execute_on<F, P, R>(
        &self,
        public_address: &Address,
        private_address: &Address,
        require_connected: bool,
        task: &F,
    ) -> Result<R>
    where
        F: Fn(ServerConnection) -> P,
        P: Future<Output = Result<R>>,
    {
        let existing_connection = { self.read_server_connections().get(private_address).cloned() };
        let server_connection = match existing_connection {
            Some(server_connection) => server_connection,
            None => match require_connected {
                false => self.record_new_server_connection(public_address.clone(), private_address.clone()).await?,
                true => {
                    return Err(self.server_connection_failed_err(Some(Addresses::from_address(public_address.clone()))))
                }
            },
        };
        task(server_connection).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn seek_primary_replica_in(
        &self,
        source_replicas: impl IntoIterator<Item = ServerReplica>,
    ) -> Result<ServerReplica> {
        self.execute_on_any(source_replicas, |server_connection| async {
            self.seek_primary_replica(server_connection).await
        })
        .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn seek_primary_replica(&self, server_connection: ServerConnection) -> Result<ServerReplica> {
        let replicas = self.fetch_replicas(&server_connection).await?;
        *self.replicas.write().expect("Expected replicas write lock") = replicas;
        if let Some(replica) = self.primary_replica() {
            self.update_server_connections().await?;
            Ok(replica)
        } else {
            Err(self.server_connection_failed_err(None))
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn fetch_replicas_from_addresses(
        background_runtime: Arc<BackgroundRuntime>,
        addresses: &Addresses,
        credentials: Credentials,
        driver_options: DriverOptions,
        driver_lang: impl AsRef<str>,
        driver_version: impl AsRef<str>,
        use_replication: bool,
    ) -> Result<(HashMap<Address, ServerConnection>, Vec<ServerReplica>)> {
        if !use_replication && addresses.len() > 1 {
            return Err(ConnectionError::MultipleAddressesForNoReplicationMode { addresses: addresses.clone() }.into());
        }

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
                Ok((server_connection, replicas)) => {
                    debug!("Fetched replicas from configured address '{address}': {replicas:?}");
                    let translated_replicas = Self::translate_replicas(replicas, &address_translation);
                    if use_replication {
                        let mut source_connections = HashMap::with_capacity(translated_replicas.len());
                        source_connections.insert(address.clone(), server_connection);
                        return Ok((source_connections, translated_replicas));
                    } else {
                        if let Some(target_replica) =
                            translated_replicas.into_iter().find(|replica| replica.address() == address)
                        {
                            let source_connections = HashMap::from([(address.clone(), server_connection)]);
                            return Ok((source_connections, vec![target_replica]));
                        }
                    }
                }
                Err(Error::Connection(err)) => {
                    debug!("Unable to fetch replicas from {}: {err:?}. Attempting next server.", address);
                }
                Err(err) => return Err(err),
            }
        }
        Err(ConnectionError::ServerConnectionFailed {
            configured_addresses: addresses.clone(),
            accessed_addresses: addresses.clone(),
        }
        .into())
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn fetch_replicas(&self, server_connection: &ServerConnection) -> Result<Vec<ServerReplica>> {
        server_connection
            .servers_all()
            .await
            .map(|replicas| Self::translate_replicas(replicas, &self.address_translation))
    }

    fn translate_replicas(
        replicas: impl IntoIterator<Item = ServerReplica>,
        address_translation: &HashMap<Address, Address>,
    ) -> Vec<ServerReplica> {
        replicas.into_iter().map(|replica| replica.translated(address_translation)).collect()
    }

    fn server_connection_failed_err(&self, accessed_addresses: Option<Addresses>) -> Error {
        let accessed_addresses = accessed_addresses.unwrap_or_else(|| {
            Addresses::from_addresses(self.read_replicas().iter().map(|replica| replica.address().clone()))
        });
        ConnectionError::ServerConnectionFailed {
            configured_addresses: self.configured_addresses.clone(),
            accessed_addresses,
        }
        .into()
    }
}

impl fmt::Debug for ServerManager {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("ServerConnection").field("replicas", &self.replicas).finish()
    }
}
