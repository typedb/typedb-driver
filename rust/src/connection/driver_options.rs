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

use std::time::Duration;

use crate::connection::driver_tls_config::DriverTlsConfig;

// When changing these numbers, also update docs in DriverOptions
const DEFAULT_REQUEST_TIMEOUT: Duration = Duration::from_secs(2 * 60 * 60); // 2 hours
const DEFAULT_REDIRECT_FAILOVER_RETRIES: usize = 1;

/// TypeDB driver connection options.
/// `DriverOptions` object can be used to override the default driver behavior while connecting to
/// TypeDB.
///
/// # Examples
///
/// ```rust
/// let options = DriverOptions::new(DriverTlsConfig::default()).request_timeout(Duration::from_secs(30));
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
    // TODO: What if the server does not respond on queries/commits?
    // Shall we apply the same or a different timeout?
    pub request_timeout: Duration,
    /// Sets the number of times the driver retries finding and re-routing to the primary server
    /// on connection failures. This value is used both for polling during leader election (up to
    /// N+1 attempts with a 2-second sleep between each) and for re-executing a failed request on
    /// a newly discovered primary. Defaults to 1.
    pub primary_failover_retries: usize,
}

impl DriverOptions {
    /// Creates new `DriverOptions` to configure connections to TypeDB using custom TLS settings.
    /// WARNING: Disabled TLS settings will make the driver sending passwords as plaintext.
    pub fn new(tls_config: DriverTlsConfig) -> Self {
        Self { tls_config, ..Default::default() }
    }

    /// Override the existing TLS configuration.
    /// WARNING: Disabled TLS settings will make the driver sending passwords as plaintext.
    pub fn tls_config(self, tls_config: DriverTlsConfig) -> Self {
        Self { tls_config, ..self }
    }

    /// Specifies the maximum time to wait for a response to a unary RPC request.
    /// This applies to operations like database creation, user management, and initial
    /// transaction opening. It does NOT apply to operations within transactions (queries, commits).
    /// Defaults to 2 hours.
    pub fn request_timeout(self, request_timeout: Duration) -> Self {
        Self { request_timeout, ..self }
    }

    /// Sets the number of times the driver retries finding and re-routing to the primary server
    /// on connection failures. This value is used both for polling during leader election (up to
    /// N+1 attempts with a 2-second sleep between each) and for re-executing a failed request on
    /// a newly discovered primary. Defaults to 1.
    pub fn primary_failover_retries(self, primary_failover_retries: usize) -> Self {
        Self { primary_failover_retries, ..self }
    }
}

impl Default for DriverOptions {
    fn default() -> Self {
        Self {
            tls_config: DriverTlsConfig::default(),
            request_timeout: DEFAULT_REQUEST_TIMEOUT,
            primary_failover_retries: DEFAULT_REDIRECT_FAILOVER_RETRIES,
        }
    }
}
