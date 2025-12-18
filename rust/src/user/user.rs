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
    info::UserInfo,
};

#[derive(Clone, Debug)]
pub struct User {
    name: String,
    password: Option<String>,
    server_manager: Arc<ServerManager>,
}

impl User {
    pub(crate) fn new(name: String, password: Option<String>, server_manager: Arc<ServerManager>) -> Self {
        Self { name, password, server_manager }
    }

    pub(crate) fn from_info(user_info: UserInfo, server_manager: Arc<ServerManager>) -> Self {
        Self::new(user_info.name, user_info.password, server_manager)
    }

    /// Retrieves the username as a string.
    pub fn name(&self) -> &str {
        self.name.as_str()
    }

    // TODO: We don't actually need to expose it?
    /// Retrieves the password as a string, if accessible.
    pub fn password(&self) -> Option<&str> {
        self.password.as_ref().map(|value| value.as_str())
    }

    /// Updates the user's password, using default strong consistency.
    ///
    /// See [`Self::update_password_with_consistency`] for more details and options.
    ///
    /// # Arguments
    ///
    /// * `password` — The new password
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "user.update_password(password);")]
    #[cfg_attr(not(feature = "sync"), doc = "user.update_password(password).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn update_password(&self, password: impl Into<String>) -> Result<()> {
        self.update_password_with_consistency(password, ConsistencyLevel::Strong).await
    }

    /// Updates the user's password.
    ///
    /// # Arguments
    ///
    /// * `password` — The new password
    /// * `consistency_level` — The consistency level to use for the operation
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "user.update_password_with_consistency(password, ConsistencyLevel::Strong);")]
    #[cfg_attr(
        not(feature = "sync"),
        doc = "user.update_password_with_consistency(password, ConsistencyLevel::Strong).await;"
    )]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn update_password_with_consistency(
        &self,
        password: impl Into<String>,
        consistency_level: ConsistencyLevel,
    ) -> Result<()> {
        let password = password.into();
        self.server_manager
            .execute(consistency_level, |server_connection| {
                let name = self.name.clone();
                let password = password.clone();
                async move { server_connection.update_password(name, password).await }
            })
            .await
    }

    /// Deletes this user, using default strong consistency.
    ///
    /// See [`Self::delete_with_consistency`] for more details and options.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "user.delete();")]
    #[cfg_attr(not(feature = "sync"), doc = "user.delete().await;")]
    /// user.delete(username).await;
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn delete(self) -> Result {
        self.delete_with_consistency(ConsistencyLevel::Strong).await
    }

    /// Deletes this user.
    ///
    /// # Arguments
    ///
    /// * `consistency_level` — The consistency level to use for the operation
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "user.delete_with_consistency(ConsistencyLevel::Strong);")]
    #[cfg_attr(not(feature = "sync"), doc = "user.delete_with_consistency(ConsistencyLevel::Strong).await;")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn delete_with_consistency(self, consistency_level: ConsistencyLevel) -> Result {
        self.server_manager
            .execute(consistency_level, |server_connection| {
                let name = self.name.clone();
                async move { server_connection.delete_user(name).await }
            })
            .await
    }
}
