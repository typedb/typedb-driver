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

use std::{
    collections::{HashMap, HashSet},
    sync::Arc,
};

use futures::{channel::mpsc, future::BoxFuture, FutureExt};
use tonic::Streaming;
use typedb_protocol::{
    cluster_database_manager, cluster_user, core_database, core_database_manager, session,
    transaction, type_db_cluster_client::TypeDbClusterClient as ClusterGRPC,
};

use crate::common::{
    credential::CallCredentials,
    error::ClientError,
    rpc::{
        builder::{cluster, cluster::user::token_req},
        channel::CallCredChannel,
        Channel, CoreRPC,
    },
    Address, Credential, Error, Executor, Result,
};

#[derive(Debug, Clone)]
pub(crate) struct ClusterRPC {
    server_rpcs: HashMap<Address, ClusterServerRPC>,
}

impl ClusterRPC {
    pub(crate) fn new(addresses: HashSet<Address>, credential: Credential) -> Result<Arc<Self>> {
        let cluster_clients = addresses
            .into_iter()
            .map(|address| {
                Ok((address.clone(), ClusterServerRPC::new(address, credential.clone())?))
            })
            .collect::<Result<_>>()?;
        Ok(Arc::new(Self { server_rpcs: cluster_clients }))
    }

    pub(crate) async fn fetch_current_addresses<T: AsRef<str>>(
        addresses: &[T],
        credential: &Credential,
    ) -> Result<HashSet<Address>> {
        for address in addresses {
            match ClusterServerRPC::new(address.as_ref().parse()?, credential.clone())?
                .validated()
                .await
            {
                Ok(mut client) => {
                    let servers = client.servers_all().await?.servers;
                    return servers.into_iter().map(|server| server.address.parse()).collect();
                }
                Err(Error::Client(ClientError::UnableToConnect())) => (),
                Err(err) => Err(err)?,
            }
        }
        Err(ClientError::UnableToConnect())?
    }

    pub(crate) fn server_rpc_count(&self) -> usize {
        self.server_rpcs.len()
    }

    pub(crate) fn addresses(&self) -> impl Iterator<Item = &Address> {
        self.server_rpcs.keys()
    }

    pub(crate) fn get_server_rpc(&self, address: &Address) -> ClusterServerRPC {
        self.server_rpcs.get(address).unwrap().clone()
    }

    pub(crate) fn get_any_server_rpc(&self) -> ClusterServerRPC {
        // TODO round robin?
        self.server_rpcs.values().next().unwrap().clone()
    }

    pub(crate) fn iter_server_rpcs_cloned(&self) -> impl Iterator<Item = ClusterServerRPC> + '_ {
        self.server_rpcs.values().cloned()
    }

    pub(crate) fn unable_to_connect(&self) -> Error {
        Error::Client(ClientError::ClusterUnableToConnect(
            self.addresses().map(Address::to_string).collect::<Vec<_>>().join(","),
        ))
    }
}

#[derive(Clone, Debug)]
pub(crate) struct ClusterServerRPC {
    address: Address,
    core_rpc: CoreRPC,
    cluster_grpc: ClusterGRPC<CallCredChannel>,
    call_credentials: Arc<CallCredentials>,
    pub(crate) executor: Arc<Executor>,
}

impl ClusterServerRPC {
    pub(crate) fn new(address: Address, credential: Credential) -> Result<Self> {
        let (channel, call_credentials) = Channel::open_encrypted(address.clone(), credential)?;
        Ok(Self {
            address,
            core_rpc: CoreRPC::new(channel.clone())?,
            cluster_grpc: ClusterGRPC::new(channel.into()),
            executor: Arc::new(Executor::new().expect("Failed to create Executor")),
            call_credentials,
        })
    }

    async fn validated(mut self) -> Result<Self> {
        self.cluster_grpc.databases_all(cluster::database_manager::all_req()).await?;
        Ok(self)
    }

    pub(crate) fn address(&self) -> &Address {
        &self.address
    }

