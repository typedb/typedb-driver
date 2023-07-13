/*
 * Copyright (C) 2022 Vaticle
 *
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

use super::{
    error::{try_release, try_release_optional, unwrap_or_default, unwrap_void},
    iterator::{iterator_next, CIterator},
    memory::{borrow, free, release, string_view},
};
use crate::{common::box_stream, Connection, User, UserManager};

#[no_mangle]
pub extern "C" fn user_manager_new(connection: *mut Connection) -> *mut UserManager {
    release(UserManager::new(borrow(connection).clone()))
}

#[no_mangle]
pub extern "C" fn user_manager_drop(user_manager: *mut UserManager) {
    free(user_manager);
}

#[no_mangle]
pub extern "C" fn users_current_user(user_manager: *const UserManager) -> *mut User {
    try_release_optional(borrow(user_manager).current_user().transpose())
}

pub struct UserIterator(CIterator<User>);

#[no_mangle]
pub extern "C" fn user_iterator_next(it: *mut UserIterator) -> *mut User {
    unsafe { iterator_next(addr_of_mut!((*it).0)) }
}

#[no_mangle]
pub extern "C" fn user_iterator_drop(it: *mut UserIterator) {
    free(it);
}

#[no_mangle]
pub extern "C" fn users_all(user_manager: *const UserManager) -> *mut UserIterator {
    try_release(borrow(user_manager).all().map(|users| UserIterator(CIterator(box_stream(users.into_iter())))))
}

#[no_mangle]
pub extern "C" fn users_contains(user_manager: *const UserManager, username: *const c_char) -> bool {
    unwrap_or_default(borrow(user_manager).contains(string_view(username)))
}

#[no_mangle]
pub extern "C" fn users_create(user_manager: *const UserManager, username: *const c_char, password: *const c_char) {
    unwrap_void(borrow(user_manager).create(string_view(username), string_view(password)));
}

#[no_mangle]
pub extern "C" fn users_delete(user_manager: *const UserManager, username: *const c_char) {
    unwrap_void(borrow(user_manager).delete(string_view(username)));
}

#[no_mangle]
pub extern "C" fn users_get(user_manager: *const UserManager, username: *const c_char) -> *mut User {
    try_release_optional(borrow(user_manager).get(string_view(username)).transpose())
}

#[no_mangle]
pub extern "C" fn users_set_password(
    user_manager: *const UserManager,
    username: *const c_char,
    password: *const c_char,
) {
    unwrap_void(borrow(user_manager).set_password(string_view(username), string_view(password)));
}
