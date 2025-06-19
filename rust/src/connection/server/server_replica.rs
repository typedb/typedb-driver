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
use std::collections::HashMap;

use crate::common::address::Address;

/// The metadata and state of an individual raft replica of a driver connection.
#[derive(Debug, Clone, Eq, PartialEq, Hash)]
pub struct ServerReplica {
    private_address: Address,
    public_address: Option<Address>,
    replica_type: ReplicaType,
    term: i64,
}

impl ServerReplica {
    pub(crate) fn from_private(private_address: Address, replica_type: ReplicaType, term: i64) -> Self {
        Self { private_address, public_address: None, replica_type, term }
    }

    pub(crate) fn translate_address(&mut self, address_translation: &HashMap<Address, Address>) -> bool {
        if let Some((public, _)) = address_translation.iter().find(|(_, private)| private == &self.address()) {
            self.private_address = public.clone();
            true
        } else {
            false
        }
    }

    pub(crate) fn translated(mut self, address_translation: &HashMap<Address, Address>) -> Self {
        self.translate_address(address_translation);
        self
    }

    pub(crate) fn private_address(&self) -> &Address {
        &self.private_address
    }

    /// The address this replica is hosted at.
    pub fn address(&self) -> &Address {
        self.public_address.as_ref().unwrap_or(&self.private_address)
    }

    /// Whether this is the primary replica of the raft cluster or any of the supporting types.
    pub fn replica_type(&self) -> ReplicaType {
        self.replica_type
    }

    pub fn is_primary(&self) -> bool {
        matches!(self.replica_type, ReplicaType::Primary)
    }

    /// The raft protocol ‘term’ of this replica.
    pub fn term(&self) -> i64 {
        self.term
    }
}

/// The metadata and state of an individual server as a raft replica.
#[derive(Debug, PartialEq, Eq, Hash)]
pub struct ReplicationStatus {
    /// The role of this replica in the raft cluster.
    pub replica_type: ReplicaType,
    /// The raft protocol ‘term’ of this server replica.
    pub term: i64,
}

impl Default for ReplicationStatus {
    fn default() -> Self {
        Self { replica_type: ReplicaType::Primary, term: 0 }
    }
}

#[derive(Debug, PartialEq, Eq, Hash, Clone, Copy)]
pub(crate) enum ReplicaType {
    Primary,
    Secondary,
}
