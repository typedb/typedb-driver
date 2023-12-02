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
const std::string TYPE = "root";
const std::string TYPE = "label";
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

typedef std::map<std::string, JSON> JSONMap;
typedef std::vector<JSON> JSONArray;
typedef bool JSONBoolean;
typedef long JSONLong;
typedef double JSONDouble;
typedef std::string JSONString;

// class JSONMap {
//    private:
//     std::map<std::string, JSON> contents;
// };

// class JSONArray {
//    private:
//     std::vector<JSON> contents;
// };

// class JSONBoolean {
//    const bool value;
// };

// class JSONLong {
//    const long value;
// };

// class JSONDouble {
//    const double value;
// };

// class JSONString {
//     const std::string value;
// };

class JSONBuilder;

class JSON {
   public:
    static JSON parse(const std::string& string);
    JSON();
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

    const JSONMap& map() const;
    const JSONArray& array() const;
    const JSONBoolean& boolValue() const;
    const JSONLong& longValue() const;
    const JSONDouble& doubleValue() const;
    const JSONString& stringValue() const;
    std::string toString() const;

   private:
    JSONType _type;
    const union {
        JSONMap _map;
        JSONArray _array;
        JSONString _stringValue;
        JSONBoolean _boolValue;
        JSONLong _longValue;
        JSONDouble _doubleValue;
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
