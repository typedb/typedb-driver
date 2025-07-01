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

use std::{ffi::c_char};

use crate::common::memory::{release_string, string_free, free};

/// <code>ServerVersion</code> is an FFI representation of a full server's version specification.
#[repr(C)]
pub struct ServerVersion {
    distribution: *mut c_char,
    version: *mut c_char,
}

impl ServerVersion {
    pub fn new(distribution: String, version: String) -> Self {
        Self { distribution: release_string(distribution), version: release_string(version) }
    }
}

impl Drop for ServerVersion {
    fn drop(&mut self) {
        string_free(self.distribution);
        string_free(self.version);
    }
}

/// Frees the native rust <code>ServerVersion</code> object
#[no_mangle]
pub extern "C" fn server_version_drop(server_version: *mut ServerVersion) {
    free(server_version);
}
