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

use std::time::Duration;

/// TypeDB session and transaction options.
/// `TypeDBOptions` object can be used to override the default server behaviour query processing.
/// Options are specified using properties assignment.
///
/// # Examples
///
/// ```rust
/// let options = Options::new().infer(true).explain(true);
/// ```
#[derive(Clone, Debug, Default)]
pub struct Options {
    /// If set to `True`, enables inference for queries. Only settable at transaction level and above. Only affects read transactions.
    pub infer: Option<bool>,
    /// If set to `True`, reasoning tracing graphs are output in the logging directory. Should be used with `parallel = False`.
    pub trace_inference: Option<bool>,
    /// If set to `True`, enables explanations for queries. Only affects read transactions.
    pub explain: Option<bool>,
    /// If set to `True`, the server uses parallel instead of single-threaded execution.
    pub parallel: Option<bool>,
    /// If set to `True`, the first batch of answers is streamed to the driver even without an explicit request for it.
    pub prefetch: Option<bool>,
    /// If set, specifies a guideline number of answers that the server should send before the driver issues a fresh request.
    pub prefetch_size: Option<i32>,
    /// If set, specifies a timeout that allows the server to close sessions if the driver terminates or becomes unresponsive.
    pub session_idle_timeout: Option<Duration>,
    /// If set, specifies a timeout for killing transactions automatically, preventing memory leaks in unclosed transactions.
    pub transaction_timeout: Option<Duration>,
    /// If set, specifies how long the driver should wait if opening a session or transaction is blocked by a schema write lock.
    pub schema_lock_acquire_timeout: Option<Duration>,
    /// If set to `True`, enables reading data from any replica, potentially boosting read throughput. Only settable in TypeDB Enterprise.
    pub read_any_replica: Option<bool>,
}

impl Options {
    pub fn new() -> Self {
        Self::default()
    }

    /// If set to `True`, enables inference for queries. Only settable at transaction level and above. Only affects read transactions.
    pub fn infer(self, infer: bool) -> Self {
        Self { infer: Some(infer), ..self }
    }

    /// If set to `True`, reasoning tracing graphs are output in the logging directory. Should be used with `parallel = False`.
    pub fn trace_inference(self, trace_inference: bool) -> Self {
        Self { trace_inference: Some(trace_inference), ..self }
    }

    /// If set to `True`, enables explanations for queries. Only affects read transactions.
    pub fn explain(self, explain: bool) -> Self {
        Self { explain: Some(explain), ..self }
    }

    /// If set to `True`, the server uses parallel instead of single-threaded execution.
    pub fn parallel(self, parallel: bool) -> Self {
        Self { parallel: Some(parallel), ..self }
    }

    /// If set to `True`, the first batch of answers is streamed to the driver even without an explicit request for it.
    pub fn prefetch(self, prefetch: bool) -> Self {
        Self { prefetch: Some(prefetch), ..self }
    }

    /// If set, specifies a guideline number of answers that the server should send before the driver issues a fresh request.
    pub fn prefetch_size(self, prefetch_size: i32) -> Self {
        Self { prefetch_size: Some(prefetch_size), ..self }
    }

    /// If set, specifies a timeout that allows the server to close sessions if the driver terminates or becomes unresponsive.
    pub fn session_idle_timeout(self, timeout: Duration) -> Self {
        Self { session_idle_timeout: Some(timeout), ..self }
    }

    /// If set, specifies a timeout for killing transactions automatically, preventing memory leaks in unclosed transactions.
    pub fn transaction_timeout(self, timeout: Duration) -> Self {
        Self { transaction_timeout: Some(timeout), ..self }
    }

    /// If set, specifies how long the driver should wait if opening a session or transaction is blocked by a schema write lock.
    pub fn schema_lock_acquire_timeout(self, timeout: Duration) -> Self {
        Self { schema_lock_acquire_timeout: Some(timeout), ..self }
    }

    /// If set to `True`, enables reading data from any replica, potentially boosting read throughput. Only settable in TypeDB Enterprise.
    pub fn read_any_replica(self, read_any_replica: bool) -> Self {
        Self { read_any_replica: Some(read_any_replica), ..self }
    }
}
