
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

#include "typedb/concept/thing/relation.hpp"
#include "typedb/common/exception.hpp"
#include "typedb/connection/transaction.hpp"

#include "inc/conceptfactory.hpp"
#include "inc/conceptresultwrapper.hpp"
#include "inc/macros.hpp"

namespace TypeDB {

// This doesn't have to be public
struct RolePlayer {
    std::unique_ptr<RoleType> role;
    std::unique_ptr<Thing> player;

    RolePlayer(_native::RolePlayer* rolePlayerNative);  // frees rolePlayernative
};

using RolePlayerIterator = TypeDBIterator<_native::RolePlayerIterator, _native::RolePlayer, RolePlayer>;
using RolePlayerIterable = TypeDBIterable<_native::RolePlayerIterator, _native::RolePlayer, RolePlayer>;

Relation::Relation(_native::Concept* conceptNative)
    : Thing(ConceptType::RELATION, conceptNative) {}

std::unique_ptr<RelationType> Relation::getType() {
    return ConceptFactory::relationType(getTypeNative());
}

VoidFuture Relation::addPlayer(Transaction& transaction, RoleType* roleType, Thing* player) {
    CONCEPTAPI_CALL(VoidFuture,
                    _native::relation_add_role_player(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::getNative(roleType), ConceptFactory::getNative(player)));
}

VoidFuture Relation::removePlayer(Transaction& transaction, RoleType* roleType, Thing* player) {
    CONCEPTAPI_CALL(VoidFuture, _native::relation_remove_role_player(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::getNative(roleType), ConceptFactory::getNative(player)));
}

ConceptIterable<Thing> Relation::getPlayersByRoleType(Transaction& transaction, const std::vector<RoleType*>& roleTypes) {
    CONCEPTAPI_ITER(Thing, _native::relation_get_players_by_role_type(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::toNativeArray(roleTypes).data()));
}

std::map<std::unique_ptr<RoleType>, std::unique_ptr<Thing>> Relation::getPlayers(Transaction& transaction) {
    auto rolePlayerIterableNative = _native::relation_get_role_players(ConceptFactory::getNative(transaction), conceptNative.get());
    TypeDBDriverException::check_and_throw();
    std::map<std::unique_ptr<RoleType>, std::unique_ptr<Thing>> rolePlayerMap;
    for (RolePlayer& rp : RolePlayerIterable(rolePlayerIterableNative)) {
        rolePlayerMap[std::move(rp.role)] = std::move(rp.player);
    }
    return rolePlayerMap;
}

ConceptIterable<RoleType> Relation::getRelating(Transaction& transaction) {
    CONCEPTAPI_ITER(RoleType, _native::relation_get_relating(ConceptFactory::getNative(transaction), conceptNative.get()));
}

// protected
_native::Concept* Relation::getTypeNative() {
    CHECK_NATIVE(conceptNative);
    return _native::relation_get_type(conceptNative.get());
}

// RolePlayer definitions

RolePlayer::RolePlayer(_native::RolePlayer* rolePlayerNative) {
    role = ConceptFactory::roleType(_native::role_player_get_role_type(rolePlayerNative));
    player = ConceptFactory::thing(_native::role_player_get_player(rolePlayerNative));
    _native::role_player_drop(rolePlayerNative);
}

TYPEDB_ITERATOR_HELPER(
    _native::RolePlayerIterator,
    _native::RolePlayer,
    RolePlayer,
    _native::role_player_iterator_drop,
    _native::role_player_iterator_next,
    _native::role_player_drop);

}  // namespace TypeDB
