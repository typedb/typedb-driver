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

use std::time::Duration;

use crate::consistency_level::ConsistencyLevel;

/// TypeDB transaction options.
/// `TransactionOptions` object can be used to override the default behaviour for opened
/// transactions.
///
/// # Examples
///
/// ```rust
/// let options = TransactionOptions::new().transaction_timeout(Duration::from_secs(60));
/// ```
#[derive(Clone, Debug, Default)]
pub struct TransactionOptions {
    /// If set, specifies a timeout for killing transactions automatically, preventing memory leaks in unclosed transactions.
    pub transaction_timeout: Option<Duration>,
    /// If set, specifies how long the driver should wait if opening a transaction is blocked by an exclusive schema write lock.
    pub schema_lock_acquire_timeout: Option<Duration>,
    /// If set, specifies the requested consistency level of the transaction opening operation.
    /// Affects only read transactions, as write and schema transactions require primary replicas.
    pub read_consistency_level: Option<ConsistencyLevel>,
}

impl TransactionOptions {
    pub fn new() -> Self {
        Self::default()
    }

    /// If set, specifies a timeout for killing transactions automatically, preventing memory leaks in unclosed transactions.
    pub fn transaction_timeout(self, timeout: Duration) -> Self {
        Self { transaction_timeout: Some(timeout), ..self }
    }

    /// If set, specifies how long the driver should wait if opening a transaction is blocked by an exclusive schema write lock.
    pub fn schema_lock_acquire_timeout(self, timeout: Duration) -> Self {
        Self { schema_lock_acquire_timeout: Some(timeout), ..self }
    }

    /// If set, specifies the requested consistency level of the transaction opening operation.
    /// Affects only read transactions, as write and schema transactions require primary replicas.
    pub fn consistency_level(self, consistency_level: ConsistencyLevel) -> Self {
        Self { read_consistency_level: Some(consistency_level), ..self }
    }
}
