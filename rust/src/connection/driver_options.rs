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
};

use tonic::transport::{Certificate, ClientTlsConfig};

// When changing these numbers, also update docs in DriverOptions
const DEFAULT_TLS_ENABLED: bool = true;
const DEFAULT_TLS_CONFIG: Option<ClientTlsConfig> = None;
const DEFAULT_TLS_ROOT_CA_PATH: Option<PathBuf> = None;
const DEFAULT_USE_REPLICATION: bool = true;
const DEFAULT_REDIRECT_FAILOVER_RETRIES: usize = 1;
const DEFAULT_DISCOVERY_FAILOVER_RETRIES: Option<usize> = None;

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
    /// WARNING: Setting this to false will make the driver sending passwords as plaintext.
    /// Defaults to true.
    pub is_tls_enabled: bool,
    /// Specifies whether the connection to TypeDB can use cluster replicas provided by the server
    /// or it should be limited to a single configured address.
    /// Defaults to true.
    pub use_replication: bool,
    /// Limits the number of attempts to redirect a strongly consistent request to another
    /// primary replica in case of a failure due to the change of replica roles.
    /// Defaults to 1.
    pub primary_failover_retries: usize,
    /// Limits the number of driver attempts to discover a single working replica to perform an
    /// operation in case of a replica unavailability. Every replica is tested once, which means
    /// that at most:
    /// - {limit} operations are performed if the limit <= the number of replicas.
    /// - {number of replicas} operations are performed if the limit > the number of replicas.
    /// - {number of replicas} operations are performed if the limit is None.
    /// Affects every eventually consistent operation, including redirect failover, when the new
    /// primary replica is unknown.
    /// Defaults to None.
    pub replica_discovery_attempts: Option<usize>,

    tls_config: Option<ClientTlsConfig>,
    tls_root_ca: Option<PathBuf>,
}

impl DriverOptions {
    pub fn new() -> Self {
        Self::default()
    }

    /// Specifies whether the connection to TypeDB must be done over TLS.
    pub fn is_tls_enabled(self, is_tls_enabled: bool) -> Self {
        Self { is_tls_enabled, ..self }
    }

    /// Specifies the root CA used in the TLS config for server certificates authentication.
    /// Uses system roots if None is set. See [`Self::is_tls_enabled`] to enable or disable TLS.
    pub fn tls_root_ca(mut self, tls_root_ca: Option<&Path>) -> crate::Result<Self> {
        self.set_tls_root_ca(tls_root_ca)?;
        Ok(self)
    }

    /// Specifies the root CA used in the TLS config for server certificates authentication.
    /// Uses system roots if None is set. See [`Self::is_tls_enabled`] to enable or disable TLS.
    pub fn set_tls_root_ca(&mut self, tls_root_ca: Option<&Path>) -> crate::Result {
        let tls_config = if let Some(tls_root_ca) = tls_root_ca {
            self.tls_root_ca = Some(tls_root_ca.to_path_buf());
            ClientTlsConfig::new().ca_certificate(Certificate::from_pem(fs::read_to_string(tls_root_ca)?))
        } else {
            self.tls_root_ca = None;
            ClientTlsConfig::new().with_native_roots()
        };
        self.tls_config = Some(tls_config);
        Ok(())
    }

    /// Retrieves the TLS config of this options object if configured.
    pub fn get_tls_config(&self) -> Option<&ClientTlsConfig> {
        self.tls_config.as_ref()
    }

    /// Retrieves the TLS root CA path of this options object if configured.
    pub fn get_tls_root_ca(&self) -> Option<&Path> {
        self.tls_root_ca.as_deref()
    }

    /// Specifies whether the connection to TypeDB can use cluster replicas provided by the server
    /// or it should be limited to the provided address.
    /// If set to false, restricts the driver to only a single address.
    pub fn use_replication(self, use_replication: bool) -> Self {
        Self { use_replication, ..self }
    }

    /// Limits the number of attempts to redirect a strongly consistent request to another
    /// primary replica in case of a failure due to the change of replica roles.
    /// Defaults to 1.
    pub fn primary_failover_retries(self, primary_failover_retries: usize) -> Self {
        Self { primary_failover_retries, ..self }
    }

    /// Limits the number of driver attempts to discover a single working replica to perform an
    /// operation in case of a replica unavailability. Every replica is tested once, which means
    /// that at most:
    /// - {limit} operations are performed if the limit <= the number of replicas.
    /// - {number of replicas} operations are performed if the limit > the number of replicas.
    /// - {number of replicas} operations are performed if the limit is None.
    /// Affects every eventually consistent operation, including redirect failover, when the new
    /// primary replica is unknown.
    /// Defaults to None.
    pub fn replica_discovery_attempts(self, replica_discovery_attempts: Option<usize>) -> Self {
        Self { replica_discovery_attempts, ..self }
    }
}

impl Default for DriverOptions {
    fn default() -> Self {
        Self {
            is_tls_enabled: DEFAULT_TLS_ENABLED,
            use_replication: DEFAULT_USE_REPLICATION,
            primary_failover_retries: DEFAULT_REDIRECT_FAILOVER_RETRIES,
            replica_discovery_attempts: DEFAULT_DISCOVERY_FAILOVER_RETRIES,

            tls_config: DEFAULT_TLS_CONFIG,
            tls_root_ca: DEFAULT_TLS_ROOT_CA_PATH,
        }
    }
}
