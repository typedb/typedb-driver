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

#pragma once

#include "utils.hpp"

using namespace TypeDB;

namespace TypeDB::BDD {

struct TypedValue {
    ValueType type;
    union {
        bool boolValue;
        int64_t longValue;
        double doubleValue;
        DateTime dateTimeValue;
    };
    std::string stringValue;

    TypedValue() {
        type = ValueType::OBJECT;
        longValue = 0;
        stringValue = "";
    }
};

struct TableEntry {
    std::string conceptType;
    std::string valueType;
    std::string value;
};

struct ConceptEntry {
    std::string entryType;
    std::string valueType;
    TypedValue value;
};

// Construction
ConceptEntry conceptEntryFrom(Transaction& tx, Concept* concept);
std::string conceptEntryToString(ConceptEntry& c);
TableEntry tableEntryFrom(std::string& tableEntry);
TypedValue typedValueFromValue(Value* value);
std::string typedValueToString(TypedValue& tv);

bool compareStringToTypedValue(std::string& s, TypedValue& tv);
bool compareEntries(TableEntry& tableEntry, ConceptEntry& conceptId);

// Utils for constructions
std::string findTypeLabelForAttribute(Attribute* attr);
std::optional<std::pair<std::string, TypedValue>> findKeyForThing(Transaction& tx, Concept* thing);  // Assumes a single key type is defined


// Preprocessing for comparison
template <typename CELL>
using ResultTable = std::vector<std::map<std::string, CELL>>;
ResultTable<TableEntry> dataTabletoMap(cucumber::messages::pickle_table& table);
ResultTable<ConceptEntry> conceptMapResultToMap(Transaction& tx, std::vector<ConceptMap>& conceptMaps, std::vector<std::string>& varNames);
ResultTable<ConceptEntry> conceptMapGroupResultToMap(Transaction& tx, std::vector<ConceptMapGroup>& conceptMaps, std::vector<std::string>& varNames);
ResultTable<ConceptEntry> valueGroupResultToMap(Transaction& tx, std::vector<ValueGroup>& conceptMaps);

std::vector<std::string> getVarsFromDataTable(ResultTable<TableEntry>& dataTable);

bool compareRow(std::map<std::string, TableEntry>& tableRow, std::map<std::string, ConceptEntry>& conceptRow);
bool compareResults(ResultTable<TableEntry>& table, ResultTable<ConceptEntry>& result);
bool compareResults(std::string& expectedUnparsed, std::vector<JSON>& actual);
bool compareResultsValueGroup(ResultTable<TableEntry>& table, ResultTable<ConceptEntry>& result);

};  // namespace TypeDB::BDD
