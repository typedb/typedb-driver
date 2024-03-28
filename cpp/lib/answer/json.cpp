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

#include "nlohmann/json.hpp"

#include "typedb/answer/json.hpp"

#include "typedb/common/error_message.hpp"

#include "../common/macros.hpp"
#include "../common/native.hpp"
#include "../common/utils.hpp"

namespace TypeDB {

const char* jsonTypeNames[] = {
    "JSONType::INVALID",

    "JSONType::MAP",
    "JSONType::ARRAY",

    "JSONType::BOOLEAN",
    "JSONType::LONG",
    "JSONType::DOUBLE",
    "JSONType::STRING",
    "JSONType::NULL_VALUE",
};

#define NAME(X) jsonTypeNames[(int)(X)]

JSON::JSON(const JSON& from) {
    *this = from;
}

JSON& JSON::operator=(const JSON& from) {
    switch (from._type) {
        case JSONType::STRING: {
            new (&stringValue) JSONString();
            stringValue = from.stringValue;
            break;
        }
        case JSONType::ARRAY: {
            new (&arrayValue) JSONArray();
            arrayValue = from.arrayValue;
            break;
        }
        case JSONType::MAP: {
            new (&mapValue) JSONMap();
            mapValue = from.mapValue;
            break;
        }
        case JSONType::BOOLEAN: {
            boolValue = from.boolValue;
            break;
        }
        case JSONType::LONG: {
            longValue = from.longValue;
            break;
        }
        case JSONType::DOUBLE: {
            doubleValue = from.doubleValue;
            break;
        }
        case JSONType::NULL_VALUE: {
            nullValue = from.nullValue;
            break;
        }
        case JSONType::INVALID: {
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
            new (&stringValue) JSONString();
            stringValue = std::move(from.stringValue);
            break;
        }
        case JSONType::ARRAY: {
            new (&arrayValue) JSONArray();
            arrayValue = std::move(from.arrayValue);
            break;
        }
        case JSONType::MAP: {
            new (&mapValue) JSONMap();
            mapValue = std::move(from.mapValue);
            break;
        }
        case JSONType::BOOLEAN: {
            boolValue = std::move(from.boolValue);
            break;
        }
        case JSONType::LONG: {
            longValue = std::move(from.longValue);
            break;
        }
        case JSONType::DOUBLE: {
            doubleValue = std::move(from.doubleValue);
            break;
        }
        case JSONType::NULL_VALUE: {
            nullValue = std::move(from.nullValue);
            break;
        }
        case JSONType::INVALID: {
            break;
        }
    }
    _type = from._type;
    from._type = JSONType::INVALID;
    return *this;
}

JSON::JSON(JSONBoolean b)
    : _type(JSONType::BOOLEAN), boolValue(b) {
}

JSON::JSON(JSONLong l)
    : _type(JSONType::LONG), longValue(l) {
}

JSON::JSON(JSONDouble d)
    : _type(JSONType::DOUBLE), doubleValue(d) {
}

JSON::JSON(JSONNull n)
    : _type(JSONType::NULL_VALUE), nullValue(n) {
}

JSON::JSON(const JSONString& str)
    : _type(JSONType::STRING), stringValue(str) {
}

JSON::JSON(const JSONMap& from)
    : _type(JSONType::MAP), mapValue(from) {}

JSON::JSON(const JSONArray& from)
    : _type(JSONType::ARRAY), arrayValue(from) {}


JSON::JSON(JSONString&& str)
    : _type(JSONType::STRING), stringValue(std::move(str)) {
}
JSON::JSON(JSONMap&& from)
    : _type(JSONType::MAP), mapValue(std::move(from)) {}

JSON::JSON(JSONArray&& from)
    : _type(JSONType::ARRAY), arrayValue(std::move(from)) {}


JSON::~JSON() {
    switch (_type) {
        case JSONType::STRING: {
            stringValue.~JSONString();
            break;
        }
        case JSONType::ARRAY: {
            arrayValue.~JSONArray();
            break;
        }
        case JSONType::MAP: {
            mapValue.~JSONMap();
            break;
        }
        case JSONType::INVALID:
        case JSONType::BOOLEAN:
        case JSONType::LONG:
        case JSONType::DOUBLE:
        case JSONType::NULL_VALUE: {
            break;
        }
    }
    _type = JSONType::INVALID;
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

bool JSON::isNull() const {
    return _type == JSONType::NULL_VALUE;
}

const JSONMap& JSON::asMap() const {
    if (!isMap()) throw Utils::exception(DriverError::INVALID_JSON_CAST, NAME(_type), NAME(JSONType::MAP));
    return mapValue;
}
const JSONArray& JSON::asArray() const {
    if (!isArray()) throw Utils::exception(DriverError::INVALID_JSON_CAST, NAME(_type), NAME(JSONType::ARRAY));
    return arrayValue;
}
const JSONBoolean& JSON::asBoolean() const {
    if (!isBoolean()) throw Utils::exception(DriverError::INVALID_JSON_CAST, NAME(_type), NAME(JSONType::BOOLEAN));
    return boolValue;
}
const JSONLong& JSON::asLong() const {
    if (!isLong()) throw Utils::exception(DriverError::INVALID_JSON_CAST, NAME(_type), NAME(JSONType::LONG));
    return longValue;
}
const JSONDouble& JSON::asDouble() const {
    if (!isDouble()) throw Utils::exception(DriverError::INVALID_JSON_CAST, NAME(_type), NAME(JSONType::DOUBLE));
    return doubleValue;
}
const JSONString& JSON::asString() const {
    if (!isString()) throw Utils::exception(DriverError::INVALID_JSON_CAST, NAME(_type), NAME(JSONType::STRING));
    return stringValue;
}
const JSONNull& JSON::asNull() const {
    if (!isNull()) throw Utils::exception(DriverError::INVALID_JSON_CAST, NAME(_type), NAME(JSONType::NULL_VALUE));
    return nullValue;
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
            return JSON((JSONDouble)from.get<double>());
        case nlohmann::json::value_t::string:
            return JSON(from.get<std::string>());

        case nlohmann::json::value_t::object:
            return buildMap(from);
        case nlohmann::json::value_t::array:
            return buildArray(from);

       case nlohmann::json::value_t::null:
            return JSON(JSONNull());

        default:
            THROW_ILLEGAL_STATE;
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

// JSONIterable
TYPEDB_ITERATOR_HELPER_1(
    _native::StringIterator,
    char,
    JSON,
    _native::string_iterator_drop,
    _native::string_iterator_next,
    _native::string_free,
    JSON::parse);

};  // namespace TypeDB
