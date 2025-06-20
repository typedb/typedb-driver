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

/// TypeDB driver connection options.
/// `DriverOptions` object can be used to override the default driver behavior while connecting to
/// TypeDB.
///
/// # Examples
///
/// ```rust
/// let options = DriverOptions::new().is_tls_enabled(true).tls_root_ca(Some(&path_to_ca)).unwrap();
/// ```
#[derive(Debug, Clone)]
pub struct DriverOptions {
    /// Specifies whether the connection to TypeDB must be done over TLS.
    /// Defaults to false.
    pub is_tls_enabled: bool,
    /// If set, specifies the TLS config to use for server certificates authentication.
    /// Defaults to None.
    pub tls_config: Option<ClientTlsConfig>,
    /// Specifies whether the connection to TypeDB can use cluster replicas provided by the server
    /// or it should be limited to the provided address.
    /// Defaults to true. If set to false, restricts the driver to only a single address.
    pub use_replication: bool,
}

impl DriverOptions {
    pub fn new() -> Self {
        Self::default()
    }

    /// Specifies whether the connection to TypeDB must be done over TLS.
    pub fn is_tls_enabled(self, is_tls_enabled: bool) -> Self {
        Self { is_tls_enabled, ..self }
    }

    /// If set, specifies the path to the CA certificate to use for server certificates authentication.
    pub fn tls_root_ca(self, tls_root_ca: Option<&Path>) -> crate::Result<Self> {
        let tls_config = Some(if let Some(tls_root_ca) = tls_root_ca {
            ClientTlsConfig::new().ca_certificate(Certificate::from_pem(fs::read_to_string(tls_root_ca)?))
        } else {
            ClientTlsConfig::new().with_native_roots()
        });
        Ok(Self { tls_config, ..self })
    }

    /// Specifies whether the connection to TypeDB can use cluster replicas provided by the server
    /// or it should be limited to the provided address.
    /// If set to false, restricts the driver to only a single address.
    pub fn use_replication(self, use_replication: bool) -> Self {
        Self { use_replication, ..self }
    }
}

impl Default for DriverOptions {
    fn default() -> Self {
        Self { is_tls_enabled: false, tls_config: None, use_replication: true }
    }
}
