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

pub(crate) mod address;
pub mod error;
mod id;
pub mod info;
mod options;
#[cfg_attr(not(feature = "sync"), path = "promise_async.rs")]
#[cfg_attr(feature = "sync", path = "promise_sync.rs")]
mod promise;
#[cfg_attr(not(feature = "sync"), path = "stream_async.rs")]
#[cfg_attr(feature = "sync", path = "stream_sync.rs")]
pub mod stream;

pub use self::{
    error::Error,
    options::Options,
    promise::{box_promise, BoxPromise, Promise},
    stream::{box_stream, BoxStream},
};

pub(crate) type Callback = Box<dyn FnOnce() + Send>;

pub(crate) type StdResult<T, E> = std::result::Result<T, E>;
pub type Result<T = ()> = StdResult<T, Error>;

pub type IID = id::ID;
pub(crate) type RequestID = id::ID;
pub(crate) type SessionID = id::ID;

/// This enum is used to specify the type of the session.
///
/// # Examples
///
/// ```rust
/// Session::new(database, SessionType::Schema).await
/// ```
#[repr(C)]
#[derive(Copy, Clone, Debug, Eq, PartialEq)]
pub enum SessionType {
    Data = 0,
    Schema = 1,
}

/// This enum is used to specify the type of transaction.
///
/// # Examples
///
/// ```rust
/// session.transaction(TransactionType::READ)
/// ```
#[repr(C)]
#[derive(Copy, Clone, Debug, Eq, PartialEq)]
pub enum TransactionType {
    Read = 0,
    Write = 1,
    Schema = 2,
}
