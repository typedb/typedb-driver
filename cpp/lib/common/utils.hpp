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

#include <string>

#include "typedb/common/exception.hpp"

namespace TypeDB {
namespace Utils {

std::string stringFromNative(char* c);

template <class... Args>
TypeDBDriverException exception(const ErrorMessage& errMsg, Args... args) {
    size_t sizeRequired = snprintf(nullptr, 0, errMsg.formatString, args...);
    char* buffer = new char[sizeRequired];
    snprintf(buffer, sizeRequired, errMsg.formatString, args...);
    TypeDBDriverException exception(errMsg.code, buffer);
    delete[] buffer;
    return exception;
}


}  // namespace Utils
}  // namespace TypeDB
