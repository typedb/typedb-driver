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

use std::fmt;

/// User credentials and TLS encryption settings for connecting to TypeDB Cloud.
#[derive(Clone)]
pub struct Credential {
    username: String,
    password: String,
}

/// User credentials and TLS encryption settings for connecting to TypeDB Cloud.
impl Credential {
    /// Creates a credential with username and password. The connection will not use TLS
    ///
    /// # Arguments
    ///
    /// * `username` --  The name of the user to connect as
    /// * `password` -- The password for the user
    ///
    /// # Examples
    ///
    /// ```rust
    /// Credential::without_tls(username, password);
    ///```
    pub fn new(username: &str, password: &str) -> Self {
        Self { username: username.to_owned(), password: password.to_owned() }
    }

    /// Retrieves the username used.
    pub fn username(&self) -> &str {
        &self.username
    }

    /// Retrieves the password used.
    pub fn password(&self) -> &str {
        &self.password
    }
}

impl fmt::Debug for Credential {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("Credential")
            .field("username", &self.username)
            .finish()
    }
}
