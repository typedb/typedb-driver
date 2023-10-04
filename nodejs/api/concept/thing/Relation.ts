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

import {Stream} from "../../../common/util/Stream";
import {TypeDBTransaction} from "../../connection/TypeDBTransaction";
import {RelationType} from "../type/RelationType";
import {RoleType} from "../type/RoleType";
import {Thing} from "./Thing";
import {RequestBuilder} from "../../../common/rpc/RequestBuilder";

/**
 * Relation is an instance of a relation type and can be uniquely addressed by
 * a combination of its type, owned attributes and role players.
 */
export interface Relation extends Thing {
    /**
     * The type which this <code>Relation</code> belongs to.
     */
    readonly type: RelationType;

    /**
     * Adds a new role player to play the given role in this <code>Relation</code>.
     *
     * ### Examples
     *
     * ```ts
     * relation.addRolePlayer(transaction, roleType, player)
     * ```
     *
     * @param transaction - The current transaction
     * @param roleType - The role to be played by the <code>player</code>
     * @param player - The thing to play the role
     */
    addRolePlayer(transaction: TypeDBTransaction, roleType: RoleType, player: Thing): Promise<void>;

    /**
     * Removes the association of the given instance that plays the given role in this <code>Relation</code>.
     *
     * ### Examples
     *
     * ```ts
     * relation.removeRolePlayer(transaction, roleType, player)
     * ```
     *
     * @param transaction - The current transaction
     * @param roleType - The role to no longer be played by the thing in this <code>Relation</code>
     * @param player - The instance to no longer play the role in this <code>Relation</code>
     */
    removeRolePlayer(transaction: TypeDBTransaction, roleType: RoleType, player: Thing): Promise<void>;

    /**
     * Retrieves all role players of this <code>Relation</code>, optionally filtered by given role types.
     *
     * ### Examples
     *
     * ```ts
     * relation.getPlayersByRoleType(transaction)
     * relation.getPlayersByRoleType(transaction, [roleType1, roleType2])
     * ```
     *
     * @param transaction - The current transaction
     * @param roleTypes - 0 or more role types
     */
    getPlayersByRoleType(transaction: TypeDBTransaction): Stream<Thing>;

    /**
     * Retrieves all role players of this <code>Relation</code>, optionally filtered by given role types.
     *
     * ### Examples
     *
     * ```ts
     * relation.getPlayersByRoleType(transaction)
     * relation.getPlayersByRoleType(transaction, [roleType1, roleType2])
     * ```
     *
     * @param transaction - The current transaction
     * @param roleTypes - 0 or more role types
     */
    getPlayersByRoleType(transaction: TypeDBTransaction, roleTypes: RoleType[]): Stream<Thing>;

    /**
     * Retrieves a mapping of all instances involved in the <code>Relation</code> and the role each play.
     *
     * ### Examples
     *
     * ```ts
     * relation.getRolePlayers(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    getRolePlayers(transaction: TypeDBTransaction): Promise<Map<RoleType, Thing[]>>;

    /**
     * Retrieves all role types currently played in this <code>Relation</code>.
     *
     * ### Examples
     *
     * ```ts
     * relation.getRelating(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    getRelating(transaction: TypeDBTransaction): Stream<RoleType>;
}

export namespace Relation {
    export function proto(relation: Relation) {
        return RequestBuilder.Thing.Relation.protoRelation(relation.iid);
    }
}
