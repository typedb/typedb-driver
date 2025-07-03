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

use std::{ffi::c_char, ptr::addr_of_mut};

use typedb_driver::{ReplicaType, ServerReplica};

use crate::common::{
    iterator::{iterator_next, CIterator},
    memory::{borrow, free, release_string},
};

/// Iterator over the <code>ServerReplica</code> corresponding to each replica of a TypeDB cluster.
pub struct ServerReplicaIterator(pub(crate) CIterator<ServerReplica>);

/// Forwards the <code>ServerReplicaIterator</code> and returns the next <code>ServerReplica</code> if it exists,
/// or null if there are no more elements.
#[no_mangle]
pub extern "C" fn server_replica_iterator_next(it: *mut ServerReplicaIterator) -> *mut ServerReplica {
    unsafe { iterator_next(addr_of_mut!((*it).0)) }
}

/// Frees the native rust <code>ServerReplicaIterator</code> object.
#[no_mangle]
pub extern "C" fn server_replica_iterator_drop(it: *mut ServerReplicaIterator) {
    free(it);
}

/// Frees the native rust <code>ServerReplica</code> object.
#[no_mangle]
pub extern "C" fn server_replica_drop(replica_info: *mut ServerReplica) {
    free(replica_info);
}

/// Returns the id of this replica.
#[no_mangle]
pub extern "C" fn server_replica_id(replica_info: *const ServerReplica) -> u64 {
    borrow(replica_info).id()
}

/// Returns the address this replica is hosted at.
#[no_mangle]
pub extern "C" fn server_replica_address(replica_info: *const ServerReplica) -> *mut c_char {
    release_string(borrow(replica_info).address().to_string())
}

/// Returns whether this is the primary replica of the raft cluster or any of the supporting types.
#[no_mangle]
pub extern "C" fn server_replica_type(replica_info: *const ServerReplica) -> ReplicaType {
    borrow(replica_info).replica_type()
}

/// Checks whether this is the primary replica of the raft cluster.
#[no_mangle]
pub extern "C" fn server_replica_is_primary(replica_info: *const ServerReplica) -> bool {
    borrow(replica_info).is_primary()
}

/// Returns the raft protocol ‘term’ of this replica.
#[no_mangle]
pub extern "C" fn server_replica_term(replica_info: *const ServerReplica) -> i64 {
    borrow(replica_info).term()
}
