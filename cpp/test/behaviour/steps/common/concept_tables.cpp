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

#include <algorithm>
#include <sstream>

#include <nlohmann/json.hpp>

#include "concept_tables.hpp"
#include "utils.hpp"

namespace TypeDB::BDD {

using TABLE_ROW = std::map<std::string, TableEntry>;
using CONCEPT_ROW = std::map<std::string, ConceptEntry>;

void debugDump(ResultTable<ConceptEntry> res);
void debugDump(ResultTable<TableEntry> res);
std::string tableRowToString(TABLE_ROW& tableRow);
std::string conceptRowToString(CONCEPT_ROW& conceptRow);

TypedValue typedValueFromValue(Value* value) {
    assert(value != nullptr);
    TypedValue t;
    switch (value->valueType()) {
        case ValueType::BOOLEAN:
            t.type = ValueType::BOOLEAN;
            t.boolValue = value->asBoolean();
            break;
        case ValueType::LONG:
            t.type = ValueType::LONG;
            t.longValue = value->asLong();
            break;
        case ValueType::DOUBLE:
            t.type = ValueType::DOUBLE;
            t.doubleValue = value->asDouble();
            break;
        case ValueType::STRING:
            t.type = ValueType::STRING;
            t.stringValue = value->asString();
            break;
        case ValueType::DATETIME:
            t.type = ValueType::DATETIME;
            t.dateTimeValue = value->asDateTime();
            break;
        default:
            THROW_ILLEGAL_STATE(__FUNCTION__);
    }
    return t;
}

std::string typedValueToString(TypedValue& tv) {
    std::stringstream ss;
    switch (tv.type) {
        case ValueType::BOOLEAN:
            ss << tv.boolValue;
            break;
        case ValueType::LONG:
            ss << tv.longValue;
            break;
        case ValueType::DOUBLE:
            ss << tv.doubleValue;
            break;
        case ValueType::STRING:
            ss << tv.stringValue;
            break;
        case ValueType::DATETIME:
            ss << Value::formatDateTime(tv.dateTimeValue);
            break;

        case ValueType::OBJECT:
            ss << tv.stringValue;
            break;
        default:
            THROW_ILLEGAL_STATE(__FUNCTION__);
    }
    return ss.str();
}

ConceptEntry conceptEntryFrom(Transaction& tx, Concept* concept) {
    assert(concept != nullptr);
    std::string conceptType;
    std::string valueType;
    TypedValue value;
    switch (concept->getConceptType()) {
        case ConceptType::VALUE: {
            conceptType = "value";
            switch (concept->asValue()->valueType()) {
                case ValueType::BOOLEAN:
                    valueType = "boolean";
                    break;
                case ValueType::LONG:
                    valueType = "long";
                    break;
                case ValueType::DOUBLE:
                    valueType = "double";
                    break;
                case ValueType::STRING:
                    valueType = "string";
                    break;
                case ValueType::DATETIME:
                    valueType = "datetime";
                    break;
                default:
                    THROW_ILLEGAL_STATE(__FUNCTION__);
            }
            value = typedValueFromValue(concept->asValue());
            break;
        }
        case ConceptType::ATTRIBUTE: {
            auto keyOpt = findKeyForThing(tx, concept);
            if (!keyOpt.has_value()) {
                conceptType = "attr";
                valueType = findTypeLabelForAttribute(concept->asAttribute());
                value = typedValueFromValue(concept->asAttribute()->getValue().get());
                break;
            }  // else fall through!
        }
        case ConceptType::ENTITY:
        case ConceptType::RELATION: {
            conceptType = "key";
            auto labelTv = findKeyForThing(tx, concept).value();
            valueType = labelTv.first;
            value = labelTv.second;
            break;
        }
        case ConceptType::ENTITY_TYPE:
        case ConceptType::ATTRIBUTE_TYPE:
        case ConceptType::RELATION_TYPE:
        case ConceptType::ROOT_THING_TYPE: {
            conceptType = "label";
            value.type = ValueType::OBJECT;
            value.stringValue = concept->asThingType()->getLabel();
            valueType = value.stringValue;
            break;
        }
        case ConceptType::ROLE_TYPE: {
            conceptType = "label";
            valueType = concept->asRoleType()->getScope();
            value.type = ValueType::OBJECT;
            value.stringValue = concept->asRoleType()->getName();
            break;
        }
        default:
            THROW_ILLEGAL_STATE(__FUNCTION__);
    }
    return ConceptEntry{conceptType, valueType, value};
}

TableEntry tableEntryFrom(std::string& tableEntry) {
    auto firstSplit = split(tableEntry, ":");
    auto secondSplit = split(firstSplit.second, ":");
    return {firstSplit.first, secondSplit.first, secondSplit.second};
}

bool compareStringToTypedValue(std::string& s, TypedValue& tv) {
    switch (tv.type) {
        case ValueType::BOOLEAN:
            return parseBoolean(s) == tv.boolValue;
        case ValueType::LONG:
            return parseLong(s) == tv.longValue;
        case ValueType::DOUBLE:
            return compareStringWithDouble(s, tv.doubleValue);
        case ValueType::STRING:
            return s == tv.stringValue;
        case ValueType::DATETIME:
            return compareStringWithDateTime(s, tv.dateTimeValue);

        case ValueType::OBJECT:
            return s == tv.stringValue;
        default:
            THROW_ILLEGAL_STATE(__FUNCTION__);
    }
}

bool compareEntries(TableEntry& tableEntry, ConceptEntry& conceptEntry) {
    if (tableEntry.conceptType == conceptEntry.entryType && tableEntry.valueType == conceptEntry.valueType) {
        return compareStringToTypedValue(tableEntry.value, conceptEntry.value);
    } else return false;
}

std::string findTypeLabelForAttribute(Attribute* attr) {
    return attr->getType()->getLabel();
}

std::optional<std::pair<std::string, TypedValue>> findKeyForThing(Transaction& tx, Concept* thing) {
    std::vector<Annotation> annotationVector;
    annotationVector.push_back(Annotation::key());
    auto keyIterable = thing->asThing()->getHas(tx, annotationVector);
    auto keyIt = keyIterable.begin();
    if (keyIt != nullptr) {
        std::string label = findTypeLabelForAttribute(keyIt->get()->asAttribute());
        TypedValue tv = typedValueFromValue(keyIt->get()->asAttribute()->getValue().get());
        return std::make_pair(label, tv);
    } else return std::optional<std::pair<std::string, TypedValue>>();
}

ResultTable<TableEntry> dataTabletoMap(cucumber::messages::pickle_table& table) {
    ResultTable<TableEntry> result;
    std::vector<std::string> varNames;
    for (cucumber::messages::pickle_table_cell cell : table.rows[0].cells) {
        varNames.push_back(cell.value);
    }
    for (int i = 1; i < table.rows.size(); i++) {
        auto& row = table.rows[i];
        std::map<std::string, TableEntry> m;
        for (int j = 0; j < varNames.size(); j++) {
            m[varNames[j]] = tableEntryFrom(row.cells[j].value);
        }
        result.push_back(m);
    }
    // debugDump(result);
    return result;
}

ResultTable<ConceptEntry> conceptMapResultToMap(Transaction& tx, std::vector<ConceptMap>& conceptMaps, std::vector<std::string>& varNames) {
    std::vector<std::map<std::string, ConceptEntry>> result;
    if (conceptMaps.size() == 0) return result;
    for (ConceptMap& cm : conceptMaps) {
        std::map<std::string, ConceptEntry> entry;
        for (std::string& varName : varNames) {
            entry[varName] = conceptEntryFrom(tx, cm.get(varName).get());
        }
        result.push_back(entry);
    }
    // debugDump(result);
    return result;
}

ResultTable<ConceptEntry> conceptMapGroupResultToMap(Transaction& tx, std::vector<ConceptMapGroup>& conceptMapGroups, std::vector<std::string>& varNames) {
    ResultTable<ConceptEntry> result;
    for (auto& group : conceptMapGroups) {
        ConceptMapIterable conceptMapIterable = group.conceptMaps();
        std::vector<ConceptMap> groupConceptMaps;
        for (auto& groupConceptMap : conceptMapIterable) {
            groupConceptMaps.push_back(std::move(groupConceptMap));
        }
        for (auto& withoutOwner : conceptMapResultToMap(tx, groupConceptMaps, varNames)) {
            withoutOwner["owner"] = conceptEntryFrom(tx, group.owner().get());
            result.push_back(withoutOwner);
        }
    }
    // debugDump(result);
    return result;
}

ResultTable<ConceptEntry> valueGroupResultToMap(Transaction& tx, std::vector<ValueGroup>& valueGroups) {
    ResultTable<ConceptEntry> result;
    for (auto& group : valueGroups) {
        CONCEPT_ROW conceptEntryMap;
        conceptEntryMap["owner"] = conceptEntryFrom(tx, group.owner().get());
        conceptEntryMap["value"] = conceptEntryFrom(tx, group.value().value().get());
        result.push_back(conceptEntryMap);
    }
    // debugDump(result);
    return result;
}

// Answer evaluation

bool compareRow(TABLE_ROW& tableRow, CONCEPT_ROW& conceptRow) {
    for (auto& tableKV : tableRow) {
        if (!compareEntries(tableKV.second, conceptRow[tableKV.first])) return false;
    }
    return true;
}

bool compareJSON(nlohmann::json& first, nlohmann::json& second);

bool compareJSONArrayUnordered(nlohmann::json& first, nlohmann::json& second) {
    // WE have
    assert(first.is_array() && second.is_array());
    if (first.size() != second.size()) return false;
    for (auto& f : first) {
        if (std::none_of(second.begin(), second.end(), [&](auto& s) { return compareJSON(f, s); })) {
            return false;
        }
    }
    return true;
}

bool compareJSON(nlohmann::json& first, nlohmann::json& second) {
    if (first.type() != second.type()) return false;

    if (first.is_primitive()) {
        return first == second;
    } else if (first.is_array()) {
        return compareJSONArrayUnordered(first, second);
    } else if (first.is_object()) {
        if (first.size() != second.size()) return false;
        std::map<std::string, nlohmann::json*> idx;
        std::transform(second.items().begin(), second.items().end(), std::inserter(idx, idx.end()), [](auto& item) { return std::make_pair(item.key(), &item.value()); });
        return std::all_of(first.items().begin(), first.items().end(), [&](auto& item) { return idx.find(item.key()) != idx.end() && compareJSON(item.value(), *idx[item.key()]); });
    } else throw std::runtime_error("UNIMPLEMENTED");
}

bool compareResults(ResultTable<TableEntry>& table, ResultTable<ConceptEntry>& result) {
    if (table.size() != result.size()) return false;
    else if (table.size() == 0) return true;

    for (CONCEPT_ROW& conceptRow : result) {
        if (std::none_of(table.begin(), table.end(), [&](auto& tableRow) { return compareRow(tableRow, conceptRow); })) {
            std::stringstream ss;
            for (auto& ce : conceptRow)
                ss << ce.first << " : \"" << conceptEntryToString(ce.second) << "\" |";
            std::cout << "Did not find matching rows for " << ss.str() << std::endl;
            debugDump(table);
            debugDump(result);
            return false;
        }
    }
    return true;
}

bool compareResults(std::string& expectedUnparsed, std::vector<JSONString>& actualVector) {
    using namespace nlohmann;
    json expected = json::parse(expectedUnparsed);
    json actual = json::array();
    std::transform(actualVector.begin(), actualVector.end(), std::back_inserter(actual), [](auto& a) { return json::parse(a); });
    return compareJSONArrayUnordered(expected, actual);
}

bool compareResultsValueGroup(ResultTable<TableEntry>& table, ResultTable<ConceptEntry>& result) {
    if (table.size() != result.size()) return false;
    else if (table.size() == 0) return true;

    for (CONCEPT_ROW& conceptRow : result) {
        if (std::none_of(table.begin(), table.end(), [&](auto& tableRow) {
                return compareEntries(tableRow["owner"], conceptRow["owner"]) &&
                       compareStringToTypedValue(tableRow["value"].value, conceptRow["value"].value);
            })) {
            return false;
        }
    }
    return true;
}

std::vector<std::string> getVarsFromDataTable(ResultTable<TableEntry>& dataTable) {
    assert(dataTable.size() > 0);
    std::vector<std::string> varNames;
    std::transform(dataTable[0].begin(), dataTable[0].end(), std::back_inserter(varNames), [&](std::pair<std::string, TableEntry> kv) { return kv.first; });
    return varNames;
}

// Debug printing

std::string conceptEntryToString(ConceptEntry& c) {
    std::stringstream ss;
    ss << c.entryType << ":" << c.valueType << ":" << typedValueToString(c.value);
    return ss.str();
}

std::string conceptRowToString(CONCEPT_ROW& conceptRow) {
    std::stringstream ss;
    for (auto& kv : conceptRow) {
        ss << kv.first << " : \"" << conceptEntryToString(kv.second) << "\" |\t";
    }
    return ss.str();
}

std::string tableEntryToString(TableEntry& t) {
    std::stringstream ss;
    ss << t.conceptType << ":" << t.valueType << ":" << t.value;
    return ss.str();
}

std::string tableRowToString(TABLE_ROW& tableRow) {
    std::stringstream ss;
    for (auto& kv : tableRow) {
        ss << kv.first << " : \"" << tableEntryToString(kv.second) << "\" |\t";
    }
    return ss.str();
}

void debugDump(ResultTable<ConceptEntry> res) {
    std::cout << "Start dumping ConceptEntry table" << std::endl;
    for (CONCEPT_ROW& cr : res) {
        std::cout << conceptRowToString(cr) << std::endl;
    }
    std::cout << "End dumping ConceptEntry table" << std::endl;
}

void debugDump(ResultTable<TableEntry> res) {
    std::cout << "Start dumping TableEntry table" << std::endl;
    for (TABLE_ROW& tr : res) {
        std::cout << tableRowToString(tr) << std::endl;
    }
    std::cout << "End dumping TableEntry table" << std::endl;
}

}  // namespace TypeDB::BDD
