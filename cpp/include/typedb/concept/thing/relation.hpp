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

/**
 * \brief Relation is an instance of a relation type.
 *
 * Relation is an instance of a relation type and can be uniquely addressed
 * by a combination of its type, owned attributes and role players.
 */
class Relation : public Thing {
public:
    /**
     * Retrieves the type which this <code>Relation</code> belongs to.
     *
     * <h3>Examples</h3>
     * <pre>
     * relation.getType();
     * </pre>
     */
    std::unique_ptr<RelationType> getType();

    /**
     * Adds a new role player to play the given role in this <code>Relation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relation.addPlayer(transaction, roleType, player).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleType The role to be played by the <code>player</code>
     * @param player The thing to play the role
     */
    [[nodiscard]] VoidFuture addPlayer(Transaction& transaction, RoleType* roleType, Thing* player);

    /**
     * Removes the association of the given instance that plays the given role in this <code>Relation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relation.removePlayer(transaction, roleType, player).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleType The role to no longer be played by the thing in this <code>Relation</code>
     * @param player The instance to no longer play the role in this <code>Relation</code>
     */
    [[nodiscard]] VoidFuture removePlayer(Transaction& transaction, RoleType* roleType, Thing* player);

    /**
     * Retrieves all role types currently played in this <code>Relation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relation.getRelating(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    ConceptIterable<RoleType> getRelating(Transaction& transaction);

    /**
     * Retrieves all role players of this <code>Relation</code>, optionally filtered by given role types.
     *
     * <h3>Examples</h3>
     * <pre>
     * relation.getPlayersByRoleType(transaction, roleTypes);
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleTypes 0 or more role types
     */
    ConceptIterable<Thing> getPlayersByRoleType(Transaction& transaction, const std::vector<std::unique_ptr<RoleType>>& roleTypes);

    /**
     * See \ref Relation::getPlayersByRoleType(Transaction&, const std::vector<std::unique_ptr<RoleType>>&) "getPlayersByRoleType"
     */
    ConceptIterable<Thing> getPlayersByRoleType(Transaction& transaction, const std::vector<RoleType*>& roleTypes);

    /**
     * Retrieves a mapping of all instances involved in the <code>Relation</code> and the role each play.
     *
     * <h3>Examples</h3>
     * <pre>
     * relation.getPlayers(transaction)
     * </pre>
     *
     * @param transaction The current transaction
     */
    std::map<std::unique_ptr<RoleType>, std::unique_ptr<Thing>> getPlayers(Transaction& transaction);

protected:
    virtual _native::Concept* getTypeNative() override;

private:
    Relation(_native::Concept* conceptNative);

    friend class ConceptFactory;
};

}  // namespace TypeDB
