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

use std::ffi::c_char;

use typedb_driver::User;

use crate::{
    common::{
        error::unwrap_void,
        memory::{borrow, free, release_string, string_view, take_ownership},
    },
    server::consistency_level::{native_consistency_level, ConsistencyLevel},
};

/// Frees the native rust <code>User</code> object.
#[no_mangle]
pub extern "C" fn user_drop(user: *mut User) {
    free(user);
}

/// Returns the name of this user.
#[no_mangle]
pub extern "C" fn user_get_name(user: *mut User) -> *mut c_char {
    release_string(borrow(user).name().to_string())
}

/// Updates the password for the current authenticated user.
///
/// @param user The user to update the password of - must be the current user.
/// @param user_manager The <code>UserManager</code> object on this connection.
/// @param password_old The current password of this user.
/// @param password_new The new password.
/// @param consistency_level The consistency level to use for the operation. Strongest possible if null.
#[no_mangle]
pub extern "C" fn user_update_password(
    user: *mut User,
    password: *const c_char,
    consistency_level: *const ConsistencyLevel,
) {
    let user = borrow(user);
    let password = string_view(password);
    let result = match native_consistency_level(consistency_level) {
        Some(consistency_level) => user.update_password_with_consistency(password, consistency_level),
        None => user.update_password(password),
    };
    unwrap_void(result);
}

/// Deletes this user.
///
/// @param user The <code>User</code> to delete.
/// @param consistency_level The consistency level to use for the operation. Strongest possible if null.
#[no_mangle]
pub extern "C" fn user_delete(user: *mut User, consistency_level: *const ConsistencyLevel) {
    let user = take_ownership(user);
    let result = match native_consistency_level(consistency_level) {
        Some(consistency_level) => user.delete_with_consistency(consistency_level),
        None => user.delete(),
    };
    unwrap_void(result);
}
