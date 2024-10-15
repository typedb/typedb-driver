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
    sync::Arc,
};

use itertools::Itertools;

use crate::{
    common::{
        address::Address,
        error::{ConnectionError, Error},
        Result,
    },
    connection::{runtime::BackgroundRuntime, server_connection::ServerConnection},
    Credential, DatabaseManager, Options, Transaction, TransactionType, UserManager,
};

/// A connection to a TypeDB server which serves as the starting point for all interaction.
pub struct TypeDBDriver {
    server_connections: HashMap<Address, ServerConnection>,
    database_manager: DatabaseManager,
    background_runtime: Arc<BackgroundRuntime>,
    username: Option<String>,
    is_cloud: bool,
}

impl TypeDBDriver {}

impl TypeDBDriver {
    const VERSION: &'static str = match option_env!("CARGO_PKG_VERSION") {
        None => "0.0.0",
        Some(version) => version,
    };

    pub const DEFAULT_ADDRESS: &'static str = "localhost:1729";

    /// Creates a new TypeDB Server connection.
    ///
    /// # Arguments
    ///
    /// * `address` -- The address (host:port) on which the TypeDB Server is running
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "Connection::new_core(\"127.0.0.1:1729\")")]
    #[cfg_attr(not(feature = "sync"), doc = "Connection::new_core(\"127.0.0.1:1729\").await")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn new_core(address: impl AsRef<str>) -> Result<Self> {
        Self::new_core_with_description(address, "rust").await
    }

    /// Creates a new TypeDB Server connection with a description.
    ///
    /// # Arguments
    ///
    /// * `address` -- The address (host:port) on which the TypeDB Server is running
    /// * `driver_lang` -- The language of the driver connecting to the server
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "Connection::new_core(\"127.0.0.1:1729\", \"rust\")")]
    #[cfg_attr(not(feature = "sync"), doc = "Connection::new_core(\"127.0.0.1:1729\", \"rust\").await")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn new_core_with_description(address: impl AsRef<str>, driver_lang: impl AsRef<str>) -> Result<Self> {
        let id = address.as_ref().to_string();
        let address: Address = id.parse()?;
        let background_runtime = Arc::new(BackgroundRuntime::new()?);

        let (server_connection, database_info) = ServerConnection::new_core(
            background_runtime.clone(),
            address.clone(),
            driver_lang.as_ref(),
            TypeDBDriver::VERSION,
        )
        .await?;

        // // validate
        // let advertised_address = server_connection
        //     .servers_all()?
        //     .into_iter()
        //     .exactly_one()
        //     .map_err(|e| ConnectionError::ServerConnectionFailedStatusError { error: e.to_string() })?;

        // TODO: this solidifies the assumption that servers don't change
        let server_connections: HashMap<Address, ServerConnection> = [(address, server_connection)].into();
        let database_manager = DatabaseManager::new(server_connections.clone(), database_info)?;

        Ok(Self { server_connections, database_manager, background_runtime, username: None, is_cloud: false })
    }

    /// Creates a new TypeDB Cloud connection.
    ///
    /// # Arguments
    ///
    /// * `init_addresses` -- Addresses (host:port) on which TypeDB Cloud nodes are running
    /// * `credential` -- User credential and TLS encryption setting
    ///
    /// # Examples
    ///
    /// ```rust
    /// Connection::new_cloud(
    ///     &["localhost:11729", "localhost:21729", "localhost:31729"],
    ///     Credential::with_tls(
    ///         "admin",
    ///         "password",
    ///         Some(&PathBuf::from(
    ///             std::env::var("ROOT_CA")
    ///                 .expect("ROOT_CA environment variable needs to be set for cloud tests to run"),
    ///         )),
    ///     )?,
    /// )
    /// ```
    pub fn new_cloud<T: AsRef<str> + Sync>(init_addresses: &[T], credential: Credential) -> Result<Self> {
        // let background_runtime = Arc::new(BackgroundRuntime::new()?);
        // let servers = Self::fetch_server_list(background_runtime.clone(), init_addresses, credential.clone())?;
        // let server_to_address = servers.into_iter().map(|address| (address.clone(), address)).collect();
        // Self::new_cloud_impl(server_to_address, background_runtime, credential)
        todo!()
    }

    /// Creates a new TypeDB Cloud connection.
    ///
    /// # Arguments
    ///
    /// * `address_translation` -- Translation map from addresses to be used by the driver for connection
    ///    to addresses received from the TypeDB server(s)
    /// * `credential` -- User credential and TLS encryption setting
    ///
    /// # Examples
    ///
    /// ```rust
    /// Connection::new_cloud_with_translation(
    ///     [
    ///         ("typedb-cloud.ext:11729", "localhost:11729"),
    ///         ("typedb-cloud.ext:21729", "localhost:21729"),
    ///         ("typedb-cloud.ext:31729", "localhost:31729"),
    ///     ].into(),
    ///     credential,
    /// )
    /// ```
    pub fn new_cloud_with_translation<T, U>(address_translation: HashMap<T, U>, credential: Credential) -> Result<Self>
    where
        T: AsRef<str> + Sync,
        U: AsRef<str> + Sync,
    {
        // let background_runtime = Arc::new(BackgroundRuntime::new()?);
        //
        // let fetched =
        //     Self::fetch_server_list(background_runtime.clone(), address_translation.keys(), credential.clone())?;
        //
        // let address_to_server: HashMap<Address, Address> = address_translation
        //     .into_iter()
        //     .map(|(public, private)| -> Result<_> { Ok((public.as_ref().parse()?, private.as_ref().parse()?)) })
        //     .try_collect()?;
        //
        // let provided: HashSet<Address> = address_to_server.values().cloned().collect();
        // let unknown = &provided - &fetched;
        // let unmapped = &fetched - &provided;
        // if !unknown.is_empty() || !unmapped.is_empty() {
        //     return Err(ConnectionError::AddressTranslationMismatch { unknown, unmapped }.into());
        // }
        //
        // debug_assert_eq!(fetched, provided);
        //
        // Self::new_cloud_impl(address_to_server, background_runtime, credential)
        todo!()
    }

    fn new_cloud_impl(
        address_to_server: HashMap<Address, Address>,
        background_runtime: Arc<BackgroundRuntime>,
        credential: Credential,
    ) -> Result<TypeDBDriver> {
        // let server_connections: HashMap<Address, ServerConnection> = address_to_server
        //     .into_iter()
        //     .map(|(public, private)| {
        //         ServerConnection::new_cloud(background_runtime.clone(), public, credential.clone())
        //             .map(|server_connection| (private, server_connection))
        //     })
        //     .try_collect()?;
        //
        // let errors = server_connections.values().map(|conn| conn.validate()).filter_map(Result::err).collect_vec();
        // if errors.len() == server_connections.len() {
        //     Err(ConnectionError::CloudAllNodesFailed {
        //         errors: errors.into_iter().map(|err| err.to_string()).join("\n"),
        //     })?
        // } else {
        //     Ok(Connection {
        //         server_connections,
        //         background_runtime,
        //         username: Some(credential.username().to_owned()),
        //         is_cloud: true,
        //     })
        // }
        todo!()
    }

    fn fetch_server_list(
        background_runtime: Arc<BackgroundRuntime>,
        addresses: impl IntoIterator<Item = impl AsRef<str>> + Clone,
        credential: Credential,
    ) -> Result<HashSet<Address>> {
        let addresses: Vec<Address> = addresses.into_iter().map(|addr| addr.as_ref().parse()).try_collect()?;
        for address in &addresses {
            let server_connection =
                ServerConnection::new_cloud(background_runtime.clone(), address.clone(), credential.clone());
            match server_connection {
                Ok(server_connection) => match server_connection.servers_all() {
                    Ok(servers) => return Ok(servers.into_iter().collect()),
                    Err(Error::Connection(
                        ConnectionError::ServerConnectionFailedStatusError { .. } | ConnectionError::ConnectionFailed,
                    )) => (),
                    Err(err) => Err(err)?,
                },
                Err(Error::Connection(
                    ConnectionError::ServerConnectionFailedStatusError { .. } | ConnectionError::ConnectionFailed,
                )) => (),
                Err(err) => Err(err)?,
            }
        }
        Err(ConnectionError::ServerConnectionFailed { addresses }.into())
    }

    /// Checks it this connection is opened.
    //
    /// # Examples
    ///
    /// ```rust
    /// connection.is_open()
    /// ```
    pub fn is_open(&self) -> bool {
        self.background_runtime.is_open()
    }

    /// Check if the connection is to an Cloud server.
    ///
    /// # Examples
    ///
    /// ```rust
    /// connection.is_cloud()
    /// ```
    pub fn is_cloud(&self) -> bool {
        self.is_cloud
    }

    pub fn databases(&self) -> &DatabaseManager {
        &self.database_manager
    }

    pub fn users(&self) -> &UserManager {
        todo!()
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn transaction(
        &self,
        database_name: impl AsRef<str>,
        transaction_type: TransactionType,
    ) -> Result<Transaction> {
        self.transaction_with_options(database_name, transaction_type, Options::new()).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn transaction_with_options(
        &self,
        database_name: impl AsRef<str>,
        transaction_type: TransactionType,
        options: Options,
    ) -> Result<Transaction> {
        let database_name = database_name.as_ref();
        let database = self.database_manager.get_cached_or_fetch(database_name).await?;
        let transaction_stream = database
            .run_failsafe(|database| async move {
                database.connection().open_transaction(database.name(), transaction_type, options).await
            })
            .await?;
        Ok(Transaction::new(transaction_stream))
    }

    /// Closes this connection if it is open.
    ///
    /// # Examples
    ///
    /// ```rust
    /// connection.force_close()
    /// ```
    pub fn force_close(&self) -> Result {
        if !self.is_open() {
            return Ok(());
        }

        let result =
            self.server_connections.values().map(ServerConnection::force_close).try_collect().map_err(Into::into);
        self.background_runtime.force_close().and(result)
    }

    pub(crate) fn server_count(&self) -> usize {
        self.server_connections.len()
    }

    pub(crate) fn servers(&self) -> impl Iterator<Item = &Address> {
        self.server_connections.keys()
    }

    pub(crate) fn connection(&self, id: &Address) -> Option<&ServerConnection> {
        self.server_connections.get(id)
    }

    pub(crate) fn connections(&self) -> impl Iterator<Item = (&Address, &ServerConnection)> + '_ {
        self.server_connections.iter()
    }

    pub(crate) fn username(&self) -> Option<&str> {
        self.username.as_deref()
    }

    pub(crate) fn unable_to_connect_error(&self) -> Error {
        Error::Connection(ConnectionError::ServerConnectionFailed {
            addresses: self.servers().map(Address::clone).collect_vec(),
        })
    }
}

impl fmt::Debug for TypeDBDriver {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("Connection").field("server_connections", &self.server_connections).finish()
    }
}
