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
#include "typedb/common/native.hpp"

#include "typedb/concept/type/thing_type.hpp"
#include "typedb/concept/value/value.hpp"

namespace TypeDB {

class Attribute;

class AttributeType : public ThingType {
public:
    ValueType getValueType();

    [[nodiscard]] VoidFuture setSupertype(Transaction& transaction, AttributeType* attributeType);

    [[nodiscard]] ConceptPtrFuture<Attribute> put(Transaction& transaction, Value* value);
    [[nodiscard]] ConceptPtrFuture<Attribute> put(Transaction& transaction, const std::string& value);
    [[nodiscard]] ConceptPtrFuture<Attribute> put(Transaction& transaction, int64_t value);
    [[nodiscard]] ConceptPtrFuture<Attribute> put(Transaction& transaction, double value);
    [[nodiscard]] ConceptPtrFuture<Attribute> put(Transaction& transaction, bool value);
    [[nodiscard]] ConceptPtrFuture<Attribute> put(Transaction& transaction, DateTime value);

    [[nodiscard]] VoidFuture setRegex(Transaction& transaction, const std::string& regex);
    [[nodiscard]] VoidFuture unsetRegex(Transaction& transaction);

    ConceptPtrFuture<Attribute> get(Transaction& transaction, Value* value);
    ConceptPtrFuture<Attribute> get(Transaction& transaction, const std::string& value);
    ConceptPtrFuture<Attribute> get(Transaction& transaction, int64_t value);
    ConceptPtrFuture<Attribute> get(Transaction& transaction, double value);
    ConceptPtrFuture<Attribute> get(Transaction& transaction, bool value);
    ConceptPtrFuture<Attribute> get(Transaction& transaction, DateTime value);
    OptionalStringFuture getRegex(Transaction& transaction);

    ConceptIterable<AttributeType> getSubtypes(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);  // Mimic overriding from Type
    ConceptIterable<AttributeType> getSubtypes(Transaction& transaction, ValueType valueType, Transitivity transitivity = Transitivity::TRANSITIVE);
    ConceptIterable<Attribute> getInstances(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);
    ConceptIterable<ThingType> getOwners(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);
    ConceptIterable<ThingType> getOwners(Transaction& transaction, const std::initializer_list<Annotation>& annotations, Transitivity transitivity = Transitivity::TRANSITIVE);
    ConceptIterable<ThingType> getOwners(Transaction& transaction, const std::vector<Annotation>& annotations, Transitivity transitivity = Transitivity::TRANSITIVE);

private:
    AttributeType(_native::Concept* conceptNative);

    friend class ConceptFactory;
};

}  // namespace TypeDB
