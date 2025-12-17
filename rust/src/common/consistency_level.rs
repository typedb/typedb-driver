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
use std::fmt;

use crate::common::address::Address;

/// Consistency levels of operations against a distributed server. All driver methods have default
/// recommended values, however, readonly operations can be configured in order to potentially
/// speed up the execution (introducing risks of stale data) or test a specific replica.
/// This setting does not affect clusters with a single node.
#[derive(Clone, Debug, PartialEq, Eq)]
pub enum ConsistencyLevel {
    /// Strongest consistency, always up-to-date due to the guarantee of the primary replica usage.
    /// May require more time for operation execution.
    Strong,

    /// Allow stale reads from any replica. May not reflect latest writes. The execution may be
    /// eventually faster compared to other consistency levels.
    Eventual,

    /// The operation is executed against the provided replica address only. Its guarantees depend
    /// on the replica selected.
    ReplicaDependent { address: Address },
}

impl fmt::Display for ConsistencyLevel {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            ConsistencyLevel::Strong => write!(f, "Strong"),
            ConsistencyLevel::Eventual => write!(f, "Eventual"),
            ConsistencyLevel::ReplicaDependent { address } => write!(f, "ReplicaDependent({address})"),
        }
    }
}
