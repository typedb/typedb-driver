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

use itertools::{enumerate, Itertools};
use log::debug;

use crate::{
    common::{
        address::{address_translation::AddressTranslation, Address, Addresses},
        consistency_level::ConsistencyLevel,
    },
    connection::{
        runtime::BackgroundRuntime,
        server::{server_connection::ServerConnection, server_replica::ServerReplica},
        server_replica::{AvailableServerReplica, Replica},
    },
    error::{ConnectionError, InternalError},
    Credentials, DriverOptions, Error, Result,
};

pub(crate) struct ServerManager {
    configured_addresses: Addresses,
    replicas: RwLock<HashSet<AvailableServerReplica>>,
    replica_connections: RwLock<HashMap<Address, ServerConnection>>,
    address_translation: RwLock<AddressTranslation>,

    background_runtime: Arc<BackgroundRuntime>,
    credentials: Credentials,
    driver_options: DriverOptions,
    driver_lang: String,
    driver_version: String,
}

impl ServerManager {
    // TODO: Introduce a timer-based connections update to have a more actualized connections list

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn new(
        background_runtime: Arc<BackgroundRuntime>,
        addresses: Addresses,
        credentials: Credentials,
        driver_options: DriverOptions,
        driver_lang: impl AsRef<str>,
        driver_version: impl AsRef<str>,
    ) -> Result<Self> {
        if !driver_options.use_replication && addresses.len() > 1 {
            return Err(ConnectionError::MultipleAddressesForNoReplicationMode { addresses: addresses.clone() }.into());
        }

        let has_scheme = addresses.addresses().any(|address| address.has_scheme());
        if has_scheme {
            return Err(ConnectionError::InvalidAddressWithScheme { addresses: addresses.clone() }.into());
        }

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
        println!("INIT REPLICA CONNECTIONS: {:?}", source_connections);
        let server_manager = Self {
            configured_addresses: addresses,
            replicas: RwLock::new(Self::filter_unavailable_replicas(replicas)),
            replica_connections: RwLock::new(source_connections),
            address_translation: RwLock::new(address_translation),
            background_runtime,
            credentials,
            driver_options,
            driver_lang: driver_lang.as_ref().to_string(),
            driver_version: driver_version.as_ref().to_string(),
        };
        server_manager.update_replica_connections().await?;
        Ok(server_manager)
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn register_replica(&self, replica_id: u64, address: String) -> Result {
        self.execute(ConsistencyLevel::Strong, |replica_connection| {
            let address = address.clone();
            async move { replica_connection.servers_register(replica_id, address).await }
        })
        .await?;
        self.update_replicas().await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn deregister_replica(&self, replica_id: u64) -> Result {
        self.execute(ConsistencyLevel::Strong, |replica_connection| async move {
            replica_connection.servers_deregister(replica_id).await
        })
        .await?;
        self.update_replicas().await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn update_address_translation(&self, addresses: Addresses) -> Result {
        if !matches!(addresses, Addresses::Translated(_)) {
            return Err(ConnectionError::AddressTranslationWithoutTranslation { addresses }.into());
        }

        *self.address_translation.write().expect("Expected address translation write access") =
            addresses.address_translation();

        self.update_replicas().await?;
        self.update_replica_connections().await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn update_replica_connections(&self) -> Result {
        let replicas = self.read_replicas().clone();
        let mut connection_errors = HashMap::with_capacity(replicas.len());
        let mut new_replica_connections = HashMap::new();
        let connection_addresses: HashSet<Address> = self.read_replica_connections().keys().cloned().collect();
        for replica in &replicas {
            let private_address = replica.private_address().clone();
            if !connection_addresses.contains(&private_address) {
                match self.new_replica_connection(replica.address().clone()).await {
                    Ok(replica_connection) => {
                        new_replica_connections.insert(private_address, replica_connection);
                    }
                    Err(err) => {
                        connection_errors.insert(replica.address().clone(), err);
                    }
                }
            }
        }

        let replica_addresses: HashSet<Address> =
            replicas.into_iter().map(|replica| replica.private_address().clone()).collect();
        let mut replica_connections = self.write_replica_connections();
        println!("UPDATE REPLICA CONNECTIONS. BEFORE: {:?}", replica_connections);
        replica_connections.retain(|address, _| replica_addresses.contains(address));
        replica_connections.extend(new_replica_connections);
        println!("UPDATE REPLICA CONNECTIONS. AFTER: {:?}", replica_connections);

        if replica_connections.is_empty() {
            Err(self.server_connection_failed_err(connection_errors))
        } else {
            Ok(())
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn new_replica_connection(&self, address: Address) -> Result<ServerConnection> {
        ServerConnection::new(
            self.background_runtime.clone(),
            address,
            self.credentials.clone(),
            self.driver_options.clone(),
            self.driver_lang.as_ref(),
            self.driver_version.as_ref(),
        )
        .await
        .map(|(replica_connection, _)| replica_connection)
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn record_new_replica_connection(
        &self,
        public_address: Address,
        private_address: Address,
    ) -> Result<ServerConnection> {
        let replica_connection = self.new_replica_connection(public_address).await?;
        let mut replica_connections = self.write_replica_connections();
        println!("RECORD NEW REPLICA CONNECTION. BEFORE: {:?}", replica_connections);
        replica_connections.insert(private_address, replica_connection.clone());
        println!("RECORD NEW REPLICA CONNECTION. AFTER: {:?}", replica_connections);
        Ok(replica_connection)
    }

    fn read_replica_connections(&self) -> RwLockReadGuard<'_, HashMap<Address, ServerConnection>> {
        self.replica_connections.read().expect("Expected server connections read access")
    }

    fn write_replica_connections(&self) -> RwLockWriteGuard<'_, HashMap<Address, ServerConnection>> {
        self.replica_connections.write().expect("Expected server connections write access")
    }

    fn read_address_translation(&self) -> RwLockReadGuard<'_, AddressTranslation> {
        self.address_translation.read().expect("Expected address translation read access")
    }

    pub(crate) fn force_close(&self) -> Result {
        self.read_replica_connections().values().map(ServerConnection::force_close).try_collect().map_err(Into::into)
    }

    pub(crate) fn username(&self) -> Result<String> {
        match self.read_replica_connections().iter().next() {
            Some((_, replica_connection)) => Ok(replica_connection.username().to_string()),
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
            // TODO: Uncomment when reads from secondary replicas are implemented
            // ConsistencyLevel::Eventual => self.execute_eventually_consistent(task).await,
            ConsistencyLevel::Eventual => {
                Err(InternalError::Unimplemented { details: "eventual consistency".to_string() }.into())
            }
            ConsistencyLevel::ReplicaDependent { address } => {
                if self.read_replicas().iter().find(|replica| replica.address() == &address).is_none() {
                    return Err(ConnectionError::UnknownDirectReplica {
                        address,
                        configured_addresses: self.configured_addresses.clone(),
                    }
                    .into());
                }
                let private_address =
                    self.read_address_translation().to_private(&address).unwrap_or_else(|| address.clone());
                self.execute_on(&address, &private_address, &task).await
            }
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn execute_strongly_consistent<F, P, R>(&self, task: F) -> Result<R>
    where
        F: Fn(ServerConnection) -> P,
        P: Future<Output = Result<R>>,
    {
        println!("STRONG");
        let mut primary_replica = match self.read_primary_replica() {
            Some(replica) => replica,
            None => {
                let replicas: HashSet<_> = self.read_replicas().iter().cloned().collect();
                self.seek_primary_replica_in(replicas).await?
            }
        };

        let retries = self.driver_options.primary_failover_retries;
        let mut connection_errors = HashMap::new();
        for x in 0..=retries {
            println!("Retry {x} on {primary_replica:?}");
            let private_address = primary_replica.private_address().clone();
            match self.execute_on(primary_replica.address(), &private_address, &task).await {
                Err(Error::Connection(connection_error)) => {
                    let replicas: HashSet<_> = self.read_replicas().iter().cloned().collect();
                    let replicas_without_old_primary = replicas.into_iter().filter(|replica| {
                        println!("REPLICAS: Filter out? {private_address:?} vs mine {:?}", replica.private_address());
                        replica.private_address() != &private_address
                    });
                    primary_replica = match &connection_error {
                        ConnectionError::ClusterReplicaNotPrimary => {
                            println!("Not primary");
                            debug!("Could not connect to the primary replica: no longer primary. Retrying...");
                            let replicas = iter::once(primary_replica).chain(replicas_without_old_primary);
                            self.seek_primary_replica_in(replicas).await?
                        }
                        err => {
                            println!("ERROR STRONG");
                            debug!("Could not connect to the primary replica: {err:?}. Retrying...");
                            self.seek_primary_replica_in(replicas_without_old_primary).await?
                        }
                    };

                    connection_errors.insert(primary_replica.address().clone(), connection_error.into());

                    if primary_replica.private_address() == &private_address {
                        break;
                    }
                }
                Err(err) => {
                    println!("ERROR RETURN res: {err:?}");
                    return Err(err);
                }
                res => {
                    println!("res ok");
                    return res;
                }
            }
        }
        Err(self.server_connection_failed_err(connection_errors))
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn execute_eventually_consistent<F, P, R>(&self, task: F) -> Result<R>
    where
        F: Fn(ServerConnection) -> P,
        P: Future<Output = Result<R>>,
    {
        let replicas: HashSet<_> = self.read_replicas().iter().cloned().collect();
        self.execute_on_any(replicas, task).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn execute_on_any<F, P, R>(
        &self,
        replicas: impl IntoIterator<Item = AvailableServerReplica>,
        task: F,
    ) -> Result<R>
    where
        F: Fn(ServerConnection) -> P,
        P: Future<Output = Result<R>>,
    {
        println!("ANY");
        let limit = self.driver_options.replica_discovery_attempts.unwrap_or(usize::MAX);
        let mut connection_errors = HashMap::new();
        for (attempt, replica) in enumerate(replicas.into_iter()) {
            println!("ATTEMPT {attempt} with {limit}");
            if attempt == limit {
                println!("BREAK");
                break;
            }
            // TODO: If we only use eventual consistency, we won't ever reconnect to disconnected /
            // new replicas. We need to think how to update the connections in this case.
            match self.execute_on(replica.address(), replica.private_address(), &task).await {
                Err(Error::Connection(ConnectionError::ClusterReplicaNotPrimary)) => {
                    return Err(ConnectionError::NotPrimaryOnReadOnly { address: replica.address().clone() }.into());
                }
                Err(Error::Connection(error)) => {
                    println!("CONNECTION ERROR ANY: {error:?}");
                    debug!("Unable to connect to {}: {error:?}. May attempt the next server.", replica.address());
                    connection_errors.insert(replica.address().clone(), error.into());
                }
                res => return res,
            }
        }
        Err(self.server_connection_failed_err(connection_errors))
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn execute_on<F, P, R>(&self, public_address: &Address, private_address: &Address, task: &F) -> Result<R>
    where
        F: Fn(ServerConnection) -> P,
        P: Future<Output = Result<R>>,
    {
        let existing_connection = { self.read_replica_connections().get(private_address).cloned() };
        let replica_connection = match existing_connection {
            Some(replica_connection) => replica_connection,
            None => self.record_new_replica_connection(public_address.clone(), private_address.clone()).await?,
        };
        task(replica_connection).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn seek_primary_replica_in(
        &self,
        source_replicas: impl IntoIterator<Item = AvailableServerReplica>,
    ) -> Result<AvailableServerReplica> {
        // TODO: Add retries with sleeps in between? Maybe replica_discovery_attempts should work / be used differently
        println!("SEEK");
        self.execute_on_any(source_replicas, |replica_connection| async {
            self.seek_primary_replica(replica_connection).await
        })
        .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn seek_primary_replica(&self, replica_connection: ServerConnection) -> Result<AvailableServerReplica> {
        let address_translation = self.read_address_translation().clone();
        println!("SEEK PRIMARY REPLICA");
        let replicas = Self::fetch_replicas_from_connection(&replica_connection, &address_translation).await?;
        println!("WRITE NEW PRIMARY");
        *self.replicas.write().expect("Expected replicas write lock") = Self::filter_unavailable_replicas(replicas);
        if let Some(replica) = self.read_primary_replica() {
            self.update_replica_connections().await?;
            Ok(replica)
        } else {
            Err(self.server_connection_failed_err(HashMap::default()))
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
    ) -> Result<(HashMap<Address, ServerConnection>, HashSet<ServerReplica>)> {
        let address_translation = addresses.address_translation();
        let mut errors = Vec::with_capacity(addresses.len());
        println!("Fetch replicas from addresses: {addresses:?}");
        for address in addresses.addresses() {
            println!("Fetch replicas. Address: {address:?}");
            let replica_connection = ServerConnection::new(
                background_runtime.clone(),
                address.clone(),
                credentials.clone(),
                driver_options.clone(),
                driver_lang.as_ref(),
                driver_version.as_ref(),
            )
            .await;
            match replica_connection {
                Ok((replica_connection, replicas)) => {
                    debug!("Fetched replicas from configured address '{address}': {replicas:?}");
                    let translated_replicas = Self::translate_replicas(replicas, &address_translation);
                    if use_replication {
                        let mut source_connections = HashMap::with_capacity(translated_replicas.len());
                        source_connections.insert(address.clone(), replica_connection);
                        return Ok((source_connections, translated_replicas));
                    } else {
                        if let Some(target_replica) =
                            translated_replicas.into_iter().find(|replica| replica.address() == Some(address))
                        {
                            let source_connections = HashMap::from([(address.clone(), replica_connection)]);
                            return Ok((source_connections, HashSet::from([target_replica])));
                        }
                    }
                }
                Err(Error::Connection(err)) => {
                    println!("Fetch replicas from addresses: push error {err:?}");
                    debug!("Unable to fetch replicas from {}: {err:?}. Attempting next server.", address);
                    errors.push(err);
                }
                Err(err) => {
                    println!("UNEXPECTED ERR: {err:?}");
                    return Err(err);
                }
            }
        }
        println!("Fetch replicas from addresses: SERVER CONNECTION FAILED! {errors:?}");
        Err(ConnectionError::ServerConnectionFailed {
            configured_addresses: addresses.clone(),
            accessed_addresses: addresses.clone(),
            details: errors.into_iter().join(";\n"),
        }
        .into())
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn update_replicas(&self) -> Result {
        self.fetch_replicas().await.map(|_| ())
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn fetch_replicas(&self) -> Result<HashSet<ServerReplica>> {
        let replicas = self
            .execute(ConsistencyLevel::Strong, |replica_connection| {
                let address_translation = self.read_address_translation().clone();
                async move { Self::fetch_replicas_from_connection(&replica_connection, &address_translation).await }
            })
            .await?;
        *self.replicas.write().expect("Expected replicas write lock") =
            Self::filter_unavailable_replicas(replicas.clone());
        Ok(replicas)
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn fetch_replicas_from_connection(
        replica_connection: &ServerConnection,
        address_translation: &AddressTranslation,
    ) -> Result<HashSet<ServerReplica>> {
        replica_connection.servers_all().await.map(|replicas| Self::translate_replicas(replicas, address_translation))
    }

    fn translate_replicas(
        replicas: impl IntoIterator<Item = ServerReplica>,
        address_translation: &AddressTranslation,
    ) -> HashSet<ServerReplica> {
        replicas.into_iter().map(|replica| replica.translated(address_translation)).collect()
    }

    fn filter_unavailable_replicas(
        replicas: impl IntoIterator<Item = ServerReplica>,
    ) -> HashSet<AvailableServerReplica> {
        replicas
            .into_iter()
            .filter_map(|replica| match replica {
                ServerReplica::Available(available) => Some(available),
                ServerReplica::Unavailable { .. } => None,
            })
            .collect()
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn fetch_primary_replica(&self) -> Result<Option<AvailableServerReplica>> {
        self.fetch_replicas().await?;
        Ok(Self::filter_primary_replica(self.read_replicas().iter()))
    }

    fn read_replicas(&self) -> RwLockReadGuard<'_, HashSet<AvailableServerReplica>> {
        self.replicas.read().expect("Expected a read replica lock")
    }

    fn read_primary_replica(&self) -> Option<AvailableServerReplica> {
        Self::filter_primary_replica(self.read_replicas().iter())
    }

    fn filter_primary_replica<'a, R: Replica + 'a>(replicas: impl IntoIterator<Item = &'a R>) -> Option<R> {
        replicas
            .into_iter()
            .filter(|replica| replica.is_primary())
            .max_by_key(|replica| replica.term().unwrap_or_default())
            .cloned()
    }

    fn server_connection_failed_err(&self, errors: HashMap<Address, Error>) -> Error {
        let accessed_addresses =
            Addresses::from_addresses(self.read_replicas().iter().map(|replica| replica.address().clone()));
        let details = if errors.is_empty() {
            "none".to_string()
        } else {
            errors.into_iter().map(|(address, error)| format!("'{address}': '{error}'")).join(";\n")
        };
        ConnectionError::ServerConnectionFailed {
            configured_addresses: self.configured_addresses.clone(),
            accessed_addresses,
            details,
        }
        .into()
    }
}

impl fmt::Debug for ServerManager {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("ServerConnection").field("replicas", &self.replicas).finish()
    }
}
