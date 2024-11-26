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

use crate::{
    common::{address::Address, Result},
    connection::server_connection::ServerConnection,
    error::ConnectionError,
    User,
};

/// Provides access to all user management methods.
#[derive(Debug)]
pub struct UserManager {
    server_connections: HashMap<Address, ServerConnection>,
}

impl UserManager {
    pub fn new(server_connections: HashMap<Address, ServerConnection>) -> Self {
        Self { server_connections }
    }

    /// Checks if a user with the given name exists.
    ///
    /// # Arguments
    ///
    /// * `username` - The user name to be checked
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.users.contains(username).await;
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn contains(&self, username: impl Into<String>) -> Result<bool> {
        let username = username.into();
        let mut error_buffer = Vec::with_capacity(self.server_connections.len());
        for (server_id, server_connection) in self.server_connections.iter() {
            match server_connection.contains_user(username.clone()).await {
                Ok(res) => return Ok(res),
                Err(err) => error_buffer.push(format!("- {}: {}", server_id, err)),
            }
        }
        Err(ConnectionError::ServerConnectionFailedWithError { error: error_buffer.join("\n") })?
    }

    /// Retrieve a user with the given name.
    ///
    /// # Arguments
    ///
    /// * `username` - The name of the user to retrieve
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.users.get(username).await;
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn get(&self, username: impl Into<String>) -> Result<Option<User>> {
        let uname = username.into();
        let mut error_buffer = Vec::with_capacity(self.server_connections.len());
        for (server_id, server_connection) in self.server_connections.iter() {
            match server_connection.get_user(uname.clone()).await {
                Ok(res) => return Ok(res),
                Err(err) => error_buffer.push(format!("- {}: {}", server_id, err)),
            }
        }
        Err(ConnectionError::ServerConnectionFailedWithError { error: error_buffer.join("\n") })?
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
        let mut error_buffer = Vec::with_capacity(self.server_connections.len());
        for (server_id, server_connection) in self.server_connections.iter() {
            match server_connection.all_users().await {
                Ok(res) => return Ok(res),
                Err(err) => error_buffer.push(format!("- {}: {}", server_id, err)),
            }
        }
        Err(ConnectionError::ServerConnectionFailedWithError { error: error_buffer.join("\n") })?
    }

    /// Create a user with the given name &amp; password.
    ///
    /// # Arguments
    ///
    /// * `username` - The name of the user to be created
    /// * `password` - The password of the user to be created
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.users.create(username, password).await;
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn create(&self, username: impl Into<String>, password: impl Into<String>) -> Result {
        let uname = username.into();
        let passwd = password.into();
        let mut error_buffer = Vec::with_capacity(self.server_connections.len());
        for (server_id, server_connection) in self.server_connections.iter() {
            match server_connection.create_user(uname.clone(), passwd.clone()).await {
                Ok(res) => return Ok(res),
                Err(err) => error_buffer.push(format!("- {}: {}", server_id, err)),
            }
        }
        Err(ConnectionError::ServerConnectionFailedWithError { error: error_buffer.join("\n") })?
    }

    /// Deletes a user with the given name.
    ///
    /// # Arguments
    ///
    /// * `username` - The name of the user to be deleted
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.users.delete(username).await;
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn delete(&self, username: impl Into<String>) -> Result {
        let uname = username.into();
        let mut error_buffer = Vec::with_capacity(self.server_connections.len());
        for (server_id, server_connection) in self.server_connections.iter() {
            match server_connection.delete_user(uname.clone()).await {
                Ok(res) => return Ok(res),
                Err(err) => error_buffer.push(format!("- {}: {}", server_id, err)),
            }
        }
        Err(ConnectionError::ServerConnectionFailedWithError { error: error_buffer.join("\n") })?
    }
}
