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

/// Server routing directive for operations against a distributed server. All driver methods have
/// default recommended values, however, some operations can be configured in order to
/// target a specific server in the cluster. This setting does not affect clusters with a single node.
#[derive(Clone, Debug, PartialEq, Eq)]
pub enum ServerRouting {
    /// Automatic server routing. Driver automatically selects the server (primary in clusters).
    Auto,

    /// Route to a specific known server at the given address. Mostly used for debugging purposes.
    Server { address: Address },
}

impl fmt::Display for ServerRouting {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            ServerRouting::Auto => write!(f, "Auto"),
            ServerRouting::Server { address } => write!(f, "Server({address})"),
        }
    }
}
