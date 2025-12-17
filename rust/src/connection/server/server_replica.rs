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
use crate::common::address::{address_translation::AddressTranslation, Address};

pub const DEFAULT_REPLICA_ID: u64 = 0;

pub trait Replica: Clone {
    /// Returns the id of this replica. 0 (default) if it's not a part of a cluster.
    fn id(&self) -> u64;

    /// Returns whether this is the primary replica of the raft cluster or any of the supporting roles.
    fn role(&self) -> Option<ReplicaRole>;

    /// Checks whether this is the primary replica of the raft cluster.
    fn is_primary(&self) -> bool;

    /// Returns the raft protocol ‘term’ of this replica.
    fn term(&self) -> Option<u64>;
}

/// The metadata and state of an individual raft replica of a driver connection.
#[derive(Debug, Clone, Eq, PartialEq, Hash)]
pub enum ServerReplica {
    Available(AvailableServerReplica),
    Unavailable { replica_status: Option<ReplicaStatus> },
}

impl ServerReplica {
    pub(crate) fn available_from_private(private_address: Address, replica_status: Option<ReplicaStatus>) -> Self {
        Self::Available(AvailableServerReplica::from_private(private_address, replica_status))
    }

    pub(crate) fn translate_address(&mut self, address_translation: &AddressTranslation) {
        match self {
            ServerReplica::Available(available) => available.translate_address(address_translation),
            ServerReplica::Unavailable { .. } => {}
        }
    }

    pub(crate) fn translated(mut self, address_translation: &AddressTranslation) -> Self {
        self.translate_address(address_translation);
        self
    }

    pub(crate) fn replica_status(&self) -> &Option<ReplicaStatus> {
        match self {
            ServerReplica::Available(available_replica) => available_replica.replica_status(),
            ServerReplica::Unavailable { replica_status } => replica_status,
        }
    }

    /// Returns the address this replica is hosted at. None if the information is unavailable.
    pub fn address(&self) -> Option<&Address> {
        match self {
            ServerReplica::Available(available_replica) => Some(available_replica.address()),
            ServerReplica::Unavailable { .. } => None,
        }
    }
}

impl Replica for ServerReplica {
    /// Returns the id of this replica. 0 (default) if it's not a part of a cluster.
    fn id(&self) -> u64 {
        self.replica_status().map(|status| status.id).unwrap_or(DEFAULT_REPLICA_ID)
    }

    /// Returns whether this is the primary replica of the raft cluster or any of the supporting roles.
    fn role(&self) -> Option<ReplicaRole> {
        self.replica_status().map(|status| status.role).flatten()
    }

    /// Checks whether this is the primary replica of the raft cluster.
    fn is_primary(&self) -> bool {
        matches!(self.role(), Some(ReplicaRole::Primary))
    }

    /// Returns the raft protocol ‘term’ of this replica.
    fn term(&self) -> Option<u64> {
        self.replica_status().map(|status| status.term).flatten()
    }
}

/// A specialization of an available `ServerReplica` with a known connection address.
#[derive(Debug, Clone, Eq, PartialEq, Hash)]
pub struct AvailableServerReplica {
    private_address: Address,
    public_address: Option<Address>,
    replica_status: Option<ReplicaStatus>,
}

impl AvailableServerReplica {
    pub(crate) fn from_private(private_address: Address, replica_status: Option<ReplicaStatus>) -> Self {
        Self { private_address, public_address: None, replica_status }
    }

    pub(crate) fn translate_address(&mut self, address_translation: &AddressTranslation) {
        if let Some(translated) = address_translation.to_public(&self.private_address) {
            self.public_address = Some(translated);
        }
    }

    pub(crate) fn translated(mut self, address_translation: &AddressTranslation) -> Self {
        self.translate_address(address_translation);
        self
    }

    pub(crate) fn private_address(&self) -> &Address {
        &self.private_address
    }

    pub(crate) fn replica_status(&self) -> &Option<ReplicaStatus> {
        &self.replica_status
    }

    /// Returns the address this replica is hosted at.
    pub fn address(&self) -> &Address {
        self.public_address.as_ref().unwrap_or_else(|| &self.private_address)
    }
}

impl Replica for AvailableServerReplica {
    /// Returns the id of this replica. 0 (default) if it's not a part of a cluster.
    fn id(&self) -> u64 {
        self.replica_status().map(|status| status.id).unwrap_or(DEFAULT_REPLICA_ID)
    }

    /// Returns whether this is the primary replica of the raft cluster or any of the supporting roles.
    fn role(&self) -> Option<ReplicaRole> {
        self.replica_status().map(|status| status.role).flatten()
    }

    /// Checks whether this is the primary replica of the raft cluster.
    fn is_primary(&self) -> bool {
        matches!(self.role(), Some(ReplicaRole::Primary))
    }

    /// Returns the raft protocol ‘term’ of this replica.
    fn term(&self) -> Option<u64> {
        self.replica_status().map(|status| status.term).flatten()
    }
}

/// The metadata and state of an individual server as a raft replica.
#[derive(Debug, PartialEq, Eq, Hash, Clone, Copy)]
pub struct ReplicaStatus {
    /// The id of this replica.
    pub(crate) id: u64,
    /// The role of this replica in the raft cluster. May be unknown when the replica is unavailable.
    pub(crate) role: Option<ReplicaRole>,
    /// The raft protocol ‘term’ of this server replica. May be unknown when the replica is unavailable.
    pub(crate) term: Option<u64>,
}

impl Default for ReplicaStatus {
    fn default() -> Self {
        Self { id: DEFAULT_REPLICA_ID, role: None, term: None }
    }
}

/// This enum is used to specify the type of replica.
#[repr(C)]
#[derive(Debug, PartialEq, Eq, Hash, Clone, Copy)]
pub enum ReplicaRole {
    Primary,
    Candidate,
    Secondary,
}
