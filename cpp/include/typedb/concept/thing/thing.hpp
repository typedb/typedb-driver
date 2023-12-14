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

#include "typedb/concept/concept.hpp"

namespace TypeDB {

class Thing : public Concept {
public:
    std::string getIID();
    std::unique_ptr<ThingType> getType();
    bool isInferred();
    BoolFuture isDeleted(Transaction& transaction);
    VoidFuture deleteThing(Transaction& transaction);

    [[nodiscard]] VoidFuture setHas(Transaction& transaction, Attribute* attribute);
    [[nodiscard]] VoidFuture unsetHas(Transaction& transaction, Attribute* attribute);

    ConceptIterable<Attribute> getHas(Transaction& transaction, const std::initializer_list<Annotation>& annotations = {});
    ConceptIterable<Attribute> getHas(Transaction& transaction, const AttributeType* attribute);
    ConceptIterable<Attribute> getHas(Transaction& transaction, const std::vector<std::unique_ptr<AttributeType>>& attributeTypes);
    ConceptIterable<Attribute> getHas(Transaction& transaction, const std::vector<const AttributeType*>& attributeTypes);
    ConceptIterable<Attribute> getHas(Transaction& transaction, const std::vector<Annotation>& annotations);

    ConceptIterable<Relation> getRelations(Transaction& transaction);
    ConceptIterable<Relation> getRelations(Transaction& transaction, const std::vector<std::unique_ptr<RoleType>>& roleTypes);
    ConceptIterable<Relation> getRelations(Transaction& transaction, const std::vector<RoleType*>& roleTypes);
    ConceptIterable<RoleType> getPlaying(Transaction& transaction);

protected:
    Thing(ConceptType conceptType, _native::Concept* conceptNative);
    virtual _native::Concept* getTypeNative() = 0;

    friend class ConceptFactory;
};

}  // namespace TypeDB
