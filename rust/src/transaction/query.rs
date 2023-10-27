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

use crate::{
    answer::{ConceptMap, ConceptMapGroup, Explainable, ValueGroup, readable_concept},
    common::{stream::Stream, Promise, Result},
    concept::Value,
    connection::TransactionStream,
    logic::Explanation,
    Options,
};

/// Provides methods for executing TypeQL queries in the transaction.
#[derive(Debug)]
pub struct QueryManager<'tx> {
    transaction_stream: Pin<&'tx TransactionStream>,
}

impl<'tx> QueryManager<'tx> {
    pub(super) fn new(transaction_stream: Pin<&'tx TransactionStream>) -> Self {
        Self { transaction_stream }
    }

    /// Performs a TypeQL Define query with default options.
    /// See [`QueryManager::define_with_options`]
    pub fn define(&self, query: &str) -> impl Promise<'tx, Result> {
        self.define_with_options(query, Options::new())
    }

    /// Performs a TypeQL Define query in the transaction.
    ///
    /// # Arguments
    ///
    /// * `query` -- The TypeQL Define query to be executed
    /// * `options` -- Specify query options
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "transaction.query().define_with_options(query, options).resolve()")]
    #[cfg_attr(not(feature = "sync"), doc = "transaction.query().define_with_options(query, options).await")]
    /// ```
    pub fn define_with_options(&self, query: &str, options: Options) -> impl Promise<'tx, Result> {
        self.transaction_stream.get_ref().define(query.to_string(), options)
    }

    /// Performs a TypeQL Undefine query with default options
    /// See [`QueryManager::undefine_with_options`]
    pub fn undefine(&self, query: &str) -> impl Promise<'tx, Result> {
        self.undefine_with_options(query, Options::new())
    }

    /// Performs a TypeQL Undefine query in the transaction.
    ///
    /// # Arguments
    ///
    /// * `query` -- The TypeQL Undefine query to be executed
    /// * `options` -- Specify query options
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "transaction.query().undefine_with_options(query, options).resolve()")]
    #[cfg_attr(not(feature = "sync"), doc = "transaction.query().undefine_with_options(query, options).await")]
    /// ```
    pub fn undefine_with_options(&self, query: &str, options: Options) -> impl Promise<'tx, Result> {
        self.transaction_stream.get_ref().undefine(query.to_string(), options)
    }

    /// Performs a TypeQL Delete query with default options.
    /// See [`QueryManager::delete_with_options`]
    pub fn delete(&self, query: &str) -> impl Promise<'tx, Result> {
        self.delete_with_options(query, Options::new())
    }

    /// Performs a TypeQL Delete query in the transaction.
    ///
    /// # Arguments
    ///
    /// * `query` -- The TypeQL Delete query to be executed
    /// * `options` -- Specify query options
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "transaction.query().delete_with_options(query, options).resolve()")]
    #[cfg_attr(not(feature = "sync"), doc = "transaction.query().delete_with_options(query, options).await")]
    /// ```
    pub fn delete_with_options(&self, query: &str, options: Options) -> impl Promise<'tx, Result> {
        self.transaction_stream.get_ref().delete(query.to_string(), options)
    }

    /// Performs a TypeQL Match (Get) query with default options.
    /// See [`QueryManager::get_with_options`]
    pub fn get(&self, query: &str) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        self.get_with_options(query, Options::new())
    }

    /// Performs a TypeQL Match (Get) query in the transaction.
    ///
    /// # Arguments
    ///
    /// * `query` -- The TypeQL Match (Get) query to be executed
    /// * `options` -- Specify query options
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.query().get_with_options(query, options)
    /// ```
    pub fn get_with_options(&self, query: &str, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        self.transaction_stream.get_ref().get(query.to_string(), options)
    }

    /// Performs a TypeQL Insert query with default options.
    /// See [`QueryManager::insert_with_options`]
    pub fn insert(&self, query: &str) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        self.insert_with_options(query, Options::new())
    }

    /// Performs a TypeQL Insert query in the transaction.
    ///
    /// # Arguments
    ///
    /// * `query` -- The TypeQL Insert query to be executed
    /// * `options` -- Specify query options
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.query().insert_with_options(query, options)
    /// ```
    pub fn insert_with_options(&self, query: &str, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        self.transaction_stream.get_ref().insert(query.to_string(), options)
    }

    /// Performs a TypeQL Update query with default options.
    /// See [`QueryManager::update_with_options`]
    pub fn update(&self, query: &str) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        self.update_with_options(query, Options::new())
    }

    /// Performs a TypeQL Update query in the transaction.
    ///
    /// # Arguments
    ///
    /// * `query` -- The TypeQL Update query to be executed
    /// * `options` -- Specify query options
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.query().update_with_options(query, options)
    /// ```
    pub fn update_with_options(&self, query: &str, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        self.transaction_stream.get_ref().update(query.to_string(), options)
    }

    /// Performs a TypeQL Match Aggregate query with default options.
    /// See [`QueryManager::get_aggregate`]
    pub fn get_aggregate(&self, query: &str) -> impl Promise<'tx, Result<Value>> {
        self.get_aggregate_with_options(query, Options::new())
    }

    /// Performs a TypeQL Match Aggregate query in the transaction.
    ///
    /// # Arguments
    ///
    /// * `query` -- The TypeQL Match Aggregate query to be executed
    /// * `options` -- Specify query options
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "transaction.query().get_aggregate_with_options(query, options).resolve()")]
    #[cfg_attr(not(feature = "sync"), doc = "transaction.query().get_aggregate_with_options(query, options).await")]
    /// ```
    pub fn get_aggregate_with_options(&self, query: &str, options: Options) -> impl Promise<'tx, Result<Value>> {
        self.transaction_stream.get_ref().get_aggregate(query.to_string(), options)
    }

    /// Performs a TypeQL Match Group query with default options.
    /// See [`QueryManager::get_group`]
    pub fn get_group(&self, query: &str) -> Result<impl Stream<Item = Result<ConceptMapGroup>>> {
        self.get_group_with_options(query, Options::new())
    }

    /// Performs a TypeQL Match Group query in the transaction.
    ///
    /// # Arguments
    ///
    /// * `query` -- The TypeQL Match Group query to be executed
    /// * `options` -- Specify query options
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.query().get_group_with_options(query, options)
    /// ```
    pub fn get_group_with_options(
        &self,
        query: &str,
        options: Options,
    ) -> Result<impl Stream<Item = Result<ConceptMapGroup>>> {
        self.transaction_stream.get_ref().get_group(query.to_string(), options)
    }

    /// Performs a TypeQL Match Group Aggregate query with default options.
    /// See [`QueryManager::get_group_aggregate_with_options`]
    pub fn get_group_aggregate(&self, query: &str) -> Result<impl Stream<Item = Result<ValueGroup>>> {
        self.get_group_aggregate_with_options(query, Options::new())
    }

    /// Performs a TypeQL Match Group Aggregate query in the transaction.
    ///
    /// # Arguments
    ///
    /// * `query` -- The TypeQL Match Group Aggregate query to be executed
    /// * `options` -- Specify query options
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.query().get_group_aggregate_with_options(query, options)
    /// ```
    pub fn get_group_aggregate_with_options(
        &self,
        query: &str,
        options: Options,
    ) -> Result<impl Stream<Item = Result<ValueGroup>>> {
        self.transaction_stream.get_ref().get_group_aggregate(query.to_string(), options)
    }

    /// Performs a TypeQL Fetch query with default options.
    /// See [`QueryManager::fetch_with_options`]
    pub fn fetch(&self, query: &str) -> Result<impl Stream<Item = Result<readable_concept::Tree>>> {
        self.fetch_with_options(query, Options::new())
    }

    /// Performs a TypeQL Match Group Aggregate query in the transaction.
    ///
    /// # Arguments
    ///
    /// * `query` -- The TypeQL Match Group Aggregate query to be executed
    /// * `options` -- Specify query options
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.query().fetch_with_options(query, options)
    /// ```
    pub fn fetch_with_options(
        &self,
        query: &str,
        options: Options,
    ) -> Result<impl Stream<Item = Result<readable_concept::Tree>>> {
        self.transaction_stream.get_ref().fetch(query.to_string(), options)
    }

    /// Performs a TypeQL Explain query in the transaction.
    /// See [``QueryManager::explain_with_options]
    pub fn explain(&self, explainable: &Explainable) -> Result<impl Stream<Item = Result<Explanation>>> {
        self.explain_with_options(explainable, Options::new())
    }

    /// Performs a TypeQL Explain query in the transaction.
    ///
    /// # Arguments
    ///
    /// * `explainable` -- The Explainable to be explained
    /// * `options` -- Specify query options
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.query().explain_with_options(explainable, options)
    /// ```
    pub fn explain_with_options(
        &self,
        explainable: &Explainable,
        options: Options,
    ) -> Result<impl Stream<Item = Result<Explanation>>> {
        self.transaction_stream.get_ref().explain(explainable.id, options)
    }
}
