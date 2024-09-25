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

use std::{ffi::c_char, ptr::null_mut};

use typedb_driver::concept::Concept;

use super::concept::{borrow_as_attribute, borrow_as_entity, borrow_as_relation};
use crate::memory::{release, release_string};

/// Retrieves the unique id of the ``Entity``.
#[no_mangle]
pub extern "C" fn entity_get_iid(thing: *mut Concept) -> *mut c_char {
    release_string(borrow_as_entity(thing).iid().to_string())
}

/// Retrieves the unique id of the ``Relation``.
#[no_mangle]
pub extern "C" fn relation_get_iid(thing: *mut Concept) -> *mut c_char {
    release_string(borrow_as_relation(thing).iid().to_string())
}

/// Retrieves the type which this ``Entity`` belongs to.
#[no_mangle]
pub extern "C" fn entity_get_type(entity: *const Concept) -> *mut Concept {
    borrow_as_entity(entity).type_().map(|type_| release(Concept::EntityType(type_.clone()))).unwrap_or_else(null_mut)
}

/// Retrieves the type which this ``Relation`` belongs to.
#[no_mangle]
pub extern "C" fn relation_get_type(relation: *const Concept) -> *mut Concept {
    borrow_as_relation(relation)
        .type_()
        .map(|type_| release(Concept::RelationType(type_.clone())))
        .unwrap_or_else(null_mut)
}

/// Retrieves the type which this ``Attribute`` belongs to.
#[no_mangle]
pub extern "C" fn attribute_get_type(attribute: *const Concept) -> *mut Concept {
    borrow_as_attribute(attribute)
        .type_()
        .map(|type_| release(Concept::AttributeType(type_.clone())))
        .unwrap_or_else(null_mut)
}

/// Retrieves the value of the ``Attribute``.
#[no_mangle]
pub extern "C" fn attribute_get_value(attribute: *const Concept) -> *mut Concept {
    release(Concept::Value(borrow_as_attribute(attribute).value.clone()))
}
