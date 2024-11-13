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

use typedb_driver::concept::Concept;

use super::concept::borrow_as_attribute_type;
use crate::memory::release_string;

/// Gets the string representation of the value type of this attribute type.
#[no_mangle]
pub extern "C" fn attribute_type_get_value_type(attribute_type: *const Concept) -> *mut c_char {
    release_string(match &borrow_as_attribute_type(attribute_type).value_type {
        None => "none".to_owned(),
        Some(value_type) => value_type.name().to_owned(),
    })
}
