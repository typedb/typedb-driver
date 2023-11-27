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


#include <iomanip>
#include <iostream>
#include <sstream>

#include "typedb/concept/concept.hpp"

#include "typedb/concept/type/attributetype.hpp"
#include "typedb/concept/type/entitytype.hpp"
#include "typedb/concept/type/relationtype.hpp"
#include "typedb/concept/type/roletype.hpp"
#include "typedb/concept/type/thingtype.hpp"

#include "typedb/concept/thing/attribute.hpp"
#include "typedb/concept/thing/entity.hpp"
#include "typedb/concept/thing/relation.hpp"

#include "typedb/concept/value/value.hpp"

#include "utils.hpp"

namespace TypeDB::BDD {


bool parseBoolean(const std::string& str) {
    return (str == "true");
}

TypeDB::TransactionType parseTransactionType(const std::string& str) {
    assert(str == "read" || str == "write");
    return str == "read" ? TypeDB::TransactionType::READ : TypeDB::TransactionType::WRITE;
}

int64_t parseLong(const std::string& str) {
    return std::stod(str);
}

double parseDouble(const std::string& str) {
    return std::stof(str);
}

TypeDB::DateTime parseDateTime(const std::string& s) {
    return TypeDB::Value::parseDateTime(s);
}

TypeDB::ValueType parseValueType(const std::string& valueTypeStr) {
    if (valueTypeStr == "boolean") return ValueType::BOOLEAN;
    else if (valueTypeStr == "long") return ValueType::LONG;
    else if (valueTypeStr == "double") return ValueType::DOUBLE;
    else if (valueTypeStr == "string") return ValueType::STRING;
    else if (valueTypeStr == "datetime") return ValueType::DATETIME;
    else throw std::runtime_error("Unexpected valueTypeStr in BDD: " + valueTypeStr);
}

std::unique_ptr<TypeDB::Value> parseValueFromString(ValueType valueType, const std::string& str) {
    switch (valueType) {
        case ValueType::BOOLEAN:
            return Value::of(parseBoolean(str));
        case ValueType::LONG:
            return Value::of(parseLong(str));
        case ValueType::DOUBLE:
            return Value::of(parseDouble(str));
        case ValueType::STRING:
            return Value::of(str);
        case ValueType::DATETIME:
            return Value::of(parseDateTime(str));
        default:
            THROW_ILLEGAL_STATE();
    }
}

std::vector<TypeDB::Annotation> parseAnnotation(const std::string& str) {
    std::vector<Annotation> annotations;
    assert(str == "unique" || str == "key");
    annotations.push_back(((str == "key") ? TypeDB::Annotation::key() : TypeDB::Annotation::unique()));
    return annotations;
}

TypeDB::Transitivity parseTransitivity(const std::string& str) {
    return (str.find("explicit") != std::string::npos) ? TypeDB::Transitivity::EXPLICIT : TypeDB::Transitivity::TRANSITIVE;
}


bool compareStringWithDouble(std::string& first, double second) {
    size_t decimalPos = first.find(".");
    size_t nDecimals = (decimalPos < first.size()) ? (first.size() - (decimalPos + 1)) : 3;
    double tolerance = 0.6 / pow(10, nDecimals);
    return (std::stof(first) - second) < tolerance;
}

bool compareStringWithDateTime(std::string& first, TypeDB::DateTime second) {
    return parseDateTime(first) == second;
}


// Concept comparison
std::pair<std::string, std::string> split(std::string str, std::string delimiter) {
    size_t delim = str.find(delimiter);
    return std::make_pair(str.substr(0, delim), str.substr(delim + 1, str.size()));  // delim + 1 -> exclude ':'
}


std::string valueToString(Value* value) {
    std::stringstream ss;
    switch (value->valueType()) {
        case ValueType::BOOLEAN:
            ss << (value->asBoolean() ? "true" : "false");
            break;
        case ValueType::LONG:
            ss << value->asLong();
            break;
        case ValueType::DOUBLE:
            ss << std::fixed << std::setprecision(5) << value->asDouble();
            break;
        case ValueType::STRING:
            ss << value->asString();
            break;
        case ValueType::DATETIME:
            ss << TypeDB::Value::formatDateTime(value->asDateTime());
            break;

        default:
            throw std::runtime_error("Unimplemented for root attribute type?");
    }
    return ss.str();
}

bool checkContains(const cucumber::messages::pickle_table& table, const std::vector<std::string>& toCheckList) {
    return std::all_of(table.rows.begin(), table.rows.end(), [&](const cucumber::messages::pickle_table_row& row) {
        return std::any_of(toCheckList.begin(), toCheckList.end(), [&](const std::string& toCheck) { return toCheck == row.cells[0].value; });
    });
}

bool checkNotContains(const cucumber::messages::pickle_table& table, const std::vector<std::string>& toCheckList) {
    return std::all_of(table.rows.begin(), table.rows.end(), [&](const cucumber::messages::pickle_table_row& row) {
        return std::none_of(toCheckList.begin(), toCheckList.end(), [&](const std::string& toCheck) { return toCheck == row.cells[0].value; });
    });
}

bool checkEqual(const cucumber::messages::pickle_table& table, const std::vector<std::string>& toCheckList) {
    return table.rows.size() == toCheckList.size() && checkContains(table, toCheckList);
}

}  // namespace TypeDB::BDD
