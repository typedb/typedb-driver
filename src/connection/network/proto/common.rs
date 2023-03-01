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

use typedb_protocol::{
    options::{
        ExplainOpt::Explain, InferOpt::Infer, ParallelOpt::Parallel, PrefetchOpt::Prefetch,
        PrefetchSizeOpt::PrefetchSize, ReadAnyReplicaOpt::ReadAnyReplica,
        SchemaLockAcquireTimeoutOpt::SchemaLockAcquireTimeoutMillis, SessionIdleTimeoutOpt::SessionIdleTimeoutMillis,
        TraceInferenceOpt::TraceInference, TransactionTimeoutOpt::TransactionTimeoutMillis,
    },
    session, transaction, Options as OptionsProto,
};

use super::IntoProto;
use crate::{Options, SessionType, TransactionType};

impl IntoProto<session::Type> for SessionType {
    fn into_proto(self) -> session::Type {
        match self {
            SessionType::Data => session::Type::Data,
            SessionType::Schema => session::Type::Schema,
        }
    }
}

impl IntoProto<transaction::Type> for TransactionType {
    fn into_proto(self) -> transaction::Type {
        match self {
            TransactionType::Read => transaction::Type::Read,
            TransactionType::Write => transaction::Type::Write,
        }
    }
}

impl IntoProto<OptionsProto> for Options {
    fn into_proto(self) -> OptionsProto {
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
