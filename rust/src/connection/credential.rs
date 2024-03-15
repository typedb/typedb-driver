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

use std::{fmt, fs, path::Path};

use tonic::transport::{Certificate, ClientTlsConfig};

use crate::Result;

/// User credentials and TLS encryption settings for connecting to TypeDB Cloud.
#[derive(Clone)]
pub struct Credential {
    username: String,
    password: String,
    is_tls_enabled: bool,
    tls_config: Option<ClientTlsConfig>,
}

/// User credentials and TLS encryption settings for connecting to TypeDB Cloud.
impl Credential {
    /// Creates a credential with username and password. Specifies the connection must use TLS
    ///
    /// # Arguments
    ///
    /// * `username` --  The name of the user to connect as
    /// * `password` -- The password for the user
    /// * `tls_root_ca` -- Path to the CA certificate to use for authenticating server certificates.
    ///
    /// # Examples
    ///
    /// ```rust
    /// Credential::with_tls(username, password, Some(&path_to_ca));
    ///```
    pub fn with_tls(username: &str, password: &str, tls_root_ca: Option<&Path>) -> Result<Self> {
        let tls_config = Some(if let Some(tls_root_ca) = tls_root_ca {
            ClientTlsConfig::new().ca_certificate(Certificate::from_pem(fs::read_to_string(tls_root_ca)?))
        } else {
            ClientTlsConfig::new()
        });

        Ok(Self { username: username.to_owned(), password: password.to_owned(), is_tls_enabled: true, tls_config })
    }

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
    pub fn without_tls(username: &str, password: &str) -> Self {
        Self { username: username.to_owned(), password: password.to_owned(), is_tls_enabled: false, tls_config: None }
    }

    /// Retrieves the username used.
    pub fn username(&self) -> &str {
        &self.username
    }

    /// Retrieves the password used.
    pub fn password(&self) -> &str {
        &self.password
    }

    /// Retrieves whether TLS is enabled for the connection.
    pub fn is_tls_enabled(&self) -> bool {
        self.is_tls_enabled
    }

    pub fn tls_config(&self) -> &Option<ClientTlsConfig> {
        &self.tls_config
    }
}

impl fmt::Debug for Credential {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("Credential")
            .field("username", &self.username)
            .field("is_tls_enabled", &self.is_tls_enabled)
            .field("tls_config", &self.tls_config)
            .finish()
    }
}
