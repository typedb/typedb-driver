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

#include <chrono>
#include <ctime>
#include <iomanip>
#include <sstream>

#include "typedb/common/exception.hpp"
#include "typedb/concept/value/value.hpp"

#include "../../common/macros.hpp"
#include "../../common/native.hpp"

namespace TypeDB {

std::string Value::formatDateTime(DateTime t) {
    std::time_t epochSeconds = std::chrono::system_clock::to_time_t(t);
    char buf[32];
    strftime(buf, 32, "%Y-%m-%dT%H:%M:%S", std::gmtime(&epochSeconds));
    return std::string(buf);
}

DateTime Value::parseDateTime(const std::string& s) {
    std::tm tm = {};
    const char* format = s.find("T") != std::string::npos ? "%Y-%m-%dT%H:%M:%S" : "%Y-%m-%d";
#ifdef _MSC_VER
    std::stringstream ss(s);
    ss >> std::get_time(&tm, format);
#else
    strptime(s.c_str(), format, &tm);
#endif
    return TypeDB::DateTime(std::chrono::seconds{std::mktime(&tm)});
}


Value::Value(_native::Concept* conceptNative)
    : Concept(ConceptType::VALUE, conceptNative) {}


bool Value::isBoolean() {
    CHECK_NATIVE(conceptNative);
    return _native::value_is_boolean(conceptNative.get());
}

bool Value::isLong() {
    CHECK_NATIVE(conceptNative);
    return _native::value_is_long(conceptNative.get());
}

bool Value::isDouble() {
    CHECK_NATIVE(conceptNative);
    return _native::value_is_double(conceptNative.get());
}

bool Value::isString() {
    CHECK_NATIVE(conceptNative);
    return _native::value_is_string(conceptNative.get());
}

bool Value::isDateTime() {
    CHECK_NATIVE(conceptNative);
    return _native::value_is_date_time(conceptNative.get());
}

ValueType Value::valueType() {
    if (_native::value_is_boolean(conceptNative.get())) {
        return ValueType::BOOLEAN;
    } else if (_native::value_is_long(conceptNative.get())) {
        return ValueType::LONG;
    } else if (_native::value_is_double(conceptNative.get())) {
        return ValueType::DOUBLE;
    } else if (_native::value_is_string(conceptNative.get())) {
        return ValueType::STRING;
    } else if (_native::value_is_date_time(conceptNative.get())) {
        return ValueType::DATETIME;
    } else throw Utils::exception(InternalError::UNEXPECTED_NATIVE_VALUE);
}

#define CHECK_CAST(EXPECTED)                                                                               \
    {                                                                                                      \
        if (EXPECTED != this->valueType()) throw Utils::exception(InternalError::ILLEGAL_CAST, #EXPECTED); \
    }

bool Value::asBoolean() {
    CHECK_CAST(ValueType::BOOLEAN);
    WRAPPED_NATIVE_CALL(RETURN_IDENTITY, _native::value_get_boolean(conceptNative.get()));
}

int64_t Value::asLong() {
    CHECK_CAST(ValueType::LONG);
    WRAPPED_NATIVE_CALL(RETURN_IDENTITY, _native::value_get_long(conceptNative.get()));
}

double Value::asDouble() {
    CHECK_CAST(ValueType::DOUBLE);
    WRAPPED_NATIVE_CALL(RETURN_IDENTITY, _native::value_get_double(conceptNative.get()));
}

std::string Value::asString() {
    CHECK_CAST(ValueType::STRING);
    WRAPPED_NATIVE_CALL(RETURN_IDENTITY, _native::value_get_string(conceptNative.get()));
}

DateTime Value::asDateTime() {
    CHECK_CAST(ValueType::DATETIME);
    int64_t dateTimeAsMillis = _native::value_get_date_time_as_millis(conceptNative.get());
    DriverException::check_and_throw();
    return DateTime(std::chrono::milliseconds{dateTimeAsMillis});
}

#undef CHECKED_CAST


std::unique_ptr<Value> Value::of(bool value) {
    return std::unique_ptr<Value>(new Value(_native::value_new_boolean(value)));
}

std::unique_ptr<Value> Value::of(int64_t value) {
    return std::unique_ptr<Value>(new Value(_native::value_new_long(value)));
}

std::unique_ptr<Value> Value::of(double value) {
    return std::unique_ptr<Value>(new Value(_native::value_new_double(value)));
}

std::unique_ptr<Value> Value::of(const std::string& value) {
    return std::unique_ptr<Value>(new Value(_native::value_new_string(value.c_str())));
}

std::unique_ptr<Value> Value::of(DateTime value) {
    return std::unique_ptr<Value>(new Value(_native::value_new_date_time_from_millis(value.time_since_epoch().count())));
}

}  // namespace TypeDB
