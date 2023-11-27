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
#include <iostream>
#include <optional>
#include <iterator>

#include "typedb/common/exception.hpp"
#include "typedb/common/native.hpp"

namespace TypeDB {
template <
    typename NATIVE_ITER,
    typename NATIVE_T,
    typename T,
    typename HELPER>
class TypeDBIterable;

template <typename NATIVE_ITER, typename NATIVE_T, typename T>
class TypeDBIteratorHelper;

template <
    typename NATIVE_ITER,
    typename NATIVE_T,
    typename T,
    typename HELPER = TypeDBIteratorHelper<NATIVE_ITER, NATIVE_T, T> >
class TypeDBIterator {  // Does not support range-based for loops yet.

    using SELF = TypeDBIterator<NATIVE_ITER, NATIVE_T, T>;

   public:
    TypeDBIterator(NATIVE_ITER* iteratorNative)
        : iteratorNative(iteratorNative, &HELPER::nativeIterDrop),
          obj() {}

    TypeDBIterator(const SELF& from) = delete;

    TypeDBIterator(SELF&& from)
        : iteratorNative(std::move(from.iteratorNative)), obj(std::move(from.obj)) {}

    SELF& operator=(const SELF& from) = delete;

    SELF& operator=(SELF&& from) {
        iteratorNative = std::move(from.iteratorNative);
        obj = std::move(from.obj);
        return *this;
    }

    bool operator==(const SELF& other) {
        return iteratorNative == other.iteratorNative;
    }

    bool operator!=(const SELF& other) {
        return !(*this == other);
    }

    SELF operator++(int) = delete;  // Just use ++it.

    SELF& operator++() {
        NATIVE_T* p = HELPER::nativeIterNext(iteratorNative.get());
        TypeDBDriverException::check_and_throw();

        if (p == nullptr) {
            iteratorNative.reset();  // Makes it equal to end.
            obj.reset();
        } else {
            obj = std::move(HELPER::instantiate(p));
        }
        return *this;
    }

    T& operator*() {
        if ((*this) == SELF()) {
            throw TypeDBDriverException::of(&InternalError::ITERATOR_INVALIDATED);
        }
        assert(obj.has_value());
        return obj.value();
    }

    T* operator->() {
        if ((*this) == SELF()) {
            throw TypeDBDriverException::of(&InternalError::ITERATOR_INVALIDATED);
        }
        assert(obj.has_value());
        return &obj.value();
    }

   private:
    TypeDBIterator()
        : iteratorNative(nullptr), obj() {}
    NativePointer<NATIVE_ITER> iteratorNative;
    std::optional<T> obj;

    friend class TypeDBIterable<NATIVE_ITER, NATIVE_T, T, HELPER>;
};

template <
    typename NATIVE_ITER,
    typename NATIVE_T,
    typename T,
    typename HELPER = TypeDBIteratorHelper<NATIVE_ITER, NATIVE_T, T> >
class TypeDBIterable {
    using SELF = TypeDBIterable<NATIVE_ITER, NATIVE_T, T>;
    using ITERATOR = TypeDBIterator<NATIVE_ITER, NATIVE_T, T, HELPER>;

   public:
    TypeDBIterable(NATIVE_ITER* iteratorNative)
        : iteratorNative(iteratorNative, HELPER::nativeIterDrop) {}
    TypeDBIterable(SELF& from) = delete;
    TypeDBIterable(SELF&& from) {
        *this = std::move(from);
    }

    TypeDBIterable& operator=(const SELF& from) = delete;

    TypeDBIterable& operator=(SELF&& from) {
        iteratorNative = std::move(from.iteratorNative);
        return *this;
    }

    ITERATOR begin() {
        ITERATOR it = ITERATOR(iteratorNative.release());
        ++it;  // initialises it
        return std::move(it);
    }

    ITERATOR end() {
        return ITERATOR();
    }

   private:
    NativePointer<NATIVE_ITER> iteratorNative;
};

template <typename NATIVE_ITER, typename NATIVE_T, typename T>
class TypeDBIteratorHelper {
    using SELF = TypeDBIteratorHelper<NATIVE_ITER, NATIVE_T, T>;

   private:
    static void nativeIterDrop(NATIVE_ITER* it);
    static NATIVE_T* nativeIterNext(NATIVE_ITER* it);
    static T instantiate(NATIVE_T* element);

    friend class TypeDBIterator<NATIVE_ITER, NATIVE_T, T, SELF>;
    friend class TypeDBIterable<NATIVE_ITER, NATIVE_T, T, SELF>;
};

using StringIterable = TypeDBIterable<_native::StringIterator, char, std::string>;
using StringIterator = TypeDBIterator<_native::StringIterator, char, std::string>;

}  // namespace TypeDB
