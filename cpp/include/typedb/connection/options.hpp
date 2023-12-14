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
#pragma once

#include "typedb/common/native.hpp"

namespace TypeDB {

// Forward declarations for friendship
class Session;
class Transaction;
class QueryManager;

class Options {
public:
    Options();
    Options(const Options&) = delete;
    Options(Options&&) = default;
    Options& operator=(const Options&) = delete;
    Options& operator=(Options&&) = default;
    ~Options() = default;

    std::optional<bool> infer();

    std::optional<bool> traceInference();

    std::optional<bool> explain();

    std::optional<bool> parallel();

    std::optional<bool> prefetch();

    std::optional<int32_t> prefetchSize();

    std::optional<int64_t> sessionIdleTimeoutMillis();

    std::optional<int64_t> transactionTimeoutMillis();

    std::optional<int64_t> schemaLockAcquireTimeoutMillis();

    std::optional<bool> readAnyReplica();


    Options& infer(bool infer);

    Options& traceInference(bool traceInference);

    Options& explain(bool explain);

    Options& parallel(bool parallel);

    Options& prefetch(bool prefetch);

    Options& prefetchSize(int32_t prefetchSize);

    Options& sessionIdleTimeoutMillis(int64_t timeoutMillis);

    Options& transactionTimeoutMillis(int64_t timeoutMillis);

    Options& schemaLockAcquireTimeoutMillis(int64_t timeoutMillis);

    Options& readAnyReplica(bool readAnyReplica);

private:
    NativePointer<_native::Options> optionsNative;
    _native::Options* getNative() const;


    friend class DatabaseManager;
    friend class Session;
    friend class QueryManager;
};

}  // namespace TypeDB
