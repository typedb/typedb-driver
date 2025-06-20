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
use std::sync::Arc;

use crate::{common::Result, connection::server::server_manager::ServerManager, error::ConnectionError, User};

/// Provides access to all user management methods.
#[derive(Debug)]
pub struct UserManager {
    server_manager: Arc<ServerManager>,
}

impl UserManager {
    pub fn new(server_manager: Arc<ServerManager>) -> Self {
        Self { server_manager }
    }

    /// Returns the user of the current connection.
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.users.get_current_user().await;
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn get_current_user(&self) -> Result<Option<User>> {
        self.get(self.server_manager.username()?).await
    }

    /// Checks if a user with the given name exists.
    ///
    /// # Arguments
    ///
    /// * `username` — The username to be checked
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.users.contains(username).await;
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn contains(&self, username: impl Into<String>) -> Result<bool> {
        let username = username.into();
        self.server_manager
            .run_read_operation(move |server_connection| {
                let username = username.clone();
                async move { server_connection.contains_user(username).await }
            })
            .await
    }

    /// Retrieve a user with the given name.
    ///
    /// # Arguments
    ///
    /// * `username` — The name of the user to retrieve
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.users.get(username).await;
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn get(&self, username: impl Into<String>) -> Result<Option<User>> {
        let username = username.into();
        self.server_manager
            .run_read_operation(|server_connection| {
                let username = username.clone();
                let server_manager = self.server_manager.clone();
                async move {
                    let user_info = server_connection.get_user(username).await?;
                    Ok(user_info.map(|user_info| User::from_info(user_info, server_manager)))
                }
            })
            .await
    }

    /// Retrieves all users which exist on the TypeDB server.
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.users.all().await;
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn all(&self) -> Result<Vec<User>> {
        self.server_manager
            .run_read_operation(|server_connection| {
                let server_manager = self.server_manager.clone();
                async move {
                    let user_infos = server_connection.all_users().await?;
                    Ok(user_infos
                        .into_iter()
                        .map(|user_info| User::from_info(user_info, server_manager.clone()))
                        .collect())
                }
            })
            .await
    }

    /// Create a user with the given name &amp; password.
    ///
    /// # Arguments
    ///
    /// * `username` — The name of the user to be created
    /// * `password` — The password of the user to be created
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.users.create(username, password).await;
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn create(&self, username: impl Into<String>, password: impl Into<String>) -> Result {
        let username = username.into();
        let password = password.into();
        self.server_manager
            .run_read_operation(move |server_connection| {
                let username = username.clone();
                let password = password.clone();
                async move { server_connection.create_user(username, password).await }
            })
            .await
    }
}
