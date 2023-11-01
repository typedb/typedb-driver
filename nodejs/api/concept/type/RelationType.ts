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
import {Concept} from "../Concept";
import {Relation} from "../thing/Relation";
import {RoleType} from "./RoleType";
import {ThingType} from "./ThingType";
import {RequestBuilder} from "../../../common/rpc/RequestBuilder";
import Transitivity = Concept.Transitivity;

export interface RelationType extends ThingType {
    /**
     * Creates and returns an instance of this <code>RelationType</code>.
     *
     * ### Examples
     *
     * ```ts
     * relationType.create(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    create(transaction: TypeDBTransaction): Promise<Relation>;

    /** @inheritDoc */
    getSupertype(transaction: TypeDBTransaction): Promise<RelationType | null>;
    /** @inheritDoc */
    setSupertype(transaction: TypeDBTransaction, type: RelationType): Promise<void>;

    /** @inheritDoc */
    getSupertypes(transaction: TypeDBTransaction): Stream<RelationType>;

    /** @inheritDoc */
    getSubtypes(transaction: TypeDBTransaction): Stream<RelationType>;
    /** @inheritDoc */
    getSubtypes(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<RelationType>;

    /** @inheritDoc */
    getInstances(transaction: TypeDBTransaction): Stream<Relation>;
    /** @inheritDoc */
    getInstances(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Relation>;

    /** RelationType#getRelates:(1) */
    getRelates(transaction: TypeDBTransaction): Stream<RoleType>;
    /**
     * Retrieves roles that this <code>RelationType</code> relates to directly or via inheritance. If <code>role_label</code> is given, returns a corresponding <code>RoleType</code> or <code>None</code>.
     *
     * ### Examples
     *
     * ```ts
     * relationType.getRelates(transaction, roleLabel, transitivity)
     * ```
     *
     * @param transaction - The current transaction
     * @param roleLabel - Label of the role we wish to retrieve (optional)
     * @param transitivity - <code>Transitivity.TRANSITIVE</code> for direct and inherited relates, <code>Transitivity.EXPLICIT</code> for direct relates only
     */
    getRelates(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<RoleType>;

    getRelatesForRoleLabel(transaction: TypeDBTransaction, roleLabel: string): Promise<RoleType | null>;

    /**
     * Retrieves a <code>RoleType</code> that is overridden by the role with the <code>role_label</code>.
     *
     * ### Examples
     *
     * ```ts
     * relationType.getRelatesOverridden(transaction, roleLabel)
     * ```
     *
     * @param transaction - The current transaction
     * @param roleLabel - Label of the role that overrides an inherited role
     */
    getRelatesOverridden(transaction: TypeDBTransaction, roleLabel: string): Promise<RoleType | null>;

    /**
     * Sets the new role that this <code>RelationType</code> relates to. If we are setting an overriding type this way, we have to also pass the overridden type as a second argument.
     *
     * ### Examples
     *
     * ```ts
     * relationType.setRelates(transaction, roleLabel)
     * relationType.setRelates(transaction, roleLabel, overriddenLabel)
     * ```
     *
     * @param transaction - The current transaction
     * @param roleLabel - The new role for the <code>RelationType</code> to relate to
     * @param overriddenLabel - The label being overridden, if applicable
     */
    setRelates(transaction: TypeDBTransaction, roleLabel: string, overriddenLabel?: string): Promise<void>;
    /**
     * Disallows this <code>RelationType</code> from relating to the given role.
     *
     * ### Examples
     *
     * ```ts
     * relationType.unsetRelates(transaction, roleLabel)
     * ```
     *
     * @param transaction - The current transaction
     * @param roleLabel - The role to not relate to the relation type.
     */
    unsetRelates(transaction: TypeDBTransaction, roleLabel: string): Promise<void>;
}

export namespace RelationType {

    export const NAME = "relation";

    export function proto(relationType: RelationType) {
        return RequestBuilder.Type.RelationType.protoRelationType(relationType.label);
    }
}
