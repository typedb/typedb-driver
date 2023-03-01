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

use std::sync::Arc;

use futures::{future::BoxFuture, FutureExt, TryFutureExt};
use log::{debug, trace};
use tokio::sync::mpsc::{unbounded_channel as unbounded_async, UnboundedSender};
use tokio_stream::wrappers::UnboundedReceiverStream;
use tonic::{Response, Status, Streaming};
use typedb_protocol::{
    cluster_database::Replica, cluster_database_manager, cluster_user, core_database, core_database_manager,
    server_manager, session, transaction, type_db_client::TypeDbClient as CoreGRPC,
    type_db_cluster_client::TypeDbClusterClient as ClusterGRPC, ClusterDatabase,
};

use super::channel::{CallCredentials, GRPCChannel};
use crate::common::{address::Address, error::ConnectionError, Error, Result, StdResult};

type TonicResult<T> = StdResult<Response<T>, Status>;

#[derive(Clone, Debug)]
pub(super) struct RPCStub<Channel: GRPCChannel> {
    address: Address,
    channel: Channel,
    core_grpc: CoreGRPC<Channel>,
    cluster_grpc: ClusterGRPC<Channel>,
    call_credentials: Option<Arc<CallCredentials>>,
}

impl<Channel: GRPCChannel> RPCStub<Channel> {
    pub(super) async fn new(
        address: Address,
        channel: Channel,
        call_credentials: Option<Arc<CallCredentials>>,
    ) -> Result<Self> {
        let this = Self {
            address,
            core_grpc: CoreGRPC::new(channel.clone()),
            cluster_grpc: ClusterGRPC::new(channel.clone()),
            channel,
            call_credentials,
        };
        let mut this = this.validated().await?;
        this.renew_token().await?;
        Ok(this)
    }

    pub(super) async fn validated(mut self) -> Result<Self> {
        self.databases_all(cluster_database_manager::all::Req {}).await?;
        Ok(self)
    }

    fn address(&self) -> &Address {
        &self.address
    }

