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
import {Concept} from "../Concept";
import {AttributeType} from "../type/AttributeType";
import {RoleType} from "../type/RoleType";
import {ThingType} from "../type/ThingType";
import {Attribute} from "./Attribute";
import {Relation} from "./Relation";
import {ErrorMessage} from "../../../common/errors/ErrorMessage";
import {TypeDBDriverError} from "../../../common/errors/TypeDBDriverError";
import Annotation = ThingType.Annotation;
import ILLEGAL_STATE = ErrorMessage.Internal.ILLEGAL_STATE;

export interface Thing extends Concept {

    /** Retrieves the unique id of the <code>Thing</code>. */
    readonly iid: string;

    /** Retrieves the type which this <code>Thing</code> belongs to. */
    readonly type: ThingType;

    /** Checks if this <code>Thing</code> is inferred by a [Reasoning Rule]. */
    readonly inferred: boolean;

    /**
     * Deletes this <code>Thing</code>.
     *
     * ### Examples
     *
     * ```ts
     * thing.delete(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    delete(transaction: TypeDBTransaction): Promise<void>;


    /**
     * Checks if this <code>Thing</code> is deleted.
     *
     * ### Examples
     *
     * ```ts
     * thing.isDeleted(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    isDeleted(transaction: TypeDBTransaction): Promise<boolean>;

    /** {@inheritDoc Thing#getHas:(4)} */
    getHas(transaction: TypeDBTransaction): Stream<Attribute>;

    /** {@inheritDoc Thing#getHas:(4)} */
    getHas(transaction: TypeDBTransaction, annotations: Annotation[]): Stream<Attribute>;

    /** {@inheritDoc Thing#getHas:(4)} */
    getHas(transaction: TypeDBTransaction, attributeType: AttributeType): Stream<Attribute>;

    /** {@inheritDoc Thing#getHas:(4)} */
    getHas(transaction: TypeDBTransaction, attributeTypes: AttributeType[]): Stream<Attribute>;

    /**
     * Retrieves the <code>Attribute</code>s that this <code>Thing</code> owns. Optionally, filtered by an <code>AttributeType</code> or a list of <code>AttributeType</code>s. Optionally, filtered by <code>Annotation</code>s.
     *
     * ### Examples
     *
     * ```ts
     * thing.getHas(transaction)
     * thing.getHas(transaction, attributeType, [Annotation.KEY])
     * ```
     *
     * @param transaction - The current transaction
     * @param attributeType - The <code>AttributeType</code> to filter the attributes by
     * @param attributeTypes - The <code>AttributeType</code>s to filter the attributes by
     * @param annotations - Only retrieve attributes with all given <code>Annotation</code>s
     */
    getHas(transaction: TypeDBTransaction, attributeTypes: AttributeType[], annotations: Annotation[]): Stream<Attribute>;

    /**
     * Assigns an <code>Attribute</code> to be owned by this <code>Thing</code>.
     *
     * ### Examples
     *
     * ```ts
     * thing.setHas(transaction, attribute)
     * ```
     *
     * @param transaction - The current transaction
     * @param attribute - The <code>Attribute</code> to be owned by this <code>Thing</code>.
     */
    setHas(transaction: TypeDBTransaction, attribute: Attribute): Promise<void>;

    /**
     * Unassigns an <code>Attribute</code> from this <code>Thing</code>.
     *
     * ### Examples
     *
     * ```ts
     * thing.unsetHas(transaction, attribute)
     * ```
     *
     * @param transaction - The current transaction
     * @param attribute - The <code>Attribute</code> to be disowned from this <code>Thing</code>.
     */
    unsetHas(transaction: TypeDBTransaction, attribute: Attribute): Promise<void>;

    /** {@inheritDoc Thing#getRelations:(1)} */
    getRelations(transaction: TypeDBTransaction): Stream<Relation>;

    /**
     * Retrieves all the <code>Relations</code> which this <code>Thing</code> plays a role in, optionally filtered by one or more given roles.
     *
     * ### Examples
     *
     * ```ts
     * thing.getRelations(transaction, roleTypes)
     * ```
     *
     * @param transaction - The current transaction
     * @param roleTypes - The list of roles to filter the relations by.
     */
    getRelations(transaction: TypeDBTransaction, roleTypes: RoleType[]): Stream<Relation>;

    /**
     * Retrieves the roles that this <code>Thing</code> is currently playing.
     *
     * ### Examples
     *
     * ```ts
     * thing.getPlaying(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    getPlaying(transaction: TypeDBTransaction): Stream<RoleType>;
}

export namespace Thing {
    export function proto(thing: Thing) {
        if (thing.isEntity()) return RequestBuilder.Thing.protoThingEntity(thing.iid);
        else if (thing.isRelation()) return RequestBuilder.Thing.protoThingRelation(thing.iid);
        else if (thing.isAttribute()) return RequestBuilder.Thing.protoThingAttribute(thing.iid);
        else throw new TypeDBDriverError(ILLEGAL_STATE.message());
    }
}
