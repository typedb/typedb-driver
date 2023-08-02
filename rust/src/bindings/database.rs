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

use std::{ffi::c_char, ptr::addr_of_mut};

use super::{
    error::{try_release_string, unwrap_void},
    iterator::{iterator_next, CIterator},
    memory::{borrow, borrow_mut, free, release, release_optional, release_string, take_ownership},
};
use crate::{
    common::{box_stream, info::ReplicaInfo},
    Database,
};

#[no_mangle]
pub extern "C" fn database_drop(database: *mut Database) {
    free(database);
}

#[no_mangle]
pub extern "C" fn database_get_name(database: *const Database) -> *mut c_char {
    release_string(borrow(database).name().to_owned())
}

#[no_mangle]
pub extern "C" fn database_delete(database: *mut Database) {
    unwrap_void(take_ownership(database).delete());
}

#[no_mangle]
pub extern "C" fn database_schema(database: *mut Database) -> *mut c_char {
    try_release_string(borrow_mut(database).schema())
}

#[no_mangle]
pub extern "C" fn database_type_schema(database: *mut Database) -> *mut c_char {
    try_release_string(borrow_mut(database).type_schema())
}

#[no_mangle]
pub extern "C" fn database_rule_schema(database: *mut Database) -> *mut c_char {
    try_release_string(borrow_mut(database).rule_schema())
}

pub struct ReplicaInfoIterator(CIterator<ReplicaInfo>);

#[no_mangle]
pub extern "C" fn replica_info_iterator_next(it: *mut ReplicaInfoIterator) -> *mut ReplicaInfo {
    unsafe { iterator_next(addr_of_mut!((*it).0)) }
}

#[no_mangle]
pub extern "C" fn replica_info_iterator_drop(it: *mut ReplicaInfoIterator) {
    free(it);
}

#[no_mangle]
pub extern "C" fn database_get_replicas_info(database: *const Database) -> *mut ReplicaInfoIterator {
    release(ReplicaInfoIterator(CIterator(box_stream(borrow(database).replicas_info().into_iter()))))
}

#[no_mangle]
pub extern "C" fn database_get_primary_replica_info(database: *const Database) -> *mut ReplicaInfo {
    release_optional(borrow(database).primary_replica_info())
}

#[no_mangle]
pub extern "C" fn database_get_preferred_replica_info(database: *const Database) -> *mut ReplicaInfo {
    release_optional(borrow(database).preferred_replica_info())
}

#[no_mangle]
pub extern "C" fn replica_info_drop(replica_info: *mut ReplicaInfo) {
    free(replica_info);
}

#[no_mangle]
pub extern "C" fn replica_info_get_address(replica_info: *const ReplicaInfo) -> *mut c_char {
    release_string(borrow(replica_info).address.to_string())
}

#[no_mangle]
pub extern "C" fn replica_info_is_primary(replica_info: *const ReplicaInfo) -> bool {
    borrow(replica_info).is_primary
}

#[no_mangle]
pub extern "C" fn replica_info_is_preferred(replica_info: *const ReplicaInfo) -> bool {
    borrow(replica_info).is_preferred
}

#[no_mangle]
pub extern "C" fn replica_info_get_term(replica_info: *const ReplicaInfo) -> i64 {
    borrow(replica_info).term
}
