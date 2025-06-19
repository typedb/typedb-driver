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

use typedb_protocol::{
    server::{version::Res as VersionProto, ReplicationStatus as ReplicationStatusProto},
    Server as ServerProto,
};

use super::TryFromProto;
use crate::{
    common::Result,
    connection::{
        server::server_replica::{ReplicaType, ReplicationStatus, ServerReplica},
        ServerVersion,
    },
    error::ConnectionError,
};

impl TryFromProto<ServerProto> for ServerReplica {
    fn try_from_proto(proto: ServerProto) -> Result<Self> {
        let address = match proto.address {
            Some(address) => address.address.parse()?,
            None => return Err(ConnectionError::MissingResponseField { field: "address" }.into()),
        };
        let replication_status = match proto.replication_status {
            Some(replication_status) => ReplicationStatus::try_from_proto(replication_status)?,
            None => ReplicationStatus::default(),
        };
        Ok(Self::from_private(address, replication_status.replica_type, replication_status.term))
    }
}

impl TryFromProto<ReplicationStatusProto> for ReplicationStatus {
    fn try_from_proto(proto: ReplicationStatusProto) -> Result<Self> {
        Ok(Self { replica_type: ReplicaType::try_from_proto(proto.replica_type)?, term: proto.term })
    }
}

impl TryFromProto<i32> for ReplicaType {
    fn try_from_proto(replica_type: i32) -> Result<Self> {
        match replica_type {
            0 => Ok(Self::Primary),
            1 => Ok(Self::Secondary),
            _ => Err(ConnectionError::UnexpectedReplicaType { replica_type }.into()),
        }
    }
}

impl TryFromProto<VersionProto> for ServerVersion {
    fn try_from_proto(proto: VersionProto) -> Result<Self> {
        Ok(Self { distribution: proto.distribution, version: proto.version })
    }
}
