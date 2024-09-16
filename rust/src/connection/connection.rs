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
    sync::{Arc, Mutex},
    time::Duration,
};
use std::sync::atomic::{AtomicU64, Ordering};

use itertools::Itertools;
use tokio::sync::mpsc::UnboundedSender;
use tokio::time::Instant;
use uuid::Uuid;

use crate::{
    common::{
        address::Address,
        error::{ConnectionError, Error},
        info::DatabaseInfo
        , Result, TransactionType,
    },
    connection::message::{Request, Response, TransactionRequest},
    Credential,
    error::InternalError,
    Options, user::User,
};
use crate::common::RequestID;

use super::{
    network::transmitter::{RPCTransmitter, TransactionTransmitter},
    runtime::BackgroundRuntime,
    TransactionStream,
};

/// A connection to a TypeDB server which serves as the starting point for all interaction.
#[derive(Clone)]
pub struct Connection {
    server_connections: HashMap<Address, ServerConnection>,
    background_runtime: Arc<BackgroundRuntime>,
    username: Option<String>,
    is_cloud: bool,
}

impl Connection {
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
        Self::new_core_with_description(address, "rust", "0.0.0.testing").await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn new_core_with_description(address: impl AsRef<str>, driver_lang: impl AsRef<str>, driver_version: impl AsRef<str>) -> Result<Self> {
        let id = address.as_ref().to_string();
        let address: Address = id.parse()?;
        let background_runtime = Arc::new(BackgroundRuntime::new()?);
        let server_connection = ServerConnection::new_core(
            background_runtime.clone(),
            address,
            driver_lang.as_ref(),
            driver_version.as_ref()
        ).await?;

        let advertised_address = server_connection
            .servers_all()?
            .into_iter()
            .exactly_one()
            .map_err(|e| ConnectionError::ServerConnectionFailedStatusError { error: e.to_string() })?;

        Ok(Self {
            server_connections: [(advertised_address, server_connection)].into(),
            background_runtime,
            username: None,
            is_cloud: false,
        })
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
    ) -> Result<Connection> {
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
        addresses: impl IntoIterator<Item=impl AsRef<str>> + Clone,
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

    /// Closes this connection.
    ///
    /// # Examples
    ///
    /// ```rust
    /// connection.force_close()
    /// ```
    pub fn force_close(&self) -> Result {
        let result =
            self.server_connections.values().map(ServerConnection::force_close).try_collect().map_err(Into::into);
        self.background_runtime.force_close().and(result)
    }

    pub(crate) fn server_count(&self) -> usize {
        self.server_connections.len()
    }

    pub(crate) fn servers(&self) -> impl Iterator<Item=&Address> {
        self.server_connections.keys()
    }

    pub(crate) fn connection(&self, id: &Address) -> Option<&ServerConnection> {
        self.server_connections.get(id)
    }

    pub(crate) fn connections(&self) -> impl Iterator<Item=(&Address, &ServerConnection)> + '_ {
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

impl fmt::Debug for Connection {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("Connection").field("server_connections", &self.server_connections).finish()
    }
}

#[derive(Clone)]
pub(crate) struct ServerConnection {
    background_runtime: Arc<BackgroundRuntime>,
    connection_id: Uuid,
    open_transactions: Arc<Mutex<HashMap<RequestID, UnboundedSender<()>>>>,
    request_transmitter: Arc<RPCTransmitter>,
    latency_tracker: LatencyTracker,
}

impl ServerConnection {
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn new_core(
        background_runtime: Arc<BackgroundRuntime>,
        address: Address,
        driver_lang: &str,
        driver_version: &str,
    ) -> Result<Self> {
        let request_transmitter = Arc::new(RPCTransmitter::start_core(address, &background_runtime)?);
        let (connection_id, latency) = Self::open_connection(&request_transmitter, driver_lang, driver_version).await?;
        let latency_tracker = LatencyTracker::new(latency);
        Ok(Self {
            background_runtime,
            connection_id,
            open_transactions: Default::default(),
            request_transmitter,
            latency_tracker,
        })
    }

