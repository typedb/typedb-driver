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

use std::fmt::{Display, Formatter};

use crate::common::{
    rpc::builder::core::database::{delete_req, rule_schema_req, schema_req, type_schema_req},
    Result, ServerRPC,
};

#[derive(Clone, Debug)]
pub struct Database {
    pub name: String,
    server_rpc: ServerRPC,
}

impl Database {
    pub(crate) fn new(name: &str, server_rpc: ServerRPC) -> Self {
        Database { name: name.into(), server_rpc }
    }

    pub async fn delete(mut self) -> Result {
        self.server_rpc.database_delete(delete_req(self.name.as_str())).await?;
        Ok(())
    }

    pub async fn schema(&mut self) -> Result<String> {
        self.server_rpc.database_schema(schema_req(self.name.as_str())).await.map(|res| res.schema)
    }

    pub async fn type_schema(&mut self) -> Result<String> {
        self.server_rpc
            .database_type_schema(type_schema_req(self.name.as_str()))
            .await
            .map(|res| res.schema)
    }

    pub async fn rule_schema(&mut self) -> Result<String> {
        self.server_rpc
            .database_rule_schema(rule_schema_req(self.name.as_str()))
            .await
            .map(|res| res.schema)
    }
}

impl Display for Database {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.name)
    }
}
