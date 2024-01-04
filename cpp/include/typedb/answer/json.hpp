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

/**
 * Specifies the exact type of this JSON object
 */
enum class JSONType {
    INVALID,

    MAP,
    ARRAY,

    BOOLEAN,
    LONG,
    DOUBLE,
    STRING,

    NULL_VALUE
};

class JSON;
class JSONNull{};

using JSONMap = std::map<std::string, JSON>;
using JSONArray = std::vector<JSON>;
using JSONBoolean = bool;
using JSONLong = long;
using JSONDouble = double;
using JSONString = std::string;

class JSONBuilder;

/**
 * \brief Simple JSON structure for results of fetch queries.
 */
class JSON {
public:
    /**
     * Parses a JSON string into a <code>JSON</code> object.
     */
    static JSON parse(const std::string& string);
    ~JSON();

    JSON(const JSON&);
    JSON& operator=(const JSON&);
    JSON(JSON&&);
    JSON& operator=(JSON&&);

    /*
     * The JSONType of this JSON object
     * <h3>Examples</h3>
     * <pre>
     * switch(json.type() { ... }
     * </pre>
     */
    JSONType type() const;

    /**
     * true if this JSON object holds a map, else false
     */
    bool isMap() const;

    /**
     * true if this JSON object holds an array, else false
     */
    bool isArray() const;

    /**
     * true if this JSON object holds a boolean value, else false
     */
    bool isBoolean() const;

    /**
     * true if this JSON object holds a long value, else false
     */
    bool isLong() const;

    /**
     * true if this JSON object holds a double value, else false
     */
    bool isDouble() const;

    /**
     * true if this JSON object holds a string value, else false
     */
    bool isString() const;
    bool isNull() const;

    /**
     * if this JSON object holds a map, returns the underlying map. Else throws an exception
     */
    const JSONMap& asMap() const;

    /**
     * if this JSON object holds an array, returns the underlying array. Else throws an exception
     */
    const JSONArray& asArray() const;

    /**
     * if this JSON object holds a boolean value, returns the value. Else throws an exception
     */
    const JSONBoolean& asBoolean() const;

    /**
     * if this JSON object holds a long value, returns the value. Else throws an exception
     */
    const JSONLong& asLong() const;

    /**
     * if this JSON object holds a double value, returns the value. Else throws an exception
     */
    const JSONDouble& asDouble() const;

    /**
     * if this JSON object holds a string value, returns the value. Else throws an exception
     */
    const JSONString& asString() const;
    const JSONNull& asNull() const;

private:
    JSONType _type;
    const union {
        JSONMap mapValue;
        JSONArray arrayValue;
        JSONString stringValue;
        JSONBoolean boolValue;
        JSONLong longValue;
        JSONDouble doubleValue;
        JSONNull nullValue;
    };

    JSON(JSONBoolean);
    JSON(JSONLong);
    JSON(JSONDouble);
    JSON(JSONNull);

    JSON(const JSONString&);
    JSON(const JSONMap&);
    JSON(const JSONArray&);

    JSON(JSONString&&);
    JSON(JSONMap&&);
    JSON(JSONArray&&);

    friend class JSONBuilder;
};

}  // namespace TypeDB
