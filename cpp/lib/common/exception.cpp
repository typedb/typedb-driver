/*
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

#include <cstring>

#include "typedb/common/exception.hpp"

#include "../common/utils.hpp"
#include "./native.hpp"

namespace TypeDB {

DriverException::DriverException(const char* code, const char* message)
    : std::runtime_error(std::string(code) + std::string(message)),
      errorCodeLength(strlen(code)),
      messageLength(strlen(message)) {}

DriverException::DriverException(_native::Error* errorNative)
    : DriverException(
          Utils::stringFromNative(_native::error_code(errorNative)).c_str(),
          Utils::stringFromNative(_native::error_message(errorNative)).c_str()) {
    _native::error_drop(errorNative);
}

DriverException::DriverException(_native::SchemaException* schemaExceptionNative)
    : DriverException(
          Utils::stringFromNative(_native::schema_exception_code(schemaExceptionNative)).c_str(),
          Utils::stringFromNative(_native::schema_exception_message(schemaExceptionNative)).c_str()) {
    _native::schema_exception_drop(schemaExceptionNative);
}

const std::string_view DriverException::code() {
    return std::string_view(what(), errorCodeLength);
}

const std::string_view DriverException::message() {
    return std::string_view(what() + errorCodeLength, messageLength);
}

void DriverException::check_and_throw() {
    if (_native::check_error()) {
        DriverException exception(_native::get_last_error());
        throw exception;
    }
}

}  // namespace TypeDB
