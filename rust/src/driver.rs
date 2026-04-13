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
use std::{collections::HashSet, fmt, sync::Arc};

use tracing::{debug, error};

use crate::{
    common::{Addresses, Result},
    connection::{
        runtime::BackgroundRuntime,
        server::{
            server_connection::ServerConnection, server_manager::ServerManager, server_routing::ServerRouting,
            server_version::ServerVersion, AvailableServer, Server,
        },
    },
    Credentials, DatabaseManager, DriverOptions, Transaction, TransactionOptions, TransactionType, UserManager,
};

/// A connection to a TypeDB server which serves as the starting point for all interaction.
pub struct TypeDBDriver {
    server_manager: Arc<ServerManager>,
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

    pub const DEFAULT_ADDRESS: &'static str = "127.0.0.1:1729";

    /// Creates a new TypeDB Server connection.
    ///
    /// # Arguments
    ///
    /// * `addresses` — The address(es) of the TypeDB Server(s), provided in a unified format
    /// * `credentials` — The Credentials to connect with
    /// * `driver_options` — The DriverOptions to connect with
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(
        feature = "sync",
        doc = "TypeDBDriver::new(Addresses::try_from_address_str(\"127.0.0.1:1729\").unwrap(), Credentials::new(\"username\", \"password\"), DriverOptions::new(true, None))"
    )]
    #[cfg_attr(
        not(feature = "sync"),
        doc = "TypeDBDriver::new(Addresses::try_from_address_str(\"127.0.0.1:1729\").unwrap(), Credentials::new(\"username\", \"password\"), DriverOptions::new(true, None)).await"
    )]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn new(addresses: Addresses, credentials: Credentials, driver_options: DriverOptions) -> Result<Self> {
        debug!("Creating new TypeDB driver connection to {:?}", addresses);
        Self::new_with_description(addresses, credentials, driver_options, Self::DRIVER_LANG).await
    }

    /// Creates a new TypeDB Server connection with a description.
    /// This method is generally used by TypeDB drivers built on top of the Rust driver.
    /// In other cases, use [`Self::new`] instead.
    ///
    /// # Arguments
    ///
    /// * `addresses` — The address(es) of the TypeDB Server(s), provided in a unified format
    /// * `credentials` — The Credentials to connect with
    /// * `driver_options` — The DriverOptions to connect with
    /// * `driver_lang` — The language of the driver connecting to the server
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(
        feature = "sync",
        doc = "TypeDBDriver::new_with_description(Addresses::try_from_address_str(\"127.0.0.1:1729\").unwrap(), Credentials::new(\"username\", \"password\"), DriverOptions::new(true, None), \"rust\")"
    )]
    #[cfg_attr(
        not(feature = "sync"),
        doc = "TypeDBDriver::new_with_description(Addresses::try_from_address_str(\"127.0.0.1:1729\").unwrap(), Credentials::new(\"username\", \"password\"), DriverOptions::new(true, None), \"rust\").await"
    )]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn new_with_description(
        addresses: Addresses,
        credentials: Credentials,
        driver_options: DriverOptions,
        driver_lang: impl AsRef<str>,
    ) -> Result<Self> {
        debug!("Initializing TypeDB driver with description: {}", driver_lang.as_ref());
        let background_runtime = Arc::new(BackgroundRuntime::new()?);

        debug!("Establishing server connection to {:?}", addresses);
        let server_manager = Arc::new(
            ServerManager::new(
                background_runtime.clone(),
                addresses,
                credentials,
                driver_options,
                driver_lang.as_ref(),
                Self::VERSION,
            )
            .await?,
        );
        debug!("Successfully connected to servers");

        let database_manager = DatabaseManager::new(server_manager.clone())?;
        let user_manager = UserManager::new(server_manager.clone());
        debug!("Created database manager and user manager");

        debug!("TypeDB driver initialization completed successfully");
        Ok(Self { server_manager, database_manager, user_manager, background_runtime })
    }

    /// Checks it this connection is opened.
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.is_open()
    /// ```
    pub fn is_open(&self) -> bool {
        self.background_runtime.is_open()
    }

    /// The ``DatabaseManager`` for this connection, providing access to database management methods.
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.databases()
    /// ```
    pub fn databases(&self) -> &DatabaseManager {
        &self.database_manager
    }

    /// The ``UserManager`` for this connection, providing access to user management methods.
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.databases()
    /// ```
    pub fn users(&self) -> &UserManager {
        &self.user_manager
    }

    /// Retrieves the server's version, using default automatic server routing.
    ///
    /// See [`Self::server_version_with_routing`] for more details and options.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.server_version()")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.server_version().await")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn server_version(&self) -> Result<ServerVersion> {
        self.server_version_with_routing(ServerRouting::Auto).await
    }

    /// Retrieves the server's version.
    ///
    /// # Arguments
    ///
    /// * `server_routing` — The server routing directive to use for the operation
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.server_version_with_routing(ServerRouting::Auto);")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.server_version_with_routing(ServerRouting::Auto).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn server_version_with_routing(&self, server_routing: ServerRouting) -> Result<ServerVersion> {
        self.server_manager
            .execute(server_routing, |server_connection| async move { server_connection.version().await })
            .await
    }

    /// Retrieves the servers, using default automatic server routing.
    ///
    /// See [`Self::servers_with_routing`] for more details and options.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.servers();")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.servers().await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn servers(&self) -> Result<HashSet<Server>> {
        self.servers_with_routing(ServerRouting::Auto).await
    }

    /// Retrieves the servers.
    ///
    /// # Arguments
    ///
    /// * `server_routing` — The server routing directive to use for the operation
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.servers_with_routing(ServerRouting::Auto);")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.servers_with_routing(ServerRouting::Auto).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn servers_with_routing(&self, server_routing: ServerRouting) -> Result<HashSet<Server>> {
        self.server_manager.fetch_servers(server_routing).await
    }

    // TODO: Add servers_get call for a specific server. How to design it?

    /// Retrieves the primary server, if exists, using default automatic server routing.
    ///
    /// See [`Self::primary_server_with_routing`] for more details and options.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.primary_server();")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.primary_server().await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn primary_server(&self) -> Result<Option<AvailableServer>> {
        self.primary_server_with_routing(ServerRouting::Auto).await
    }

    /// Retrieves the primary server, if exists.
    ///
    /// # Arguments
    ///
    /// * `server_routing` — The server routing directive to use for the operation
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.primary_server_with_routing(ServerRouting::Auto);")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.primary_server_with_routing(ServerRouting::Auto).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn primary_server_with_routing(&self, server_routing: ServerRouting) -> Result<Option<AvailableServer>> {
        self.server_manager.fetch_primary_server(server_routing).await
    }

    /// Updates address translation of the driver. This lets you actualize new translation
    /// information without recreating the driver from scratch. Useful after registering new
    /// replicas requiring address translation.
    /// This operation will update existing connections using the provided addresses.
    ///
    /// The ``DriverOptions`` for this connection.
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.options()
    /// ```
    pub fn options(&self) -> &DriverOptions {
        self.server_manager.driver_options()
    }

    /// The ``Addresses`` this connection is configured to.
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.configured_addresses()
    /// ```
    pub fn configured_addresses(&self) -> &Addresses {
        self.server_manager.configured_addresses()
    }

    /// Opens a transaction with default options.
    ///
    /// See [`TypeDBDriver::transaction_with_options`] for more details.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.transaction(database_name, TransactionType::Read);")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.transaction(database_name, TransactionType::Read).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn transaction(
        &self,
        database_name: impl AsRef<str>,
        transaction_type: TransactionType,
    ) -> Result<Transaction> {
        self.transaction_with_options(database_name, transaction_type, TransactionOptions::new()).await
    }

    /// Opens a new transaction with custom transaction options.
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
    #[cfg_attr(
        feature = "sync",
        doc = "transaction.transaction_with_options(database_name, transaction_type, options)"
    )]
    #[cfg_attr(
        not(feature = "sync"),
        doc = "transaction.transaction_with_options(database_name, transaction_type, options).await"
    )]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn transaction_with_options(
        &self,
        database_name: impl AsRef<str>,
        transaction_type: TransactionType,
        options: TransactionOptions,
    ) -> Result<Transaction> {
        let database_name = database_name.as_ref();
        let open_fn = |server_connection: ServerConnection| {
            let options = options.clone();
            async move { server_connection.open_transaction(database_name, transaction_type, options).await }
        };

        debug!("Opening transaction for database: {} with type: {:?}", database_name, transaction_type);
        let transaction_stream = self.server_manager.execute(ServerRouting::Auto, open_fn).await?;

        debug!("Successfully opened transaction for database: {}", database_name);
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

        debug!("Closing TypeDB driver connection");
        let close_result = self.server_manager.force_close().and(self.background_runtime.force_close());
        match &close_result {
            Ok(_) => debug!("Successfully closed TypeDB driver connection"),
            Err(e) => error!("Failed to close TypeDB driver connection: {}", e),
        }
        close_result
    }
}

impl fmt::Debug for TypeDBDriver {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("TypeDBDriver").field("server_manager", &self.server_manager).finish()
    }
}
