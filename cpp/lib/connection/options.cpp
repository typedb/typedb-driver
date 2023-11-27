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

#include <optional>

#include "typedb/common/exception.hpp"
#include "typedb/connection/options.hpp"

#include "inc/macros.hpp"

namespace TypeDB {

Options::Options()
    : optionsNative(_native::options_new(), _native::options_drop) {}

_native::Options* Options::getNative() const {
    return optionsNative.get();
}

std::optional<bool> Options::infer() {
    CHECK_NATIVE(optionsNative);
    auto t = _native::options_has_infer(optionsNative.get()) ? std::optional<bool>(_native::options_get_infer(optionsNative.get())) : std::optional<bool>();
    TypeDBDriverException::check_and_throw();
    return t;
}

std::optional<bool> Options::traceInference() {
    CHECK_NATIVE(optionsNative);
    auto t = _native::options_has_trace_inference(optionsNative.get()) ? std::optional<bool>(_native::options_get_trace_inference(optionsNative.get())) : std::optional<bool>();
    TypeDBDriverException::check_and_throw();
    return t;
}

std::optional<bool> Options::explain() {
    CHECK_NATIVE(optionsNative);
    auto t = _native::options_has_explain(optionsNative.get()) ? std::optional<bool>(_native::options_get_explain(optionsNative.get())) : std::optional<bool>();
    TypeDBDriverException::check_and_throw();
    return t;
}

std::optional<bool> Options::parallel() {
    CHECK_NATIVE(optionsNative);
    auto t = _native::options_has_parallel(optionsNative.get()) ? std::optional<bool>(_native::options_get_parallel(optionsNative.get())) : std::optional<bool>();
    TypeDBDriverException::check_and_throw();
    return t;
}

std::optional<bool> Options::prefetch() {
    CHECK_NATIVE(optionsNative);
    auto t = _native::options_has_prefetch(optionsNative.get()) ? std::optional<bool>(_native::options_get_prefetch(optionsNative.get())) : std::optional<bool>();
    TypeDBDriverException::check_and_throw();
    return t;
}

std::optional<int32_t> Options::prefetchSize() {
    CHECK_NATIVE(optionsNative);
    auto t = _native::options_has_prefetch_size(optionsNative.get()) ? std::optional<int32_t>(_native::options_get_prefetch_size(optionsNative.get())) : std::optional<int32_t>();
    TypeDBDriverException::check_and_throw();
    return t;
}

std::optional<int64_t> Options::sessionIdleTimeoutMillis() {
    CHECK_NATIVE(optionsNative);
    auto t = _native::options_has_session_idle_timeout_millis(optionsNative.get()) ? std::optional<int64_t>(_native::options_get_session_idle_timeout_millis(optionsNative.get())) : std::optional<int64_t>();
    TypeDBDriverException::check_and_throw();
    return t;
}

std::optional<int64_t> Options::transactionTimeoutMillis() {
    CHECK_NATIVE(optionsNative);
    auto t = _native::options_has_transaction_timeout_millis(optionsNative.get()) ? std::optional<int64_t>(_native::options_get_transaction_timeout_millis(optionsNative.get())) : std::optional<int64_t>();
    TypeDBDriverException::check_and_throw();
    return t;
}

std::optional<int64_t> Options::schemaLockAcquireTimeoutMillis() {
    CHECK_NATIVE(optionsNative);
    auto t = _native::options_has_schema_lock_acquire_timeout_millis(optionsNative.get()) ? std::optional<int64_t>(_native::options_get_schema_lock_acquire_timeout_millis(optionsNative.get())) : std::optional<int64_t>();
    TypeDBDriverException::check_and_throw();
    return t;
}

std::optional<bool> Options::readAnyReplica() {
    CHECK_NATIVE(optionsNative);
    auto t = _native::options_has_read_any_replica(optionsNative.get()) ? std::optional<bool>(_native::options_get_read_any_replica(optionsNative.get())) : std::optional<bool>();
    TypeDBDriverException::check_and_throw();
    return t;
}

Options& Options::infer(bool infer) {
    CHECK_NATIVE(optionsNative);
    _native::options_set_infer(optionsNative.get(), infer);
    TypeDBDriverException::check_and_throw();
    return *this;
}

Options& Options::traceInference(bool traceInference) {
    CHECK_NATIVE(optionsNative);
    _native::options_set_trace_inference(optionsNative.get(), traceInference);
    TypeDBDriverException::check_and_throw();
    return *this;
}

Options& Options::explain(bool explain) {
    CHECK_NATIVE(optionsNative);
    _native::options_set_explain(optionsNative.get(), explain);
    TypeDBDriverException::check_and_throw();
    return *this;
}

Options& Options::parallel(bool parallel) {
    CHECK_NATIVE(optionsNative);
    _native::options_set_parallel(optionsNative.get(), parallel);
    TypeDBDriverException::check_and_throw();
    return *this;
}

Options& Options::prefetch(bool prefetch) {
    CHECK_NATIVE(optionsNative);
    _native::options_set_prefetch(optionsNative.get(), prefetch);
    TypeDBDriverException::check_and_throw();
    return *this;
}

Options& Options::prefetchSize(int32_t prefetchSize) {
    CHECK_NATIVE(optionsNative);
    _native::options_set_prefetch_size(optionsNative.get(), prefetchSize);
    TypeDBDriverException::check_and_throw();
    return *this;
}

Options& Options::sessionIdleTimeoutMillis(int64_t sessionIdleTimeoutMillis) {
    CHECK_NATIVE(optionsNative);
    _native::options_set_session_idle_timeout_millis(optionsNative.get(), sessionIdleTimeoutMillis);
    TypeDBDriverException::check_and_throw();
    return *this;
}

Options& Options::transactionTimeoutMillis(int64_t transactionTimeoutMillis) {
    CHECK_NATIVE(optionsNative);
    _native::options_set_transaction_timeout_millis(optionsNative.get(), transactionTimeoutMillis);
    TypeDBDriverException::check_and_throw();
    return *this;
}

Options& Options::schemaLockAcquireTimeoutMillis(int64_t schemaLockAcquireTimeoutMillis) {
    CHECK_NATIVE(optionsNative);
    _native::options_set_schema_lock_acquire_timeout_millis(optionsNative.get(), schemaLockAcquireTimeoutMillis);
    TypeDBDriverException::check_and_throw();
    return *this;
}

Options& Options::readAnyReplica(bool readAnyReplica) {
    CHECK_NATIVE(optionsNative);
    _native::options_set_read_any_replica(optionsNative.get(), readAnyReplica);
    TypeDBDriverException::check_and_throw();
    return *this;
}
}  // namespace TypeDB
