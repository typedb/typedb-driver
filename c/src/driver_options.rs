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

use std::{ffi::c_char, path::Path};

use typedb_driver::DriverOptions;

use crate::common::{
    error::unwrap_void,
    memory::{borrow, borrow_mut, borrow_optional, free, release, release_string, string_view},
};

/// Creates a new <code>DriverOptions</code> for connecting to TypeDB Server.
#[no_mangle]
pub extern "C" fn driver_options_new() -> *mut DriverOptions {
    release(DriverOptions::new())
}

/// Frees the native rust <code>DriverOptions</code> object.
#[no_mangle]
pub extern "C" fn driver_options_drop(driver_options: *mut DriverOptions) {
    free(driver_options);
}

/// Explicitly sets whether the connection to TypeDB must be done over TLS.
/// WARNING: Setting this to false will make the driver sending passwords as plaintext.
/// Defaults to true.
#[no_mangle]
pub extern "C" fn driver_options_set_tls_enabled(options: *mut DriverOptions, tls_enabled: bool) {
    borrow_mut(options).is_tls_enabled = tls_enabled;
}

/// Returns the value set for the TLS flag in this <code>DriverOptions</code> object.
/// Specifies whether the connection to TypeDB must be done over TLS.
#[no_mangle]
pub extern "C" fn driver_options_get_tls_enabled(options: *const DriverOptions) -> bool {
    borrow(options).is_tls_enabled
}

/// Specifies the root CA used in the TLS config for server certificates authentication.
/// Uses system roots if None is set.
#[no_mangle]
pub extern "C" fn driver_options_set_tls_root_ca_path(options: *mut DriverOptions, tls_root_ca: *const c_char) {
    let tls_root_ca_path = borrow_optional(tls_root_ca).map(|str| Path::new(string_view(str)));
    unwrap_void(borrow_mut(options).set_tls_root_ca(tls_root_ca_path))
}

/// Returns the TLS root CA set in this <code>DriverOptions</code> object.
/// Specifies the root CA used in the TLS config for server certificates authentication.
/// Uses system roots if None is set.
#[no_mangle]
pub extern "C" fn driver_options_get_tls_root_ca_path(options: *const DriverOptions) -> *mut c_char {
    release_string(borrow(options).get_tls_root_ca().unwrap().to_string_lossy().to_string())
}

/// Checks whether TLS root CA was explicitly set for this <code>DriverOptions</code> object.
#[no_mangle]
pub extern "C" fn driver_options_has_tls_root_ca_path(options: *const DriverOptions) -> bool {
    borrow(options).get_tls_root_ca().is_some()
}

/// Specifies whether the connection to TypeDB can use cluster replicas provided by the server
/// or it should be limited to a single configured address.
/// Defaults to true.
#[no_mangle]
pub extern "C" fn driver_options_set_use_replication(options: *mut DriverOptions, use_replication: bool) {
    borrow_mut(options).use_replication = use_replication;
}

/// Returns the value set for the replication usage flag in this <code>DriverOptions</code> object.
/// Specifies whether the connection to TypeDB can use cluster replicas provided by the server
/// or it should be limited to a single configured address.
#[no_mangle]
pub extern "C" fn driver_options_get_use_replication(options: *const DriverOptions) -> bool {
    borrow(options).use_replication
}

/// Limits the number of attempts to redirect a strongly consistent request to another
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
/// Limits the number of attempts to redirect a strongly consistent request to another
/// primary replica in case of a failure due to the change of replica roles.
#[no_mangle]
pub extern "C" fn driver_options_get_primary_failover_retries(options: *const DriverOptions) -> i64 {
    borrow(options).primary_failover_retries as i64
}

/// Limits the number of driver attempts to discover a single working replica to perform an
/// operation in case of a replica unavailability. Every replica is tested once, which means
/// that at most:
/// - {limit} operations are performed if the limit <= the number of replicas.
/// - {number of replicas} operations are performed if the limit > the number of replicas.
/// - {number of replicas} operations are performed if the limit is None.
/// Affects every eventually consistent operation, including redirect failover, when the new
/// primary replica is unknown. If not set, the maximum (practically unlimited) value is used.
#[no_mangle]
pub extern "C" fn driver_options_set_replica_discovery_attempts(
    options: *mut DriverOptions,
    replica_discovery_attempts: i64,
) {
    borrow_mut(options).replica_discovery_attempts = Some(replica_discovery_attempts as usize);
}

/// Returns the value set for the replica discovery attempts limit in this <code>DriverOptions</code> object.
/// Limits the number of driver attempts to discover a single working replica to perform an
/// operation in case of a replica unavailability. Every replica is tested once, which means
/// that at most:
/// - {limit} operations are performed if the limit <= the number of replicas.
/// - {number of replicas} operations are performed if the limit > the number of replicas.
/// - {number of replicas} operations are performed if the limit is None.
/// Affects every eventually consistent operation, including redirect failover, when the new
/// primary replica is unknown.
#[no_mangle]
pub extern "C" fn driver_options_get_replica_discovery_attempts(options: *const DriverOptions) -> i64 {
    borrow(options).replica_discovery_attempts.unwrap() as i64
}

/// Checks whether the replica discovery attempts limit was explicitly set for this <code>DriverOptions</code> object.
#[no_mangle]
pub extern "C" fn driver_options_has_replica_discovery_attempts(options: *const DriverOptions) -> bool {
    borrow(options).replica_discovery_attempts.is_some()
}
