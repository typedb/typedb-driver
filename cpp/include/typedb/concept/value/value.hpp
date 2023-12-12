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

#include <chrono>

#include "typedb/concept/concept.hpp"

namespace TypeDB {

typedef std::chrono::time_point<std::chrono::system_clock, std::chrono::milliseconds> DateTime;

class Value : public Concept {
public:
    ValueType valueType();

    bool isBoolean();
    bool isLong();
    bool isDouble();
    bool isString();
    bool isDateTime();

    bool asBoolean();
    int64_t asLong();
    double asDouble();
    std::string asString();
    DateTime asDateTime();

    static std::string formatDateTime(DateTime t);
    static DateTime parseDateTime(const std::string& s);

    static std::unique_ptr<Value> of(bool value);
    static std::unique_ptr<Value> of(int64_t value);
    static std::unique_ptr<Value> of(double value);
    static std::unique_ptr<Value> of(const std::string& value);
    static std::unique_ptr<Value> of(DateTime value);

private:
    Value(_native::Concept* conceptNative);

    friend class ConceptFactory;
};

}  // namespace TypeDB
