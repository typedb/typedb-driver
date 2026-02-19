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

use std::{
    fs,
    path::{Path, PathBuf},
    sync::Arc,
};

use tonic::transport::{Certificate, ClientTlsConfig as NetworkTlsConfig};

/// TLS configuration for the TypeDB driver.
///
/// `DriverTlsConfig` represents a fully constructed and validated TLS configuration.
/// If TLS is enabled, the underlying TLS config is built eagerly at construction time,
/// ensuring that no connection attempt can observe a partially-configured TLS state.
///
/// The driver defaults to using TLS with **native system trust roots**.
/// This matches typical system and container deployments while still allowing
/// explicit opt-out or custom PKI configuration.
#[derive(Debug, Clone)]
pub struct DriverTlsConfig {
    network_config: Option<Arc<NetworkTlsConfig>>,
    root_ca_path: Option<PathBuf>,
}

impl DriverTlsConfig {
    /// Disable TLS entirely (NOT recommended for production).
    ///
    /// Disabling TLS causes credentials and data to be transmitted in plaintext.
    /// This should only be used in trusted, local, or test environments.
    ///
    /// # Examples
    ///
    /// ```rust
    /// let tls = DriverTlsConfig::disabled();
    /// ```
    pub fn disabled() -> Self {
        Self { network_config: None, root_ca_path: None }
    }

    /// Default TLS using system trust roots.
    ///
    /// # Examples
    ///
    /// ```rust
    /// let tls = DriverTlsConfig::enabled_with_native_root_ca();
    /// ```
    pub fn enabled_with_native_root_ca() -> Self {
        let network_config = Some(Arc::new(NetworkTlsConfig::new().with_native_roots()));
        Self { network_config, root_ca_path: None }
    }

    /// TLS with a custom root CA.
    ///
    /// # Examples
    ///
    /// ```rust
    /// let tls = DriverTlsConfig::enabled_with_root_ca("path/to/ca-certificate.pem").unwrap();
    /// ```
    pub fn enabled_with_root_ca(tls_root_ca: &Path) -> crate::Result<Self> {
        let ca_certificate = Certificate::from_pem(fs::read_to_string(tls_root_ca)?);
        let network_config = Some(Arc::new(NetworkTlsConfig::new().ca_certificate(ca_certificate)));
        Ok(Self { network_config, root_ca_path: Some(tls_root_ca.to_path_buf()) })
    }

    /// Returns whether TLS is enabled in the configuration.
    ///
    /// # Examples
    ///
    /// ```rust
    /// config.is_enabled()
    /// ```
    pub fn is_enabled(&self) -> bool {
        self.network_config.is_some()
    }

    /// Returns the network TLS config object, if present.
    ///
    /// # Examples
    ///
    /// ```rust
    /// config.network_config()
    /// ```
    pub fn network_config(&self) -> Option<&NetworkTlsConfig> {
        self.network_config.as_deref()
    }

    /// Returns the custom root CA path, if provided.
    ///
    /// # Examples
    ///
    /// ```rust
    /// config.root_ca_path()
    /// ```
    pub fn root_ca_path(&self) -> Option<&Path> {
        self.root_ca_path.as_deref()
    }
}

impl Default for DriverTlsConfig {
    fn default() -> Self {
        Self::enabled_with_native_root_ca()
    }
}
