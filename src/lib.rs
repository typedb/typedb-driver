/*
 * Copyright (C) 2021 Vaticle
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
pub mod common;
pub mod concept;
pub mod database;
pub mod query;
mod rpc;
pub mod session;
pub mod transaction;

use std::cell::UnsafeCell;
pub use crate::common::Result;
pub use crate::database::DatabaseManager;
pub use crate::database::Database;
pub use crate::session::Session;
pub use crate::transaction::Transaction;

use std::sync::{Arc, mpsc};
use crossbeam::channel::internal::SelectHandle;
use crossbeam::queue::ArrayQueue;
use futures::executor;
use log::{debug, warn};
use crate::rpc::builder::session::close_req;

use crate::rpc::client::RpcClient;
use crate::session::SessionId;

pub struct TypeDBClient {
    pub databases: DatabaseManager,
    pub(crate) rpc_client: RpcClient,
}

impl TypeDBClient {
    pub async fn new(address: &str) -> Result<Self> {
        let rpc_client = RpcClient::new(address).await?;
        Ok(TypeDBClient {
            databases: DatabaseManager::new(&rpc_client),
            rpc_client,
        })
    }

    pub async fn with_default_address() -> Result<Self> {
        Self::new("http://0.0.0.0:1729").await
    }

    pub async fn session(&mut self, db_name: &str, session_type: session::Type) -> Result<Session> {
        Session::new(db_name, session_type, &self.rpc_client).await
    }
}

pub(crate) struct CloseSessionsTask {
    session_id_sink: crossbeam::channel::Sender<SessionId>,
    session_id_receiver: crossbeam::channel::Receiver<SessionId>,
    rpc_client: RpcClient
}

impl CloseSessionsTask {
    fn new(rpc_client: &RpcClient) -> Self {
        let (session_id_sink, session_id_receiver) = crossbeam::channel::unbounded::<SessionId>();
        // let (session_id_sink, session_id_receiver) = mpsc::channel::<SessionId>();
        CloseSessionsTask {
            session_id_sink,
            session_id_receiver,
            rpc_client: rpc_client.clone()
        }
    }

    fn spawn(rpc_client: &RpcClient) -> crossbeam::channel::Sender<SessionId> {
        let mut task = CloseSessionsTask::new(rpc_client);
        let session_id_sink = task.session_id_sink.clone();
        tokio::spawn(async move { task.run().await; });
        session_id_sink
    }

    async fn run(&mut self) {
        let mut session_id_iter = self.session_id_receiver.iter();
        while let Some(session_id) = session_id_iter.next() {
            println!("Session close channel listener received session ID {:?}", session_id.clone());
            if let Err(_) = self.rpc_client.clone().session_close(close_req(session_id.clone())).await {
                println!("Session close request for session with ID {:?} failed. Maybe already closed?", session_id);
            } else {
                println!("Session with ID {:?} has been closed", session_id);
            }
        }
    }
}

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
