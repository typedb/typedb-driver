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

use std::fmt::Debug;
use std::future::Future;
use std::sync::Arc;
use futures::channel::mpsc;
use futures::SinkExt;
use tonic::{Response, Status, Streaming};
use tonic::transport::Channel;
use typedb_protocol::{core_database, core_database_manager, session, transaction};

use crate::common::error::Error;
use crate::common::{Executor, Result};
use crate::rpc::builder::core;
use crate::rpc::builder::transaction::client_msg;

#[derive(Clone, Debug)]
pub(crate) struct RpcClient {
    typedb: typedb_protocol::type_db_client::TypeDbClient<Channel>,
    pub(crate) executor: Arc<Executor>,
}

impl RpcClient {
    pub(crate) async fn new(address: &str) -> Result<Self> {
        match typedb_protocol::type_db_client::TypeDbClient::connect(address.to_string()).await {
            Ok(mut client) => {
                // TODO: temporary hack to validate connection until we have client pulse
                match RpcClient::check_connection(&mut client).await {
                    Ok(_) => Ok(RpcClient {
                        typedb: client,
                        executor: Arc::new(futures::executor::ThreadPool::new().expect("Failed to create ThreadPool"))
                    }),
                    Err(err) => Err(err)
                }
            }
            // TODO: better error message than 'transport error'
            Err(err) => Err(Error::new(err.to_string())),
            // Err(err) => Err(Error::from_grpc(err)),
        }
    }

    async fn check_connection(client: &mut typedb_protocol::type_db_client::TypeDbClient<Channel>) -> Result<()> {
        client.databases_all(core::database_manager::all_req()).await.map(|_| ()).map_err(|status| Error::new(status.to_string()))
    }

    pub(crate) async fn databases_contains(&mut self, req: core_database_manager::contains::Req) -> Result<core_database_manager::contains::Res> {
        Self::single(self.typedb.databases_contains(req)).await
    }

    pub(crate) async fn databases_create(&mut self, req: core_database_manager::create::Req) -> Result<core_database_manager::create::Res> {
        Self::single(self.typedb.databases_create(req)).await
    }

    pub(crate) async fn databases_all(&mut self, req: core_database_manager::all::Req) -> Result<core_database_manager::all::Res> {
        Self::single(self.typedb.databases_all(req)).await
    }

    pub(crate) async fn database_delete(&mut self, req: core_database::delete::Req) -> Result<core_database::delete::Res> {
        Self::single(self.typedb.database_delete(req)).await
    }

    pub(crate) async fn database_rule_schema(&mut self, req: core_database::rule_schema::Req) -> Result<core_database::rule_schema::Res> {
        Self::single(self.typedb.database_rule_schema(req)).await
    }

    pub(crate) async fn database_schema(&mut self, req: core_database::schema::Req) -> Result<core_database::schema::Res> {
        Self::single(self.typedb.database_schema(req)).await
    }

    pub(crate) async fn database_type_schema(&mut self, req: core_database::type_schema::Req) -> Result<core_database::type_schema::Res> {
        Self::single(self.typedb.database_type_schema(req)).await
    }

    pub(crate) async fn session_open(&mut self, req: session::open::Req) -> Result<session::open::Res> {
        Self::single(self.typedb.session_open(req)).await
    }

    pub(crate) async fn session_close(&mut self, req: session::close::Req) -> Result<session::close::Res> {
        Self::single(self.typedb.session_close(req)).await
    }

    pub(crate) async fn transaction(&mut self, open_req: transaction::Req) -> Result<(mpsc::Sender<transaction::Client>, Streaming<transaction::Server>)> {
        // TODO: refactor to crossbeam channel
        let (mut sender, receiver) = mpsc::channel::<transaction::Client>(256);
        sender.send(client_msg(vec![open_req])).await.unwrap();
        Self::bidi_stream(sender, self.typedb.transaction(receiver)).await
    }

    async fn single<T>(res: impl Future<Output = ::core::result::Result<Response<T>, Status>>) -> Result<T> {
        // TODO: check if we need ensureConnected() from client-java
        res.await.map(|res| res.into_inner()).map_err(|status| status.into())
    }

    async fn bidi_stream<T, U>(req_sink: mpsc::Sender<T>, res: impl Future<Output = ::core::result::Result<Response<Streaming<U>>, Status>>) -> Result<(mpsc::Sender<T>, Streaming<U>)> {
        res.await
            .map(|resp| (req_sink, resp.into_inner()))
            .map_err(|status| status.into())
    }
}
