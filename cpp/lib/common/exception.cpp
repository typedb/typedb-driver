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

namespace TypeDB {

TypeDBDriverException::TypeDBDriverException(const char* code, const char* message)
    : std::runtime_error(""),
      errCode(code),
      errMsg(message) {}

const std::string& TypeDBDriverException::code() {
    return errCode;
}

const std::string& TypeDBDriverException::message() {
    return errMsg;
}

const char* TypeDBDriverException::what() const noexcept {
    return errMsg.c_str();
}


void TypeDBDriverException::check_and_throw() {
    if (_native::check_error()) {
        _native::Error* error = _native::get_last_error();
        char* errcode = _native::error_code(error);
        char* errmsg = _native::error_message(error);
        TypeDBDriverException exception(errcode, errmsg);
        _native::string_free(errmsg);
        _native::string_free(errcode);
        _native::error_drop(error);
        throw exception;
    }
}

TypeDBDriverException TypeDBDriverException::of(const ErrorMessage* errMsg, ...) {
    va_list args;
    char buffer[2048];
    va_start(args, errMsg);
    vsnprintf(buffer, sizeof(buffer), errMsg->formatString, args);
    va_end(args);
    return TypeDBDriverException(errMsg->code, buffer);
}

}  // namespace TypeDB
