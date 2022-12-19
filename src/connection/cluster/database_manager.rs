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

use std::{fmt::Debug, future::Future, sync::Arc};

use super::Database;
use crate::{
    common::{
        error::ClientError,
        rpc::builder::{
            cluster::database_manager::all_req,
            core::database_manager::{contains_req, create_req},
        },
        ClusterRPC, ClusterServerRPC, Result,
    },
    connection::server,
};

#[derive(Clone, Debug)]
pub struct DatabaseManager {
    cluster_rpc: Arc<ClusterRPC>,
}

impl DatabaseManager {
    pub(crate) fn new(cluster_rpc: Arc<ClusterRPC>) -> Self {
        Self { cluster_rpc }
    }

    pub async fn get(&mut self, name: &str) -> Result<Database> {
        Database::get(name, self.cluster_rpc.clone()).await
    }

    pub async fn contains(&mut self, name: &str) -> Result<bool> {
        Ok(self
            .run_failsafe(name, move |database, mut server_rpc, _| {
                let req = contains_req(&database.name);
                async move { server_rpc.databases_contains(req).await }
            })
            .await?
            .contains)
    }

    pub async fn create(&mut self, name: &str) -> Result {
        self.run_failsafe(name, |database, mut server_rpc, _| {
            let req = create_req(&database.name);
            async move { server_rpc.databases_create(req).await }
        })
        .await?;
        Ok(())
    }

    pub async fn all(&mut self) -> Result<Vec<Database>> {
        let mut error_buffer = Vec::with_capacity(self.cluster_rpc.server_rpc_count());
        for mut server_rpc in self.cluster_rpc.iter_server_rpcs_cloned() {
            match server_rpc.databases_all(all_req()).await {
                Ok(list) => {
                    return list
                        .databases
                        .into_iter()
                        .map(|proto_db| Database::new(proto_db, self.cluster_rpc.clone()))
                        .collect()
                }
                Err(err) => error_buffer.push(format!("- {}: {}", server_rpc.address(), err)),
            }
        }
        Err(ClientError::ClusterAllNodesFailed(error_buffer.join("\n")))?
    }

    async fn run_failsafe<F, P, R>(&mut self, name: &str, task: F) -> Result<R>
    where
        F: Fn(server::Database, ClusterServerRPC, bool) -> P,
        P: Future<Output = Result<R>>,
    {
        Database::get(name, self.cluster_rpc.clone()).await?.run_failsafe(&task).await
    }
}
