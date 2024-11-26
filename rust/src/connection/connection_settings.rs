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

use std::{fs, path::Path};

use tonic::transport::{Certificate, ClientTlsConfig};

/// User connection settings for connecting to TypeDB.
#[derive(Debug)]
pub struct ConnectionSettings {
    is_tls_enabled: bool,
    tls_config: Option<ClientTlsConfig>,
}

impl ConnectionSettings {
    /// Creates a credential with username and password. Specifies the connection must use TLS
    ///
    /// # Arguments
    ///
    /// * `tls_root_ca` — Path to the CA certificate to use for authenticating server certificates.
    ///
    /// # Examples
    ///
    /// ```rust
    /// ConnectionSettings::new(true, Some(&path_to_ca));
    ///```
    pub fn new(is_tls_enabled: bool, tls_root_ca: Option<&Path>) -> crate::Result<Self> {
        let tls_config = Some(if let Some(tls_root_ca) = tls_root_ca {
            ClientTlsConfig::new().ca_certificate(Certificate::from_pem(fs::read_to_string(tls_root_ca)?))
        } else {
            ClientTlsConfig::new()
        });

        Ok(Self { is_tls_enabled, tls_config })
    }

    /// Retrieves whether TLS is enabled for the connection.
    pub fn is_tls_enabled(&self) -> bool {
        self.is_tls_enabled
    }

    pub fn tls_config(&self) -> &Option<ClientTlsConfig> {
        &self.tls_config
    }
}
