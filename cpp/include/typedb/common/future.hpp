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

#include <iostream>
#include <iterator>
#include <optional>

#include "typedb/common/exception.hpp"
#include "typedb/common/native.hpp"

namespace TypeDB {

/**
 * \private
 */
template <typename RETURN, typename NATIVE_PROMISE>
class FutureHelper;

/**
 * \brief A structure emulating std::future, used as result of an asynchronous call to the server.
 *
 * Note that a future must be evaluated for any server-side exceptions to be raised.
 */
template <typename RETURN, typename NATIVE_PROMISE, typename HELPER = FutureHelper<RETURN, NATIVE_PROMISE> >
class Future {
    using SELF = Future<RETURN, NATIVE_PROMISE>;

public:
    Future(NATIVE_PROMISE* promiseNative)
        : promiseNative(promiseNative, &HELPER::resolve) {}

    Future(const SELF& from) = delete;
    Future(SELF&& from) {
        *this = std::move(from);
    }

    Future& operator=(const SELF& from) = delete;

    Future& operator=(SELF&& from) {
        promiseNative = std::move(from.promiseNative);
        return *this;
    }

    /**
     * Waits for the call to complete and returns the result.
     */
    RETURN get() {
        if constexpr (std::is_same_v<RETURN, void>) {
            HELPER::resolve(promiseNative.release());
            DriverException::check_and_throw();
        } else {
            auto t = HELPER::resolve(promiseNative.release());
            DriverException::check_and_throw();
            return t;
        }
    }

    /**
     * Waits for the call to complete and ignores the result.
     * Any exceptions will still be thrown.
     */
    void wait() {
        get();
    }

private:
    NativePointer<NATIVE_PROMISE> promiseNative;
};

/**
 * \private
 */
template <typename RETURN, typename NATIVE_PROMISE>
class FutureHelper {
    using SELF = FutureHelper<RETURN, NATIVE_PROMISE>;

private:
    static RETURN resolve(NATIVE_PROMISE* nativePromise);

    friend class Future<RETURN, NATIVE_PROMISE, SELF>;
};

using VoidFuture = Future<void, _native::VoidPromise>;
using BoolFuture = Future<bool, _native::BoolPromise>;
using StringFuture = Future<std::string, _native::StringPromise>;
using OptionalStringFuture = Future<std::optional<std::string>, _native::StringPromise>;

}  // namespace TypeDB
