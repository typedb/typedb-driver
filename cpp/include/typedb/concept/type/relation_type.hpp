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

#include "typedb/concept/type/thing_type.hpp"

namespace TypeDB {

class RelationType : public ThingType {
public:
    [[nodiscard]] ConceptPtrFuture<Relation> create(Transaction& transaction);
    [[nodiscard]] VoidFuture setSupertype(Transaction& transaction, RelationType* superRelationType);

    [[nodiscard]] VoidFuture setRelates(Transaction& transaction, const std::string& roleLabel);
    [[nodiscard]] VoidFuture setRelates(Transaction& transaction, const std::string& roleLabel, RoleType* overriddenType);
    [[nodiscard]] VoidFuture setRelates(Transaction& transaction, const std::string& roleLabel, const std::string& overriddenLabel);

    [[nodiscard]] VoidFuture unsetRelates(Transaction& transaction, RoleType* roleType);
    [[nodiscard]] VoidFuture unsetRelates(Transaction& transaction, const std::string& roleLabel);

    ConceptIterable<Relation> getInstances(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);
    ConceptIterable<RelationType> getSubtypes(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

    ConceptIterable<RoleType> getRelates(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);
    ConceptPtrFuture<RoleType> getRelates(Transaction& transaction, const std::string& roleLabel);
    ConceptPtrFuture<RoleType> getRelatesOverridden(Transaction& transaction, RoleType* roleType);
    ConceptPtrFuture<RoleType> getRelatesOverridden(Transaction& transaction, const std::string& roleLabel);

private:
    RelationType(_native::Concept* conceptNative);

    friend class ConceptFactory;
    VoidFuture setRelatesNative(Transaction& transaction, const char* roleLabel, const char* overriddenRoleLabel);
};

}  // namespace TypeDB
