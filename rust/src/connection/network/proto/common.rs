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

use typedb_protocol::{transaction, Options as OptionsProto};

use super::{IntoProto, TryFromProto};
use crate::{answer::QueryType, error::ConnectionError, Options, Result, TransactionType};

impl IntoProto<i32> for TransactionType {
    fn into_proto(self) -> i32 {
        match self {
            Self::Read => transaction::Type::Read.into(),
            Self::Write => transaction::Type::Write.into(),
            Self::Schema => transaction::Type::Schema.into(),
        }
    }
}

impl TryFromProto<i32> for QueryType {
    fn try_from_proto(query_type: i32) -> Result<Self> {
        match query_type {
            0 => Ok(Self::ReadQuery),
            1 => Ok(Self::WriteQuery),
            2 => Ok(Self::SchemaQuery),
            _ => Err(ConnectionError::UnexpectedQueryType { query_type }.into()),
        }
    }
}

impl IntoProto<OptionsProto> for Options {
    fn into_proto(self) -> OptionsProto {
        OptionsProto {
            parallel: self.parallel,
            prefetch_size: self.prefetch_size,
            prefetch: self.prefetch,
            transaction_timeout_millis: self.transaction_timeout.map(|val| val.as_millis() as u64),
            schema_lock_acquire_timeout_millis: self.schema_lock_acquire_timeout.map(|val| val.as_millis() as u64),
            read_any_replica: self.read_any_replica,
        }
    }
}
