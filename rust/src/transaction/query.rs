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

use crate::{
    answer::{ConceptMap, ConceptMapGroup, Explainable, Numeric, NumericGroup},
    common::{stream::Stream, Result},
    connection::TransactionStream,
    logic::Explanation,
    Options,
};

/// Provides methods for executing TypeQL queries in the transaction.
#[derive(Debug)]
pub struct QueryManager {
    transaction_stream: Arc<TransactionStream>,
}

impl QueryManager {
    pub(super) fn new(transaction_stream: Arc<TransactionStream>) -> Self {
        Self { transaction_stream }
    }

    /// Performs a TypeQL Define query with default options.
    /// See [`QueryManager::define_with_options`]
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn define(&self, query: &str) -> Result {
        self.define_with_options(query, Options::new()).await
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
    #[cfg_attr(feature = "sync", doc = "transaction.query().define_with_options(query, options)")]
    #[cfg_attr(not(feature = "sync"), doc = "transaction.query().define_with_options(query, options).await")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn define_with_options(&self, query: &str, options: Options) -> Result {
        self.transaction_stream.define(query.to_string(), options).await
    }

    /// Performs a TypeQL Undefine query with default options
    /// See [`QueryManager::undefine_with_options`]
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn undefine(&self, query: &str) -> Result {
        self.undefine_with_options(query, Options::new()).await
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
    #[cfg_attr(feature = "sync", doc = "transaction.query().undefine_with_options(query, options)")]
    #[cfg_attr(not(feature = "sync"), doc = "transaction.query().undefine_with_options(query, options).await")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn undefine_with_options(&self, query: &str, options: Options) -> Result {
        self.transaction_stream.undefine(query.to_string(), options).await
    }

    /// Performs a TypeQL Delete query with default options.
    /// See [`QueryManager::delete_with_options`]
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn delete(&self, query: &str) -> Result {
        self.delete_with_options(query, Options::new()).await
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
    #[cfg_attr(feature = "sync", doc = "transaction.query().delete_with_options(query, options)")]
    #[cfg_attr(not(feature = "sync"), doc = "transaction.query().delete_with_options(query, options).await")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn delete_with_options(&self, query: &str, options: Options) -> Result {
        self.transaction_stream.delete(query.to_string(), options).await
    }

    /// Performs a TypeQL Match (Get) query with default options.
    /// See [`QueryManager::match_with_options`]
    pub fn match_(&self, query: &str) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        self.match_with_options(query, Options::new())
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
    /// transaction.query().match_with_options(query, options)
    /// ```
    pub fn match_with_options(&self, query: &str, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        self.transaction_stream.match_(query.to_string(), options)
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
        self.transaction_stream.insert(query.to_string(), options)
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
        self.transaction_stream.update(query.to_string(), options)
    }

    /// Performs a TypeQL Match Aggregate query with default options.
    /// See [`QueryManager::match_aggregate`]
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn match_aggregate(&self, query: &str) -> Result<Numeric> {
        self.match_aggregate_with_options(query, Options::new()).await
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
    #[cfg_attr(feature = "sync", doc = "transaction.query().match_aggregate_with_options(query, options)")]
    #[cfg_attr(not(feature = "sync"), doc = "transaction.query().match_aggregate_with_options(query, options).await")]
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn match_aggregate_with_options(&self, query: &str, options: Options) -> Result<Numeric> {
        self.transaction_stream.match_aggregate(query.to_string(), options).await
    }

    /// Performs a TypeQL Match Group query with default options.
    /// See [`QueryManager::match_group`]
    pub fn match_group(&self, query: &str) -> Result<impl Stream<Item = Result<ConceptMapGroup>>> {
        self.match_group_with_options(query, Options::new())
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
    /// transaction.query().match_group_with_options(query, options)
    /// ```
    pub fn match_group_with_options(
        &self,
        query: &str,
        options: Options,
    ) -> Result<impl Stream<Item = Result<ConceptMapGroup>>> {
        self.transaction_stream.match_group(query.to_string(), options)
    }

    /// Performs a TypeQL Match Group Aggregate query with default options.
    /// See [`QueryManager::match_group_aggregate_with_options`]
    pub fn match_group_aggregate(&self, query: &str) -> Result<impl Stream<Item = Result<NumericGroup>>> {
        self.match_group_aggregate_with_options(query, Options::new())
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
    /// transaction.query().match_group_aggregate(query, options)
    /// ```
    pub fn match_group_aggregate_with_options(
        &self,
        query: &str,
        options: Options,
    ) -> Result<impl Stream<Item = Result<NumericGroup>>> {
        self.transaction_stream.match_group_aggregate(query.to_string(), options)
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
        self.transaction_stream.explain(explainable.id, options)
    }
}
