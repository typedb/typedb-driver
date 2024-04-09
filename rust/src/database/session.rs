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

use core::fmt;
use std::sync::{Arc, Mutex, RwLock};

use crossbeam::atomic::AtomicCell;
use log::warn;

use crate::{
    common::{error::ConnectionError, info::SessionInfo, Result, SessionType, TransactionType},
    connection::ServerConnection,
    Database, Options, Transaction,
};

type Callback = Box<dyn FnMut() + Send>;

/// A session with a TypeDB database.
pub struct Session {
    database: Database,
    server_session: RwLock<ServerSession>,
    session_type: SessionType,
    is_open: Arc<AtomicCell<bool>>,
    on_close: Arc<Mutex<Vec<Callback>>>,
    on_reopen: Mutex<Vec<Callback>>,
}

#[derive(Clone, Debug)]
struct ServerSession {
    connection: ServerConnection,
    info: SessionInfo,
}

impl fmt::Debug for Session {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("Session")
            .field("database", &self.database)
            .field("session_type", &self.session_type)
            .field("server_session", &self.server_session)
            .field("is_open", &self.is_open)
            .finish()
    }
}

impl Drop for Session {
    fn drop(&mut self) {
        if let Err(err) = self.force_close() {
            warn!("Error encountered while closing session: {}", err);
        }
    }
}

impl Session {
    /// Opens a communication tunnel (session) to the given database with default options.
    /// See [`Session::new_with_options`]
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn new(database: Database, session_type: SessionType) -> Result<Self> {
        Self::new_with_options(database, session_type, Options::new()).await
    }

