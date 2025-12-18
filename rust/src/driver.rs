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

use tracing::{debug, error, info, trace};
use tracing_subscriber::{fmt as tracing_fmt, layer::SubscriberExt, util::SubscriberInitExt, EnvFilter};

use crate::{
    common::{consistency_level::ConsistencyLevel, Addresses, Result},
    connection::{
        runtime::BackgroundRuntime,
        server::{
            server_connection::ServerConnection, server_manager::ServerManager, server_replica::ServerReplica,
            server_version::ServerVersion,
        },
        server_replica::AvailableServerReplica,
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

    /// Initialize logging configuration for the TypeDB driver.
    ///
    /// This function sets up tracing with the following priority:
    /// 1. TYPEDB_DRIVER_LOG environment variable (if set). Use TYPEDB_DRIVER_CLIB_LOG to see memory exchanges
    /// 1.  environment variable (if set)
    /// 2. RUST_LOG environment variable (if set)
    /// 3. Default level (INFO)
    ///
    /// The logging is initialized only once using a static flag to prevent
    /// multiple initializations in applications that create multiple drivers.
    pub fn init_logging() {
        use std::sync::Once;
        static INIT: Once = Once::new();

        INIT.call_once(|| {
            let clib_level = if let Ok(typedb_driver_clib_log) = std::env::var("TYPEDB_DRIVER_CLIB_LOG") {
                typedb_driver_clib_log
            } else {
                "info".to_owned()
            };
            // Try to get log level from TYPEDB_DRIVER_LOG first
            let env_filter = if let Ok(typedb_log_level) = std::env::var("TYPEDB_DRIVER_LOG") {
                EnvFilter::new(&format!("typedb_driver={},typedb_driver_clib={}", typedb_log_level, clib_level))
            } else if let Ok(rust_log) = std::env::var("RUST_LOG") {
                // If RUST_LOG is set, use it but scope it to typedb_driver only
                EnvFilter::new(&format!("typedb_driver={},typedb_driver_clib={}", rust_log, clib_level))
            } else {
                EnvFilter::new(&format!("typedb_driver=info,typedb_driver_clib={}", clib_level))
            };

            // Initialize the tracing subscriber
            if let Err(e) =
                tracing_subscriber::registry().with(env_filter).with(tracing_fmt::layer().with_target(false)).try_init()
            {
                eprintln!("Failed to initialize logging: {}", e);
            }
        });
    }

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
        Self::init_logging();

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

    /// Retrieves the server's version, using default strong consistency.
    ///
    /// See [`Self::server_version_with_consistency`] for more details and options.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.server_version()")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.server_version().await")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn server_version(&self) -> Result<ServerVersion> {
        self.server_version_with_consistency(ConsistencyLevel::Strong).await
    }

    /// Retrieves the server's version, using default strong consistency.
    ///
    /// # Arguments
    ///
    /// * `consistency_level` — The consistency level to use for the operation
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.server_version_with_consistency(ConsistencyLevel::Strong);")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.server_version_with_consistency(ConsistencyLevel::Strong).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn server_version_with_consistency(&self, consistency_level: ConsistencyLevel) -> Result<ServerVersion> {
        self.server_manager
            .execute(consistency_level, |server_connection| async move { server_connection.version().await })
            .await
    }

    // TODO: Maybe call it just "servers"?
    /// Retrieves the server's replicas, using default strong consistency.
    ///
    /// See [`Self::replicas_with_consistency`] for more details and options.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.replicas();")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.replicas().await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn replicas(&self) -> Result<HashSet<ServerReplica>> {
        self.replicas_with_consistency(ConsistencyLevel::Strong).await
    }

    /// Retrieves the server's replicas, using default strong consistency.
    ///
    /// # Arguments
    ///
    /// * `consistency_level` — The consistency level to use for the operation
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.replicas_with_consistency();")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.replicas_with_consistency(ConsistencyLevel::Strong).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn replicas_with_consistency(
        &self,
        consistency_level: ConsistencyLevel,
    ) -> Result<HashSet<ServerReplica>> {
        self.server_manager.fetch_replicas(consistency_level).await
    }

    // TODO: Add servers_get call for a specific server. How to design it?

    /// Retrieves the server's primary replica, if exists, using default strong consistency.
    ///
    /// See [`Self::primary_replica_with_consistency`] for more details and options.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.primary_replica();")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.primary_replica().await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn primary_replica(&self) -> Result<Option<AvailableServerReplica>> {
        self.primary_replica_with_consistency(ConsistencyLevel::Strong).await
    }

    /// Retrieves the server's primary replica, if exists.
    ///
    /// # Arguments
    ///
    /// * `consistency_level` — The consistency level to use for the operation
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.primary_replica_with_consistency(ConsistencyLevel::Strong);")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.primary_replica_with_consistency(ConsistencyLevel::Strong).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn primary_replica_with_consistency(
        &self,
        consistency_level: ConsistencyLevel,
    ) -> Result<Option<AvailableServerReplica>> {
        self.server_manager.fetch_primary_replica(consistency_level).await
    }

    /// Registers a new replica in the cluster the driver is currently connected to. The registered
    /// replica will become available eventually, depending on the behavior of the whole cluster.
    /// To register a replica, its clustering address should be passed, not the connection address.
    ///
    /// # Arguments
    ///
    /// * `replica_id` — The numeric identifier of the new replica
    /// * `address` — The clustering address of the TypeDB replica as a string
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.register_replica(2, \"127.0.0.1:2729\")")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.register_replica(2, \"127.0.0.1:2729\").await")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn register_replica(&self, replica_id: u64, address: String) -> Result {
        self.server_manager.register_replica(replica_id, address).await
    }

    // TODO: Rename to replica_register and replica_deregister? Does not actually matter since we're removing this

    /// Deregisters a replica from the cluster the driver is currently connected to. This replica
    /// will no longer play a raft role in this cluster.
    ///
    /// # Arguments
    ///
    /// * `replica_id` — The numeric identifier of the deregistered replica
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.deregister_replica(2)")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.deregister_replica(2).await")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn deregister_replica(&self, replica_id: u64) -> Result {
        self.server_manager.deregister_replica(replica_id).await
    }

    /// Updates address translation of the driver. This lets you actualize new translation
    /// information without recreating the driver from scratch. Useful after registering new
    /// replicas requiring address translation.
    /// This operation will update existing connections using the provided addresses.
    ///
    /// # Arguments
    ///
    /// * `addresses` — Addresses containing the new address translation information
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(
        feature = "sync",
        doc = "driver.update_address_translation(Addresses::try_from_translation_str([(\"typedb-cloud.ext:11729\", \"127.0.0.1:1729\")].into()).unwrap());"
    )]
    #[cfg_attr(
        not(feature = "sync"),
        doc = "driver.update_address_translation(Addresses::try_from_translation_str([(\"typedb-cloud.ext:11729\", \"127.0.0.1:1729\")].into()).unwrap()).await;"
    )]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn update_address_translation(&self, addresses: Addresses) -> Result {
        self.server_manager.update_address_translation(addresses).await
    }

    /// Opens a transaction with default options.
    ///
    /// See [`TypeDBDriver::transaction_with_options`] for more details.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.users().all_with_consistency(ConsistencyLevel::Strong);")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.users().all_with_consistency(ConsistencyLevel::Strong).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn transaction(
        &self,
        database_name: impl AsRef<str>,
        transaction_type: TransactionType,
    ) -> Result<Transaction> {
        self.transaction_with_options(database_name, transaction_type, TransactionOptions::new()).await
    }

    /// Opens a new transaction with the following consistency level:
    /// * read transaction - strong consistency, can be overridden through `options`;
    /// * write transaction - strong consistency, cannot be overridden;
    /// * schema transaction - strong consistency, cannot be overridden.
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
        let transaction_stream = match transaction_type {
            TransactionType::Read => {
                let consistency_level = options.read_consistency_level.clone().unwrap_or(ConsistencyLevel::Strong);
                self.server_manager.execute(consistency_level, open_fn).await?
            }
            TransactionType::Write | TransactionType::Schema => {
                self.server_manager.execute(ConsistencyLevel::Strong, open_fn).await?
            }
        };

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
