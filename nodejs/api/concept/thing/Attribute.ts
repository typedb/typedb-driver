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

import {AttributeType} from "../type/AttributeType";
import {Stream} from "../../../common/util/Stream";
import {Thing} from "./Thing";
import {ThingType} from "../type/ThingType";
import {TypeDBTransaction} from "../../connection/TypeDBTransaction";
import {Concept} from "../Concept";
import {RequestBuilder} from "../../../common/rpc/RequestBuilder";
import ValueType = Concept.ValueType;

/**
 * Attribute is an instance of the attribute type and has a value. This value is fixed and unique for every given instance of the attribute type.
 * Attribute type can be uniquely addressed by its type and value.
 */
export interface Attribute extends Thing {
    /**
     * The type which this <code>Attribute</code> belongs to.
     */
    readonly type: AttributeType;

     /**
     * The type of the value which the <code>Attribute</code> instance holds.
     */
    readonly valueType: ValueType;

    /**
     * The value which the <code>Attribute</code> instance holds.
     */
    readonly value: boolean | number | string | Date;

    /**
     * Retrieves the instances that own this <code>Attribute</code>.
     *
     * ### Examples
     *
     * ```ts
     * attribute.getOwners(transaction)
     * attribute.getOwners(transaction, ownerType)
     * ```
     *
     * @param transaction - The current transaction
     * @param ownerType - If specified, filter results for only owners of the given type
     */
    getOwners(transaction: TypeDBTransaction, ownerType?: ThingType): Stream<Thing>;
}

export namespace Attribute {
    export function proto(relation: Attribute) {
        return RequestBuilder.Thing.Attribute.protoAttribute(relation.iid);
    }
}
