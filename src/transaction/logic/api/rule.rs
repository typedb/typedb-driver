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

use async_trait::async_trait;

use crate::{logic::Rule, Result, Transaction};

#[async_trait]
pub trait RuleAPI: Clone + Sync + Send {
    fn label(&self) -> &String;
    async fn delete(&mut self, transaction: &Transaction<'_>) -> Result;
    async fn set_label(&mut self, transaction: &Transaction<'_>, new_label: String) -> Result;
}

#[async_trait]
impl RuleAPI for Rule {
    fn label(&self) -> &String {
        &self.label
    }

    async fn delete(&mut self, transaction: &Transaction<'_>) -> Result {
        transaction.logic().transaction_stream.rule_delete(self.clone()).await
    }

    async fn set_label(&mut self, transaction: &Transaction<'_>, new_label: String) -> Result {
        transaction.logic().transaction_stream.rule_set_label(self.clone(), new_label).await
    }
}
