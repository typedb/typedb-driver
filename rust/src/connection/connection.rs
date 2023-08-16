/*
 * Copyright (C) 2022 Vaticle
 *
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

use itertools::Itertools;
use tokio::{
    select,
    sync::mpsc::{unbounded_channel as unbounded_async, UnboundedReceiver, UnboundedSender},
    time::{sleep_until, Instant},
};

use super::{
    network::transmitter::{RPCTransmitter, TransactionTransmitter},
    runtime::BackgroundRuntime,
    TransactionStream,
};
use crate::{
    common::{
        address::Address,
        error::{ConnectionError, Error},
        info::{DatabaseInfo, SessionInfo},
        Result, SessionID, SessionType, TransactionType,
    },
    connection::message::{Request, Response, TransactionRequest},
    error::InternalError,
    user::User,
    Credential, Options,
};

#[derive(Clone)]
pub struct Connection {
    server_connections: HashMap<Address, ServerConnection>,
    background_runtime: Arc<BackgroundRuntime>,
    username: Option<String>,
}

impl Connection {
    pub fn new_plaintext(address: impl AsRef<str>) -> Result<Self> {
        let address: Address = address.as_ref().parse()?;
        let background_runtime = Arc::new(BackgroundRuntime::new()?);
        let mut server_connection = ServerConnection::new_plaintext(background_runtime.clone(), address)?;
        let address = server_connection
            .servers_all()?
            .into_iter()
            .exactly_one()
            .map_err(|_| ConnectionError::UnableToConnect())?;
        server_connection.set_address(address.clone());
        Ok(Self { server_connections: [(address, server_connection)].into(), background_runtime, username: None })
    }

    pub fn new_encrypted<T: AsRef<str> + Sync>(init_addresses: &[T], credential: Credential) -> Result<Self> {
        let background_runtime = Arc::new(BackgroundRuntime::new()?);

        let init_addresses = init_addresses.iter().map(|addr| addr.as_ref().parse()).try_collect()?;
        let addresses = Self::fetch_current_addresses(background_runtime.clone(), init_addresses, credential.clone())?;

        let server_connections: HashMap<Address, ServerConnection> = addresses
            .into_iter()
            .map(|address| {
                ServerConnection::new_encrypted(background_runtime.clone(), address.clone(), credential.clone())
                    .map(|server_connection| (address, server_connection))
            })
            .try_collect()?;

        let errors: Vec<Error> =
            server_connections.values().map(|conn| conn.validate()).filter_map(Result::err).collect();
        if errors.len() == server_connections.len() {
            Err(ConnectionError::ClusterAllNodesFailed(
                errors.into_iter().map(|err| err.to_string()).collect::<Vec<_>>().join("\n"),
            ))?
        } else {
            Ok(Self { server_connections, background_runtime, username: Some(credential.username().to_string()) })
        }
    }

    fn fetch_current_addresses(
        background_runtime: Arc<BackgroundRuntime>,
        addresses: Vec<Address>,
        credential: Credential,
    ) -> Result<HashSet<Address>> {
        for address in addresses {
            let server_connection =
                ServerConnection::new_encrypted(background_runtime.clone(), address.clone(), credential.clone());
            match server_connection {
                Ok(server_connection) => match server_connection.servers_all() {
                    Ok(servers) => return Ok(servers.into_iter().collect()),
                    Err(Error::Connection(
                        ConnectionError::UnableToConnect() | ConnectionError::ConnectionRefused(),
                    )) => (),
                    Err(err) => Err(err)?,
                },
                Err(Error::Connection(ConnectionError::UnableToConnect() | ConnectionError::ConnectionRefused())) => (),
                Err(err) => Err(err)?,
            }
        }
        Err(ConnectionError::UnableToConnect().into())
    }

    pub fn is_open(&self) -> bool {
        self.background_runtime.is_open()
    }

    pub fn force_close(&self) -> Result {
        let result =
            self.server_connections.values().map(ServerConnection::force_close).try_collect().map_err(Into::into);
        self.background_runtime.force_close().and(result)
    }

    pub(crate) fn server_count(&self) -> usize {
        self.server_connections.len()
    }

    pub(crate) fn addresses(&self) -> impl Iterator<Item = &Address> {
        self.server_connections.keys()
    }

    pub(crate) fn connection(&self, address: &Address) -> Result<&ServerConnection> {
        self.server_connections
            .get(address)
            .ok_or_else(|| InternalError::UnknownConnectionAddress(address.clone()).into())
    }

    pub(crate) fn connections(&self) -> impl Iterator<Item = &ServerConnection> + '_ {
        self.server_connections.values()
    }

    pub(crate) fn username(&self) -> Option<&str> {
        self.username.as_ref().map(|s| s.as_ref())
    }

    pub(crate) fn unable_to_connect_error(&self) -> Error {
        Error::Connection(ConnectionError::ClusterUnableToConnect(
            self.addresses().map(Address::to_string).collect::<Vec<_>>().join(","),
        ))
    }
}

impl fmt::Debug for Connection {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("Connection").field("server_connections", &self.server_connections).finish()
    }
}

#[derive(Clone)]
pub(crate) struct ServerConnection {
    address: Address,
    background_runtime: Arc<BackgroundRuntime>,
    open_sessions: Arc<Mutex<HashMap<SessionID, UnboundedSender<()>>>>,
    request_transmitter: Arc<RPCTransmitter>,
}

impl ServerConnection {
    fn new_plaintext(background_runtime: Arc<BackgroundRuntime>, address: Address) -> Result<Self> {
        let request_transmitter = Arc::new(RPCTransmitter::start_plaintext(address.clone(), &background_runtime)?);
        Ok(Self { address, background_runtime, open_sessions: Default::default(), request_transmitter })
    }

    fn new_encrypted(
        background_runtime: Arc<BackgroundRuntime>,
        address: Address,
        credential: Credential,
    ) -> Result<Self> {
        let request_transmitter =
            Arc::new(RPCTransmitter::start_encrypted(address.clone(), credential, &background_runtime)?);
        Ok(Self { address, background_runtime, open_sessions: Default::default(), request_transmitter })
    }

    pub(crate) fn validate(&self) -> Result {
        match self.request_blocking(Request::ConnectionOpen)? {
            Response::ConnectionOpen => Ok(()),
            _other => Err(ConnectionError::UnableToConnect().into()),
        }
    }

    fn set_address(&mut self, address: Address) {
        self.address = address;
    }

    pub(crate) fn address(&self) -> &Address {
        &self.address
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn request(&self, request: Request) -> Result<Response> {
        if !self.background_runtime.is_open() {
            return Err(ConnectionError::ConnectionIsClosed().into());
        }
        self.request_transmitter.request(request).await
    }

    fn request_blocking(&self, request: Request) -> Result<Response> {
        if !self.background_runtime.is_open() {
            return Err(ConnectionError::ConnectionIsClosed().into());
        }
        self.request_transmitter.request_blocking(request)
    }

    pub(crate) fn force_close(&self) -> Result {
        let session_ids: Vec<SessionID> = self.open_sessions.lock().unwrap().keys().cloned().collect();
        for session_id in session_ids {
            self.close_session(session_id).ok();
        }
        self.request_transmitter.force_close()
    }

    pub(crate) fn servers_all(&self) -> Result<Vec<Address>> {
        match self.request_blocking(Request::ServersAll)? {
            Response::ServersAll { servers } => Ok(servers),
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn database_exists(&self, database_name: String) -> Result<bool> {
        match self.request(Request::DatabasesContains { database_name }).await? {
            Response::DatabasesContains { contains } => Ok(contains),
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
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
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn all_databases(&self) -> Result<Vec<DatabaseInfo>> {
        match self.request(Request::DatabasesAll).await? {
            Response::DatabasesAll { databases } => Ok(databases),
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn database_schema(&self, database_name: String) -> Result<String> {
        match self.request(Request::DatabaseSchema { database_name }).await? {
            Response::DatabaseSchema { schema } => Ok(schema),
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn database_type_schema(&self, database_name: String) -> Result<String> {
        match self.request(Request::DatabaseTypeSchema { database_name }).await? {
            Response::DatabaseTypeSchema { schema } => Ok(schema),
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn database_rule_schema(&self, database_name: String) -> Result<String> {
        match self.request(Request::DatabaseRuleSchema { database_name }).await? {
            Response::DatabaseRuleSchema { schema } => Ok(schema),
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn delete_database(&self, database_name: String) -> Result {
        self.request(Request::DatabaseDelete { database_name }).await?;
        Ok(())
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn open_session(
        &self,
        database_name: String,
        session_type: SessionType,
        options: Options,
    ) -> Result<SessionInfo> {
        let start = Instant::now();
        match self.request(Request::SessionOpen { database_name, session_type, options }).await? {
            Response::SessionOpen { session_id, server_duration } => {
                let (on_close_register_sink, on_close_register_source) = unbounded_async();
                let (pulse_shutdown_sink, pulse_shutdown_source) = unbounded_async();
                self.open_sessions.lock().unwrap().insert(session_id.clone(), pulse_shutdown_sink);
                self.background_runtime.spawn(session_pulse(
                    session_id.clone(),
                    self.request_transmitter.clone(),
                    on_close_register_source,
                    pulse_shutdown_source,
                ));
                Ok(SessionInfo {
                    address: self.address.clone(),
                    session_id,
                    network_latency: start.elapsed() - server_duration,
                    on_close_register_sink,
                })
            }
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
        }
    }

    pub(crate) fn close_session(&self, session_id: SessionID) -> Result {
        if let Some(sink) = self.open_sessions.lock().unwrap().remove(&session_id) {
            sink.send(()).ok();
        }
        self.request_blocking(Request::SessionClose { session_id })?;
        Ok(())
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn open_transaction(
        &self,
        session_id: SessionID,
        transaction_type: TransactionType,
        options: Options,
        network_latency: Duration,
    ) -> Result<(TransactionStream, UnboundedSender<()>)> {
        match self
            .request(Request::Transaction(TransactionRequest::Open {
                session_id,
                transaction_type,
                options: options.clone(),
                network_latency,
            }))
            .await?
        {
            Response::TransactionOpen { request_sink, response_source } => {
                let transmitter = TransactionTransmitter::new(&self.background_runtime, request_sink, response_source);
                let transmitter_shutdown_sink = transmitter.shutdown_sink().clone();
                let transaction_stream = TransactionStream::new(transaction_type, options, transmitter);
                Ok((transaction_stream, transmitter_shutdown_sink))
            }
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn all_users(&self) -> Result<Vec<User>> {
        match self.request(Request::UsersAll).await? {
            Response::UsersAll { users } => Ok(users),
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn contains_user(&self, username: String) -> Result<bool> {
        match self.request(Request::UsersContain { username }).await? {
            Response::UsersContain { contains } => Ok(contains),
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn create_user(&self, username: String, password: String) -> Result {
        match self.request(Request::UsersCreate { username, password }).await? {
            Response::UsersCreate => Ok(()),
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn delete_user(&self, username: String) -> Result {
        match self.request(Request::UsersDelete { username }).await? {
            Response::UsersDelete => Ok(()),
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn get_user(&self, username: String) -> Result<Option<User>> {
        match self.request(Request::UsersGet { username }).await? {
            Response::UsersGet { user } => Ok(user),
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn set_user_password(&self, username: String, password: String) -> Result {
        match self.request(Request::UsersPasswordSet { username, password }).await? {
            Response::UsersPasswordSet => Ok(()),
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
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
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
        }
    }
}

impl fmt::Debug for ServerConnection {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("ServerConnection")
            .field("address", &self.address)
            .field("open_sessions", &self.open_sessions)
            .finish()
    }
}

async fn session_pulse(
    session_id: SessionID,
    request_transmitter: Arc<RPCTransmitter>,
    mut on_close_callback_source: UnboundedReceiver<Box<dyn FnOnce() + Send>>,
    mut shutdown_source: UnboundedReceiver<()>,
) {
    const PULSE_INTERVAL: Duration = Duration::from_secs(5);
    let mut next_pulse = Instant::now();
    let mut on_close = vec![];
    loop {
        select! {
            _ = sleep_until(next_pulse) => {
                request_transmitter
                    .request_async(Request::SessionPulse { session_id: session_id.clone() })
                    .await
                    .ok();
                next_pulse += PULSE_INTERVAL;
            }
            callback = on_close_callback_source.recv() => {
                if let Some(callback) = callback {
                    on_close.push(callback)
                }
            }
            _ = shutdown_source.recv() => break,
        }
    }
    for callback in on_close {
        callback();
    }
}
