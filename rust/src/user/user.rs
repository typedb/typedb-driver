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
use std::sync::Arc;
use crate::common::address::Address;
use crate::connection::server_connection::ServerConnection;
use crate::common::Result;
use crate::error::ConnectionError;

#[derive(Clone, Debug)]
pub struct User {
    pub name: String,
    pub password: Option<String>,
    pub server_connections: HashMap<Address, ServerConnection>,
}

impl User {


    /// Update the user's password.
    ///
    /// # Arguments
    ///
    /// * `username` — The name of the user
    /// * `password` — The new password
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "user.update_password(username, password);")]
    #[cfg_attr(not(feature = "sync"), doc = "user.update_password(username, password).await;")]
    /// user.update_password(username, password).await;
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn update_password(&self, password: impl Into<String>) -> Result<()> {
        let password = password.into();
        let mut error_buffer = Vec::with_capacity(self.server_connections.len());
        for (server_id, server_connection) in self.server_connections.iter() {
            match server_connection.update_password(self.name.clone(), password.clone()).await {
                Ok(res) => return Ok(()),
                Err(err) => error_buffer.push(format!("- {}: {}", server_id, err)),
            }
        }
        Err(ConnectionError::ServerConnectionFailedWithError { error: error_buffer.join("\n") })?
    }

    /// Deletes this user
    ///
    /// * `username` — The name of the user to be deleted
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
        let mut error_buffer = Vec::with_capacity(self.server_connections.len());
        for (server_id, server_connection) in self.server_connections.iter() {
            match server_connection.delete_user(self.name.clone()).await {
                Ok(res) => return Ok(res),
                Err(err) => error_buffer.push(format!("- {}: {}", server_id, err)),
            }
        }
        Err(ConnectionError::ServerConnectionFailedWithError { error: error_buffer.join("\n") })?
    }
}