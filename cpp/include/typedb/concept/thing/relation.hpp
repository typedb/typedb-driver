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

#include <map>

#include "typedb/concept/thing/thing.hpp"

namespace TypeDB {

class Relation : public Thing {
public:
    std::unique_ptr<RelationType> getType();

    [[nodiscard]] VoidFuture addPlayer(Transaction& transaction, RoleType* roleType, Thing* player);
    [[nodiscard]] VoidFuture removePlayer(Transaction& transaction, RoleType* roleType, Thing* player);

    ConceptIterable<RoleType> getRelating(Transaction& transaction);
    ConceptIterable<Thing> getPlayersByRoleType(Transaction& transaction, const std::vector<std::unique_ptr<RoleType>>& roleTypes);
    ConceptIterable<Thing> getPlayersByRoleType(Transaction& transaction, const std::vector<RoleType*>& roleTypes);
    std::map<std::unique_ptr<RoleType>, std::unique_ptr<Thing>> getPlayers(Transaction& transaction);

protected:
    virtual _native::Concept* getTypeNative() override;

private:
    Relation(_native::Concept* conceptNative);

    friend class ConceptFactory;
};

}  // namespace TypeDB
