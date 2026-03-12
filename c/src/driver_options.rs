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

use typedb_driver::{DriverOptions, DriverTlsConfig};

use crate::common::memory::{borrow, borrow_mut, free, release};

/// Creates a new <code>DriverOptions</code> for connecting to TypeDB Server using custom TLS settings.
/// WARNING: Disabled TLS settings will make the driver sending passwords as plaintext.
#[no_mangle]
pub extern "C" fn driver_options_new(tls_config: *const DriverTlsConfig) -> *mut DriverOptions {
    release(DriverOptions::new(borrow(tls_config).clone()))
}

/// Frees the native rust <code>DriverOptions</code> object.
#[no_mangle]
pub extern "C" fn driver_options_drop(driver_options: *mut DriverOptions) {
    free(driver_options);
}

/// Overrides the TLS configuration on <code>DriverOptions</code>.
/// WARNING: Disabled TLS settings will make the driver sending passwords as plaintext.
#[no_mangle]
pub extern "C" fn driver_options_set_tls_config(options: *mut DriverOptions, tls_config: *const DriverTlsConfig) {
    borrow_mut(options).tls_config = borrow(tls_config).clone();
}

/// Returns the TLS Config set for this <code>DriverOptions</code> object.
/// Specifies the TLS configuration of the connection to TypeDB.
#[no_mangle]
pub extern "C" fn driver_options_get_tls_config(options: *const DriverOptions) -> *mut DriverTlsConfig {
    release(borrow(options).tls_config.clone())
}

/// Limits the number of attempts to redirect a request to another
/// primary replica in case of a failure due to the change of replica roles.
/// Defaults to 1.
#[no_mangle]
pub extern "C" fn driver_options_set_primary_failover_retries(
    options: *mut DriverOptions,
    primary_failover_retries: i64,
) {
    borrow_mut(options).primary_failover_retries = primary_failover_retries as usize;
}

/// Returns the value set for the primary failover retries limit in this <code>DriverOptions</code> object.
/// Limits the number of attempts to redirect a request to another
/// primary replica in case of a failure due to the change of replica roles.
#[no_mangle]
pub extern "C" fn driver_options_get_primary_failover_retries(options: *const DriverOptions) -> i64 {
    borrow(options).primary_failover_retries as i64
}

/// Sets the maximum time (in milliseconds) to wait for a response to a unary RPC request.
/// This applies to operations like database creation, user management, and initial
/// transaction opening. It does NOT apply to operations within transactions (queries, commits).
/// Set to 0 to disable the timeout (not recommended for production use).
/// Defaults to 2 hours (7200000 milliseconds).
#[no_mangle]
pub extern "C" fn driver_options_set_request_timeout_millis(options: *mut DriverOptions, timeout_millis: i64) {
    borrow_mut(options).request_timeout = Duration::from_millis(timeout_millis as u64);
}

/// Returns the request timeout in milliseconds set for this <code>DriverOptions</code> object.
/// Specifies the maximum time to wait for a response to a unary RPC request.
/// This applies to operations like database creation, user management, and initial
/// transaction opening. It does NOT apply to operations within transactions (queries, commits).
#[no_mangle]
pub extern "C" fn driver_options_get_request_timeout_millis(options: *const DriverOptions) -> i64 {
    borrow(options).request_timeout.as_millis() as i64
}
