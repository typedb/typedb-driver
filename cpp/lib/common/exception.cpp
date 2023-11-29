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

#include "typedb/common/exception.hpp"
#include "typedb/common/native.hpp"

#include "inc/utils.hpp"

namespace TypeDB {

TypeDBDriverException::TypeDBDriverException(const char* code, const char* message)
    : std::runtime_error(""),
      errorCode(code),
      errorMessage(message) {}

TypeDBDriverException::TypeDBDriverException(_native::Error* errorNative)
    : std::runtime_error(""),
      errorCode(Utils::stringAndFree(_native::error_code(errorNative))),
      errorMessage(Utils::stringAndFree(_native::error_message(errorNative))) {
    _native::error_drop(errorNative);
}

TypeDBDriverException::TypeDBDriverException(_native::SchemaException* schemaExceptionNative)
    : std::runtime_error(""),
      errorCode(Utils::stringAndFree(_native::schema_exception_code(schemaExceptionNative))),
      errorMessage(Utils::stringAndFree(_native::schema_exception_message(schemaExceptionNative))) {
    _native::schema_exception_drop(schemaExceptionNative);
}

const std::string& TypeDBDriverException::code() {
    return errorCode;
}

const std::string& TypeDBDriverException::message() {
    return errorMessage;
}

const char* TypeDBDriverException::what() const noexcept {
    return errorMessage.c_str();
}

void TypeDBDriverException::check_and_throw() {
    if (_native::check_error()) {
        TypeDBDriverException exception(_native::get_last_error());
        throw exception;
    }
}

}  // namespace TypeDB
