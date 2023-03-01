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

use std::time::Duration;

use super::{address::Address, SessionID};

#[derive(Clone, Debug)]
pub(crate) struct SessionInfo {
    pub(crate) address: Address,
    pub(crate) session_id: SessionID,
    pub(crate) network_latency: Duration,
}

#[derive(Debug)]
pub(crate) struct DatabaseInfo {
    pub(crate) name: String,
    pub(crate) replicas: Vec<ReplicaInfo>,
}

#[derive(Debug)]
pub(crate) struct ReplicaInfo {
    pub(crate) address: Address,
    pub(crate) is_primary: bool,
    pub(crate) is_preferred: bool,
    pub(crate) term: i64,
}