    async fn call_with_auto_renew_token<F, R>(&mut self, call: F) -> Result<R>
    where
        for<'a> F: Fn(&'a mut Self) -> BoxFuture<'a, Result<R>>,
    {
        match call(self).await {
            Err(Error::Connection(ConnectionError::ClusterTokenCredentialInvalid())) => {
                self.renew_token().await?;
                call(self).await
            }
            res => res,
        }
    }

    async fn renew_token(&mut self) -> Result {
        if let Some(call_credentials) = &self.call_credentials {
            trace!("renewing token...");
            call_credentials.reset_token();
            let req = cluster_user::token::Req { username: call_credentials.username().to_owned() };
            trace!("sending token request...");
            let token = self.cluster_grpc.user_token(req).await?.into_inner().token;
            call_credentials.set_token(token);
            trace!("renewed token");
        }
        Ok(())
    }

    pub(super) async fn servers_all(&mut self, req: server_manager::all::Req) -> Result<server_manager::all::Res> {
        self.single(|this| Box::pin(this.cluster_grpc.servers_all(req.clone()))).await
    }

    pub(super) async fn databases_contains(
        &mut self,
        req: core_database_manager::contains::Req,
    ) -> Result<core_database_manager::contains::Res> {
        self.single(|this| Box::pin(this.core_grpc.databases_contains(req.clone()))).await
    }

    pub(super) async fn databases_create(
        &mut self,
        req: core_database_manager::create::Req,
    ) -> Result<core_database_manager::create::Res> {
        self.single(|this| Box::pin(this.core_grpc.databases_create(req.clone()))).await
    }

    // FIXME: merge after protocol merge
    pub(super) async fn databases_get(
        &mut self,
        req: cluster_database_manager::get::Req,
    ) -> Result<cluster_database_manager::get::Res> {
        if self.channel.is_plaintext() {
            self.databases_get_core(req).await
        } else {
            self.databases_get_cluster(req).await
        }
    }

    pub(super) async fn databases_all(
        &mut self,
        req: cluster_database_manager::all::Req,
    ) -> Result<cluster_database_manager::all::Res> {
        if self.channel.is_plaintext() {
            self.databases_all_core(req).await
        } else {
            self.databases_all_cluster(req).await
        }
    }

    async fn databases_get_core(
        &mut self,
        req: cluster_database_manager::get::Req,
    ) -> Result<cluster_database_manager::get::Res> {
        Ok(cluster_database_manager::get::Res {
            database: Some(ClusterDatabase {
                name: req.name,
                replicas: vec![Replica {
                    address: self.address().to_string(),
                    primary: true,
                    preferred: true,
                    term: 0,
                }],
            }),
        })
    }

    async fn databases_all_core(
        &mut self,
        _req: cluster_database_manager::all::Req,
    ) -> Result<cluster_database_manager::all::Res> {
        let database_names =
            self.single(|this| Box::pin(this.core_grpc.databases_all(core_database_manager::all::Req {}))).await?.names;
        Ok(cluster_database_manager::all::Res {
            databases: database_names
                .into_iter()
                .map(|db_name| ClusterDatabase {
                    name: db_name,
                    replicas: vec![Replica {
                        address: self.address().to_string(),
                        primary: true,
                        preferred: true,
                        term: 0,
                    }],
                })
                .collect(),
        })
    }

    async fn databases_get_cluster(
        &mut self,
        req: cluster_database_manager::get::Req,
    ) -> Result<cluster_database_manager::get::Res> {
        self.single(|this| Box::pin(this.cluster_grpc.databases_get(req.clone()))).await
    }

    async fn databases_all_cluster(
        &mut self,
        req: cluster_database_manager::all::Req,
    ) -> Result<cluster_database_manager::all::Res> {
        self.single(|this| Box::pin(this.cluster_grpc.databases_all(req.clone()))).await
    }
    // FIXME: end FIXME

    pub(super) async fn database_delete(
        &mut self,
        req: core_database::delete::Req,
    ) -> Result<core_database::delete::Res> {
        self.single(|this| Box::pin(this.core_grpc.database_delete(req.clone()))).await
    }

    pub(super) async fn database_schema(
        &mut self,
        req: core_database::schema::Req,
    ) -> Result<core_database::schema::Res> {
        self.single(|this| Box::pin(this.core_grpc.database_schema(req.clone()))).await
    }

    pub(super) async fn database_type_schema(
        &mut self,
        req: core_database::type_schema::Req,
    ) -> Result<core_database::type_schema::Res> {
        self.single(|this| Box::pin(this.core_grpc.database_type_schema(req.clone()))).await
    }

    pub(super) async fn database_rule_schema(
        &mut self,
        req: core_database::rule_schema::Req,
    ) -> Result<core_database::rule_schema::Res> {
        self.single(|this| Box::pin(this.core_grpc.database_rule_schema(req.clone()))).await
    }

    pub(super) async fn session_open(&mut self, req: session::open::Req) -> Result<session::open::Res> {
        self.single(|this| Box::pin(this.core_grpc.session_open(req.clone()))).await
    }

    pub(super) async fn session_close(&mut self, req: session::close::Req) -> Result<session::close::Res> {
        debug!("closing session");
        self.single(|this| Box::pin(this.core_grpc.session_close(req.clone()))).await
    }

    pub(super) async fn session_pulse(&mut self, req: session::pulse::Req) -> Result<session::pulse::Res> {
        self.single(|this| Box::pin(this.core_grpc.session_pulse(req.clone()))).await
    }

    pub(super) async fn transaction(
        &mut self,
        open_req: transaction::Req,
    ) -> Result<(UnboundedSender<transaction::Client>, Streaming<transaction::Server>)> {
        self.call_with_auto_renew_token(|this| {
            let transaction_req = transaction::Client { reqs: vec![open_req.clone()] };
            Box::pin(async {
                let (sender, receiver) = unbounded_async();
                sender.send(transaction_req)?;
                this.core_grpc
                    .transaction(UnboundedReceiverStream::new(receiver))
                    .map_ok(|stream| Response::new((sender, stream.into_inner())))
                    .map(|r| Ok(r?.into_inner()))
                    .await
            })
        })
        .await
    }

    async fn single<F, R>(&mut self, call: F) -> Result<R>
    where
        for<'a> F: Fn(&'a mut Self) -> BoxFuture<'a, TonicResult<R>> + Send + Sync,
        R: 'static,
    {
        self.call_with_auto_renew_token(|this| Box::pin(call(this).map(|r| Ok(r?.into_inner())))).await
    }
}
