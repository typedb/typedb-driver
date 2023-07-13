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

use std::{ffi::c_char, ptr::null_mut};

use super::{
    iterator::CIterator,
    memory::{borrow_mut, free, release_optional, release_string, string_free},
};

pub struct StringIterator(pub CIterator<String>);

#[no_mangle]
pub extern "C" fn string_iterator_next(it: *mut StringIterator) -> *mut c_char {
    borrow_mut(it).0 .0.next().map(release_string).unwrap_or_else(null_mut)
}

#[no_mangle]
pub extern "C" fn string_iterator_drop(it: *mut StringIterator) {
    free(it);
}

#[repr(C)]
pub struct StringPair(*mut c_char, *mut c_char);

impl From<(String, String)> for StringPair {
    fn from((left, right): (String, String)) -> Self {
        Self(release_string(left), release_string(right))
    }
}

impl Drop for StringPair {
    fn drop(&mut self) {
        string_free(self.0);
        string_free(self.1);
    }
}

#[no_mangle]
pub extern "C" fn string_pair_drop(string_pair: *mut StringPair) {
    free(string_pair);
}

pub struct StringPairIterator(pub CIterator<(String, String)>);

#[no_mangle]
pub extern "C" fn string_pair_iterator_next(it: *mut StringPairIterator) -> *mut StringPair {
    release_optional(borrow_mut(it).0 .0.next().map(Into::into))
}

#[no_mangle]
pub extern "C" fn string_pair_iterator_drop(it: *mut StringPairIterator) {
    free(it);
}
