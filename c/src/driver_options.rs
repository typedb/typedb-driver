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

use typedb_driver::{
    DriverOptions,
};

use crate::common::{
    error::{try_release},
    memory::{free, string_view,},
};

// TODO: Refactor

/// Creates a new <code>DriverOptions</code> for connecting to TypeDB Server.
///
/// @param tls_root_ca Path to the CA certificate to use for authenticating server certificates.
/// @param with_tls Specify whether the connection to TypeDB Cloud must be done over TLS
#[no_mangle]
pub extern "C" fn driver_options_new(is_tls_enabled: bool, tls_root_ca: *const c_char) -> *mut DriverOptions {
    let tls_root_ca_path = unsafe { tls_root_ca.as_ref().map(|str| Path::new(string_view(str))) };
    try_release(DriverOptions::new().is_tls_enabled(is_tls_enabled).tls_root_ca(tls_root_ca_path))
}

/// Frees the native rust <code>DriverOptions</code> object
#[no_mangle]
pub extern "C" fn driver_options_drop(driver_options: *mut DriverOptions) {
    free(driver_options);
}
