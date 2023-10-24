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
    common::{box_promise, BoxPromise},
    logic::Rule,
    promisify, resolve, Result, Transaction,
};

pub trait RuleAPI: Clone + Sync + Send {
    /// Retrieves the unique label of the rule.
    fn label(&self) -> &String;

    /// Deletes this rule.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current `Transaction`
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "rule.delete(transaction).resolve()")]
    #[cfg_attr(not(feature = "sync"), doc = "rule.delete(transaction).await")]
    /// ```
    fn delete<'tx>(&mut self, transaction: &'tx Transaction<'tx>) -> BoxPromise<'tx, Result>;

    /// Check if this rule has been deleted.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current `Transaction`
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "rule.is_deleted(transaction).resolve()")]
    #[cfg_attr(not(feature = "sync"), doc = "rule.is_deleted(transaction).await")]
    /// ```
    fn is_deleted<'tx>(&self, transaction: &'tx Transaction<'tx>) -> BoxPromise<'tx, Result<bool>> {
        let promise = transaction.transaction_stream.get_rule(self.label().to_owned());
        box_promise(promisify! { resolve!(promise).map(|rule| rule.is_none()) })
    }

    /// Renames the label of the rule. The new label must remain unique.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current `Transaction`
    /// * `new_label` -- The new label to be given to the rule
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "rule.set_label(transaction, new_label).resolve()")]
    #[cfg_attr(not(feature = "sync"), doc = "rule.set_label(transaction, new_label).await")]
    /// ```
    fn set_label<'tx>(&mut self, transaction: &'tx Transaction<'tx>, new_label: String) -> BoxPromise<'tx, Result>;
}

impl RuleAPI for Rule {
    fn label(&self) -> &String {
        &self.label
    }

    fn delete<'tx>(&mut self, transaction: &'tx Transaction<'tx>) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.rule_delete(self.clone()))
    }

    fn set_label<'tx>(&mut self, transaction: &'tx Transaction<'tx>, new_label: String) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.rule_set_label(self.clone(), new_label))
    }
}
