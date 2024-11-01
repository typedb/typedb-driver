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
#[cfg(not(feature = "sync"))]
use std::future::Future;

use crate::{
    common::Result, connection::server_connection::ServerConnection, driver::TypeDBDriver, error::ConnectionError,
    DatabaseManager, Error, User,
};
use crate::common::address::Address;

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
    /// * `username` -- The user name to be checked
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.users.contains(username).await;
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn contains(&self, username: impl Into<String>) -> Result<bool> {
        self.run_failsafe(username.into(), |server_connection, username| async move { server_connection.contains_user(username).await }).await
    }

    /// Retrieve a user with the given name.
    ///
    /// # Arguments
    ///
    /// * `username` -- The name of the user to retrieve
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.users.get(username).await;
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn get(&self, username: impl Into<String>) -> Result<Option<User>> {
        self.run_failsafe(username.into(), |server_connection, username| async move { server_connection.get_user(username).await }).await
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
        self.run_failsafe(todo!(), |server_connection, _| async move { server_connection.all_users().await }).await
    }

    /// Create a user with the given name &amp; password.
    ///
    /// # Arguments
    ///
    /// * `username` -- The name of the user to be created
    /// * `password` -- The password of the user to be created
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.users.create(username, password).await;
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn create(&self, username: impl Into<String>, password: impl Into<String>) -> Result {
        self.run_failsafe(username.into(), |server_connection, username| async move { server_connection.create_user(username, todo!()).await }).await
    }

    /// Deletes a user with the given name.
    ///
    /// # Arguments
    ///
    /// * `username` -- The name of the user to be deleted
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.users.delete(username).await;
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn delete(&self, username: impl Into<String>) -> Result {
        self.run_failsafe(username.into(), |server_connection, username| async move { server_connection.delete_user(username).await }).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn run_failsafe<F, P, R>(&self, name: String, task: F) -> Result<R>
    where
        F: Fn(ServerConnection, String) -> P,
        P: Future<Output = Result<R>>,
    {
        todo!()
    }

}
