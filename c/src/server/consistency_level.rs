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
use std::ffi::c_char;

use typedb_driver::consistency_level::ConsistencyLevel as NativeConsistencyLevel;

use crate::common::{
    error::unwrap_or_default,
    memory::{free, release, release_string, string_free, string_view},
};

/// <code>ConsistencyLevelTag</code> is used to represent consistency levels in FFI.
/// It is the tag part, which is combined with optional fields to form an instance of the original
/// enum.
#[repr(C)]
#[derive(Debug, Clone, Copy)]
pub enum ConsistencyLevelTag {
    Strong,
    Eventual,
    ReplicaDependant,
}

/// <code>ConsistencyLevel</code> is used to represent consistency levels in FFI.
/// It combines <code>ConsistencyLevelTag</code> and optional fields to form an instance of the
/// original enum.
/// <code>address</code> is not null only when tag is <code>ReplicaDependant</code>.
#[repr(C)]
#[derive(Debug, Clone)]
pub struct ConsistencyLevel {
    pub(crate) tag: ConsistencyLevelTag,
    pub(crate) address: *mut c_char,
}

impl Drop for ConsistencyLevel {
    fn drop(&mut self) {
        string_free(self.address);
    }
}

/// Creates a strong <code>ConsistencyLevel</code> object.
#[no_mangle]
pub extern "C" fn consistency_level_strong() -> *mut ConsistencyLevel {
    release(ConsistencyLevel { tag: ConsistencyLevelTag::Strong, address: std::ptr::null_mut() })
}

/// Creates an eventual <code>ConsistencyLevel</code> object.
#[no_mangle]
pub extern "C" fn consistency_level_eventual() -> *mut ConsistencyLevel {
    release(ConsistencyLevel { tag: ConsistencyLevelTag::Eventual, address: std::ptr::null_mut() })
}

/// Creates a replica dependant <code>ConsistencyLevel</code> object.
///
/// @param address The address of the replica to depend on.
#[no_mangle]
pub extern "C" fn consistency_level_replica_dependant(address: *const c_char) -> *mut ConsistencyLevel {
    release(ConsistencyLevel {
        tag: ConsistencyLevelTag::ReplicaDependant,
        address: release_string(string_view(address.clone()).to_string()),
    })
}

/// Drops the <code>ConsistencyLevel</code> object.
#[no_mangle]
pub extern "C" fn consistency_level_drop(consistency_level: *mut ConsistencyLevel) {
    free(consistency_level)
}

impl Into<NativeConsistencyLevel> for ConsistencyLevel {
    fn into(self) -> NativeConsistencyLevel {
        let ConsistencyLevel { tag, address } = self;
        match tag {
            ConsistencyLevelTag::Strong => NativeConsistencyLevel::Strong,
            ConsistencyLevelTag::Eventual => NativeConsistencyLevel::Eventual,
            ConsistencyLevelTag::ReplicaDependant => {
                let address = unwrap_or_default(string_view(address).parse());
                NativeConsistencyLevel::ReplicaDependant { address }
            }
        }
    }
}
