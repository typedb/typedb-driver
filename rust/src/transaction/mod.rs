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

pub mod concept;
pub mod logic;
pub mod query;

use std::{fmt, marker::PhantomData, pin::Pin};

use self::{concept::ConceptManager, logic::LogicManager, query::QueryManager};
use crate::{
    common::{Promise, Result, TransactionType},
    connection::TransactionStream,
    error::ConnectionError,
    Options,
};

pub struct Transaction<'a> {
    /// The transaction’s type (READ or WRITE)
    type_: TransactionType,
    /// The options for the transaction
    options: Options,
    transaction_stream: Pin<Box<TransactionStream>>,

    _lifetime_guard: PhantomData<&'a ()>,
}

impl Transaction<'_> {
    pub(super) fn new(transaction_stream: TransactionStream) -> Self {
        let transaction_stream = Box::pin(transaction_stream);
        Transaction {
            type_: transaction_stream.type_(),
            options: transaction_stream.options(),
            transaction_stream,
            _lifetime_guard: PhantomData,
        }
    }

    /// Closes the transaction.
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.close()
    /// ```
    pub fn is_open(&self) -> bool {
        self.transaction_stream.is_open()
    }

    /// Retrieves the transaction’s type (READ or WRITE).
    pub fn type_(&self) -> TransactionType {
        self.type_
    }

    /// Retrieves the``QueryManager`` for this Transaction, from which any TypeQL query can be executed.
    pub fn query(&self) -> QueryManager<'_> {
        QueryManager::new(self.transaction_stream.as_ref())
    }

    /// The `ConceptManager` for this transaction, providing access to all Concept API methods.
    pub fn concept(&self) -> ConceptManager<'_> {
        ConceptManager::new(self.transaction_stream.as_ref())
    }

    /// Retrieves the `LogicManager` for this Transaction, providing access to all Concept API - Logic methods.
    pub fn logic(&self) -> LogicManager<'_> {
        LogicManager::new(self.transaction_stream.as_ref())
    }

    /// Registers a callback function which will be executed when this transaction is closed.
    ///
    /// # Arguments
    ///
    /// * `function` -- The callback function.
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.on_close(function)
    /// ```
    pub fn on_close(&self, callback: impl FnOnce(ConnectionError) + Send + Sync + 'static) {
        self.transaction_stream.on_close(callback)
    }

    /// Closes the transaction.
    ///
    /// # Examples
    ///
    /// ```rust
    /// transaction.force_close()
    /// ```
    pub fn force_close(&self) {
        self.transaction_stream.force_close();
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

impl fmt::Debug for Transaction<'_> {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("Transaction").field("type_", &self.type_).field("options", &self.options).finish()
    }
}