    fn new_cloud(background_runtime: Arc<BackgroundRuntime>, address: Address, credential: Credential) -> Result<Self> {
        todo!()
        // let request_transmitter = Arc::new(RPCTransmitter::start_cloud(address, credential, &background_runtime)?);
        // Ok(Self { background_runtime, open_sessions: Default::default(), request_transmitter })
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn open_connection(request_transmitter: &RPCTransmitter, driver_lang: &str, driver_version: &str) -> Result<(Uuid, Duration)> {
        let message = Request::ConnectionOpen {
            driver_lang: driver_lang.to_owned(),
            driver_version: driver_version.to_owned(),
        };

        let request_time = Instant::now();
        match request_transmitter.request(message).await? {
            Response::ConnectionOpen { connection_id, server_duration_millis } => {
                let latency = Instant::now().duration_since(request_time) - Duration::from_millis(server_duration_millis);
                Ok((connection_id, latency))
            },
            other => Err(ConnectionError::UnexpectedResponse { response: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn request(&self, request: Request) -> Result<Response> {
        if !self.background_runtime.is_open() {
            return Err(ConnectionError::ConnectionIsClosed.into());
        }
        self.request_transmitter.request(request).await
    }

    fn request_blocking(&self, request: Request) -> Result<Response> {
        if !self.background_runtime.is_open() {
            return Err(ConnectionError::ConnectionIsClosed.into());
        }
        self.request_transmitter.request_blocking(request)
    }

    pub(crate) fn force_close(&self) -> Result {
        // let session_ids: Vec<SessionID> = self.open_sessions.lock().unwrap().keys().cloned().collect();
        // for session_id in session_ids {
        //     self.close_session(session_id).ok();
        // }
        todo!()
        // self.request_transmitter.force_close()
    }

    pub(crate) fn servers_all(&self) -> Result<Vec<Address>> {
        match self.request_blocking(Request::ServersAll)? {
            Response::ServersAll { servers } => Ok(servers),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn database_exists(&self, database_name: String) -> Result<bool> {
        match self.request(Request::DatabasesContains { database_name }).await? {
            Response::DatabasesContains { contains } => Ok(contains),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn create_database(&self, database_name: String) -> Result {
        self.request(Request::DatabaseCreate { database_name }).await?;
        Ok(())
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn get_database_replicas(&self, database_name: String) -> Result<DatabaseInfo> {
        match self.request(Request::DatabaseGet { database_name }).await? {
            Response::DatabaseGet { database } => Ok(database),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn all_databases(&self) -> Result<Vec<DatabaseInfo>> {
        match self.request(Request::DatabasesAll).await? {
            Response::DatabasesAll { databases } => Ok(databases),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn database_schema(&self, database_name: String) -> Result<String> {
        match self.request(Request::DatabaseSchema { database_name }).await? {
            Response::DatabaseSchema { schema } => Ok(schema),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn database_type_schema(&self, database_name: String) -> Result<String> {
        match self.request(Request::DatabaseTypeSchema { database_name }).await? {
            Response::DatabaseTypeSchema { schema } => Ok(schema),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn delete_database(&self, database_name: String) -> Result {
        self.request(Request::DatabaseDelete { database_name }).await?;
        Ok(())
    }

    // #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    // pub(crate) async fn open_session(
    //     &self,
    //     database_name: String,
    //     session_type: SessionType,
    //     options: Options,
    // ) -> Result<SessionInfo> {
    //     let start = Instant::now();
    //     match self.request(Request::SessionOpen { database_name, session_type, options }).await? {
    //         Response::SessionOpen { session_id, server_duration } => {
    //             let (on_close_register_sink, on_close_register_source) = unbounded_async();
    //             let (pulse_shutdown_sink, pulse_shutdown_source) = unbounded_async();
    //             self.open_sessions.lock().unwrap().insert(session_id.clone(), pulse_shutdown_sink);
    //             self.background_runtime.spawn(session_pulse(
    //                 session_id.clone(),
    //                 self.request_transmitter.clone(),
    //                 on_close_register_source,
    //                 self.background_runtime.callback_handler_sink(),
    //                 pulse_shutdown_source,
    //             ));
    //             Ok(SessionInfo {
    //                 session_id,
    //                 network_latency: start.elapsed().saturating_sub(server_duration),
    //                 on_close_register_sink,
    //             })
    //         }
    //         other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
    //     }
    // }
    //
    // pub(crate) fn close_session(&self, session_id: SessionID) -> Result {
    //     if let Some(sink) = self.open_sessions.lock().unwrap().remove(&session_id) {
    //         sink.send(()).ok();
    //     }
    //     self.request_blocking(Request::SessionClose { session_id })?;
    //     Ok(())
    // }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn open_transaction(
        &self,
        database_name: &str,
        transaction_type: TransactionType,
        options: Options,
    ) -> Result<(TransactionStream, UnboundedSender<()>)> {
        let network_latency = self.latency_tracker.current_latency();
        match self
            .request(Request::Transaction(TransactionRequest::Open {
                database: database_name.to_owned(),
                transaction_type,
                options,
                network_latency,
            }))
            .await?
        {
            Response::TransactionOpen { request_sink, response_source } => {
                let transmitter = TransactionTransmitter::new(
                    &self.background_runtime,
                    request_sink,
                    response_source,
                    self.background_runtime.callback_handler_sink(),
                );
                let transmitter_shutdown_sink = transmitter.shutdown_sink().clone();
                let transaction_stream = TransactionStream::new(transaction_type, options, transmitter);
                Ok((transaction_stream, transmitter_shutdown_sink))
            }
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn all_users(&self) -> Result<Vec<User>> {
        match self.request(Request::UsersAll).await? {
            Response::UsersAll { users } => Ok(users),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn contains_user(&self, username: String) -> Result<bool> {
        match self.request(Request::UsersContain { username }).await? {
            Response::UsersContain { contains } => Ok(contains),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn create_user(&self, username: String, password: String) -> Result {
        match self.request(Request::UsersCreate { username, password }).await? {
            Response::UsersCreate => Ok(()),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn delete_user(&self, username: String) -> Result {
        match self.request(Request::UsersDelete { username }).await? {
            Response::UsersDelete => Ok(()),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn get_user(&self, username: String) -> Result<Option<User>> {
        match self.request(Request::UsersGet { username }).await? {
            Response::UsersGet { user } => Ok(user),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn set_user_password(&self, username: String, password: String) -> Result {
        match self.request(Request::UsersPasswordSet { username, password }).await? {
            Response::UsersPasswordSet => Ok(()),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn update_user_password(
        &self,
        username: String,
        password_old: String,
        password_new: String,
    ) -> Result {
        match self.request(Request::UserPasswordUpdate { username, password_old, password_new }).await? {
            Response::UserPasswordUpdate => Ok(()),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }
}

impl fmt::Debug for ServerConnection {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("ServerConnection").field("connection_id", &self.connection_id).finish()
    }
}

#[derive(Debug, Clone)]
struct LatencyTracker {
    latency_millis: Arc<AtomicU64>,
}

impl LatencyTracker {
    fn new(initial_latency: Duration) -> Self {
        Self { latency_millis: Arc::new(AtomicU64::new(initial_latency.as_millis() as u64)) }
    }

    pub(crate) fn update_latency(&self, latency_millis: Duration) {
        let previous_latency = self.latency_millis.load(Ordering::Relaxed);
        // TODO: this is a strange but simple averaging scheme
        self.latency_millis.store((latency_millis.as_millis() as u64 + previous_latency) / 2, Ordering::Relaxed);
    }

    fn current_latency(&self) -> Duration {
        Duration::from_millis(self.latency_millis.load(Ordering::Relaxed))
    }
}
