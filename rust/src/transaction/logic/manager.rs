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

use std::pin::Pin;

use typeql::pattern::{Conjunction, Statement};

use crate::{
    common::{stream::Stream, Promise, Result},
    connection::TransactionStream,
    logic::Rule,
};

/// Provides methods for manipulating rules in the database.
#[derive(Clone, Debug)]
pub struct LogicManager<'tx> {
    pub(super) transaction_stream: Pin<&'tx TransactionStream>,
}

impl<'tx> LogicManager<'tx> {
    pub(crate) fn new(transaction_stream: Pin<&'tx TransactionStream>) -> Self {
        Self { transaction_stream }
    }

    /// Creates a new Rule if none exists with the given label, or replaces the existing one.
    ///
    /// # Arguments
    ///
    /// * `label` -- The label of the Rule to create or replace
    /// * `when` -- The when body of the rule to create
    /// * `then` -- The then body of the rule to create
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "transaction.logic().put_rule(label, when, then).resolve()")]
    #[cfg_attr(not(feature = "sync"), doc = "transaction.logic().put_rule(label, when, then).await")]
    /// ```
    pub fn put_rule(&self, label: String, when: Conjunction, then: Statement) -> impl Promise<'tx, Result<Rule>> {
        self.transaction_stream.get_ref().put_rule(label, when, then)
    }

    /// Retrieves the Rule that has the given label.
    ///
    /// # Arguments
    ///
    /// * `label` -- The label of the Rule to create or retrieve
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "transaction.logic().get_rule(label).resolve()")]
    #[cfg_attr(not(feature = "sync"), doc = "transaction.logic().get_rule(label).await")]
    /// ```
    pub fn get_rule(&self, label: String) -> impl Promise<'tx, Result<Option<Rule>>> {
        self.transaction_stream.get_ref().get_rule(label)
    }

    /// Retrieves all rules.
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.logic.get_rules()
    /// ```
    pub fn get_rules(&self) -> Result<impl Stream<Item = Result<Rule>>> {
        self.transaction_stream.get_ref().get_rules()
    }
}
