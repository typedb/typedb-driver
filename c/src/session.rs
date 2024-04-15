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

use std::ffi::{c_char, c_void};

use typedb_driver::{DatabaseManager, Options, Session, SessionType};

use super::{
    error::{try_release, unwrap_void},
    memory::{borrow, borrow_mut, free, release_string, string_view},
};

/// Opens a session to the given database.
///
/// @param databases The <code>DatabaseManager</code> object on this connection.
/// @param database_name The name of the database with which the session connects
/// @param session_type The type of session to be created (Schema or Data)
/// @param options <code>Options</code> for this session
#[no_mangle]
pub extern "C" fn session_new(
    databases: *mut DatabaseManager,
    database_name: *const c_char,
    session_type: SessionType,
    options: *const Options,
) -> *mut Session {
    try_release(
        borrow(databases)
            .get(string_view(database_name))
            .and_then(|db| Session::new_with_options(db, session_type, *borrow(options))),
    )
}

/// Closes the session. Before opening a new session, the session currently open should first be closed.
/// The native rust object is freed on close.
#[no_mangle]
pub extern "C" fn session_close(session: *mut Session) {
    free(session);
}

/// Returns the name of the database of the session.
#[no_mangle]
pub extern "C" fn session_get_database_name(session: *const Session) -> *mut c_char {
    release_string(borrow(session).database_name().to_owned())
}

/// Checks whether this session is open.
#[no_mangle]
pub extern "C" fn session_is_open(session: *const Session) -> bool {
    borrow(session).is_open()
}

/// Forcibly closes the session. To be used in exceptional cases.
#[no_mangle]
pub extern "C" fn session_force_close(session: *mut Session) {
    unwrap_void(borrow_mut(session).force_close())
}

mod private {
    use std::{ffi::c_void, mem::ManuallyDrop};

    struct SendPtr(*mut c_void);
    unsafe impl Send for SendPtr {}

    pub(super) struct ForeignCallback<Callback, Finished: FnOnce(*mut c_void)> {
        data: SendPtr,
        callback: Callback,
        finished: ManuallyDrop<Finished>,
    }

    impl<Callback, Finished: FnOnce(*mut c_void)> ForeignCallback<Callback, Finished> {
        pub(super) fn new(data: *mut c_void, callback: Callback, finished: Finished) -> Self {
            Self { data: SendPtr(data), callback, finished: ManuallyDrop::new(finished) }
        }
    }

    impl<Callback: FnMut(*mut c_void), Finished: FnOnce(*mut c_void)> ForeignCallback<Callback, Finished> {
        pub(super) fn call(&mut self) {
            (self.callback)(self.data.0)
        }
    }

    impl<Callback, Finished: FnOnce(*mut c_void)> Drop for ForeignCallback<Callback, Finished> {
        fn drop(&mut self) {
            // SAFETY: `finished` is inaccessible outside of `new()`, where it is initialized, and
            // `drop()`, where it is consumed.
            unsafe { (ManuallyDrop::take(&mut self.finished))(self.data.0) }
        }
    }
}

/// Registers a callback function which will be executed when this session is closed.
///
/// @param session The session on which to register the callback
/// @param data The argument to be passed to the callback function when it is executed
/// @param callback The function to be called
/// @param finished A function which will be executed when the session is destroyed, allowing cleanup
#[no_mangle]
pub extern "C" fn session_on_close(
    session: *const Session,
    data: *mut c_void,
    callback: extern "C" fn(*mut c_void),
    finished: extern "C" fn(*mut c_void),
) {
    #[allow(clippy::redundant_closure)]
    let mut callback = private::ForeignCallback::new(data, move |data| callback(data), move |data| finished(data));
    borrow(session).on_close(move || callback.call())
}

/// Registers a callback function which will be executed when this session is reopened.
/// A session may be closed if it times out, or loses the connection to the database.
/// In such situations, the session is reopened automatically when opening a new transaction.
///
/// @param session The session on which to register the callback
/// @param data The argument to be passed to the callback function when it is executed
/// @param callback The function to be called
/// @param finished A function which will be executed when the session is destroyed, allowing cleanup
#[no_mangle]
pub extern "C" fn session_on_reopen(
    session: *const Session,
    data: *mut c_void,
    callback: extern "C" fn(*mut c_void),
    finished: extern "C" fn(*mut c_void),
) {
    #[allow(clippy::redundant_closure)]
    let mut callback = private::ForeignCallback::new(data, move |data| callback(data), move |data| finished(data));
    borrow(session).on_reopen(move || callback.call())
}
