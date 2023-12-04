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

#include <cassert>
#include "typedb/common/errormessage.hpp"
#include "typedb/common/exception.hpp"

#include "inc/conceptfuture.hpp"
#include "inc/conceptiterator.hpp"

#define THROW_ILLEGAL_STATE \
    { throw TypeDB::Utils::exception(TypeDB::InternalError::ILLEGAL_STATE, __FILE__, __LINE__); }

// Helper for TypeDBIteratorHelper
#define TYPEDB_FUTURE_HELPER_1(RETURN, NATIVE_PROMISE, NATIVE_PROMISE_RESOLVE, INSTANTIATE) \
    template <>                                                                             \
    RETURN FutureHelper<RETURN, NATIVE_PROMISE>::resolve(NATIVE_PROMISE* promiseNative) {   \
        auto p = NATIVE_PROMISE_RESOLVE(promiseNative);                                     \
        TypeDBDriverException::check_and_throw();                                           \
        return INSTANTIATE(p);                                                              \
    }

#define TYPEDB_FUTURE_HELPER(RETURN, NATIVE_PROMISE, NATIVE_PROMISE_RESOLVE) \
    TYPEDB_FUTURE_HELPER_1(RETURN, NATIVE_PROMISE, NATIVE_PROMISE_RESOLVE, RETURN_IDENTITY)

#define TYPEDB_ITERATOR_HELPER_1(NATIVE_ITER, NATIVE_T, T, NATIVE_ITER_DROP, NATIVE_ITER_NEXT, NATIVE_T_DROP, INSTANTIATE) \
    template <>                                                                                                            \
    void TypeDBIteratorHelper<NATIVE_ITER, NATIVE_T, T>::nativeIterDrop(NATIVE_ITER* it) {                                 \
        NATIVE_ITER_DROP(it);                                                                                              \
    }                                                                                                                      \
    template <>                                                                                                            \
    NATIVE_T* TypeDBIteratorHelper<NATIVE_ITER, NATIVE_T, T>::nativeIterNext(NATIVE_ITER* it) {                            \
        return NATIVE_ITER_NEXT(it);                                                                                       \
    }                                                                                                                      \
    template <>                                                                                                            \
    T TypeDBIteratorHelper<NATIVE_ITER, NATIVE_T, T>::instantiate(NATIVE_T* tNative) {                                     \
        return INSTANTIATE(tNative);                                                                                       \
    }

#define TYPEDB_ITERATOR_HELPER(NATIVE_ITER, NATIVE_T, T, NATIVE_ITER_DROP, NATIVE_ITER_NEXT, NATIVE_T_DROP) \
    TYPEDB_ITERATOR_HELPER_1(NATIVE_ITER, NATIVE_T, T, NATIVE_ITER_DROP, NATIVE_ITER_NEXT, NATIVE_T_DROP, T)


// Helpers for Wrapping native calls
#define RETURN_IDENTITY(X) (X)

#define CHECK_NATIVE(PTR)                                                                         \
    {                                                                                             \
        if (nullptr == PTR) throw Utils::exception(TypeDB::InternalError::INVALID_NATIVE_HANDLE); \
    }

#define WRAPPED_NATIVE_CALL(TYPE, NATIVE_CALL)    \
    {                                             \
        auto p = NATIVE_CALL;                     \
        TypeDBDriverException::check_and_throw(); \
        return TYPE(p);                           \
    }

// Specific to concept-api
#define CONCEPTAPI_CALL(RET_TYPE, NATIVE_CALL)                                                      \
    {                                                                                               \
        if (!transaction.isOpen()) throw Utils::exception(TypeDB::DriverError::TRANSACTION_CLOSED); \
        CHECK_NATIVE(conceptNative.get());                                                          \
        WRAPPED_NATIVE_CALL(RET_TYPE, NATIVE_CALL);                                                 \
    }

#define CONCEPTAPI_FUTURE(RET_TYPE, NATIVE_CALL) CONCEPTAPI_CALL(ConceptPtrFuture<RET_TYPE>, new ConceptFutureWrapperSimple(NATIVE_CALL))

#define CONCEPTAPI_ITER(RET_TYPE, NATIVE_CALL) CONCEPTAPI_CALL(ConceptIterable<RET_TYPE>, new ConceptIteratorWrapperSimple(NATIVE_CALL))
