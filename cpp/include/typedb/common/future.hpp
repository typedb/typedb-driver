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
#include <optional>
#include <iterator>

#include "typedb/common/exception.hpp"
#include "typedb/common/native.hpp"

namespace TypeDB {

template <typename RETURN, typename NATIVE_PROMISE>
class TypeDBFuture {
    using SELF = TypeDBFuture<RETURN, NATIVE_PROMISE>;

   public:
    TypeDBFuture(NATIVE_PROMISE* promiseNative)
        : promiseNative(promiseNative, SELF::fn_nativePromiseResolve) {}

    RETURN get() {
        if constexpr (std::is_same_v<RETURN, void>) {
            fn_nativePromiseResolve(promiseNative.release());
            TypeDBDriverException::check_and_throw();
        } else {
            auto t = fn_nativePromiseResolve(promiseNative.release());
            TypeDBDriverException::check_and_throw();
            return t;
        }
    }

    void wait() {
        get();
    }

   private:
    static std::function<RETURN(NATIVE_PROMISE*)> fn_nativePromiseResolve;

    NativePointer<NATIVE_PROMISE> promiseNative;
};


using VoidFuture = TypeDBFuture<void, _native::VoidPromise>;
using BoolFuture = TypeDBFuture<bool, _native::BoolPromise>;
using StringFuture = TypeDBFuture<std::string, _native::StringPromise>;
using OptionalStringFuture = TypeDBFuture<std::optional<std::string>, _native::StringPromise>;

#ifndef _MSC_VER
template <>
std::function<void(_native::VoidPromise*)> VoidFuture::fn_nativePromiseResolve;
template <>
std::function<bool(_native::BoolPromise*)> BoolFuture::fn_nativePromiseResolve;
template <>
std::function<std::string(_native::StringPromise*)> StringFuture::fn_nativePromiseResolve;
template <>
std::function<std::optional<std::string>(_native::StringPromise*)> OptionalStringFuture::fn_nativePromiseResolve;
#endif

}  // namespace TypeDB
