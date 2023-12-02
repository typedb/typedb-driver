/*
 * Copyright (C) 2022 Vaticle
help *
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

#include "nlohmann/json.hpp"

#include "typedb/answer/json.hpp"

#include "typedb/common/errormessage.hpp"

#include "inc/utils.hpp"

#include <iostream>
#include <sstream>

namespace TypeDB {

const char* jsonTypeNames[] = {
    "JSONType::NONE",

    "JSONType::MAP",
    "JSONType::ARRAY",

    "JSONType::BOOLEAN",
    "JSONType::LONG",
    "JSONType::DOUBLE",
    "JSONType::STRING",
};

#define NAME(X) jsonTypeNames[(int)(X)]

JSON::JSON()
    : _type(JSONType::NONE), _longValue(0L) {}

JSON::JSON(const JSON& from) {
    *this = from;
}

JSON& JSON::operator=(const JSON& from) {
    switch (from._type) {
       case JSONType::STRING: {
            new(&_stringValue) JSONString();
            _stringValue = from._stringValue;
            break;
        }
        case JSONType::ARRAY: {
            new(&_array) JSONArray();
            _array = from._array;
            break;
        }
        case JSONType::MAP: {
            new(&_map) JSONMap();
            _map = from._map;
            break;
        }
        case JSONType::BOOLEAN: {
            _boolValue = from._boolValue;
            break;
        }
        case JSONType::LONG: {
            _longValue = from._longValue;
            break;
        }
        case JSONType::DOUBLE: {
            _doubleValue = from._doubleValue;
            break;
        }
        case JSONType::NONE: {
            break;
        }
    }
    _type = from._type;
    return *this;
}


JSON::JSON(JSON&& from) {
    *this = std::move(from);
}

JSON& JSON::operator=(JSON&& from) {
    switch (from._type) {
       case JSONType::STRING: {
            new(&_stringValue) JSONString();
            _stringValue = std::move(from._stringValue);
            break;
        }
        case JSONType::ARRAY: {
            new(&_array) JSONArray();
            _array = std::move(from._array);
            break;
        }
        case JSONType::MAP: {
            new(&_map) JSONMap();
            _map = std::move(from._map);
            break;
        }
        case JSONType::BOOLEAN: {
            _boolValue = std::move(from._boolValue);
            break;
        }
        case JSONType::LONG: {
            _longValue = std::move(from._longValue);
            break;
        }
        case JSONType::DOUBLE: {
            _doubleValue = std::move(from._doubleValue);
            break;
        }
        case JSONType::NONE: {
            break;
        }
    }
    _type = from._type;
    from._type = JSONType::NONE;
    return *this;
}

JSON::JSON(JSONBoolean b)
    : _type(JSONType::BOOLEAN), _boolValue(b) {
}

JSON::JSON(JSONLong l)
    : _type(JSONType::LONG), _longValue(l) {
}

JSON::JSON(JSONDouble d)
    : _type(JSONType::DOUBLE), _doubleValue(d) {
}

JSON::JSON(const JSONString& str)
    : _type(JSONType::STRING), _stringValue(str) {
}

JSON::JSON(const JSONMap& from)
    : _type(JSONType::MAP), _map(from) {}

JSON::JSON(const JSONArray& from)
    : _type(JSONType::ARRAY), _array(from) {}


JSON::JSON(JSONString&& str)
    : _type(JSONType::STRING), _stringValue(std::move(str)) {
}
JSON::JSON(JSONMap&& from)
    : _type(JSONType::MAP), _map(std::move(from)) {}

JSON::JSON(JSONArray&& from)
    : _type(JSONType::ARRAY), _array(std::move(from)) {}


JSON::~JSON() {
    switch (_type) {
        case JSONType::STRING: {
            _stringValue.~JSONString();
            break;
        }
        case JSONType::ARRAY: {
            _array.~JSONArray();
            break;
        }
        case JSONType::MAP: {
            _map.~JSONMap();
            break;
        }
        case JSONType::NONE:
        case JSONType::BOOLEAN:
        case JSONType::LONG:
        case JSONType::DOUBLE: {
            break;
        }
    }
    memset(this, 0, sizeof(JSON));
    _type = JSONType::NONE;
}

JSONType JSON::type() const {
    return _type;
}

bool JSON::isMap() const {
    return _type == JSONType::MAP;
}

bool JSON::isArray() const {
    return _type == JSONType::ARRAY;
}

bool JSON::isBoolean() const {
    return _type == JSONType::BOOLEAN;
}

bool JSON::isLong() const {
    return _type == JSONType::LONG;
}

bool JSON::isDouble() const {
    return _type == JSONType::DOUBLE;
}

bool JSON::isString() const {
    return _type == JSONType::STRING;
}

const JSONMap& JSON::map() const {
    if (!isMap()) throw Utils::exception(DriverError::INVALID_JSON_CAST, NAME(_type), NAME(JSONType::MAP));
    return _map;
}
const JSONArray& JSON::array() const {
    if (!isArray()) throw Utils::exception(DriverError::INVALID_JSON_CAST, NAME(_type), NAME(JSONType::ARRAY));
    return _array;
}
const JSONBoolean& JSON::boolValue() const {
    if (!isBoolean()) throw Utils::exception(DriverError::INVALID_JSON_CAST, NAME(_type), NAME(JSONType::BOOLEAN));
    return _boolValue;
}
const JSONLong& JSON::longValue() const {
    if (!isLong()) throw Utils::exception(DriverError::INVALID_JSON_CAST, NAME(_type), NAME(JSONType::LONG));
    return _longValue;
}
const JSONDouble& JSON::doubleValue() const {
    if (!isDouble()) throw Utils::exception(DriverError::INVALID_JSON_CAST, NAME(_type), NAME(JSONType::DOUBLE));
    return _doubleValue;
}
const JSONString& JSON::stringValue() const {
    if (!isString()) throw Utils::exception(DriverError::INVALID_JSON_CAST, NAME(_type), NAME(JSONType::STRING));
    return _stringValue;
}

class JSONBuilder {
   public:
    JSONBuilder();
    JSON build(const nlohmann::json& from);

   private:
    JSON buildMap(const nlohmann::json& from);
    JSON buildArray(const nlohmann::json& from);
};


JSON JSON::parse(const std::string& string) {
    return JSONBuilder().build(nlohmann::json::parse(string));
}

JSONBuilder::JSONBuilder() {}

JSON JSONBuilder::build(const nlohmann::json& from) {
    switch (from.type()) {
        case nlohmann::json::value_t::boolean:
            return JSON((JSONBoolean)from.get<bool>());
        case nlohmann::json::value_t::number_integer:
            return JSON((JSONLong)from.get<int>());
        case nlohmann::json::value_t::number_unsigned:
            return JSON((JSONLong)from.get<unsigned>());
        case nlohmann::json::value_t::number_float:
            return JSON((JSONDouble)from.get<unsigned>());
        case nlohmann::json::value_t::string:
            return JSON(std::move(from.get<std::string>()));

        case nlohmann::json::value_t::object:
            return buildMap(from);
        case nlohmann::json::value_t::array:
            return buildArray(from);

        default:
            throw Utils::exception(InternalError::ILLEGAL_STATE);
    }
}

JSON JSONBuilder::buildMap(const nlohmann::json& from) {
    assert(from.is_object());
    JSONMap m;
    for (auto& item : from.items()) {
        m.emplace(item.key(), build(item.value()));
    }
    return JSON(std::move(m));
}

JSON JSONBuilder::buildArray(const nlohmann::json& from) {
    JSONArray a;
    for (auto& item : from) {
        a.emplace_back(build(item));
    }
    return JSON(std::move(a));
}

std::string JSON::toString() const {
    std::stringstream ss;
    switch (_type) {
        case JSONType::STRING: {
            ss << _stringValue;
            break;
        }
        case JSONType::ARRAY: {
            ss << "<array>";
            break;
        }
        case JSONType::MAP: {
            ss << "<map>";
            break;
        }
        case JSONType::BOOLEAN: {
            ss << _boolValue;
            break;
        }
        case JSONType::LONG: {
            ss << _longValue;
            break;
        }
        case JSONType::DOUBLE: {
            ss << _doubleValue;
            break;
        }
        case JSONType::NONE: {
            ss << "<NONE>";
            break;
        }
    }
    return ss.str();
}

};  // namespace TypeDB
