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

#include "typedb/concept/type/type.hpp"

namespace TypeDB {

class RoleType : public Type {
public:
    std::string getName();
    std::string getScope();

    // Inherited
    virtual std::string getLabel() override;
    [[nodiscard]] virtual VoidFuture setLabel(Transaction& transaction, const std::string& newLabel) override;
    virtual bool isAbstract() override;

    [[nodiscard]] virtual VoidFuture deleteType(Transaction& transaction) override;
    [[nodiscard]] virtual BoolFuture isDeleted(Transaction& transaction) override;

    // Mimic overriding from Type
    ConceptPtrFuture<RoleType> getSupertype(Transaction& transaction);
    ConceptIterable<RoleType> getSupertypes(Transaction& transaction);
    ConceptIterable<RoleType> getSubtypes(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

    ConceptPtrFuture<RelationType> getRelationType(Transaction& transaction);
    ConceptIterable<RelationType> getRelationTypes(Transaction& transaction);
    ConceptIterable<ThingType> getPlayerTypes(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);
    ConceptIterable<Relation> getRelationInstances(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);
    ConceptIterable<Thing> getPlayerInstances(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

private:
    RoleType(_native::Concept* conceptNative);

    friend class ConceptFactory;
};

}  // namespace TypeDB
