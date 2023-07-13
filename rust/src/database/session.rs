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
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn new(database: Database, session_type: SessionType) -> Result<Self> {
        Self::new_with_options(database, session_type, Options::new()).await
    }

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

    pub fn database_name(&self) -> &str {
        self.database.name()
    }

    pub fn type_(&self) -> SessionType {
        self.session_type
    }

    pub fn is_open(&self) -> bool {
        self.is_open.load()
    }

    pub fn force_close(&self) -> Result {
        if self.is_open.compare_exchange(true, false).is_ok() {
            let session_info = self.server_session_info.write().unwrap();
            let connection = self.database.connection().connection(&session_info.address).unwrap();
            connection.close_session(session_info.session_id.clone())?;
        }
        Ok(())
    }

    pub fn on_close(&self, callback: impl FnOnce() + Send + 'static) {
        let session_info = self.server_session_info.write().unwrap();
        session_info.on_close_register_sink.send(Box::new(callback)).ok();
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn transaction(&self, transaction_type: TransactionType) -> Result<Transaction> {
        self.transaction_with_options(transaction_type, Options::new()).await
    }

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
