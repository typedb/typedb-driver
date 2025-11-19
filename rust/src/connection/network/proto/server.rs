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
    server::{version::Res as VersionProto, ReplicaStatus as ReplicaStatusProto},
    Server as ServerProto,
};

use super::TryFromProto;
use crate::{
    common::Result,
    connection::{
        server_replica::{ReplicaRole, ReplicaStatus, ServerReplica},
        server_version::ServerVersion,
    },
    error::ConnectionError,
};

impl TryFromProto<ServerProto> for ServerReplica {
    fn try_from_proto(proto: ServerProto) -> Result<ServerReplica> {
        let replica_status = proto.replica_status.map(|status| ReplicaStatus::try_from_proto(status)).transpose()?;
        match proto.address {
            Some(address) => Ok(ServerReplica::available_from_private(address.parse()?, replica_status)),
            None => Ok(ServerReplica::Unavailable { replica_status }),
        }
    }
}

impl TryFromProto<ReplicaStatusProto> for ReplicaStatus {
    fn try_from_proto(proto: ReplicaStatusProto) -> Result<Self> {
        Ok(Self {
            id: proto.replica_id,
            role: Option::<ReplicaRole>::try_from_proto(proto.replica_role)?,
            term: proto.term,
        })
    }
}

impl TryFromProto<Option<i32>> for Option<ReplicaRole> {
    fn try_from_proto(replica_role: Option<i32>) -> Result<Option<ReplicaRole>> {
        let Some(replica_role) = replica_role else {
            return Ok(None);
        };
        match replica_role {
            0 => Ok(Some(ReplicaRole::Primary)),
            1 => Ok(Some(ReplicaRole::Candidate)),
            2 => Ok(Some(ReplicaRole::Secondary)),
            _ => Err(ConnectionError::UnexpectedReplicaRole { replica_role }.into()),
        }
    }
}

impl TryFromProto<VersionProto> for ServerVersion {
    fn try_from_proto(proto: VersionProto) -> Result<Self> {
        Ok(Self { distribution: proto.distribution, version: proto.version })
    }
}
