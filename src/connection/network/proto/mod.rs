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

mod common;
mod concept;
mod database;
mod logic;
mod message;
mod user;

use crate::Result;

pub(super) trait IntoProto<Proto> {
    fn into_proto(self) -> Proto;
}

pub(super) trait TryIntoProto<Proto> {
    fn try_into_proto(self) -> Result<Proto>;
}

pub(super) trait FromProto<Proto> {
    fn from_proto(proto: Proto) -> Self;
}

pub(super) trait TryFromProto<Proto>: Sized {
    fn try_from_proto(proto: Proto) -> Result<Self>;
}
