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

use crate::{
    common::{CoreRPC, Result, SessionType},
    connection::{core, server},
};

pub struct Client {
    databases: core::DatabaseManager,
    core_rpc: CoreRPC,
}

impl Client {
    pub async fn new(address: &str) -> Result<Self> {
        let core_rpc = CoreRPC::connect(address.parse()?).await?;
        Ok(Self { databases: core::DatabaseManager::new(core_rpc.clone()), core_rpc })
    }

    pub async fn with_default_address() -> Result<Self> {
        Self::new("http://localhost:1729").await
    }

    pub fn databases(&mut self) -> &mut core::DatabaseManager {
        &mut self.databases
    }

    pub async fn session(
        &mut self,
        database_name: &str,
        session_type: SessionType,
    ) -> Result<server::Session> {
        self.session_with_options(database_name, session_type, core::Options::default()).await
    }

    pub async fn session_with_options(
        &mut self,
        database_name: &str,
        session_type: SessionType,
        options: core::Options,
    ) -> Result<server::Session> {
        server::Session::new(database_name, session_type, options, self.core_rpc.clone().into())
            .await
    }
}