    async fn call_with_auto_renew_token<F, R>(&mut self, call: F) -> Result<R>
    where
        for<'a> F: Fn(&'a mut Self) -> BoxFuture<'a, Result<R>>,
    {
        match call(self).await {
            Err(Error::Client(ClientError::ClusterTokenCredentialInvalid())) => {
                self.renew_token().await?;
                call(self).await
            }
            res => res,
        }
    }

    async fn renew_token(&mut self) -> Result {
        self.call_credentials.reset_token();
        let req = token_req(self.call_credentials.username());
        let token = self.user_token(req).await?.token;
        self.call_credentials.set_token(token);
        Ok(())
    }

    async fn user_token(
        &mut self,
        username: cluster_user::token::Req,
    ) -> Result<cluster_user::token::Res> {
        Ok(self.cluster_grpc.user_token(username).await?.into_inner())
    }

    pub(crate) async fn servers_all(
        &mut self,
    ) -> Result<typedb_protocol::server_manager::all::Res> {
        self.call_with_auto_renew_token(|this| {
            Box::pin(
                this.cluster_grpc
                    .servers_all(cluster::server_manager::all_req())
                    .map(|res| Ok(res?.into_inner())),
            )
        })
        .await
    }

    pub(crate) async fn databases_get(
        &mut self,
        req: cluster_database_manager::get::Req,
    ) -> Result<cluster_database_manager::get::Res> {
        self.call_with_auto_renew_token(|this| {
            Box::pin(this.cluster_grpc.databases_get(req.clone()).map(|res| Ok(res?.into_inner())))
        })
        .await
    }

    pub(crate) async fn databases_all(
        &mut self,
        req: cluster_database_manager::all::Req,
    ) -> Result<cluster_database_manager::all::Res> {
        self.call_with_auto_renew_token(|this| {
            Box::pin(this.cluster_grpc.databases_all(req.clone()).map(|res| Ok(res?.into_inner())))
        })
        .await
    }

    // server client pass-through
    pub(crate) async fn databases_contains(
        &mut self,
        req: core_database_manager::contains::Req,
    ) -> Result<core_database_manager::contains::Res> {
        self.call_with_auto_renew_token(|this| {
            Box::pin(this.core_rpc.databases_contains(req.clone()))
        })
        .await
    }

    pub(crate) async fn databases_create(
        &mut self,
        req: core_database_manager::create::Req,
    ) -> Result<core_database_manager::create::Res> {
        self.call_with_auto_renew_token(|this| {
            Box::pin(this.core_rpc.databases_create(req.clone()))
        })
        .await
    }

    pub(crate) async fn database_delete(
        &mut self,
        req: core_database::delete::Req,
    ) -> Result<core_database::delete::Res> {
        self.call_with_auto_renew_token(|this| Box::pin(this.core_rpc.database_delete(req.clone())))
            .await
    }

    pub(crate) async fn database_schema(
        &mut self,
        req: core_database::schema::Req,
    ) -> Result<core_database::schema::Res> {
        self.call_with_auto_renew_token(|this| Box::pin(this.core_rpc.database_schema(req.clone())))
            .await
    }

    pub(crate) async fn database_rule_schema(
        &mut self,
        req: core_database::rule_schema::Req,
    ) -> Result<core_database::rule_schema::Res> {
        self.call_with_auto_renew_token(|this| {
            Box::pin(this.core_rpc.database_rule_schema(req.clone()))
        })
        .await
    }

    pub(crate) async fn database_type_schema(
        &mut self,
        req: core_database::type_schema::Req,
    ) -> Result<core_database::type_schema::Res> {
        self.call_with_auto_renew_token(|this| {
            Box::pin(this.core_rpc.database_type_schema(req.clone()))
        })
        .await
    }

    pub(crate) async fn session_open(
        &mut self,
        req: session::open::Req,
    ) -> Result<session::open::Res> {
        self.call_with_auto_renew_token(|this| Box::pin(this.core_rpc.session_open(req.clone())))
            .await
    }

    pub(crate) async fn session_close(
        &mut self,
        req: session::close::Req,
    ) -> Result<session::close::Res> {
        self.call_with_auto_renew_token(|this| Box::pin(this.core_rpc.session_close(req.clone())))
            .await
    }

    pub(crate) async fn transaction(
        &mut self,
        req: transaction::Req,
    ) -> Result<(mpsc::Sender<transaction::Client>, Streaming<transaction::Server>)> {
        self.call_with_auto_renew_token(|this| Box::pin(this.core_rpc.transaction(req.clone())))
            .await
    }
}
