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

pub mod answer;

pub mod concept;

pub mod common;
pub use common::Result;

mod database;
pub use database::{Database, DatabaseManager};

mod options;
pub use options::Options;

pub mod query;

mod rpc;

pub mod session;
pub use session::Session;

pub mod transaction;
pub use transaction::Transaction;

use crate::rpc::client::RpcClient;

pub struct TypeDBClient {
    pub databases: DatabaseManager,
    pub(crate) rpc_client: RpcClient,
}

impl TypeDBClient {
    pub async fn new(address: &str) -> Result<Self> {
        let rpc_client = RpcClient::new(address).await?;
        Ok(TypeDBClient { databases: DatabaseManager::new(&rpc_client), rpc_client })
    }

    pub async fn with_default_address() -> Result<Self> {
        Self::new("http://0.0.0.0:1729").await
    }

    pub async fn session(&mut self, db_name: &str, type_: session::Type) -> Result<Session> {
        self.session_with_options(db_name, type_, Options::default()).await
    }

    pub async fn session_with_options(
        &mut self,
        db_name: &str,
        type_: session::Type,
        options: Options,
    ) -> Result<Session> {
        Session::new(db_name, type_, options, &self.rpc_client).await
    }
}

// TODO: this is one potential option for single-threaded environments - but inelegant
// #[macro_export]
// macro_rules! session {
//     ($client:tt, $db_name:tt, $session_type:tt, $body:expr) => {
//         async {
//             match $client.session($db_name, $session_type).await {
//                 Ok(mut session) => {
//                     $body;
//                     session.close().await;
//                     Ok(())
//                 }
//                 Err(err) => Err(err)
//             }
//         }.await
//     };
// }
