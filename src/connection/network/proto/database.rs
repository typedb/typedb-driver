/*
 * Copyright (C) 2022 Vaticle
 *
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

use itertools::Itertools;
use typedb_protocol::{database_replicas::Replica as ReplicaProto, DatabaseReplicas as DatabaseProto};

use super::TryFromProto;
use crate::{
    common::info::{DatabaseInfo, ReplicaInfo},
    Result,
};

impl TryFromProto<DatabaseProto> for DatabaseInfo {
    fn try_from_proto(proto: DatabaseProto) -> Result<Self> {
        Ok(Self {
            name: proto.name,
            replicas: proto.replicas.into_iter().map(ReplicaInfo::try_from_proto).try_collect()?,
        })
    }
}

impl TryFromProto<ReplicaProto> for ReplicaInfo {
    fn try_from_proto(proto: ReplicaProto) -> Result<Self> {
        Ok(Self {
            address: proto.address.parse()?,
            is_primary: proto.primary,
            is_preferred: proto.preferred,
            term: proto.term,
        })
    }
}
