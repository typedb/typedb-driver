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

pub(crate) mod address;
mod credential;
pub mod error;
mod id;
pub(crate) mod info;
mod options;
#[cfg_attr(not(feature = "sync"), path = "stream_async.rs")]
#[cfg_attr(feature = "sync", path = "stream_sync.rs")]
pub mod stream;

pub(crate) use self::stream::box_stream;
pub use self::{credential::Credential, error::Error, options::Options};

pub(crate) type StdResult<T, E> = std::result::Result<T, E>;
pub type Result<T = ()> = StdResult<T, Error>;

pub(crate) type IID = id::ID;
pub(crate) type RequestID = id::ID;
pub(crate) type SessionID = id::ID;

#[repr(C)]
#[derive(Copy, Clone, Debug, Eq, PartialEq)]
pub enum SessionType {
    Data = 0,
    Schema = 1,
}

#[repr(C)]
#[derive(Copy, Clone, Debug, Eq, PartialEq)]
pub enum TransactionType {
    Read = 0,
    Write = 1,
}
