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

pub(crate) mod server_connection;
pub(crate) mod server_manager;
pub mod server_routing;
pub mod server_version;

use crate::common::address::{address_translation::AddressTranslation, Address};

pub const DEFAULT_SERVER_ID: u64 = 0;

pub trait Replica: Clone {
    /// Returns the id of this replica. 0 (default) if it's not a part of a cluster.
    fn id(&self) -> u64;

    /// Returns whether this is the primary replica of the raft cluster or any of the supporting roles.
    fn role(&self) -> Option<ReplicationRole>;

    /// Checks whether this is the primary replica of the raft cluster.
    fn is_primary(&self) -> bool;

    /// Returns the raft protocol 'term' of this replica.
    fn term(&self) -> Option<u64>;
}

/// The metadata and state of an individual server of a driver connection.
#[derive(Debug, Clone, Eq, PartialEq, Hash)]
pub enum Server {
    Available(AvailableServer),
    Unavailable { replication_status: Option<ReplicationStatus> },
}

impl Server {
    pub(crate) fn available_from_private(
        private_address: Address,
        replication_status: Option<ReplicationStatus>,
    ) -> Self {
        Self::Available(AvailableServer::from_private(private_address, replication_status))
    }

    pub(crate) fn translate_address(&mut self, address_translation: &AddressTranslation) {
        match self {
            Server::Available(available) => available.translate_address(address_translation),
            Server::Unavailable { .. } => {}
        }
    }

    pub(crate) fn translated(mut self, address_translation: &AddressTranslation) -> Self {
        self.translate_address(address_translation);
        self
    }

    pub(crate) fn replication_status(&self) -> &Option<ReplicationStatus> {
        match self {
            Server::Available(available_server) => available_server.replication_status(),
            Server::Unavailable { replication_status } => replication_status,
        }
    }

    /// Returns the address this server is hosted at. None if the information is unavailable.
    pub fn address(&self) -> Option<&Address> {
        match self {
            Server::Available(available_server) => Some(available_server.address()),
            Server::Unavailable { .. } => None,
        }
    }
}

impl Replica for Server {
    /// Returns the id of this replica. 0 (default) if it's not a part of a cluster.
    fn id(&self) -> u64 {
        self.replication_status().map(|status| status.id).unwrap_or(DEFAULT_SERVER_ID)
    }

    /// Returns whether this is the primary replica of the raft cluster or any of the supporting roles.
    fn role(&self) -> Option<ReplicationRole> {
        self.replication_status().map(|status| status.role).flatten()
    }

    /// Checks whether this is the primary replica of the raft cluster.
    fn is_primary(&self) -> bool {
        matches!(self.role(), Some(ReplicationRole::Primary))
    }

    /// Returns the raft protocol 'term' of this replica.
    fn term(&self) -> Option<u64> {
        self.replication_status().map(|status| status.term).flatten()
    }
}

/// A specialization of an available `Server` with a known connection address.
#[derive(Debug, Clone, Eq, PartialEq, Hash)]
pub struct AvailableServer {
    private_address: Address,
    public_address: Option<Address>,
    replication_status: Option<ReplicationStatus>,
}

impl AvailableServer {
    pub(crate) fn from_private(private_address: Address, replication_status: Option<ReplicationStatus>) -> Self {
        Self { private_address, public_address: None, replication_status }
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

    pub(crate) fn replication_status(&self) -> &Option<ReplicationStatus> {
        &self.replication_status
    }

    /// Returns the address this server is hosted at.
    pub fn address(&self) -> &Address {
        self.public_address.as_ref().unwrap_or_else(|| &self.private_address)
    }
}

impl Replica for AvailableServer {
    /// Returns the id of this replica. 0 (default) if it's not a part of a cluster.
    fn id(&self) -> u64 {
        self.replication_status().map(|status| status.id).unwrap_or(DEFAULT_SERVER_ID)
    }

    /// Returns whether this is the primary replica of the raft cluster or any of the supporting roles.
    fn role(&self) -> Option<ReplicationRole> {
        self.replication_status().map(|status| status.role).flatten()
    }

    /// Checks whether this is the primary replica of the raft cluster.
    fn is_primary(&self) -> bool {
        matches!(self.role(), Some(ReplicationRole::Primary))
    }

    /// Returns the raft protocol 'term' of this replica.
    fn term(&self) -> Option<u64> {
        self.replication_status().map(|status| status.term).flatten()
    }
}

/// The replication metadata and state of an individual server as a raft replica.
#[derive(Debug, PartialEq, Eq, Hash, Clone, Copy)]
pub struct ReplicationStatus {
    /// The id of this replica.
    pub(crate) id: u64,
    /// The role of this replica in the raft cluster. May be unknown when the server is unavailable.
    pub(crate) role: Option<ReplicationRole>,
    /// The raft protocol 'term' of this server. May be unknown when the server is unavailable.
    pub(crate) term: Option<u64>,
}

impl Default for ReplicationStatus {
    fn default() -> Self {
        Self { id: DEFAULT_SERVER_ID, role: None, term: None }
    }
}

/// This enum is used to specify the replication role of a server.
#[repr(C)]
#[derive(Debug, PartialEq, Eq, Hash, Clone, Copy)]
pub enum ReplicationRole {
    Primary,
    Candidate,
    Secondary,
}
