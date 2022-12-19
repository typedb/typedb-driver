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

use typedb_protocol::{
    options::{
        ExplainOpt::Explain, InferOpt::Infer, ParallelOpt::Parallel, PrefetchOpt::Prefetch,
        PrefetchSizeOpt::PrefetchSize, ReadAnyReplicaOpt::ReadAnyReplica,
        SchemaLockAcquireTimeoutOpt::SchemaLockAcquireTimeoutMillis,
        SessionIdleTimeoutOpt::SessionIdleTimeoutMillis, TraceInferenceOpt::TraceInference,
        TransactionTimeoutOpt::TransactionTimeoutMillis,
    },
    Options as OptionsProto,
};

macro_rules! options {
    {pub struct $name:ident { $(pub $field_name:ident : Option<$field_type:ty>),* $(,)? }} => {
        #[derive(Clone, Debug, Default)]
        pub struct $name {
            $(pub $field_name: Option<$field_type>,)*
        }

        impl $name {
            $(
            pub fn $field_name(mut self, value: $field_type) -> Self {
                self.$field_name = value.into();
                self
            }
            )*
        }
    };
}

options! {
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
    }
}

options! {
    pub struct ClusterOptions {
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
}

impl Options {
    pub fn new_core() -> Options {
        Options::default()
    }

    pub fn new_cluster() -> ClusterOptions {
        ClusterOptions::default()
    }

    pub(crate) fn to_proto(&self) -> OptionsProto {
        OptionsProto {
            infer_opt: self.infer.map(Infer),
            trace_inference_opt: self.trace_inference.map(TraceInference),
            explain_opt: self.explain.map(Explain),
            parallel_opt: self.parallel.map(Parallel),
            prefetch_size_opt: self.prefetch_size.map(PrefetchSize),
            prefetch_opt: self.prefetch.map(Prefetch),
            session_idle_timeout_opt: self
                .session_idle_timeout
                .map(|val| SessionIdleTimeoutMillis(val.as_millis() as i32)),
            transaction_timeout_opt: self
                .transaction_timeout
                .map(|val| TransactionTimeoutMillis(val.as_millis() as i32)),
            schema_lock_acquire_timeout_opt: self
                .schema_lock_acquire_timeout
                .map(|val| SchemaLockAcquireTimeoutMillis(val.as_millis() as i32)),
            read_any_replica_opt: None,
        }
    }
}

impl ClusterOptions {
    pub(crate) fn to_proto(&self) -> OptionsProto {
        OptionsProto {
            infer_opt: self.infer.map(Infer),
            trace_inference_opt: self.trace_inference.map(TraceInference),
            explain_opt: self.explain.map(Explain),
            parallel_opt: self.parallel.map(Parallel),
            prefetch_size_opt: self.prefetch_size.map(PrefetchSize),
            prefetch_opt: self.prefetch.map(Prefetch),
            session_idle_timeout_opt: self
                .session_idle_timeout
                .map(|val| SessionIdleTimeoutMillis(val.as_millis() as i32)),
            transaction_timeout_opt: self
                .transaction_timeout
                .map(|val| TransactionTimeoutMillis(val.as_millis() as i32)),
            schema_lock_acquire_timeout_opt: self
                .schema_lock_acquire_timeout
                .map(|val| SchemaLockAcquireTimeoutMillis(val.as_millis() as i32)),
            read_any_replica_opt: self.read_any_replica.map(ReadAnyReplica),
        }
    }
}
