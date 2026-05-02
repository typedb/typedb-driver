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
    Server as ServerProto,
    server::{ReplicationStatus as ReplicationStatusProto, version::Res as VersionProto},
};

use super::TryFromProto;
use crate::{
    common::Result,
    connection::server::{ReplicationRole, ReplicationStatus, Server, server_version::ServerVersion},
    error::ConnectionError,
};

impl TryFromProto<ServerProto> for Server {
    fn try_from_proto(proto: ServerProto) -> Result<Server> {
        let replication_status =
            proto.replication_status.map(|status| ReplicationStatus::try_from_proto(status)).transpose()?;
        match proto.address {
            Some(address) => Ok(Server::available_from_private(address.parse()?, replication_status)),
            None => Ok(Server::Unavailable { replication_status }),
        }
    }
}

impl TryFromProto<ReplicationStatusProto> for ReplicationStatus {
    fn try_from_proto(proto: ReplicationStatusProto) -> Result<Self> {
        Ok(Self { id: proto.id, role: Option::<ReplicationRole>::try_from_proto(proto.role)?, term: proto.term })
    }
}

impl TryFromProto<Option<i32>> for Option<ReplicationRole> {
    fn try_from_proto(replication_role: Option<i32>) -> Result<Option<ReplicationRole>> {
        match replication_role {
            Some(0) => Ok(Some(ReplicationRole::Primary)),
            Some(1) => Ok(Some(ReplicationRole::Candidate)),
            Some(2) => Ok(Some(ReplicationRole::Secondary)),
            Some(replication_role) => Err(ConnectionError::UnexpectedServerReplicationRole { replication_role }.into()),
            None => Ok(None),
        }
    }
}

impl TryFromProto<VersionProto> for ServerVersion {
    fn try_from_proto(proto: VersionProto) -> Result<Self> {
        Ok(Self { distribution: proto.distribution, version: proto.version })
    }
}
