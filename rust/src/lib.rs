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

#![deny(elided_lifetimes_in_paths)]
#![deny(unused_must_use)]

pub use self::{
    common::{box_stream, error, info, BoxPromise, BoxStream, Error, Options, Promise, Result, TransactionType, IID},
    connection::Credential,
    database::{Database, DatabaseManager},
    driver::TypeDBDriver,
    transaction::Transaction,
    user::{User, UserManager},
};

pub mod answer;
mod common;
pub mod concept;
mod connection;
mod database;
pub mod driver;
pub mod transaction;
mod user;
