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

/**
 * Attribute types represent properties that other types can own.
 * Attribute types have a value type. This value type is fixed and unique for every given instance of the attribute type.
 * Other types can own an attribute type. That means that instances of these other types can own an instance of this attribute type.
 * This usually means that an object in our domain has a property with the matching value.
 * Multiple types can own the same attribute type, and different instances of the same type or different types can share ownership of the same attribute instance.
 */
export interface AttributeType extends ThingType {
    /**
     * The <code>ValueType</code> of this <code>AttributeType</code>.
     */
    readonly valueType: ValueType;

    /**
     * Adds and returns an <code>Attribute</code> of this <code>AttributeType</code> with the given value.
     *
     * ### Examples
     *
     * ```ts
     * attribute = attributeType.put(transaction, value)
     * ```
     *
     * @param transaction - The current transaction
     * @param value - New <code>Attribute</code>’s value
     */
    put(transaction: TypeDBTransaction, value: Value): Promise<Attribute>;

    /** {@inheritDoc AttributeType#put} */
    putBoolean(transaction: TypeDBTransaction, value: boolean): Promise<Attribute>;
    /** {@inheritDoc AttributeType#put} */
    putLong(transaction: TypeDBTransaction, value: number): Promise<Attribute>;
    /** {@inheritDoc AttributeType#put} */
    putDouble(transaction: TypeDBTransaction, value: number): Promise<Attribute>;
    /** {@inheritDoc AttributeType#put} */
    putString(transaction: TypeDBTransaction, value: string): Promise<Attribute>;
    /** {@inheritDoc AttributeType#put} */
    putDateTime(transaction: TypeDBTransaction, value: Date): Promise<Attribute>;

    /**
     * Retrieves an <code>Attribute</code> of this <code>AttributeType</code>
     * with the given value if such <code>Attribute</code> exists.
     * Otherwise, returns <code>None</code>.
     *
     * ### Examples
     *
     * ```ts
     * attribute = attributeType.get(transaction, value)
     * ```
     *
     * @param transaction - The current transaction
     * @param value - <code>Attribute</code>’s value
     */
    get(transaction: TypeDBTransaction, value: Value): Promise<Attribute>;
    /** {@inheritDoc AttributeType#get} */
    getBoolean(transaction: TypeDBTransaction, value: boolean): Promise<Attribute>;
    /** {@inheritDoc AttributeType#get} */
    getLong(transaction: TypeDBTransaction, value: number): Promise<Attribute>;
    /** {@inheritDoc AttributeType#get} */
    getDouble(transaction: TypeDBTransaction, value: number): Promise<Attribute>;
    /** {@inheritDoc AttributeType#get} */
    getString(transaction: TypeDBTransaction, value: string): Promise<Attribute>;
    /** {@inheritDoc AttributeType#get} */
    getDateTime(transaction: TypeDBTransaction, value: Date): Promise<Attribute>;

    /** @inheritdoc */
    getSupertype(transaction: TypeDBTransaction): Promise<AttributeType>;
    setSupertype(transaction: TypeDBTransaction, type: AttributeType): Promise<void>;

    /** @inheritdoc */
    getSupertypes(transaction: TypeDBTransaction): Stream<AttributeType>;

    /** @inheritdoc */
    getSubtypes(transaction: TypeDBTransaction): Stream<AttributeType>;
    getSubtypes(transaction: TypeDBTransaction, valueType: ValueType): Stream<AttributeType>;
    getSubtypes(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<AttributeType>;
    /**
    *
    */
    getSubtypes(transaction: TypeDBTransaction, valueType: ValueType, transitivity: Transitivity): Stream<AttributeType>;

    /**
     * Retrieves all direct and indirect (or direct only) <code>Attributes</code>
     * that are instances of this <code>AttributeType</code>.
     *
     * ### Examples
     *
     * ```ts
     * attributeType.getInstances(transaction)
     * attributeType.getInstances(transaction, Transitivity.EXPLICIT)
     * ```
     *
     * @param transaction - The current transaction
     * @param transitivity - <code>Transitivity.TRANSITIVE</code> for direct and indirect subtypes, <code>Transitivity.EXPLICIT</code> for direct subtypes only
     */
    getInstances(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Attribute>;
    getInstances(transaction: TypeDBTransaction): Stream<Attribute>;

    /**
     * Retrieve all <code>Things</code> that own an attribute of this <code>AttributeType</code>.
     * Optionally, filtered by <code>Annotation</code>s.
     *
     * ### Examples
     *
     * ```ts
     * attributeType.getOwners(transaction)
     * attributeType.getOwners(transaction, [Annotation.UNIQUE])
     * attributeType.getOwners(transaction, Transitivity.TRANSITIVE)
     * attributeType.getOwners(transaction, [Annotation.UNIQUE], Transitivity.TRANSITIVE)
     * ```
     *
     * @param transaction - The current transaction
     * @param annotations - Only retrieve <code>ThingTypes</code> that have an attribute of this <code>AttributeType</code> with all given <code>Annotation</code>s
     * @param transitivity - <code>Transitivity.TRANSITIVE</code> for direct and inherited ownership, <code>Transitivity.EXPLICIT</code> for direct ownership only
     */
    getOwners(transaction: TypeDBTransaction, annotations: Annotation[], transitivity: Transitivity): Stream<ThingType>;
    getOwners(transaction: TypeDBTransaction): Stream<ThingType>;
    getOwners(transaction: TypeDBTransaction, annotations: Annotation[]): Stream<ThingType>;
    getOwners(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<ThingType>;

    /**
     * Retrieves the regular expression that is defined for this <code>AttributeType</code>.
     *
     * ### Examples
     *
     * ```ts
     * attributeType.getRegex(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    getRegex(transaction: TypeDBTransaction): Promise<string>;

    /**
     * Sets a regular expression as a constraint for this <code>AttributeType</code>. <code>Values</code> of all <code>Attribute</code>s of this type (inserted earlier or later) should match this regex.
     * Can only be applied for <code>AttributeType</code>s with a <code>string</code> value type.
     *
     * ### Examples
     *
     * ```ts
     * attributeType.setRegex(transaction, regex)
     * ```
     *
     * @param transaction - The current transaction
     * @param regex - Regular expression
     */
    setRegex(transaction: TypeDBTransaction, regex: string): Promise<void>;

    /**
     * Removes the regular expression that is defined for this <code>AttributeType</code>.
     *
     * ### Examples
     *
     * ```ts
     * attributeType.unsetRegex(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    unsetRegex(transaction: TypeDBTransaction): Promise<void>;
}

export namespace AttributeType {

    export const NAME: string = "attribute";

    export function proto(attributeType: AttributeType) {
        return RequestBuilder.Type.AttributeType.protoAttributeType(attributeType.label);
    }
}
