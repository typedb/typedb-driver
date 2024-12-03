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

use std::sync::Arc;

use futures::{future::BoxFuture, FutureExt, TryFutureExt};
use log::{debug, trace, warn};
use tokio::sync::mpsc::{unbounded_channel as unbounded_async, UnboundedSender};
use tokio_stream::wrappers::UnboundedReceiverStream;
use tonic::{Response, Status, Streaming};
use typedb_protocol::{
    connection, database, database_manager, server_manager, transaction, type_db_client::TypeDbClient as GRPC, user,
    user_manager,
};

use super::channel::{CallCredentials, GRPCChannel};
use crate::common::{error::ConnectionError, Error, Result, StdResult};

type TonicResult<T> = StdResult<Response<T>, Status>;

#[derive(Clone, Debug)]
pub(super) struct RPCStub<Channel: GRPCChannel> {
    grpc: GRPC<Channel>,
    call_credentials: Option<Arc<CallCredentials>>,
}

impl<Channel: GRPCChannel> RPCStub<Channel> {
    pub(super) async fn new(channel: Channel, call_credentials: Option<Arc<CallCredentials>>) -> Self {
        Self { grpc: GRPC::new(channel), call_credentials }
    }

    async fn call<F, R>(&mut self, call: F) -> Result<R>
    where
        for<'a> F: Fn(&'a mut Self) -> BoxFuture<'a, Result<R>>,
    {
        call(self).await
    }

    pub(super) async fn connection_open(&mut self, req: connection::open::Req) -> Result<connection::open::Res> {
        self.single(|this| Box::pin(this.grpc.connection_open(req.clone()))).await
    }

    pub(super) async fn servers_all(&mut self, req: server_manager::all::Req) -> Result<server_manager::all::Res> {
        self.single(|this| Box::pin(this.grpc.servers_all(req.clone()))).await
    }

    pub(super) async fn databases_all(
        &mut self,
        req: database_manager::all::Req,
    ) -> Result<database_manager::all::Res> {
        self.single(|this| Box::pin(this.grpc.databases_all(req.clone()))).await
    }

    pub(super) async fn databases_get(
        &mut self,
        req: database_manager::get::Req,
    ) -> Result<database_manager::get::Res> {
        self.single(|this| Box::pin(this.grpc.databases_get(req.clone()))).await
    }

    pub(super) async fn databases_contains(
        &mut self,
        req: database_manager::contains::Req,
    ) -> Result<database_manager::contains::Res> {
        self.single(|this| Box::pin(this.grpc.databases_contains(req.clone()))).await
    }

    pub(super) async fn databases_create(
        &mut self,
        req: database_manager::create::Req,
    ) -> Result<database_manager::create::Res> {
        self.single(|this| Box::pin(this.grpc.databases_create(req.clone()))).await
    }

    pub(super) async fn database_delete(&mut self, req: database::delete::Req) -> Result<database::delete::Res> {
        self.single(|this| Box::pin(this.grpc.database_delete(req.clone()))).await
    }

    pub(super) async fn database_schema(&mut self, req: database::schema::Req) -> Result<database::schema::Res> {
        self.single(|this| Box::pin(this.grpc.database_schema(req.clone()))).await
    }

    pub(super) async fn database_type_schema(
        &mut self,
        req: database::type_schema::Req,
    ) -> Result<database::type_schema::Res> {
        self.single(|this| Box::pin(this.grpc.database_type_schema(req.clone()))).await
    }

    pub(super) async fn transaction(
        &mut self,
        open_req: transaction::Req,
    ) -> Result<(UnboundedSender<transaction::Client>, Streaming<transaction::Server>)> {
        self.call(|this| {
            let transaction_req = transaction::Client { reqs: vec![open_req.clone()] };
            Box::pin(async {
                let (sender, receiver) = unbounded_async();
                sender.send(transaction_req)?;
                this.grpc
                    .transaction(UnboundedReceiverStream::new(receiver))
                    .map_ok(|stream| Response::new((sender, stream.into_inner())))
                    .map(|r| Ok(r?.into_inner()))
                    .await
            })
        })
        .await
    }

    pub(super) async fn users_all(&mut self, req: user_manager::all::Req) -> Result<user_manager::all::Res> {
        self.single(|this| Box::pin(this.grpc.users_all(req.clone()))).await
    }

    pub(super) async fn users_get(&mut self, req: user_manager::get::Req) -> Result<user_manager::get::Res> {
        self.single(|this| Box::pin(this.grpc.users_get(req.clone()))).await
    }

    pub(super) async fn users_contains(
        &mut self,
        req: user_manager::contains::Req,
    ) -> Result<user_manager::contains::Res> {
        self.single(|this| Box::pin(this.grpc.users_contains(req.clone()))).await
    }

    pub(super) async fn users_create(&mut self, req: user_manager::create::Req) -> Result<user_manager::create::Res> {
        self.single(|this| Box::pin(this.grpc.users_create(req.clone()))).await
    }

    pub(super) async fn users_update(&mut self, req: user::update::Req) -> Result<user::update::Res> {
        self.single(|this| Box::pin(this.grpc.users_update(req.clone()))).await
    }

    pub(super) async fn users_delete(&mut self, req: user::delete::Req) -> Result<user::delete::Res> {
        self.single(|this| Box::pin(this.grpc.users_delete(req.clone()))).await
    }

    async fn single<F, R>(&mut self, call: F) -> Result<R>
    where
        for<'a> F: Fn(&'a mut Self) -> BoxFuture<'a, TonicResult<R>> + Send + Sync,
        R: 'static,
    {
        self.call(|this| Box::pin(call(this).map(|r| Ok(r?.into_inner())))).await
    }
}
