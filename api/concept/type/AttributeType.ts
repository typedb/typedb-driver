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
import {Attribute} from "../thing/Attribute";
import {ThingType} from "./ThingType";
import {Concept} from "../Concept";
import {Value} from "../value/Value";
import {RequestBuilder} from "../../../common/rpc/RequestBuilder";
import Annotation = ThingType.Annotation;
import Transitivity = Concept.Transitivity;
import ValueType = Concept.ValueType;

export interface AttributeType extends ThingType {
    readonly valueType: ValueType;

    put(transaction: TypeDBTransaction, value: Value): Promise<Attribute>;
    putBoolean(transaction: TypeDBTransaction, value: boolean): Promise<Attribute>;
    putLong(transaction: TypeDBTransaction, value: number): Promise<Attribute>;
    putDouble(transaction: TypeDBTransaction, value: number): Promise<Attribute>;
    putString(transaction: TypeDBTransaction, value: string): Promise<Attribute>;
    putDateTime(transaction: TypeDBTransaction, value: Date): Promise<Attribute>;

    get(transaction: TypeDBTransaction, value: Value): Promise<Attribute>;
    getBoolean(transaction: TypeDBTransaction, value: boolean): Promise<Attribute>;
    getLong(transaction: TypeDBTransaction, value: number): Promise<Attribute>;
    getDouble(transaction: TypeDBTransaction, value: number): Promise<Attribute>;
    getString(transaction: TypeDBTransaction, value: string): Promise<Attribute>;
    getDateTime(transaction: TypeDBTransaction, value: Date): Promise<Attribute>;

    getSupertype(transaction: TypeDBTransaction): Promise<AttributeType>;
    setSupertype(transaction: TypeDBTransaction, type: AttributeType): Promise<void>;

    getSupertypes(transaction: TypeDBTransaction): Stream<AttributeType>;

    getSubtypes(transaction: TypeDBTransaction): Stream<AttributeType>;
    getSubtypes(transaction: TypeDBTransaction, valueType: ValueType): Stream<AttributeType>;
    getSubtypes(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<AttributeType>;
    getSubtypes(transaction: TypeDBTransaction, valueType: ValueType, transitivity: Transitivity): Stream<AttributeType>;

    getInstances(transaction: TypeDBTransaction): Stream<Attribute>;
    getInstances(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Attribute>;

    getOwners(transaction: TypeDBTransaction): Stream<ThingType>;
    getOwners(transaction: TypeDBTransaction, annotations: Annotation[]): Stream<ThingType>;
    getOwners(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<ThingType>;
    getOwners(transaction: TypeDBTransaction, annotations: Annotation[], transitivity: Transitivity): Stream<ThingType>;

    getRegex(transaction: TypeDBTransaction): Promise<string>;
    setRegex(transaction: TypeDBTransaction, regex: string): Promise<void>;
    unsetRegex(transaction: TypeDBTransaction): Promise<void>;
}

export namespace AttributeType {
    export function proto(attributeType: AttributeType) {
        return RequestBuilder.Type.AttributeType.protoAttributeType(attributeType.label);
    }
}
