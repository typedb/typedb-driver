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

import {RequestBuilder} from "../../../common/rpc/RequestBuilder";
import {Stream} from "../../../common/util/Stream";
import {TypeDBTransaction} from "../../connection/TypeDBTransaction";
import {Relation} from "../thing/Relation";
import {Thing} from "../thing/Thing";
import {RelationType} from "./RelationType";
import {ThingType} from "./ThingType";
import {Type} from "./Type";
import {Concept} from "../Concept";
import Transitivity = Concept.Transitivity;

/**
 * Roles are special internal types used by relations. We can not create an instance of a role in a database. But we can set an instance of another type (role player) to play a role in a particular instance of a relation type.
 * Roles allow a schema to enforce logical constraints on types of role players.
 */
export interface RoleType extends Type {
    /** @inheritDoc */
    getSupertype(transaction: TypeDBTransaction): Promise<RoleType | null>;

    /** @inheritDoc */
    getSupertypes(transaction: TypeDBTransaction): Stream<RoleType>;

    /** @inheritDoc */
    getSubtypes(transaction: TypeDBTransaction): Stream<RoleType>;

    /**
     * Retrieves the <code>RelationType</code> that this role is directly related to.
     *
     * ### Examples
     *
     * ```ts
     * roleType.getRelationType(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    getRelationType(transaction: TypeDBTransaction): Promise<RelationType>;

    /**
     * Retrieves <code>RelationType</code>s that this role is related to (directly or indirectly).
     *
     * ### Examples
     *
     * ```ts
     * roleType.getRelationTypes(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    getRelationTypes(transaction: TypeDBTransaction): Stream<RelationType>;

    /** {@inheritDoc RoleType#getPlayerTypes:(1)} */
    getPlayerTypes(transaction: TypeDBTransaction): Stream<ThingType>;
    /**
     * Retrieves the <code>ThingType</code>s whose instances play this role.
     *
     * ### Examples
     *
     * ```ts
     * roleType.getPlayerTypes(transaction, transitivity)
     * ```
     *
     * @param transaction - The current transaction
     * @param transitivity - <code>Transitivity.TRANSITIVE</code> for direct and indirect playing, <code>Transitivity.EXPLICIT</code> for direct playing only
     */
    getPlayerTypes(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<ThingType>;

    /** {@inheritDoc RoleType#getRelationInstances:(1)} */
    getRelationInstances(transaction: TypeDBTransaction): Stream<Relation>;
    /**
     * Retrieves the <code>Relation</code> instances that this role is related to.
     *
     * ### Examples
     *
     * ```ts
     * roleType.getRelationInstances(transaction, transitivity)
     * ```
     *
     * @param transaction - The current transaction
     * @param transitivity - <code>Transitivity.TRANSITIVE</code> for direct and indirect relation, <code>Transitivity.EXPLICIT</code> for direct relation only
     */
    getRelationInstances(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Relation>;

    /** {@inheritDoc RoleType#getPlayerInstances:(1)} */
    getPlayerInstances(transaction: TypeDBTransaction): Stream<Thing>;
    /**
     * Retrieves the <code>Thing</code> instances that play this role.
     *
     * ### Examples
     *
     * ```ts
     * roleType.getPlayerInstances(transaction, transitivity)
     * ```
     *
     * @param transaction - The current transaction
     * @param transitivity - <code>Transitivity.TRANSITIVE</code> for direct and indirect playing, <code>Transitivity.EXPLICIT</code> for direct playing only
     */
    getPlayerInstances(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Thing>;
}

export namespace RoleType {

    export const NAME = "relation:role";

    export function proto(roleType: RoleType) {
        return RequestBuilder.Type.RoleType.protoRoleType(roleType.label);
    }
}
