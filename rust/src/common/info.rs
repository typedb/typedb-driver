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

use tokio::sync::mpsc::UnboundedSender;
use crate::common::address::Address;

use super::{Callback, SessionID};

#[derive(Clone, Debug)]
pub(crate) struct SessionInfo {
    pub(crate) session_id: SessionID,
    pub(crate) network_latency: Duration,
    pub(crate) on_close_register_sink: UnboundedSender<Callback>,
}

#[derive(Debug)]
pub(crate) struct DatabaseInfo {
    pub(crate) name: String,
    pub(crate) replicas: Vec<ReplicaInfo>,
}

/// The metadata and state of an individual raft replica of a database.
#[derive(Debug)]
pub struct ReplicaInfo {
    /// The server hosting this replica
    pub server: Address,
    /// Whether this is the primary replica of the raft cluster.
    pub is_primary: bool,
    /// Whether this is the preferred replica of the raft cluster.
    /// If true, Operations which can be run on any replica will prefer to use this replica.
    pub is_preferred: bool,
    /// The raft protocol ‘term’ of this replica.
    pub term: i64,
}
