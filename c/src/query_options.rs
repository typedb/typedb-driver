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

use typedb_driver::QueryOptions;

use super::memory::{borrow, borrow_mut, free, release};

/// Produces a new <code>QueryOptions</code> object.
#[no_mangle]
pub extern "C" fn query_options_new() -> *mut QueryOptions {
    release(QueryOptions::new())
}

/// Frees the native Rust <code>QueryOptions</code> object.
#[no_mangle]
pub extern "C" fn query_options_drop(options: *mut QueryOptions) {
    free(options);
}

/// Explicitly set the "include instance types" flag.
/// If set, specifies if types should be included in instance structs returned in ConceptRow answers.
/// This option allows reducing the amount of unnecessary data transmitted.
#[no_mangle]
pub extern "C" fn query_options_set_include_instance_types(options: *mut QueryOptions, include_instance_types: bool) {
    borrow_mut(options).include_instance_types = Some(include_instance_types);
}

/// Returns the value set for the "include instance types" flag in this <code>QueryOptions</code> object.
/// If set, specifies if types should be included in instance structs returned in ConceptRow answers.
/// This option allows reducing the amount of unnecessary data transmitted.
#[no_mangle]
pub extern "C" fn query_options_get_include_instance_types(options: *const QueryOptions) -> bool {
    borrow(options).include_instance_types.unwrap()
}

/// Checks whether the "include instance types" flag was explicitly set for this <code>QueryOptions</code> object.
#[no_mangle]
pub extern "C" fn query_options_has_include_instance_types(options: *const QueryOptions) -> bool {
    borrow(options).include_instance_types.is_some()
}
