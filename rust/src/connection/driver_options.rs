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
    time::Duration,
};

use crate::connection::driver_tls_config::DriverTlsConfig;

// When changing these numbers, also update docs in DriverOptions
const DEFAULT_REQUEST_TIMEOUT: Duration = Duration::from_secs(2 * 60 * 60); // 2 hours
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
/// let options = DriverOptions::new(DriverTlsConfig::default()).use_replication(false);
/// ```
#[derive(Debug, Clone)]
pub struct DriverOptions {
    /// Specifies the TLS configuration of the connection to TypeDB.
    /// WARNING: Disabled TLS settings will make the driver sending passwords as plaintext.
    /// Defaults to an enabled TLS configuration based on the system's native trust roots.
    pub tls_config: DriverTlsConfig,
    /// Specifies the maximum time to wait for a response to a unary RPC request.
    /// This applies to operations like database creation, user management, and initial
    /// transaction opening. It does NOT apply to operations within transactions (queries, commits).
    /// Defaults to 2 hours.
    pub request_timeout: Duration,
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
}

impl DriverOptions {
    /// Creates new `DriverOptions` to configure connections to TypeDB using custom TLS settings.
    /// WARNING: Disabled TLS settings will make the driver sending passwords as plaintext.
    pub fn new(tls_config: DriverTlsConfig) -> Self {
        Self { tls_config, ..Default::default() }
    }

    /// Override the existing TLS configuration.
    /// WARNING: Disabled TLS settings will make the driver sending passwords as plaintext.
    pub fn tls_config(mut self, tls_config: DriverTlsConfig) -> Self {
        Self { tls_config, ..self }
    }

    /// Specifies the maximum time to wait for a response to a unary RPC request.
    /// This applies to operations like database creation, user management, and initial
    /// transaction opening. It does NOT apply to operations within transactions (queries, commits).
    /// Defaults to 2 hours.
    pub fn request_timeout(self, request_timeout: Duration) -> Self {
        Self { request_timeout, ..self }
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
            tls_config: DriverTlsConfig::default(),
            request_timeout: DEFAULT_REQUEST_TIMEOUT,
            use_replication: DEFAULT_USE_REPLICATION,
            primary_failover_retries: DEFAULT_REDIRECT_FAILOVER_RETRIES,
            replica_discovery_attempts: DEFAULT_DISCOVERY_FAILOVER_RETRIES,
        }
    }
}
