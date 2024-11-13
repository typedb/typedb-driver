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

#[cfg(not(feature = "sync"))]
use std::future::Future;

use crate::{common::Result, connection::server_connection::ServerConnection, driver::TypeDBDriver, User};

/// Provides access to all user management methods.
#[derive(Debug)]
pub struct UserManager {
    // FIXME: currently required to be `pub` because we refer to it in bindings and over FFI
    #[doc(hidden)]
    pub connection: TypeDBDriver,
}

impl UserManager {
    const SYSTEM_DB: &'static str = "_system";

    pub fn new(connection: TypeDBDriver) -> Self {
        Self { connection }
    }

    /// Returns the logged-in user for the connection.
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.users.current_user().await;
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn current_user(&self) -> Result<Option<User>> {
        match self.connection.username() {
            Some(username) => self.get(username).await,
            None => Ok(None), // FIXME error
        }
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
        self.run_any_node(|server_connection: ServerConnection| async move { server_connection.all_users().await })
            .await
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
        let username = username.into();
        self.run_any_node(|server_connection: ServerConnection| {
            let username = username.clone();
            async move { server_connection.contains_user(username).await }
        })
        .await
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
        let username = username.into();
        let password = password.into();
        self.run_any_node(|server_connection: ServerConnection| {
            let username = username.clone();
            let password = password.clone();
            async move { server_connection.create_user(username, password).await }
        })
        .await
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
        let username = username.into();
        self.run_any_node(|server_connection: ServerConnection| {
            let username = username.clone();
            async move { server_connection.delete_user(username).await }
        })
        .await
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
        let username = username.into();
        self.run_any_node(|server_connection: ServerConnection| {
            let username = username.clone();
            async move { server_connection.get_user(username).await }
        })
        .await
    }

    /// Sets a new password for a user. This operation can only be performed by administrators.
    ///
    /// # Arguments
    ///
    /// * `username` -- The name of the user to set the password of
    /// * `password` -- The new password
    ///
    /// # Examples
    ///
    /// ```rust
    /// driver.users.password_set(username, password).await;
    /// ```
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
        // if !self.connection.is_cloud() {
        //     Err(Error::Connection(ConnectionError::UserManagementCloudOnly))
        // } else {
        //     DatabaseManager::new(self.connection.clone())
        //         .get(Self::SYSTEM_DB)
        //         .await?
        //         .run_failsafe(|database| task(database.connection().clone()))
        //         .await
        // }
        todo!()
    }
}
