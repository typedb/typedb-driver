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

use std::{ffi::c_char, ptr::addr_of_mut};

use typedb_driver::{box_stream, TypeDBDriver, User};

use crate::{
    common::{
        error::{try_release, try_release_optional, unwrap_or_default, unwrap_void},
        iterator::{iterator_next, CIterator},
        memory::{borrow, free, string_view},
    },
    server::consistency_level::{native_consistency_level, ConsistencyLevel},
};

/// Iterator over a set of <code>User</code>s.
pub struct UserIterator(CIterator<User>);

/// Forwards the <code>UserIterator</code> and returns the next <code>User</code> if it exists,
/// or null if there are no more elements.
#[no_mangle]
pub extern "C" fn user_iterator_next(it: *mut UserIterator) -> *mut User {
    unsafe { iterator_next(addr_of_mut!((*it).0)) }
}

/// Frees the native rust <code>UserIterator</code> object.
#[no_mangle]
pub extern "C" fn user_iterator_drop(it: *mut UserIterator) {
    free(it);
}

/// Retrieves all users which exist on the TypeDB server.
///
/// @param driver The <code>TypeDBDriver</code> object.
/// @param consistency_level The consistency level to use for the operation. Strongest possible if null.
#[no_mangle]
pub extern "C" fn users_all(
    driver: *const TypeDBDriver,
    consistency_level: *const ConsistencyLevel,
) -> *mut UserIterator {
    let users = borrow(driver).users();
    let result = match native_consistency_level(consistency_level) {
        Some(consistency_level) => users.all_with_consistency(consistency_level),
        None => users.all(),
    };
    try_release(result.map(|users| UserIterator(CIterator(box_stream(users.into_iter())))))
}

/// Checks if a user with the given name exists.
///
/// @param driver The <code>TypeDBDriver</code> object.
/// @param username The username of the user.
/// @param consistency_level The consistency level to use for the operation. Strongest possible if null.
#[no_mangle]
pub extern "C" fn users_contains(
    driver: *const TypeDBDriver,
    username: *const c_char,
    consistency_level: *const ConsistencyLevel,
) -> bool {
    let users = borrow(driver).users();
    let username = string_view(username);
    unwrap_or_default(match native_consistency_level(consistency_level) {
        Some(consistency_level) => users.contains_with_consistency(username, consistency_level),
        None => users.contains(username),
    })
}

/// Retrieves a user with the given name.
///
/// @param driver The <code>TypeDBDriver</code> object.
/// @param username The username of the user.
/// @param consistency_level The consistency level to use for the operation. Strongest possible if null.
#[no_mangle]
pub extern "C" fn users_get(
    driver: *const TypeDBDriver,
    username: *const c_char,
    consistency_level: *const ConsistencyLevel,
) -> *mut User {
    let users = borrow(driver).users();
    let username = string_view(username);
    let result = match native_consistency_level(consistency_level) {
        Some(consistency_level) => users.get_with_consistency(username, consistency_level),
        None => users.get(username),
    };
    try_release_optional(result.transpose())
}

/// Retrieves the username of the user who opened this connection.
///
/// @param driver The <code>TypeDBDriver</code> object.
/// @param consistency_level The consistency level to use for the operation. Strongest possible if null.
#[no_mangle]
pub extern "C" fn users_get_current(
    driver: *const TypeDBDriver,
    consistency_level: *const ConsistencyLevel,
) -> *mut User {
    let users = borrow(driver).users();
    let result = match native_consistency_level(consistency_level) {
        Some(consistency_level) => users.get_current_with_consistency(consistency_level),
        None => users.get_current(),
    };
    try_release_optional(result.transpose())
}

/// Creates a user with the given name &amp; password.
///
/// @param username The username of the created user.
/// @param password The password of the created user.
/// @param consistency_level The consistency level to use for the operation. Strongest possible if null.
#[no_mangle]
pub extern "C" fn users_create(
    driver: *const TypeDBDriver,
    username: *const c_char,
    password: *const c_char,
    consistency_level: *const ConsistencyLevel,
) {
    let users = borrow(driver).users();
    let username = string_view(username);
    let password = string_view(password);
    let result = match native_consistency_level(consistency_level) {
        Some(consistency_level) => users.create_with_consistency(username, password, consistency_level),
        None => users.create(username, password),
    };
    unwrap_void(result);
}
