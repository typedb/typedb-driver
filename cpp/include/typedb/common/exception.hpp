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

#include <stdexcept>
#include <string>
#include <string_view>

#include "typedb/common/error_message.hpp"
#include "typedb/common/native.hpp"

namespace TypeDB {

template <typename NATIVE_ITER, typename NATIVE_T, typename T>
class IteratorHelper;

class DriverException : public std::runtime_error {
public:
    DriverException(_native::Error* errorNative);
    DriverException(const char* code, const char* message);

    DriverException(const DriverException& from) = default;
    DriverException& operator=(const DriverException& from) = default;

    const std::string_view code();
    const std::string_view message();

    static void check_and_throw();

private:
    DriverException(_native::SchemaException* schemaExceptionNative);

    size_t errorCodeLength;
    size_t messageLength;

    friend class IteratorHelper<_native::SchemaExceptionIterator, _native::SchemaException, DriverException>;
};


}  // namespace TypeDB
