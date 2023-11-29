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

#include "typedb/common/errormessage.hpp"
#include "typedb/common/native.hpp"

namespace TypeDB {

class TypeDBDriverException : public std::runtime_error {
   public:
    TypeDBDriverException(const char* code, const char* message);
    TypeDBDriverException(_native::SchemaException* schemaExceptionNative);
    
    TypeDBDriverException(const TypeDBDriverException& from) = default;
    TypeDBDriverException& operator=(const TypeDBDriverException& from) = default;

    const std::string& code();
    const std::string& message();
    const char* what() const noexcept override;

    static void check_and_throw();
    static TypeDBDriverException of(const ErrorMessage* message, ...);
    
   private:
    TypeDBDriverException(_native::Error* errorNative);
    
    std::string errorCode;
    std::string errorMessage;
    
};


}  // namespace TypeDB
