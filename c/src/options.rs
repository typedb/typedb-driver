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

// /// Produces a new <code>TypeDBOptions</code> object.
// #[no_mangle]
// pub extern "C" fn options_new() -> *mut Options {
//     release(Options::new())
// }
//
// /// Frees the native rust <code>Options</code> object.
// #[no_mangle]
// pub extern "C" fn options_drop(options: *mut Options) {
//     free(options);
// }
//
// /// Explicitly enables or disables inference.
// /// Only settable at transaction level and above. Only affects read transactions.
// #[no_mangle]
// pub extern "C" fn options_set_infer(options: *mut Options, infer: bool) {
//     borrow_mut(options).infer = Some(infer)
// }
//
// /// Explicitly enables or disables reasoning tracing.
// /// If set to <code>true</code>, reasoning tracing graphs are output in the logging directory.
// /// Should be used with <code>parallel = False</code>.
// #[no_mangle]
// pub extern "C" fn options_set_trace_inference(options: *mut Options, trace_inference: bool) {
//     borrow_mut(options).trace_inference = Some(trace_inference);
// }
//
// /// Explicitly enables or disables explanations.
// /// If set to <code>true</code>, enables explanations for queries. Only affects read transactions.
// #[no_mangle]
// pub extern "C" fn options_set_explain(options: *mut Options, explain: bool) {
//     borrow_mut(options).explain = Some(explain);
// }
//
// /// Explicitly enables or disables parallel execution.
// /// If set to <code>true</code>, the server uses parallel instead of single-threaded execution.
// #[no_mangle]
// pub extern "C" fn options_set_parallel(options: *mut Options, parallel: bool) {
//     borrow_mut(options).parallel = Some(parallel);
// }
//
// /// Explicitly enables or disables prefetching.
// /// If set to <code>true</code>, the first batch of answers is streamed to the driver even without
// /// an explicit request for it.
// #[no_mangle]
// pub extern "C" fn options_set_prefetch(options: *mut Options, prefetch: bool) {
//     borrow_mut(options).prefetch = Some(prefetch);
// }
//
// /// Explicitly sets a prefetch size.
// /// If set, specifies a guideline number of answers that the server should send before the driver
// /// issues a fresh request.
// ///
// /// @param prefetchSize Number of answers that the server should send before the driver issues a fresh request
// #[no_mangle]
// pub extern "C" fn options_set_prefetch_size(options: *mut Options, prefetch_size: i32) {
//     borrow_mut(options).prefetch_size = Some(prefetch_size);
// }
//
// /// Explicitly sets a session idle timeout.
// /// If set, specifies a timeout that allows the server to close sessions if the driver terminates
// /// or becomes unresponsive.
// #[no_mangle]
// pub extern "C" fn options_set_session_idle_timeout_millis(options: *mut Options, timeout_millis: i64) {
//     borrow_mut(options).session_idle_timeout = Some(Duration::from_millis(timeout_millis as u64));
// }
//
// /// Explicitly set a transaction timeout.
// /// If set, specifies a timeout for killing transactions automatically, preventing memory leaks
// /// in unclosed transactions.
// #[no_mangle]
// pub extern "C" fn options_set_transaction_timeout_millis(options: *mut Options, timeout_millis: i64) {
//     borrow_mut(options).transaction_timeout = Some(Duration::from_millis(timeout_millis as u64));
// }
//
// /// Explicitly sets schema lock acquire timeout.
// /// If set, specifies how long the driver should wait if opening a session or transaction is blocked
// /// by a schema write lock.
// #[no_mangle]
// pub extern "C" fn options_set_schema_lock_acquire_timeout_millis(options: *mut Options, timeout_millis: i64) {
//     borrow_mut(options).schema_lock_acquire_timeout = Some(Duration::from_millis(timeout_millis as u64));
// }
//
// /// Explicitly enables or disables reading data from any replica.
// /// If set to <code>True</code>, enables reading data from any replica, potentially boosting read throughput.
// /// Only settable in TypeDB Cloud.
// #[no_mangle]
// pub extern "C" fn options_set_read_any_replica(options: *mut Options, read_any_replica: bool) {
//     borrow_mut(options).read_any_replica = Some(read_any_replica);
// }
//
// /// Returns the value set for the inference in this <code>TypeDBOptions</code> object.
// #[no_mangle]
// pub extern "C" fn options_get_infer(options: *const Options) -> bool {
//     borrow(options).infer.unwrap()
// }
//
// /// Returns the value set for reasoning tracing in this <code>TypeDBOptions</code> object.
// /// If set to <code>true</code>, reasoning tracing graphs are output in the logging directory.
// #[no_mangle]
// pub extern "C" fn options_get_trace_inference(options: *const Options) -> bool {
//     borrow(options).trace_inference.unwrap()
// }
//
// ///Returns the value set for the explanation in this <code>TypeDBOptions</code> object.
// /// If set to <code>true</code>, explanations for queries are enabled.
// #[no_mangle]
// pub extern "C" fn options_get_explain(options: *const Options) -> bool {
//     borrow(options).explain.unwrap()
// }
//
// /// Returns the value set for the parallel execution in this <code>TypeDBOptions</code> object.
// /// If set to <code>true</code>, the server uses parallel instead of single-threaded execution.
// #[no_mangle]
// pub extern "C" fn options_get_parallel(options: *const Options) -> bool {
//     borrow(options).parallel.unwrap()
// }
//
// /// Returns the value set for the prefetching in this <code>TypeDBOptions</code> object.
// /// If set to <code>true</code>, the first batch of answers is streamed to the driver even without
// /// an explicit request for it.
// #[no_mangle]
// pub extern "C" fn options_get_prefetch(options: *const Options) -> bool {
//     borrow(options).prefetch.unwrap()
// }
//
// /// Returns the value set for the prefetch size in this <code>TypeDBOptions</code> object.
// /// If set, specifies a guideline number of answers that the server should send before the driver
// /// issues a fresh request.
// #[no_mangle]
// pub extern "C" fn options_get_prefetch_size(options: *const Options) -> i32 {
//     borrow(options).prefetch_size.unwrap()
// }
//
// /// Returns the value set for the session idle timeout in this <code>TypeDBOptions</code> object.
// /// If set, specifies a timeout that allows the server to close sessions if the driver terminates
// /// or becomes unresponsive.
// #[no_mangle]
// pub extern "C" fn options_get_session_idle_timeout_millis(options: *const Options) -> i64 {
//     borrow(options).session_idle_timeout.unwrap().as_millis() as i64
// }
//
// /// Returns the value set for the transaction timeout in this <code>TypeDBOptions</code> object.
// /// If set, specifies a timeout for killing transactions automatically, preventing memory leaks
// /// in unclosed transactions.
// #[no_mangle]
// pub extern "C" fn options_get_transaction_timeout_millis(options: *const Options) -> i64 {
//     borrow(options).transaction_timeout.unwrap().as_millis() as i64
// }
//
// /// Returns the value set for the schema lock acquire timeout in this <code>TypeDBOptions</code> object.
// /// If set, specifies how long the driver should wait if opening a session or transaction is blocked
// /// by a schema write lock.
// #[no_mangle]
// pub extern "C" fn options_get_schema_lock_acquire_timeout_millis(options: *const Options) -> i64 {
//     borrow(options).schema_lock_acquire_timeout.unwrap().as_millis() as i64
// }
//
// /// Returns the value set for reading data from any replica in this <code>TypeDBOptions</code> object.
// /// If set to <code>True</code>, enables reading data from any replica, potentially boosting read throughput.
// #[no_mangle]
// pub extern "C" fn options_get_read_any_replica(options: *const Options) -> bool {
//     borrow(options).read_any_replica.unwrap()
// }
//
// /// Checks whether the option for inference was explicitly set for this <code>TypeDBOptions</code> object.
// #[no_mangle]
// pub extern "C" fn options_has_infer(options: *const Options) -> bool {
//     borrow(options).infer.is_some()
// }
//
// /// Checks whether the option for reasoning tracing was explicitly set for this <code>TypeDBOptions</code> object.
// #[no_mangle]
// pub extern "C" fn options_has_trace_inference(options: *const Options) -> bool {
//     borrow(options).trace_inference.is_some()
// }
//
// /// Checks whether the option for explanation was explicitly set for this <code>TypeDBOptions</code> object.
// #[no_mangle]
// pub extern "C" fn options_has_explain(options: *const Options) -> bool {
//     borrow(options).explain.is_some()
// }
//
// /// Checks whether the option for parallel execution was explicitly set for this <code>TypeDBOptions</code> object.
// #[no_mangle]
// pub extern "C" fn options_has_parallel(options: *const Options) -> bool {
//     borrow(options).parallel.is_some()
// }
//
// /// Checks whether the option for prefetching was explicitly set for this <code>TypeDBOptions</code> object.
// #[no_mangle]
// pub extern "C" fn options_has_prefetch(options: *const Options) -> bool {
//     borrow(options).prefetch.is_some()
// }
//
// /// Checks whether the option for prefetch size was explicitly set for this <code>TypeDBOptions</code> object.
// #[no_mangle]
// pub extern "C" fn options_has_prefetch_size(options: *const Options) -> bool {
//     borrow(options).prefetch_size.is_some()
// }
//
// /// Checks whether the option for the session idle timeout was explicitly set for this <code>TypeDBOptions</code> object.
// #[no_mangle]
// pub extern "C" fn options_has_session_idle_timeout_millis(options: *const Options) -> bool {
//     borrow(options).session_idle_timeout.is_some()
// }
//
// /// Checks whether the option for transaction timeout was explicitly set for this <code>TypeDBOptions</code> object.
// #[no_mangle]
// pub extern "C" fn options_has_transaction_timeout_millis(options: *const Options) -> bool {
//     borrow(options).transaction_timeout.is_some()
// }
//
// /// Checks whether the option for schema lock acquire timeout was explicitly set for this <code>TypeDBOptions</code> object.
// #[no_mangle]
// pub extern "C" fn options_has_schema_lock_acquire_timeout_millis(options: *const Options) -> bool {
//     borrow(options).schema_lock_acquire_timeout.is_some()
// }
//
// /// Checks whether the option for reading data from any replica was explicitly set for this <code>TypeDBOptions</code> object.
// #[no_mangle]
// pub extern "C" fn options_has_read_any_replica(options: *const Options) -> bool {
//     borrow(options).read_any_replica.is_some()
// }
