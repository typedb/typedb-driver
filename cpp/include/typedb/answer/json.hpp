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

#include <map>
#include <string>
#include <vector>

namespace TypeDB {


namespace JSONKey {

const std::string VALUE = "value";
const std::string VALUE_TYPE = "value_type";
const std::string TYPE = "type";

namespace Type {
const std::string ROOT = "root";
const std::string LABEL = "label";
}  // namespace Type

}  // namespace JSONKey

namespace JSONConstant {

namespace ValueTypes {
const std::string BOOLEAN = "boolean";
const std::string LONG = "long";
const std::string DOUBLE = "double";
const std::string STRING = "string";
const std::string DATETIME = "datetime";
}  // namespace ValueTypes

namespace Root {
const std::string ENTITY = "entity";
const std::string RELATION = "relation";
const std::string ATTRIBUTE = "attribute";
}  // namespace Root

}  // namespace JSONConstant

enum class JSONType {
    NONE,

    MAP,
    ARRAY,

    BOOLEAN,
    LONG,
    DOUBLE,
    STRING
};

class JSON;

using JSONMap = std::map<std::string, JSON>;
using JSONArray = std::vector<JSON>;
using JSONBoolean = bool;
using JSONLong = long;
using JSONDouble = double;
using JSONString = std::string;

class JSONBuilder;

class JSON {
public:
    static JSON parse(const std::string& string);
    ~JSON();

    JSON(const JSON&);
    JSON& operator=(const JSON&);
    JSON(JSON&&);
    JSON& operator=(JSON&&);

    JSONType type() const;

    bool isMap() const;
    bool isArray() const;
    bool isBoolean() const;
    bool isLong() const;
    bool isDouble() const;
    bool isString() const;

    const JSONMap& asMap() const;
    const JSONArray& asArray() const;
    const JSONBoolean& asBoolean() const;
    const JSONLong& asLong() const;
    const JSONDouble& asDouble() const;
    const JSONString& asString() const;

private:
    JSONType _type;
    const union {
        JSONMap mapValue;
        JSONArray arrayValue;
        JSONString stringValue;
        JSONBoolean boolValue;
        JSONLong longValue;
        JSONDouble doubleValue;
    };

    JSON(JSONBoolean);
    JSON(JSONLong);
    JSON(JSONDouble);

    JSON(const JSONString&);
    JSON(const JSONMap&);
    JSON(const JSONArray&);

    JSON(JSONString&&);
    JSON(JSONMap&&);
    JSON(JSONArray&&);

    friend class JSONBuilder;
};

}  // namespace TypeDB
