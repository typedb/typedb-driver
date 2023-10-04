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

use std::sync::RwLock;

use crossbeam::atomic::AtomicCell;
use log::warn;

use crate::{
    common::{error::ConnectionError, info::SessionInfo, Result, SessionType, TransactionType},
    Database, Options, Transaction,
};

#[derive(Debug)]
pub struct Session {
    database: Database,
    server_session_info: RwLock<SessionInfo>,
    session_type: SessionType,
    is_open: AtomicCell<bool>,
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
        let server_session_info = RwLock::new(
            database
                .run_failsafe(|database, _, _| {
                    let options = options.clone();
                    async move {
                        database.connection().open_session(database.name().to_owned(), session_type, options).await
                    }
                })
                .await?,
        );

        Ok(Self { database, session_type, server_session_info, is_open: AtomicCell::new(true) })
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
            let session_info = self.server_session_info.write().unwrap();
            let connection = self.database.connection().connection(&session_info.address).unwrap();
            connection.close_session(session_info.session_id.clone())?;
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
    pub fn on_close(&self, callback: impl FnOnce() + Send + 'static) {
        let session_info = self.server_session_info.write().unwrap();
        session_info.on_close_register_sink.send(Box::new(callback)).ok();
    }

    /// Opens a transaction to perform read or write queries on the database connected to the session.
    /// See [`Session::transaction_with_options`]
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn transaction(&self, transaction_type: TransactionType) -> Result<Transaction> {
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
    ) -> Result<Transaction> {
        if !self.is_open() {
            return Err(ConnectionError::SessionIsClosed().into());
        }

        let (session_info, (transaction_stream, shutdown_sink)) = self
            .database
            .run_failsafe(|database, _, is_first_run| {
                let session_info = self.server_session_info.read().unwrap().clone();
                let session_type = self.session_type;
                let options = options.clone();
                async move {
                    let connection = database.connection();
                    let session_info = if is_first_run {
                        session_info
                    } else {
                        connection.open_session(database.name().to_owned(), session_type, options.clone()).await?
                    };
                    Ok((
                        session_info.clone(),
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

        *self.server_session_info.write().unwrap() = session_info;

        self.on_close(move || {
            shutdown_sink.send(()).ok();
        });
        Ok(Transaction::new(transaction_stream))
    }
}
