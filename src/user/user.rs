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

use crate::{common::Result, error::ConnectionError, Connection};

#[derive(Clone, Debug)]
pub struct User {
    pub username: String,
    pub password_expiry_seconds: Option<i64>,
}

impl User {
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn password_update(
        &self,
        connection: &Connection,
        password_old: impl Into<String>,
        password_new: impl Into<String>,
    ) -> Result {
        let password_old = password_old.into();
        let password_new = password_new.into();
        let mut error_buffer = Vec::with_capacity(connection.server_count());
        for server_connection in connection.connections() {
            match server_connection
                .update_user_password(self.username.clone(), password_old.clone(), password_new.clone())
                .await
            {
                Ok(()) => return Ok(()),
                Err(err) => error_buffer.push(format!("- {}: {}", server_connection.address(), err)),
            }
        }
        Err(ConnectionError::ClusterAllNodesFailed(error_buffer.join("\n")))?
    }
}
