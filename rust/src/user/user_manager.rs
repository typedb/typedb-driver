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

#[cfg(not(feature = "sync"))]
use std::future::Future;

use crate::{common::Result, connection::ServerConnection, Connection, DatabaseManager, User};

#[derive(Clone, Debug)]
pub struct UserManager {
    pub(crate) connection: Connection,
}

impl UserManager {
    const SYSTEM_DB: &'static str = "_system";

    pub fn new(connection: Connection) -> Self {
        Self { connection }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn current_user(&self) -> Result<Option<User>> {
        match self.connection.username() {
            Some(username) => self.get(username).await,
            None => Ok(None), // FIXME error
        }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn all(&self) -> Result<Vec<User>> {
        self.run_any_node(|server_connection: ServerConnection| async move { server_connection.all_users().await })
            .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn contains(&self, username: impl Into<String>) -> Result<bool> {
        let username = username.into();
        self.run_any_node(|server_connection: ServerConnection| {
            let username = username.clone();
            async move { server_connection.contains_user(username).await }
        })
        .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn create(&self, username: impl Into<String>, password: impl Into<String>) -> Result {
        let username = username.into();
        let password = password.into();
        self.run_any_node(|server_connection: ServerConnection| {
            let username = username.clone();
            let password = password.clone();
            async move { server_connection.create_user(username, password).await }
        })
        .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn delete(&self, username: impl Into<String>) -> Result {
        let username = username.into();
        self.run_any_node(|server_connection: ServerConnection| {
            let username = username.clone();
            async move { server_connection.delete_user(username).await }
        })
        .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn get(&self, username: impl Into<String>) -> Result<Option<User>> {
        let username = username.into();
        self.run_any_node(|server_connection: ServerConnection| {
            let username = username.clone();
            async move { server_connection.get_user(username).await }
        })
        .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn set_password(&self, username: impl Into<String>, password: impl Into<String>) -> Result {
        let username = username.into();
        let password = password.into();
        self.run_any_node(|server_connection: ServerConnection| {
            let username = username.clone();
            let password = password.clone();
            async move { server_connection.set_user_password(username, password).await }
        })
        .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn run_any_node<F, P, R>(&self, task: F) -> Result<R>
    where
        F: Fn(ServerConnection) -> P,
        P: Future<Output = Result<R>>,
    {
        DatabaseManager::new(self.connection.clone())
            .get(Self::SYSTEM_DB)
            .await?
            .run_failsafe(|_, server_connection, _| task(server_connection))
            .await
    }
}
