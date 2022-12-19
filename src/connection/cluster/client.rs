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

use super::{DatabaseManager, Session};
use crate::common::{ClusterRPC, Credential, Result, SessionType};

pub struct Client {
    databases: DatabaseManager,
    cluster_rpc: Arc<ClusterRPC>,
}

impl Client {
    pub async fn new<T: AsRef<str>>(init_addresses: &[T], credential: Credential) -> Result<Self> {
        let addresses = ClusterRPC::fetch_current_addresses(init_addresses, &credential).await?;
        let cluster_rpc = ClusterRPC::new(addresses, credential)?;
        let databases = DatabaseManager::new(cluster_rpc.clone());
        Ok(Self { cluster_rpc, databases })
    }

    pub fn databases(&mut self) -> &mut DatabaseManager {
        &mut self.databases
    }

    pub async fn session(
        &mut self,
        database_name: &str,
        session_type: SessionType,
    ) -> Result<Session> {
        Session::new(
            self.databases.get(database_name).await?,
            session_type,
            self.cluster_rpc.clone(),
        )
        .await
    }
}
