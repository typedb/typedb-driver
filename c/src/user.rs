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

// /// Frees the native rust <code>User</code> object.
// #[no_mangle]
// pub extern "C" fn user_drop(user: *mut User) {
//     free(user);
// }
//
// /// Returns the name of this user.
// #[no_mangle]
// pub extern "C" fn user_get_username(user: *mut User) -> *mut c_char {
//     release_string(borrow(user).username.clone())
// }
//
// /// Returns the number of seconds remaining till this user’s current password expires.
// #[no_mangle]
// pub extern "C" fn user_get_password_expiry_seconds(user: *mut User) -> i64 {
//     borrow(user).password_expiry_seconds.unwrap_or(-1)
// }
//
// /// Updates the password for the current authenticated user.
// ///
// /// @param user The user to update the password of - must be the current user.
// /// @param user_manager The <code>UserManager</code> object on this connection.
// /// @param password_old The current password of this user
// /// @param password_new The new password
// #[no_mangle]
// pub extern "C" fn user_password_update(
//     user: *mut User,
//     user_manager: *const UserManager,
//     password_old: *const c_char,
//     password_new: *const c_char,
// ) {
//     unwrap_void(borrow(user).password_update(
//         &borrow(user_manager).connection,
//         string_view(password_old),
//         string_view(password_new),
//     ));
// }
