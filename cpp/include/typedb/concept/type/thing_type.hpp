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

class ThingType : public Type {
public:
    virtual std::string getLabel() override;
    [[nodiscard]] virtual VoidFuture setLabel(Transaction& transaction, const std::string& newLabel) override;
    bool isRoot();
    virtual bool isAbstract() override;
    virtual BoolFuture isDeleted(Transaction& transaction) override;
    [[nodiscard]] virtual VoidFuture deleteType(Transaction& transaction) override;

    [[nodiscard]] VoidFuture setAbstract(Transaction& transaction);
    [[nodiscard]] VoidFuture unsetAbstract(Transaction& transaction);

    [[nodiscard]] VoidFuture setOwns(Transaction& transaction, AttributeType* attributeType, const std::initializer_list<Annotation>& annotations = {});
    [[nodiscard]] VoidFuture setOwns(Transaction& transaction, AttributeType* attributeType, const std::vector<Annotation>& annotations);
    [[nodiscard]] VoidFuture setOwns(Transaction& transaction, AttributeType* attributeType, AttributeType* overriddenType, const std::initializer_list<Annotation>& annotations = {});
    [[nodiscard]] VoidFuture setOwns(Transaction& transaction, AttributeType* attributeType, AttributeType* overriddenType, const std::vector<Annotation>& annotations);
    [[nodiscard]] VoidFuture unsetOwns(Transaction& transaction, AttributeType* attributeType);

    [[nodiscard]] VoidFuture setPlays(Transaction& transaction, RoleType* roleType);
    [[nodiscard]] VoidFuture setPlays(Transaction& transaction, RoleType* roleType, RoleType* overriddenRoleType);
    [[nodiscard]] VoidFuture unsetPlays(Transaction& transaction, RoleType* roleType);

    ConceptIterable<RoleType> getPlays(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);
    ConceptPtrFuture<RoleType> getPlaysOverridden(Transaction& transaction, RoleType* roleType);

    ConceptIterable<AttributeType> getOwns(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);
    ConceptIterable<AttributeType> getOwns(Transaction& transaction, const std::initializer_list<Annotation>& annotations, Transitivity transitivity = Transitivity::TRANSITIVE);
    ConceptIterable<AttributeType> getOwns(Transaction& transaction, const std::vector<Annotation>& annotations, Transitivity transitivity = Transitivity::TRANSITIVE);
    ConceptIterable<AttributeType> getOwns(Transaction& transaction, ValueType valueType, Transitivity transitivity = Transitivity::TRANSITIVE);
    ConceptIterable<AttributeType> getOwns(Transaction& transaction, ValueType valueType, const std::initializer_list<Annotation>& annotations, Transitivity transitivity = Transitivity::TRANSITIVE);
    ConceptIterable<AttributeType> getOwns(Transaction& transaction, ValueType valueType, const std::vector<Annotation>& annotations, Transitivity transitivity = Transitivity::TRANSITIVE);
    ConceptPtrFuture<AttributeType> getOwnsOverridden(Transaction& transaction, AttributeType* attributeType);

    StringFuture getSyntax(Transaction& transaction);

    // non-virtual to avoid the complexity of making Iterable covariant
    ConceptPtrFuture<ThingType> getSupertype(Transaction& transaction);
    ConceptIterable<ThingType> getSupertypes(Transaction& transaction);
    ConceptIterable<ThingType> getSubtypes(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

protected:
    ThingType(ConceptType conceptType, _native::Concept* conceptNative);

    friend class ConceptFactory;
};

class RootThingType : public ThingType {
protected:
    RootThingType(_native::Concept* conceptNative);

    friend class ConceptFactory;
};

}  // namespace TypeDB
