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

use futures::channel::mpsc;
use tonic::Streaming;
use typedb_protocol::{core_database, core_database_manager, session, transaction};

use crate::{
    async_enum_dispatch,
    common::{
        rpc::{core::CoreRPC, ClusterServerRPC},
        Executor, Result,
    },
};

#[derive(Clone, Debug)]
pub(crate) enum ServerRPC {
    Core(CoreRPC),
    Cluster(ClusterServerRPC),
}

impl From<CoreRPC> for ServerRPC {
    fn from(server_client: CoreRPC) -> Self {
        ServerRPC::Core(server_client)
    }
}

impl From<ClusterServerRPC> for ServerRPC {
    fn from(cluster_client: ClusterServerRPC) -> Self {
        ServerRPC::Cluster(cluster_client)
    }
}

impl ServerRPC {
    pub(crate) fn executor(&self) -> &Arc<Executor> {
        match self {
            Self::Core(client) => &client.executor,
            Self::Cluster(client) => &client.executor,
        }
    }

    async_enum_dispatch! { { Core, Cluster }
        pub(crate) async fn databases_contains(
            &mut self,
            req: core_database_manager::contains::Req,
        ) -> Result<core_database_manager::contains::Res>;

        pub(crate) async fn databases_create(
            &mut self,
            req: core_database_manager::create::Req,
        ) -> Result<core_database_manager::create::Res>;

        pub(crate) async fn database_delete(
            &mut self,
            req: core_database::delete::Req,
        ) -> Result<core_database::delete::Res>;

        pub(crate) async fn database_schema(
            &mut self,
            req: core_database::schema::Req,
        ) -> Result<core_database::schema::Res>;

        pub(crate) async fn database_type_schema(
            &mut self,
            req: core_database::type_schema::Req,
        ) -> Result<core_database::type_schema::Res>;

        pub(crate) async fn database_rule_schema(
            &mut self,
            req: core_database::rule_schema::Req,
        ) -> Result<core_database::rule_schema::Res>;

        pub(crate) async fn session_open(
            &mut self,
            req: session::open::Req,
        ) -> Result<session::open::Res>;

        pub(crate) async fn session_close(
            &mut self,
            req: session::close::Req,
        ) -> Result<session::close::Res>;

        pub(crate) async fn transaction(
            &mut self,
            req: transaction::Req,
        ) -> Result<(mpsc::Sender<transaction::Client>, Streaming<transaction::Server>)>;
    }
}
