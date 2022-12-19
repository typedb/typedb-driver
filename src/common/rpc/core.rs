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

use std::{future::Future, sync::Arc};

use futures::{channel::mpsc, SinkExt};
use tonic::{Response, Status, Streaming};
use typedb_protocol::{
    core_database, core_database_manager, session, transaction,
    type_db_client::TypeDbClient as RawCoreGRPC,
};

use crate::{
    async_enum_dispatch,
    common::{
        rpc::{
            builder::{core, transaction::client_msg},
            channel::CallCredChannel,
            Channel,
        },
        Address, Executor, Result, StdResult, TonicChannel,
    },
};

#[derive(Clone, Debug)]
enum CoreGRPC {
    Plaintext(RawCoreGRPC<TonicChannel>),
    Encrypted(RawCoreGRPC<CallCredChannel>),
}

impl CoreGRPC {
    pub fn new(channel: Channel) -> Self {
        match channel {
            Channel::Plaintext(channel) => Self::Plaintext(RawCoreGRPC::new(channel)),
            Channel::Encrypted(channel) => Self::Encrypted(RawCoreGRPC::new(channel)),
        }
    }

    async_enum_dispatch! { { Plaintext, Encrypted }
        pub async fn databases_contains(
            &mut self,
            request: core_database_manager::contains::Req,
        ) -> StdResult<Response<core_database_manager::contains::Res>, Status>;

        pub async fn databases_create(
            &mut self,
            request: core_database_manager::create::Req,
        ) -> StdResult<Response<core_database_manager::create::Res>, Status>;

        pub async fn databases_all(
            &mut self,
            request: core_database_manager::all::Req,
        ) -> StdResult<Response<core_database_manager::all::Res>, Status>;

        pub async fn database_schema(
            &mut self,
            request: core_database::schema::Req,
        ) -> StdResult<Response<core_database::schema::Res>, Status>;

        pub async fn database_type_schema(
            &mut self,
            request: core_database::type_schema::Req,
        ) -> StdResult<Response<core_database::type_schema::Res>, Status>;

        pub async fn database_rule_schema(
            &mut self,
            request: core_database::rule_schema::Req,
        ) -> StdResult<Response<core_database::rule_schema::Res>, Status>;

        pub async fn database_delete(
            &mut self,
            request: core_database::delete::Req,
        ) -> StdResult<Response<core_database::delete::Res>, Status>;

        pub async fn session_open(
            &mut self,
            request: session::open::Req,
        ) -> StdResult<Response<session::open::Res>, Status>;

        pub async fn session_close(
            &mut self,
            request: session::close::Req,
        ) -> StdResult<Response<session::close::Res>, Status>;

        pub async fn session_pulse(
            &mut self,
            request: session::pulse::Req,
        ) -> StdResult<Response<session::pulse::Res>, Status>;

        pub async fn transaction(
            &mut self,
            request: impl tonic::IntoStreamingRequest<Message = transaction::Client>,
        ) -> StdResult<Response<Streaming<transaction::Server>>, Status>;
    }
}

#[derive(Clone, Debug)]
pub(crate) struct CoreRPC {
    core_grpc: CoreGRPC,
    pub(crate) executor: Arc<Executor>,
}

impl CoreRPC {
    pub(crate) fn new(channel: Channel) -> Result<Self> {
        Ok(Self {
            core_grpc: CoreGRPC::new(channel),
            executor: Arc::new(Executor::new().expect("Failed to create Executor")),
        })
    }

    pub(crate) async fn connect(address: Address) -> Result<Self> {
        Self::new(Channel::open_plaintext(address)?)?.validated().await
    }

    async fn validated(mut self) -> Result<Self> {
        // TODO: temporary hack to validate connection until we have client pulse
        self.core_grpc.databases_all(core::database_manager::all_req()).await?;
        Ok(self)
    }

    pub(crate) async fn databases_contains(
        &mut self,
        req: core_database_manager::contains::Req,
    ) -> Result<core_database_manager::contains::Res> {
        single(self.core_grpc.databases_contains(req)).await
    }

    pub(crate) async fn databases_create(
        &mut self,
        req: core_database_manager::create::Req,
    ) -> Result<core_database_manager::create::Res> {
        single(self.core_grpc.databases_create(req)).await
    }

    pub(crate) async fn databases_all(
        &mut self,
        req: core_database_manager::all::Req,
    ) -> Result<core_database_manager::all::Res> {
        single(self.core_grpc.databases_all(req)).await
    }

    pub(crate) async fn database_delete(
        &mut self,
        req: core_database::delete::Req,
    ) -> Result<core_database::delete::Res> {
        single(self.core_grpc.database_delete(req)).await
    }

    pub(crate) async fn database_schema(
        &mut self,
        req: core_database::schema::Req,
    ) -> Result<core_database::schema::Res> {
        single(self.core_grpc.database_schema(req)).await
    }

    pub(crate) async fn database_type_schema(
        &mut self,
        req: core_database::type_schema::Req,
    ) -> Result<core_database::type_schema::Res> {
        single(self.core_grpc.database_type_schema(req)).await
    }

    pub(crate) async fn database_rule_schema(
        &mut self,
        req: core_database::rule_schema::Req,
    ) -> Result<core_database::rule_schema::Res> {
        single(self.core_grpc.database_rule_schema(req)).await
    }

    pub(crate) async fn session_open(
        &mut self,
        req: session::open::Req,
    ) -> Result<session::open::Res> {
        single(self.core_grpc.session_open(req)).await
    }

    pub(crate) async fn session_close(
        &mut self,
        req: session::close::Req,
    ) -> Result<session::close::Res> {
        single(self.core_grpc.session_close(req)).await
    }

    pub(crate) async fn transaction(
        &mut self,
        open_req: transaction::Req,
    ) -> Result<(mpsc::Sender<transaction::Client>, Streaming<transaction::Server>)> {
        // TODO: refactor to crossbeam channel
        let (mut sender, receiver) = mpsc::channel::<transaction::Client>(256);
        sender.send(client_msg(vec![open_req])).await.unwrap();
        bidi_stream(sender, self.core_grpc.transaction(receiver)).await
    }
}

pub(crate) async fn single<T>(
    res: impl Future<Output = StdResult<Response<T>, Status>>,
) -> Result<T> {
    Ok(res.await?.into_inner())
}

pub(crate) async fn bidi_stream<T, U>(
    req_sink: mpsc::Sender<T>,
    res: impl Future<Output = StdResult<Response<Streaming<U>>, Status>>,
) -> Result<(mpsc::Sender<T>, Streaming<U>)> {
    Ok((req_sink, res.await?.into_inner()))
}
