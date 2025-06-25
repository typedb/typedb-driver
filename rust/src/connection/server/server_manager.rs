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
    thread::sleep,
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
    const PRIMARY_REPLICA_TASK_MAX_RETRIES: usize = 1;
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
        let replicas = self.read_replicas();
        let replica_addresses: HashSet<Address> =
            replicas.iter().map(|replica| replica.private_address().clone()).collect();
        let mut connection_errors = Vec::with_capacity(replicas.len());
        let mut server_connections = self.write_server_connections();
        server_connections.retain(|address, _| replica_addresses.contains(address));
        for replica in replicas.iter() {
            let private_address = replica.private_address().clone();
            if !server_connections.contains_key(&private_address) {
                match self.new_server_connection(replica.address().clone()).await {
                    Ok(server_connection) => {
                        server_connections.insert(private_address, server_connection);
                    }
                    Err(err) => {
                        connection_errors.push(err);
                    }
                }
            }
        }

        if server_connections.is_empty() {
            Err(self.server_connection_failed_err())
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

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn servers_all(&self) -> Result<Vec<ServerReplica>> {
        // TODO: May need to expose multiple consistency levels. Check the driver's "replicas" impl!
        self.execute(ConsistencyLevel::Strong, |server_connection| async move { server_connection.servers_all().await })
            .await
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
                self.execute_on(&address, self.private_address(&address), &task).await
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
            None => {
                let replicas = self.read_replicas().clone();
                self.seek_primary_replica(replicas).await?
            }
        };
        let server_connections = self.read_server_connections();

        for _ in 0..Self::PRIMARY_REPLICA_TASK_MAX_RETRIES {
            let private_address = primary_replica.private_address().clone();
            // TODO: Instead of "if let some", make connection if empty or throw networking error"
            if let Some(server_connection) = server_connections.get(&private_address) {
                match task(server_connection.clone()).await {
                    Err(Error::Connection(ConnectionError::ClusterReplicaNotPrimary)) => {
                        debug!("Could not connect to the primary replica: no longer primary...");
                        let replicas = iter::once(primary_replica).chain(
                            self.read_replicas()
                                .clone()
                                .into_iter()
                                .filter(|replica| replica.private_address() != &private_address),
                        );
                        primary_replica = self.seek_primary_replica(replicas).await?;
                    }
                    Err(Error::Connection(err)) => {
                        debug!("Could not connect to the primary replica...");
                        let replicas = self
                            .read_replicas()
                            .clone()
                            .into_iter()
                            .filter(|replica| replica.private_address() != primary_replica.private_address());
                        primary_replica = self.seek_primary_replica(replicas).await?;
                    }
                    res => return res,
                }
            }
        }
        Err(self.server_connection_failed_err())
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn execute_eventually_consistent<F, P, R>(&self, task: F) -> Result<R>
    where
        F: Fn(ServerConnection) -> P,
        P: Future<Output = Result<R>>,
    {
        let replicas = self.read_replicas().clone();
        self.execute_on_any(replicas, task).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn execute_on_any<F, P, R>(&self, replicas: impl IntoIterator<Item = ServerReplica>, task: F) -> Result<R>
    where
        F: Fn(ServerConnection) -> P,
        P: Future<Output = Result<R>>,
    {
        for replica in replicas.into_iter() {
            match self.execute_on(replica.address(), replica.private_address(), &task).await {
                Err(Error::Connection(ConnectionError::ClusterReplicaNotPrimary)) => {
                    return Err(ConnectionError::NotPrimaryOnReadOnly { address: replica.address().clone() }.into());
                }
                Err(Error::Connection(err)) => {
                    debug!("Unable to connect to {}: {err:?}. Attempting next server.", replica.address());
                }
                res => return res,
            }
        }
        Err(self.server_connection_failed_err())
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn execute_on<F, P, R>(&self, public_address: &Address, private_address: &Address, task: &F) -> Result<R>
    where
        F: Fn(ServerConnection) -> P,
        P: Future<Output = Result<R>>,
    {
        let server_connection = match self.read_server_connections().get(private_address).cloned() {
            Some(server_connection) => server_connection,
            None => self.record_new_server_connection(public_address.clone(), private_address.clone()).await?,
        };
        task(server_connection).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn seek_primary_replica(
        &self,
        source_replicas: impl IntoIterator<Item = ServerReplica>,
    ) -> Result<ServerReplica> {
        self.execute_on_any(source_replicas, |server_connection| async {
            self.try_fetch_primary_replica(server_connection).await
        })
        .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn try_fetch_primary_replica(&self, server_connection: ServerConnection) -> Result<ServerReplica> {
        let replicas = self.fetch_replicas(&server_connection).await?;
        *self.replicas.write().expect("Expected replicas write lock") = replicas;
        if let Some(replica) = self.primary_replica() {
            self.update_server_connections().await?;
            Ok(replica)
        } else {
            Err(self.server_connection_failed_err())
        }
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

    fn server_connection_failed_err(&self) -> Error {
        let accessed_addresses =
            Addresses::from_addresses(self.read_replicas().iter().map(|replica| replica.address().clone()));
        ConnectionError::ServerConnectionFailed {
            configured_addresses: self.configured_addresses.clone(),
            accessed_addresses,
        }
        .into()
    }

    fn private_address<'a>(&'a self, address: &'a Address) -> &'a Address {
        self.address_translation.get(address).unwrap_or_else(|| address)
    }
}

impl fmt::Debug for ServerManager {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("ServerConnection").field("replicas", &self.replicas).finish()
    }
}
