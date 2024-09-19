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


use std::collections::HashMap;
use std::fmt;
use std::sync::{Arc, Mutex};
use std::sync::atomic::{AtomicU64, Ordering};
use std::time::Duration;
use tokio::sync::mpsc::UnboundedSender;
use tokio::time::Instant;
use uuid::Uuid;
use crate::common::address::Address;
use crate::common::RequestID;
use crate::connection::message::{Request, Response, TransactionRequest};
use crate::connection::network::transmitter::{RPCTransmitter, TransactionTransmitter};
use crate::connection::runtime::BackgroundRuntime;
use crate::{Credential, Database, Options, TransactionType, User};
use crate::connection::TransactionStream;
use crate::error::{ConnectionError, InternalError};
use crate::info::DatabaseInfo;

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
    pub(crate) async fn new_core(
        background_runtime: Arc<BackgroundRuntime>,
        address: Address,
        driver_lang: &str,
        driver_version: &str,
    ) -> crate::Result<(Self, Vec<DatabaseInfo>)> {
        let request_transmitter = Arc::new(RPCTransmitter::start_core(address, &background_runtime)?);
        let (connection_id, latency, database_info) = Self::open_connection(&request_transmitter, driver_lang, driver_version).await?;
        let latency_tracker = LatencyTracker::new(latency);
        let server_connection = Self {
            background_runtime,
            connection_id,
            open_transactions: Default::default(),
            request_transmitter,
            latency_tracker,
        };
        Ok((server_connection, database_info))
    }

    pub(crate) fn new_cloud(background_runtime: Arc<BackgroundRuntime>, address: Address, credential: Credential) -> crate::Result<Self> {
        todo!()
        // let request_transmitter = Arc::new(RPCTransmitter::start_cloud(address, credential, &background_runtime)?);
        // Ok(Self { background_runtime, open_sessions: Default::default(), request_transmitter })
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn open_connection(
        request_transmitter: &RPCTransmitter,
        driver_lang: &str,
        driver_version: &str
    ) -> crate::Result<(Uuid, Duration, Vec<DatabaseInfo>)> {
        let message = Request::ConnectionOpen {
            driver_lang: driver_lang.to_owned(),
            driver_version: driver_version.to_owned(),
        };

        let request_time = Instant::now();
        match request_transmitter.request(message).await? {
            Response::ConnectionOpen { connection_id, server_duration_millis, databases: database_info } => {
                let latency = Instant::now().duration_since(request_time) - Duration::from_millis(server_duration_millis);
                Ok((connection_id, latency, database_info))
            }
            other => Err(ConnectionError::UnexpectedResponse { response: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn request(&self, request: Request) -> crate::Result<Response> {
        if !self.background_runtime.is_open() {
            return Err(ConnectionError::ConnectionIsClosed.into());
        }
        self.request_transmitter.request(request).await
    }

    fn request_blocking(&self, request: Request) -> crate::Result<Response> {
        if !self.background_runtime.is_open() {
            return Err(ConnectionError::ConnectionIsClosed.into());
        }
        self.request_transmitter.request_blocking(request)
    }

    pub(crate) fn force_close(&self) -> crate::Result {
        // let session_ids: Vec<SessionID> = self.open_sessions.lock().unwrap().keys().cloned().collect();
        // for session_id in session_ids {
        //     self.close_session(session_id).ok();
        // }
        todo!()
        // self.request_transmitter.force_close()
    }

    pub(crate) fn servers_all(&self) -> crate::Result<Vec<Address>> {
        match self.request_blocking(Request::ServersAll)? {
            Response::ServersAll { servers } => Ok(servers),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn database_exists(&self, database_name: String) -> crate::Result<bool> {
        match self.request(Request::DatabasesContains { database_name }).await? {
            Response::DatabasesContains { contains } => Ok(contains),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn create_database(&self, database_name: String) -> crate::Result<DatabaseInfo> {
        match self.request(Request::DatabaseCreate { database_name }).await? {
            Response::DatabaseCreate { database } => Ok(database),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn get_database_replicas(&self, database_name: String) -> crate::Result<DatabaseInfo> {
        match self.request(Request::DatabaseGet { database_name }).await? {
            Response::DatabaseGet { database } => Ok(database),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn all_databases(&self) -> crate::Result<Vec<DatabaseInfo>> {
        match self.request(Request::DatabasesAll).await? {
            Response::DatabasesAll { databases } => Ok(databases),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn database_schema(&self, database_name: String) -> crate::Result<String> {
        match self.request(Request::DatabaseSchema { database_name }).await? {
            Response::DatabaseSchema { schema } => Ok(schema),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn database_type_schema(&self, database_name: String) -> crate::Result<String> {
        match self.request(Request::DatabaseTypeSchema { database_name }).await? {
            Response::DatabaseTypeSchema { schema } => Ok(schema),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn delete_database(&self, database_name: String) -> crate::Result {
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
    ) -> crate::Result<(TransactionStream, UnboundedSender<()>)> {
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
                    self.background_runtime.clone(),
                    request_sink,
                    response_source,
                );
                let transmitter_shutdown_sink = transmitter.shutdown_sink().clone();
                let transaction_stream = TransactionStream::new(transaction_type, options, transmitter);
                Ok((transaction_stream, transmitter_shutdown_sink))
            }
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn all_users(&self) -> crate::Result<Vec<User>> {
        match self.request(Request::UsersAll).await? {
            Response::UsersAll { users } => Ok(users),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn contains_user(&self, username: String) -> crate::Result<bool> {
        match self.request(Request::UsersContain { username }).await? {
            Response::UsersContain { contains } => Ok(contains),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn create_user(&self, username: String, password: String) -> crate::Result {
        match self.request(Request::UsersCreate { username, password }).await? {
            Response::UsersCreate => Ok(()),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn delete_user(&self, username: String) -> crate::Result {
        match self.request(Request::UsersDelete { username }).await? {
            Response::UsersDelete => Ok(()),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn get_user(&self, username: String) -> crate::Result<Option<User>> {
        match self.request(Request::UsersGet { username }).await? {
            Response::UsersGet { user } => Ok(user),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn set_user_password(&self, username: String, password: String) -> crate::Result {
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
    ) -> crate::Result {
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
