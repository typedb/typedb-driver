/*
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

use std::{fmt, pin::Pin};

use tracing::debug;

use crate::{
    analyze::AnalyzedQuery,
    answer::QueryAnswer,
    common::{Promise, Result, TransactionType},
    connection::TransactionStream,
    Error, QueryOptions, TransactionOptions,
};

/// A transaction with a TypeDB database.
pub struct Transaction {
    /// The transaction’s type (READ or WRITE)
    type_: TransactionType,
    /// The options for the transaction
    options: TransactionOptions,
    transaction_stream: Pin<Box<TransactionStream>>,
}

impl Transaction {
    pub(super) fn new(transaction_stream: TransactionStream) -> Self {
        let transaction_stream = Box::pin(transaction_stream);
        Transaction {
            type_: transaction_stream.type_(),
            options: transaction_stream.options().clone(),
            transaction_stream,
        }
    }

    /// Checks if the transaction is open.
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.is_open()
    /// ```
    pub fn is_open(&self) -> bool {
        self.transaction_stream.is_open()
    }

    /// Performs a TypeQL query with default options.
    /// See [`Transaction::query_with_options`]
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.query(query)
    /// ```
    pub fn query(&self, query: impl AsRef<str>) -> impl Promise<'static, Result<QueryAnswer>> {
        self.query_with_options(query, QueryOptions::new())
    }

    /// Performs a TypeQL query in this transaction.
    ///
    /// # Arguments
    ///
    /// * `query` — The TypeQL query to be executed
    /// * `options` — The QueryOptions to execute the query with
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.query_with_options(query, options)
    /// ```
    pub fn query_with_options(
        &self,
        query: impl AsRef<str>,
        options: QueryOptions,
    ) -> impl Promise<'static, Result<QueryAnswer>> {
        let query = query.as_ref();
        debug!("Transaction submitting query: {}", query);
        self.transaction_stream.query(query, options)
    }

    /// Analyzes a TypeQL query in this transaction,
    /// returning the translated structure & inferred types.
    ///
    /// # Arguments
    ///
    /// * `query` — The TypeQL query to be analyzed
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.analyze(query)
    /// ```
    pub fn analyze(&self, query: impl AsRef<str>) -> impl Promise<'static, Result<AnalyzedQuery>> {
        self.transaction_stream.analyze(query.as_ref())
    }

    /// Retrieves the transaction’s type (READ or WRITE).
    pub fn type_(&self) -> TransactionType {
        self.type_
    }

    /// Registers a callback function which will be executed when this transaction is closed
    /// returns a resolvable promise that must be awaited otherwise the callback may not be registered
    ///
    /// # Arguments
    ///
    /// * `function` — The callback function.
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.on_close(function)
    /// ```
    pub fn on_close(
        &self,
        callback: impl FnOnce(Option<Error>) + Send + Sync + 'static,
    ) -> impl Promise<'_, Result<()>> {
        self.transaction_stream.on_close(callback)
    }

    /// Closes the transaction and returns a resolvable promise
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "transaction.close().resolve()")]
    #[cfg_attr(not(feature = "sync"), doc = "transaction.close().await")]
    /// ```
    pub fn close(&self) -> impl Promise<'_, Result<()>> {
        self.transaction_stream.close()
    }

    /// Commits the changes made via this transaction to the TypeDB database. Whether or not the transaction is commited successfully, it gets closed after the commit call.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "transaction.commit()")]
    #[cfg_attr(not(feature = "sync"), doc = "transaction.commit().await")]
    /// ```
    pub fn commit(self) -> impl Promise<'static, Result> {
        let stream = self.transaction_stream;
        stream.commit()
    }

    /// Rolls back the uncommitted changes made via this transaction.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "transaction.rollback()")]
    #[cfg_attr(not(feature = "sync"), doc = "transaction.rollback().await")]
    /// ```
    pub fn rollback(&self) -> impl Promise<'_, Result> {
        self.transaction_stream.rollback()
    }
}

impl fmt::Debug for Transaction {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("Transaction").field("type_", &self.type_).field("options", &self.options).finish()
    }
}
