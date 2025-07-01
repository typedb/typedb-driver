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
use std::{
    collections::HashMap,
    net::{IpAddr, Ipv4Addr, SocketAddr},
};

use crate::common::address::Address;

/// The metadata and state of an individual raft replica of a driver connection.
#[derive(Debug, Clone, Eq, PartialEq, Hash)]
pub struct ServerReplica {
    private_address: Address,
    public_address: Option<Address>,
    replica_status: ReplicaStatus,
}

impl ServerReplica {
    pub(crate) fn from_private(private_address: Address, replica_status: ReplicaStatus) -> Self {
        Self { private_address, public_address: None, replica_status }
    }

    pub(crate) fn translate_address(
        &mut self,
        connection_scheme: &http::uri::Scheme,
        address_translation: &HashMap<Address, Address>,
    ) {
        if let Some(translated) = address_translation
            .iter()
            .find(|(_, private)| private == &self.private_address())
            .map(|(public, _)| public.clone())
        {
            self.public_address = Some(translated);
        } else if let Some(scheme) = self.address().uri_scheme() {
            if scheme != connection_scheme {
                self.public_address = Some(self.address().with_scheme(connection_scheme.clone()));
            }
        }
    }

    pub(crate) fn translated(
        mut self,
        connection_scheme: &http::uri::Scheme,
        address_translation: &HashMap<Address, Address>,
    ) -> Self {
        self.translate_address(connection_scheme, address_translation);
        self
    }

    pub(crate) fn private_address(&self) -> &Address {
        &self.private_address
    }

    /// Returns the address this replica is hosted at.
    pub fn address(&self) -> &Address {
        self.public_address.as_ref().unwrap_or(&self.private_address)
    }

    /// Returns whether this is the primary replica of the raft cluster or any of the supporting types.
    pub fn replica_type(&self) -> ReplicaType {
        self.replica_status.replica_type
    }

    /// Checks whether this is the primary replica of the raft cluster.
    pub fn is_primary(&self) -> bool {
        matches!(self.replica_type(), ReplicaType::Primary)
    }

    /// Returns the raft protocol ‘term’ of this replica.
    pub fn term(&self) -> i64 {
        self.replica_status.term
    }
}

/// The metadata and state of an individual server as a raft replica.
#[derive(Debug, PartialEq, Eq, Hash, Clone, Copy)]
pub(crate) struct ReplicaStatus {
    /// The role of this replica in the raft cluster.
    pub replica_type: ReplicaType,
    /// The raft protocol ‘term’ of this server replica.
    pub term: i64,
}

impl Default for ReplicaStatus {
    fn default() -> Self {
        Self { replica_type: ReplicaType::Primary, term: 0 }
    }
}

/// This enum is used to specify the type of replica.
#[repr(C)]
#[derive(Debug, PartialEq, Eq, Hash, Clone, Copy)]
pub enum ReplicaType {
    Primary,
    Secondary,
}
