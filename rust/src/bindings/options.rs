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

use super::memory::{borrow, borrow_mut, free, release};
use crate::Options;

#[no_mangle]
pub extern "C" fn options_new() -> *mut Options {
    release(Options::new())
}

#[no_mangle]
pub extern "C" fn options_drop(options: *mut Options) {
    free(options);
}

#[no_mangle]
pub extern "C" fn options_set_infer(options: *mut Options, infer: bool) {
    borrow_mut(options).infer = Some(infer)
}

#[no_mangle]
pub extern "C" fn options_set_trace_inference(options: *mut Options, trace_inference: bool) {
    borrow_mut(options).trace_inference = Some(trace_inference);
}

#[no_mangle]
pub extern "C" fn options_set_explain(options: *mut Options, explain: bool) {
    borrow_mut(options).explain = Some(explain);
}

#[no_mangle]
pub extern "C" fn options_set_parallel(options: *mut Options, parallel: bool) {
    borrow_mut(options).parallel = Some(parallel);
}

#[no_mangle]
pub extern "C" fn options_set_prefetch(options: *mut Options, prefetch: bool) {
    borrow_mut(options).prefetch = Some(prefetch);
}

#[no_mangle]
pub extern "C" fn options_set_prefetch_size(options: *mut Options, prefetch_size: i32) {
    borrow_mut(options).prefetch_size = Some(prefetch_size);
}

#[no_mangle]
pub extern "C" fn options_set_session_idle_timeout_millis(options: *mut Options, timeout_millis: i64) {
    borrow_mut(options).session_idle_timeout = Some(Duration::from_millis(timeout_millis as u64));
}

#[no_mangle]
pub extern "C" fn options_set_transaction_timeout_millis(options: *mut Options, timeout_millis: i64) {
    borrow_mut(options).transaction_timeout = Some(Duration::from_millis(timeout_millis as u64));
}

#[no_mangle]
pub extern "C" fn options_set_schema_lock_acquire_timeout_millis(options: *mut Options, timeout_millis: i64) {
    borrow_mut(options).schema_lock_acquire_timeout = Some(Duration::from_millis(timeout_millis as u64));
}

#[no_mangle]
pub extern "C" fn options_set_read_any_replica(options: *mut Options, read_any_replica: bool) {
    borrow_mut(options).read_any_replica = Some(read_any_replica);
}

#[no_mangle]
pub extern "C" fn options_get_infer(options: *const Options) -> bool {
    borrow(options).infer.unwrap()
}

#[no_mangle]
pub extern "C" fn options_get_trace_inference(options: *const Options) -> bool {
    borrow(options).trace_inference.unwrap()
}

#[no_mangle]
pub extern "C" fn options_get_explain(options: *const Options) -> bool {
    borrow(options).explain.unwrap()
}

#[no_mangle]
pub extern "C" fn options_get_parallel(options: *const Options) -> bool {
    borrow(options).parallel.unwrap()
}

#[no_mangle]
pub extern "C" fn options_get_prefetch(options: *const Options) -> bool {
    borrow(options).prefetch.unwrap()
}

#[no_mangle]
pub extern "C" fn options_get_prefetch_size(options: *const Options) -> i32 {
    borrow(options).prefetch_size.unwrap()
}

#[no_mangle]
pub extern "C" fn options_get_session_idle_timeout_millis(options: *const Options) -> i64 {
    borrow(options).session_idle_timeout.unwrap().as_millis() as i64
}

#[no_mangle]
pub extern "C" fn options_get_transaction_timeout_millis(options: *const Options) -> i64 {
    borrow(options).transaction_timeout.unwrap().as_millis() as i64
}

#[no_mangle]
pub extern "C" fn options_get_schema_lock_acquire_timeout_millis(options: *const Options) -> i64 {
    borrow(options).schema_lock_acquire_timeout.unwrap().as_millis() as i64
}

#[no_mangle]
pub extern "C" fn options_get_read_any_replica(options: *const Options) -> bool {
    borrow(options).read_any_replica.unwrap()
}

#[no_mangle]
pub extern "C" fn options_has_infer(options: *const Options) -> bool {
    borrow(options).infer.is_some()
}

#[no_mangle]
pub extern "C" fn options_has_trace_inference(options: *const Options) -> bool {
    borrow(options).trace_inference.is_some()
}

#[no_mangle]
pub extern "C" fn options_has_explain(options: *const Options) -> bool {
    borrow(options).explain.is_some()
}

#[no_mangle]
pub extern "C" fn options_has_parallel(options: *const Options) -> bool {
    borrow(options).parallel.is_some()
}

#[no_mangle]
pub extern "C" fn options_has_prefetch(options: *const Options) -> bool {
    borrow(options).prefetch.is_some()
}

#[no_mangle]
pub extern "C" fn options_has_prefetch_size(options: *const Options) -> bool {
    borrow(options).prefetch_size.is_some()
}

#[no_mangle]
pub extern "C" fn options_has_session_idle_timeout_millis(options: *const Options) -> bool {
    borrow(options).session_idle_timeout.is_some()
}

#[no_mangle]
pub extern "C" fn options_has_transaction_timeout_millis(options: *const Options) -> bool {
    borrow(options).transaction_timeout.is_some()
}

#[no_mangle]
pub extern "C" fn options_has_schema_lock_acquire_timeout_millis(options: *const Options) -> bool {
    borrow(options).schema_lock_acquire_timeout.is_some()
}

#[no_mangle]
pub extern "C" fn options_has_read_any_replica(options: *const Options) -> bool {
    borrow(options).read_any_replica.is_some()
}
