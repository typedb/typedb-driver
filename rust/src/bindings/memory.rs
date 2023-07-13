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

use std::{
    cell::RefCell,
    ffi::{c_char, CStr, CString},
    ptr::null_mut,
};

use log::trace;

use crate::Error;

thread_local! {
    static LAST_ERROR: RefCell<Option<Error>> = RefCell::new(None);
}

pub(super) fn release<T>(t: T) -> *mut T {
    let raw = Box::into_raw(Box::new(t));
    trace!("Releasing ownership of <{}> @ {:?}", std::any::type_name::<T>(), raw);
    raw
}

pub(super) fn release_optional<T>(t: Option<T>) -> *mut T {
    t.map(release).unwrap_or_else(null_mut)
}

pub(super) fn release_string(str: String) -> *mut c_char {
    let raw = CString::new(str).unwrap().into_raw();
    trace!("Releasing ownership of <CString> @ {:?}", raw);
    raw
}

pub(super) fn borrow<T>(raw: *const T) -> &'static T {
    trace!("Borrowing <{}> @ {:?}", std::any::type_name::<T>(), raw);
    assert!(!raw.is_null());
    unsafe { &*raw }
}

pub(super) fn borrow_mut<T>(raw: *mut T) -> &'static mut T {
    trace!("Borrowing (mut) <{}> @ {:?}", std::any::type_name::<T>(), raw);
    assert!(!raw.is_null());
    unsafe { &mut *raw }
}

pub(super) fn borrow_optional<T>(raw: *const T) -> Option<&'static T> {
    trace!("Borrowing optional (null ok) <{}> @ {:?}", std::any::type_name::<T>(), raw);
    unsafe { raw.as_ref() }
}

pub(super) fn take_ownership<T>(raw: *mut T) -> T {
    trace!("Taking ownership of <{}> @ {:?}", std::any::type_name::<T>(), raw);
    assert!(!raw.is_null());
    unsafe { *Box::from_raw(raw) }
}

pub(super) fn free<T>(raw: *mut T) {
    trace!("Freeing <{}> @ {:?}", std::any::type_name::<T>(), raw);
    if !raw.is_null() {
        unsafe { drop(Box::from_raw(raw)) }
    }
}

pub(super) fn string_view(str: *const c_char) -> &'static str {
    assert!(!str.is_null());
    unsafe { CStr::from_ptr(str).to_str().unwrap() }
}

#[no_mangle]
pub extern "C" fn string_free(str: *mut c_char) {
    trace!("Freeing <CString> @ {:?}", str);
    if !str.is_null() {
        unsafe { drop(CString::from_raw(str)) }
    }
}

pub(super) fn array_view<T>(ts: *const *const T) -> impl Iterator<Item = &'static T> {
    assert!(!ts.is_null());
    unsafe { (0..).map_while(move |i| (*ts.add(i)).as_ref()) }
}

pub(super) fn string_array_view(strs: *const *const c_char) -> impl Iterator<Item = &'static str> {
    assert!(!strs.is_null());
    unsafe { (0..).map_while(move |i| (*strs.add(i)).as_ref()).map(|p| string_view(p)) }
}
