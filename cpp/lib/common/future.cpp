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
#include "typedb/common/future.hpp"
#include "typedb/common/native.hpp"

#include "../common/macros.hpp"

namespace TypeDB {

std::optional<std::string> optional_string_promise_resolve_wrapper(_native::StringPromise* promise) {
    char* c = _native::string_promise_resolve(promise);
    std::optional<std::string> s = c != nullptr ? std::string(c) : std::optional<std::string>();
    _native::string_free(c);
    return s;
}

std::string string_promise_resolve_wrapper(_native::StringPromise* promise) {
    return optional_string_promise_resolve_wrapper(promise).value();
}

template <>
void FutureHelper<void, _native::VoidPromise>::resolve(_native::VoidPromise* promiseNative) {
    _native::void_promise_resolve(promiseNative);
    TypeDBDriverException::check_and_throw();
}

TYPEDB_FUTURE_HELPER(bool, _native::BoolPromise, _native::bool_promise_resolve);
TYPEDB_FUTURE_HELPER(std::string, _native::StringPromise, string_promise_resolve_wrapper);
TYPEDB_FUTURE_HELPER(std::optional<std::string>, _native::StringPromise, optional_string_promise_resolve_wrapper);

}  // namespace TypeDB
