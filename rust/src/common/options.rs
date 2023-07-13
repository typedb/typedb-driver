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

#[derive(Clone, Debug, Default)]
pub struct Options {
    pub infer: Option<bool>,
    pub trace_inference: Option<bool>,
    pub explain: Option<bool>,
    pub parallel: Option<bool>,
    pub prefetch: Option<bool>,
    pub prefetch_size: Option<i32>,
    pub session_idle_timeout: Option<Duration>,
    pub transaction_timeout: Option<Duration>,
    pub schema_lock_acquire_timeout: Option<Duration>,
    pub read_any_replica: Option<bool>,
}

impl Options {
    pub fn new() -> Self {
        Self::default()
    }

    pub fn infer(self, infer: bool) -> Self {
        Self { infer: Some(infer), ..self }
    }

    pub fn trace_inference(self, trace_inference: bool) -> Self {
        Self { trace_inference: Some(trace_inference), ..self }
    }

    pub fn explain(self, explain: bool) -> Self {
        Self { explain: Some(explain), ..self }
    }

    pub fn parallel(self, parallel: bool) -> Self {
        Self { parallel: Some(parallel), ..self }
    }

    pub fn prefetch(self, prefetch: bool) -> Self {
        Self { prefetch: Some(prefetch), ..self }
    }

    pub fn prefetch_size(self, prefetch_size: i32) -> Self {
        Self { prefetch_size: Some(prefetch_size), ..self }
    }

    pub fn session_idle_timeout(self, timeout: Duration) -> Self {
        Self { session_idle_timeout: Some(timeout), ..self }
    }

    pub fn transaction_timeout(self, timeout: Duration) -> Self {
        Self { transaction_timeout: Some(timeout), ..self }
    }

    pub fn schema_lock_acquire_timeout(self, timeout: Duration) -> Self {
        Self { schema_lock_acquire_timeout: Some(timeout), ..self }
    }

    pub fn read_any_replica(self, read_any_replica: bool) -> Self {
        Self { read_any_replica: Some(read_any_replica), ..self }
    }
}
