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
    Credentials, DatabaseManager, DriverOptions, Transaction, TransactionOptions, TransactionType, UserManager,
};

/// A connection to a TypeDB server which serves as the starting point for all interaction.
pub struct TypeDBDriver {
    server_connections: HashMap<Address, ServerConnection>,
    database_manager: DatabaseManager,
    user_manager: UserManager,
    background_runtime: Arc<BackgroundRuntime>,
}

impl TypeDBDriver {
    const DRIVER_LANG: &'static str = "rust";
    const VERSION: &'static str = match option_env!("CARGO_PKG_VERSION") {
        None => "0.0.0",
        Some(version) => version,
    };

    pub const DEFAULT_ADDRESS: &'static str = "localhost:1729";

    /// Creates a new TypeDB Server connection.
    ///
    /// # Arguments
    ///
    /// * `address` — The address (host:port) on which the TypeDB Server is running
    /// * `credentials` — The Credentials to connect with
    /// * `driver_options` — The DriverOptions to connect with
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "TypeDBDriver::new(\"127.0.0.1:1729\")")]
    #[cfg_attr(not(feature = "sync"), doc = "TypeDBDriver::new(\"127.0.0.1:1729\").await")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn new(
        address: impl AsRef<str>,
        credentials: Credentials,
        driver_options: DriverOptions,
    ) -> Result<Self> {
        Self::new_with_description(address, credentials, driver_options, Self::DRIVER_LANG).await
    }

    /// Creates a new TypeDB Server connection with a description.
    /// This method is generally used by TypeDB drivers built on top of the Rust driver.
    /// In other cases, use [`Self::new`] instead.
    ///
    /// # Arguments
    ///
    /// * `address` — The address (host:port) on which the TypeDB Server is running
    /// * `credentials` — The Credentials to connect with
    /// * `driver_options` — The DriverOptions to connect with
    /// * `driver_lang` — The language of the driver connecting to the server
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "TypeDBDriver::new_with_description(\"127.0.0.1:1729\", \"rust\")")]
    #[cfg_attr(not(feature = "sync"), doc = "TypeDBDriver::new_with_description(\"127.0.0.1:1729\", \"rust\").await")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn new_with_description(
        address: impl AsRef<str>,
        credentials: Credentials,
        driver_options: DriverOptions,
        driver_lang: impl AsRef<str>,
    ) -> Result<Self> {
        let id = address.as_ref().to_string();
        let address: Address = id.parse()?;
        let background_runtime = Arc::new(BackgroundRuntime::new()?);

        let (server_connection, database_info) = ServerConnection::new(
            background_runtime.clone(),
            address.clone(),
            credentials,
            driver_options,
            driver_lang.as_ref(),
            Self::VERSION,
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
        let user_manager = UserManager::new(server_connections.clone());

        Ok(Self { server_connections, database_manager, user_manager, background_runtime })
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn fetch_server_list(
        background_runtime: Arc<BackgroundRuntime>,
        addresses: impl IntoIterator<Item = impl AsRef<str>> + Clone,
        credentials: Credentials,
        driver_options: DriverOptions,
    ) -> Result<HashSet<Address>> {
        let addresses: Vec<Address> = addresses.into_iter().map(|addr| addr.as_ref().parse()).try_collect()?;
        for address in &addresses {
            let server_connection = ServerConnection::new(
                background_runtime.clone(),
                address.clone(),
                credentials.clone(),
                driver_options.clone(),
                Self::DRIVER_LANG,
                Self::VERSION,
            )
            .await;
            match server_connection {
                Ok((server_connection, _)) => match server_connection.servers_all() {
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
    /// driver.is_open()
    /// ```
    pub fn is_open(&self) -> bool {
        self.background_runtime.is_open()
    }

    pub fn databases(&self) -> &DatabaseManager {
        &self.database_manager
    }

    pub fn users(&self) -> &UserManager {
        &self.user_manager
    }

    /// Opens a transaction with default options.
    /// See [`TypeDBDriver::transaction_with_options`]
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn transaction(
        &self,
        database_name: impl AsRef<str>,
        transaction_type: TransactionType,
    ) -> Result<Transaction> {
        self.transaction_with_options(database_name, transaction_type, TransactionOptions::new()).await
    }

    /// Performs a TypeQL query in this transaction.
    ///
    /// # Arguments
    ///
    /// * `database_name` — The name of the database to connect to
    /// * `transaction_type` — The TransactionType to open the transaction with
    /// * `options` — The TransactionOptions to open the transaction with
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.transaction_with_options(database_name, transaction_type, options)
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn transaction_with_options(
        &self,
        database_name: impl AsRef<str>,
        transaction_type: TransactionType,
        options: TransactionOptions,
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
    /// driver.force_close()
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
