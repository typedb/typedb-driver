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
    sync::{
        atomic::{AtomicU64, Ordering},
        Arc, Mutex,
    },
    time::Duration,
};

use tokio::{sync::mpsc::UnboundedSender, time::Instant};
use uuid::Uuid;

use crate::{
    common::{address::Address, RequestID},
    connection::{
        database::{export_stream::DatabaseExportStream, import_stream::DatabaseImportStream},
        message::{DatabaseImportRequest, Request, Response, TransactionRequest, TransactionResponse},
        network::transmitter::{
            DatabaseExportTransmitter, DatabaseImportTransmitter, RPCTransmitter, TransactionTransmitter,
        },
        runtime::BackgroundRuntime,
        TransactionStream,
    },
    error::{ConnectionError, InternalError},
    info::{DatabaseInfo, UserInfo},
    Credentials, DriverOptions, TransactionOptions, TransactionType, User,
};

#[derive(Clone)]
pub(crate) struct ServerConnection {
    background_runtime: Arc<BackgroundRuntime>,
    username: String,
    connection_id: Uuid,
    request_transmitter: Arc<RPCTransmitter>,
    shutdown_senders: Arc<Mutex<Vec<UnboundedSender<()>>>>,
    latency_tracker: LatencyTracker,
}

impl ServerConnection {
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn new(
        background_runtime: Arc<BackgroundRuntime>,
        address: Address,
        credentials: Credentials,
        driver_options: DriverOptions,
        driver_lang: &str,
        driver_version: &str,
    ) -> crate::Result<(Self, Vec<DatabaseInfo>)> {
        let username = credentials.username().to_string();
        let request_transmitter =
            Arc::new(RPCTransmitter::start(address, credentials.clone(), driver_options, &background_runtime)?);
        let (connection_id, latency, database_info) =
            Self::open_connection(&request_transmitter, driver_lang, driver_version, credentials).await?;
        let latency_tracker = LatencyTracker::new(latency);
        let server_connection = Self {
            background_runtime,
            username,
            connection_id,
            request_transmitter,
            shutdown_senders: Default::default(),
            latency_tracker,
        };
        Ok((server_connection, database_info))
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn open_connection(
        request_transmitter: &RPCTransmitter,
        driver_lang: &str,
        driver_version: &str,
        credentials: Credentials,
    ) -> crate::Result<(Uuid, Duration, Vec<DatabaseInfo>)> {
        let message = Request::ConnectionOpen {
            driver_lang: driver_lang.to_owned(),
            driver_version: driver_version.to_owned(),
            credentials,
        };

        let request_time = Instant::now();
        match request_transmitter.request(message).await? {
            Response::ConnectionOpen { connection_id, server_duration_millis, databases: database_info } => {
                let latency =
                    Instant::now().duration_since(request_time) - Duration::from_millis(server_duration_millis);
                Ok((connection_id, latency, database_info))
            }
            other => Err(ConnectionError::UnexpectedResponse { response: format!("{other:?}") }.into()),
        }
    }

    pub fn username(&self) -> &str {
        self.username.as_str()
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn request(&self, request: Request) -> crate::Result<Response> {
        if !self.background_runtime.is_open() {
            return Err(ConnectionError::ServerConnectionIsClosed.into());
        }
        self.request_transmitter.request(request).await
    }

    fn request_blocking(&self, request: Request) -> crate::Result<Response> {
        if !self.background_runtime.is_open() {
            return Err(ConnectionError::ServerConnectionIsClosed.into());
        }
        self.request_transmitter.request_blocking(request)
    }

    pub(crate) fn force_close(&self) -> crate::Result {
        for sender in self.shutdown_senders.lock().unwrap().drain(..) {
            let _ = sender.send(());
        }
        self.request_transmitter.force_close()
    }

    pub(crate) fn servers_all(&self) -> crate::Result<Vec<Address>> {
        match self.request_blocking(Request::ServersAll)? {
            Response::ServersAll { servers } => Ok(servers),
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
    pub(crate) async fn get_database_replicas(&self, database_name: String) -> crate::Result<DatabaseInfo> {
        match self.request(Request::DatabaseGet { database_name }).await? {
            Response::DatabaseGet { database } => Ok(database),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn contains_database(&self, database_name: String) -> crate::Result<bool> {
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
    pub(crate) async fn import_database(
        &self,
        database_name: String,
        schema: String,
    ) -> crate::Result<DatabaseImportStream> {
        match self
            .request(Request::DatabaseImport(DatabaseImportRequest::Initial { name: database_name, schema }))
            .await?
        {
            Response::DatabaseImport { request_sink } => {
                let transmitter = DatabaseImportTransmitter::new(self.background_runtime.clone(), request_sink);
                let transmitter_shutdown_sink = transmitter.shutdown_sink().clone();
                let import_stream = DatabaseImportStream::new(transmitter);

                // TODO: Is it needed?
                self.shutdown_senders.lock().unwrap().push(transmitter_shutdown_sink);
                Ok(import_stream)
            }
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn delete_database(&self, database_name: String) -> crate::Result {
        self.request(Request::DatabaseDelete { database_name }).await?;
        Ok(())
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
    pub(crate) async fn database_export(&self, database_name: String) -> crate::Result<DatabaseExportStream> {
        match self.request(Request::DatabaseExport { database_name }).await? {
            Response::DatabaseExportStream { response_source } => {
                let transmitter = DatabaseExportTransmitter::new(self.background_runtime.clone(), response_source);
                let transmitter_shutdown_sink = transmitter.shutdown_sink().clone();
                let export_stream = DatabaseExportStream::new(transmitter);
                self.shutdown_senders.lock().unwrap().push(transmitter_shutdown_sink);
                Ok(export_stream)
            }
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn open_transaction(
        &self,
        database_name: &str,
        transaction_type: TransactionType,
        options: TransactionOptions,
    ) -> crate::Result<TransactionStream> {
        let network_latency = self.latency_tracker.current_latency();
        let open_request_start = Instant::now();

        match self
            .request(Request::Transaction(TransactionRequest::Open {
                database: database_name.to_owned(),
                transaction_type,
                options,
                network_latency,
            }))
            .await?
        {
            Response::TransactionStream {
                open_request_id: request_id,
                request_sink,
                response_source,
                server_duration_millis,
            } => {
                let open_latency =
                    Instant::now().duration_since(open_request_start).as_millis() as u64 - server_duration_millis;
                self.latency_tracker.update_latency(open_latency);

                let transmitter =
                    TransactionTransmitter::new(self.background_runtime.clone(), request_sink, response_source);
                let transmitter_shutdown_sink = transmitter.shutdown_sink().clone();
                let transaction_stream = TransactionStream::new(transaction_type, options, transmitter);
                self.shutdown_senders.lock().unwrap().push(transmitter_shutdown_sink);
                Ok(transaction_stream)
            }
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn all_users(&self) -> crate::Result<Vec<UserInfo>> {
        match self.request(Request::UsersAll).await? {
            Response::UsersAll { users } => Ok(users),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn get_user(&self, name: String) -> crate::Result<Option<UserInfo>> {
        match self.request(Request::UsersGet { name }).await? {
            Response::UsersGet { user } => Ok(user),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn contains_user(&self, name: String) -> crate::Result<bool> {
        match self.request(Request::UsersContains { name }).await? {
            Response::UsersContain { contains } => Ok(contains),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn create_user(&self, name: String, password: String) -> crate::Result {
        match self.request(Request::UsersCreate { user: UserInfo { name, password: Some(password) } }).await? {
            Response::UsersCreate => Ok(()),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn update_password(&self, name: String, password: String) -> crate::Result {
        match self
            .request(Request::UsersUpdate {
                username: name,
                user: UserInfo { name: "".to_string(), password: Some(password) }, // TODO: make 'name' optional
            })
            .await?
        {
            Response::UsersUpdate => Ok(()),
            other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub(crate) async fn delete_user(&self, name: String) -> crate::Result {
        match self.request(Request::UsersDelete { name }).await? {
            Response::UsersDelete => Ok(()),
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
pub(crate) struct LatencyTracker {
    latency_millis: Arc<AtomicU64>,
}

impl LatencyTracker {
    fn new(initial_latency: Duration) -> Self {
        Self { latency_millis: Arc::new(AtomicU64::new(initial_latency.as_millis() as u64)) }
    }

    pub(crate) fn update_latency(&self, latency_millis: u64) {
        let previous_latency = self.latency_millis.load(Ordering::Relaxed);
        // TODO: this is a strange but simple averaging scheme
        //       it might actually be useful as it weights the recent measurement the same as the entire history
        self.latency_millis.store((latency_millis + previous_latency) / 2, Ordering::Relaxed);
    }

    fn current_latency(&self) -> Duration {
        Duration::from_millis(self.latency_millis.load(Ordering::Relaxed))
    }
}
