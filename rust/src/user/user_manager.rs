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

use crate::{
    common::{consistency_level::ConsistencyLevel, Result},
    connection::server::server_manager::ServerManager,
    User,
};

/// Provides access to all user management methods.
#[derive(Debug)]
pub struct UserManager {
    server_manager: Arc<ServerManager>,
}

impl UserManager {
    pub fn new(server_manager: Arc<ServerManager>) -> Self {
        Self { server_manager }
    }

    /// Checks if a user with the given name exists, using default strong consistency.
    ///
    /// See [`Self::contains_with_consistency`] for more details and options.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.users().contains(username);")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.users().contains(username).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn contains(&self, username: impl Into<String>) -> Result<bool> {
        self.contains_with_consistency(username, ConsistencyLevel::Strong).await
    }

    /// Checks if a user with the given name exists.
    ///
    /// # Arguments
    ///
    /// * `username` — The username to be checked
    /// * `consistency_level` — The consistency level to use for the operation
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.users().contains_with_consistency(username, ConsistencyLevel::Strong);")]
    #[cfg_attr(
        not(feature = "sync"),
        doc = "driver.users().contains_with_consistency(username, ConsistencyLevel::Strong).await;"
    )]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn contains_with_consistency(
        &self,
        username: impl Into<String>,
        consistency_level: ConsistencyLevel,
    ) -> Result<bool> {
        let username = username.into();
        self.server_manager
            .execute(consistency_level, move |server_connection| {
                let username = username.clone();
                async move { server_connection.contains_user(username).await }
            })
            .await
    }

    /// Retrieves a user with the given name, using default strong consistency.
    ///
    /// See [`Self::get_with_consistency`] for more details and options.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.users().get(username);")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.users().get(username).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn get(&self, username: impl Into<String>) -> Result<Option<User>> {
        self.get_with_consistency(username, ConsistencyLevel::Strong).await
    }

    /// Retrieves a user with the given name.
    ///
    /// # Arguments
    ///
    /// * `username` — The name of the user to retrieve
    /// * `consistency_level` — The consistency level to use for the operation
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.users().get_with_consistency(username, ConsistencyLevel::Strong);")]
    #[cfg_attr(
        not(feature = "sync"),
        doc = "driver.users().get_with_consistency(username, ConsistencyLevel::Strong).await;"
    )]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn get_with_consistency(
        &self,
        username: impl Into<String>,
        consistency_level: ConsistencyLevel,
    ) -> Result<Option<User>> {
        let username = username.into();
        self.server_manager
            .execute(consistency_level, |server_connection| {
                let username = username.clone();
                let server_manager = self.server_manager.clone();
                async move {
                    let user_info = server_connection.get_user(username).await?;
                    Ok(user_info.map(|user_info| User::from_info(user_info, server_manager)))
                }
            })
            .await
    }

    /// Returns the user of the current connection, using default strong consistency.
    ///
    /// See [`Self::get_current_with_consistency`] for more details and options.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.users().get_current();")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.users().get_current().await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn get_current(&self) -> Result<Option<User>> {
        self.get(self.server_manager.username()?).await
    }

    /// Returns the user of the current connection.
    ///
    /// # Arguments
    ///
    /// * `consistency_level` — The consistency level to use for the operation
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.users().get_current_with_consistency(ConsistencyLevel::Strong);")]
    #[cfg_attr(
        not(feature = "sync"),
        doc = "driver.users().get_current_with_consistency(ConsistencyLevel::Strong).await;"
    )]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn get_current_with_consistency(&self, consistency_level: ConsistencyLevel) -> Result<Option<User>> {
        self.get_with_consistency(self.server_manager.username()?, consistency_level).await
    }

    /// Retrieves all users which exist on the TypeDB server, using default strong consistency.
    ///
    /// See [`Self::all_with_consistency`] for more details and options.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.users().all();")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.users().all().await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn all(&self) -> Result<Vec<User>> {
        self.all_with_consistency(ConsistencyLevel::Strong).await
    }

    /// Retrieves all users which exist on the TypeDB server.
    ///
    /// # Arguments
    ///
    /// * `consistency_level` — The consistency level to use for the operation
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.users().all_with_consistency(ConsistencyLevel::Strong);")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.users().all_with_consistency(ConsistencyLevel::Strong).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn all_with_consistency(&self, consistency_level: ConsistencyLevel) -> Result<Vec<User>> {
        self.server_manager
            .execute(consistency_level, |server_connection| {
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

    /// Creates a user with the given name &amp; password. Always uses strong consistency.
    ///
    /// # Arguments
    ///
    /// * `username` — The name of the user to be created
    /// * `password` — The password of the user to be created
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "driver.users().create(username, password);")]
    #[cfg_attr(not(feature = "sync"), doc = "driver.users().create(username, password).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn create(&self, username: impl Into<String>, password: impl Into<String>) -> Result {
        self.create_with_consistency(username, password, ConsistencyLevel::Strong).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn create_with_consistency(
        &self,
        username: impl Into<String>,
        password: impl Into<String>,
        consistency_level: ConsistencyLevel,
    ) -> Result {
        let username = username.into();
        let password = password.into();
        self.server_manager
            .execute(consistency_level, move |server_connection| {
                let username = username.clone();
                let password = password.clone();
                async move { server_connection.create_user(username, password).await }
            })
            .await
    }
}