    /// Opens a communication tunnel (session) to the given database on the running TypeDB server.
    ///
    /// # Arguments
    ///
    /// * `database` -- The database with which the session connects
    /// * `session_type` -- The type of session to be created (DATA or SCHEMA)
    /// * `options` -- `TypeDBOptions` for the session
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "Session::new_with_options(database, session_type, options);")]
    #[cfg_attr(not(feature = "sync"), doc = "Session::new_with_options(database, session_type, options).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn new_with_options(database: Database, session_type: SessionType, options: Options) -> Result<Self> {
        let server_session = database
            .run_failsafe(|database| async move {
                let session_info =
                    database.connection().open_session(database.name().to_owned(), session_type, options).await?;
                Ok(ServerSession { connection: database.connection().clone(), info: session_info })
            })
            .await?;

        let is_open = Arc::new(AtomicCell::new(true));
        let on_close: Arc<Mutex<Vec<Callback>>> = Arc::new(Mutex::new(vec![Box::new({
            let is_open = is_open.clone();
            move || is_open.store(false)
        })]));
        register_persistent_on_close(&server_session.info, on_close.clone());

        Ok(Self {
            database,
            session_type,
            server_session: RwLock::new(server_session),
            is_open,
            on_close,
            on_reopen: Mutex::default(),
        })
    }

    /// Returns the name of the database of the session.
    ///
    /// # Examples
    ///
    /// ```rust
    /// session.database_name();
    /// ```
    pub fn database_name(&self) -> &str {
        self.database.name()
    }

    /// The current session’s type (SCHEMA or DATA)
    pub fn type_(&self) -> SessionType {
        self.session_type
    }

    /// Checks whether this session is open.
    ///
    /// # Examples
    ///
    /// ```rust
    /// session.is_open();
    /// ```
    pub fn is_open(&self) -> bool {
        self.is_open.load()
    }

    /// Closes the session. Before opening a new session, the session currently open should first be closed.
    ///
    /// # Examples
    ///
    /// ```rust
    /// session.force_close();
    /// ```
    pub fn force_close(&self) -> Result {
        if self.is_open.compare_exchange(true, false).is_ok() {
            let server_session = self.server_session.write().unwrap();
            server_session.connection.close_session(server_session.info.session_id.clone())?;
        }
        Ok(())
    }

    /// Registers a callback function which will be executed when this session is closed.
    ///
    /// # Arguments
    ///
    /// * `function` -- The callback function.
    ///
    /// # Examples
    ///
    /// ```rust
    /// session.on_close(function);
    /// ```
    pub fn on_close(&self, callback: impl FnMut() + Send + 'static) {
        self.on_close.lock().unwrap().push(Box::new(callback));
    }

    fn on_server_session_close(&self, callback: impl FnOnce() + Send + 'static) {
        let server_session = self.server_session.write().unwrap();
        server_session.info.on_close_register_sink.send(Box::new(callback)).ok();
    }

    /// Registers a callback function which will be executed when this session is reopened.
    /// A session may be closed if it times out, or loses the connection to the database.
    /// In such situations, the session is reopened automatically when opening a new transaction.
    ///
    /// # Arguments
    ///
    /// * `function` -- The callback function.
    ///
    /// # Examples
    ///
    /// ```rust
    /// session.on_reopen(function);
    /// ```
    pub fn on_reopen(&self, callback: impl FnMut() + Send + 'static) {
        self.on_reopen.lock().unwrap().push(Box::new(callback));
    }

    fn reopened(&self) {
        self.on_reopen.lock().unwrap().iter_mut().for_each(|callback| (callback)());
        let server_session = self.server_session.write().unwrap();
        register_persistent_on_close(&server_session.info, self.on_close.clone());
    }

    /// Opens a transaction to perform read or write queries on the database connected to the session.
    /// See [`Session::transaction_with_options`]
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn transaction(&self, transaction_type: TransactionType) -> Result<Transaction<'_>> {
        self.transaction_with_options(transaction_type, Options::new()).await
    }

    /// Opens a transaction to perform read or write queries on the database connected to the session.
    ///
    /// # Arguments
    ///
    /// * `transaction_type` -- The type of transaction to be created (READ or WRITE)
    /// * `options` -- Options for the session
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "session.transaction_with_options(transaction_type, options);")]
    #[cfg_attr(not(feature = "sync"), doc = "session.transaction_with_options(transaction_type, options).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn transaction_with_options(
        &self,
        transaction_type: TransactionType,
        options: Options,
    ) -> Result<Transaction<'_>> {
        if !self.is_open() {
            return Err(ConnectionError::SessionIsClosed.into());
        }

        let ServerSession { connection: server_connection, info: SessionInfo { session_id, network_latency, .. } } =
            self.server_session.read().unwrap().clone();

        let (transaction_stream, transaction_shutdown_sink) = match server_connection
            .open_transaction(session_id.clone(), transaction_type, options, network_latency)
            .await
        {
            Ok((transaction_stream, transaction_shutdown_sink)) => (transaction_stream, transaction_shutdown_sink),
            Err(_err) => {
                self.is_open.store(false);
                server_connection.close_session(session_id).ok();

                let (server_session, (transaction_stream, transaction_shutdown_sink)) = self
                    .database
                    .run_failsafe(|database| {
                        let session_type = self.session_type;
                        async move {
                            let connection = database.connection();
                            let database_name = database.name().to_owned();
                            let session_info = connection.open_session(database_name, session_type, options).await?;
                            Ok((
                                ServerSession { connection: connection.clone(), info: session_info.clone() },
                                connection
                                    .open_transaction(
                                        session_info.session_id,
                                        transaction_type,
                                        options,
                                        session_info.network_latency,
                                    )
                                    .await?,
                            ))
                        }
                    })
                    .await?;
                *self.server_session.write().unwrap() = server_session;
                self.is_open.store(true);
                self.reopened();
                (transaction_stream, transaction_shutdown_sink)
            }
        };

        self.on_server_session_close(move || {
            transaction_shutdown_sink.send(()).ok();
        });

        Ok(Transaction::new(transaction_stream))
    }
}

fn register_persistent_on_close(server_session_info: &SessionInfo, callbacks: Arc<Mutex<Vec<Callback>>>) {
    server_session_info
        .on_close_register_sink
        .send(Box::new(move || callbacks.lock().unwrap().iter_mut().for_each(|callback| (callback)())))
        .ok();
}
