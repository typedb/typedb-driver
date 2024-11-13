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

// /// Creates a <code>UserManager</code> on the specified connection
// #[no_mangle]
// pub extern "C" fn user_manager_new(connection: *const TypeDBDriver) -> *mut UserManager {
//     release(UserManager::new(borrow(connection).clone()))
// }
//
// /// Frees the native rust <code>UserManager</code> object
// #[no_mangle]
// pub extern "C" fn user_manager_drop(user_manager: *mut UserManager) {
//     free(user_manager);
// }
//
// /// Retrieves the user who opened this connection
// #[no_mangle]
// pub extern "C" fn users_current_user(user_manager: *const UserManager) -> *mut User {
//     try_release_optional(borrow(user_manager).current_user().transpose())
// }
//
// /// Iterator over a set of <code>User</code>s
// pub struct UserIterator(CIterator<User>);
//
// /// Forwards the <code>UserIterator</code> and returns the next <code>User</code> if it exists,
// /// or null if there are no more elements.
// #[no_mangle]
// pub extern "C" fn user_iterator_next(it: *mut UserIterator) -> *mut User {
//     unsafe { iterator_next(addr_of_mut!((*it).0)) }
// }
//
// /// Frees the native rust <code>UserIterator</code> object
// #[no_mangle]
// pub extern "C" fn user_iterator_drop(it: *mut UserIterator) {
//     free(it);
// }
//
// /// Retrieves all users which exist on the TypeDB server.
// #[no_mangle]
// pub extern "C" fn users_all(user_manager: *const UserManager) -> *mut UserIterator {
//     try_release(borrow(user_manager).all().map(|users| UserIterator(CIterator(box_stream(users.into_iter())))))
// }
//
// /// Checks if a user with the given name exists.
// #[no_mangle]
// pub extern "C" fn users_contains(user_manager: *const UserManager, username: *const c_char) -> bool {
//     unwrap_or_default(borrow(user_manager).contains(string_view(username)))
// }
//
// /// Creates a user with the given name &amp; password.
// #[no_mangle]
// pub extern "C" fn users_create(user_manager: *const UserManager, username: *const c_char, password: *const c_char) {
//     unwrap_void(borrow(user_manager).create(string_view(username), string_view(password)));
// }
//
// /// Deletes the user with the given username.
// #[no_mangle]
// pub extern "C" fn users_delete(user_manager: *const UserManager, username: *const c_char) {
//     unwrap_void(borrow(user_manager).delete(string_view(username)));
// }
//
// /// Retrieves a user with the given name.
// #[no_mangle]
// pub extern "C" fn users_get(user_manager: *const UserManager, username: *const c_char) -> *mut User {
//     try_release_optional(borrow(user_manager).get(string_view(username)).transpose())
// }
//
// /// Sets a new password for a user. This operation can only be performed by administrators.
// ///
// /// @param user_manager The </code>UserManager</code> object to be used.
// ///                     This must be on a connection opened by an administrator.
// /// @param username The name of the user to set the password of
// /// @param password The new password
// #[no_mangle]
// pub extern "C" fn users_set_password(
//     user_manager: *const UserManager,
//     username: *const c_char,
//     password: *const c_char,
// ) {
//     unwrap_void(borrow(user_manager).set_password(string_view(username), string_view(password)));
// }
