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

import {Attribute} from "./thing/Attribute";
import {Entity} from "./thing/Entity";
import {Relation} from "./thing/Relation";
import {Thing} from "./thing/Thing";
import {AttributeType} from "./type/AttributeType";
import {EntityType} from "./type/EntityType";
import {RelationType} from "./type/RelationType";
import {RoleType} from "./type/RoleType";
import {ThingType} from "./type/ThingType";
import {Type} from "./type/Type";
import {TypeTransitivity as TransitivityProto, ValueType as ValueTypeProto} from "typedb-protocol/proto/concept";
import {Value} from "./value/Value";

export interface Concept {
    /**
     * Checks if the concept is a <code>Type</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.isType()
     * ```
     */
    isType(): boolean;

    /**
     * Checks if the concept is a <code>RoleType</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.isRoleType()
     * ```
     */
    isRoleType(): boolean;
    /**
     * Checks if the concept is a <code>ThingType</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.isThingType()
     * ```
     */
    isThingType(): boolean;

    /**
     * Checks if the concept is an <code>EntityType</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.isEntityType()
     * ```
     */
    isEntityType(): boolean;
    /**
     * Checks if the concept is a <code>RelationType</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.isRelationType()
     * ```
     */
    isRelationType(): boolean;
    /**
     * Checks if the concept is an <code>AttributeType</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.isAttributeType()
     * ```
     */
    isAttributeType(): boolean;

    /**
     * Checks if the concept is a <code>Thing</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.isThing()
     * ```
     */
    isThing(): boolean;

    /**
     * Checks if the concept is an <code>Entity</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.isEntity()
     * ```
     */
    isEntity(): boolean;
    /**
     * Checks if the concept is a <code>Relation</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.isRelation()
     * ```
     */
    isRelation(): boolean;
    /**
     * Checks if the concept is an <code>Attribute</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.isAttribute()
     * ```
     */
    isAttribute(): boolean;

    /**
     * Checks if the concept is a <code>Value</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.isValue()
     * ```
     */
    isValue(): boolean;

    /**
     * Casts the concept to <code>Type</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.asType()
     * ```
     */
    asType(): Type;

    /**
     * Casts the concept to <code>ThingType</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.asThingType()
     * ```
     */
    asThingType(): ThingType;
    /**
     * Casts the concept to <code>RoleType</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.asRoleType()
     * ```
     */
    asRoleType(): RoleType;

    /**
     * Casts the concept to <code>EntityType</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.asEntityType()
     * ```
     */
    asEntityType(): EntityType;
    /**
     * Casts the concept to <code>RelationType</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.asRelationType()
     * ```
     */
    asRelationType(): RelationType;
    /**
     * Casts the concept to <code>AttributeType</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.asAttributeType()
     * ```
     */
    asAttributeType(): AttributeType;

    /**
     * Casts the concept to <code>Thing</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.asThing()
     * ```
     */
    asThing(): Thing;

    /**
     * Casts the concept to <code>Entity</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.asEntity()
     * ```
     */
    asEntity(): Entity;
    /**
     * Casts the concept to <code>Relation</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.asRelation()
     * ```
     */
    asRelation(): Relation;
    /**
     * Casts the concept to <code>Attribute</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.asAttribute()
     * ```
     */
    asAttribute(): Attribute;

    /**
     * Casts the concept to <code>Value</code>.
     *
     * ### Examples
     *
     * ```ts
     * concept.asValue()
     * ```
     */
    asValue(): Value;

    /**
    * Checks if this concept is equal to the argument <code>concept</code>.
    * @param concept - The concept to compare to.
    */
    equals(concept: Concept): boolean;

    /**
     * Retrieves the concept as JSON.
     *
     * ### Examples
     *
     * ```ts
     * concept.toJSONRecord()
     * ```
     */
    toJSONRecord(): Record<string, boolean | string | number>;
}

export namespace Concept {
    export class ValueType {
        private readonly _proto: ValueTypeProto;
        private readonly _name: string;

        constructor(type: ValueTypeProto, name: string) {
            this._proto = type;
            this._name = name;
        }

        proto(): ValueTypeProto {
            return this._proto;
        }

        name(): string {
            return this._name.toLowerCase();
        }

        toString() {
            return "ValueType[" + this._name + "]";
        }
    }

    /** TypeQL value types for attributes & value concepts. */
    export namespace ValueType {
        export const OBJECT = new ValueType(ValueTypeProto.OBJECT, "OBJECT");
        export const BOOLEAN = new ValueType(ValueTypeProto.BOOLEAN, "BOOLEAN");
        export const LONG = new ValueType(ValueTypeProto.LONG, "LONG");
        export const DOUBLE = new ValueType(ValueTypeProto.DOUBLE, "DOUBLE");
        export const STRING = new ValueType(ValueTypeProto.STRING, "STRING");
        export const DATETIME = new ValueType(ValueTypeProto.DATETIME, "DATETIME");

        export function of(proto: ValueTypeProto): ValueType {
            switch (proto) {
                case ValueTypeProto.OBJECT: return OBJECT;
                case ValueTypeProto.BOOLEAN: return BOOLEAN;
                case ValueTypeProto.LONG: return LONG;
                case ValueTypeProto.DOUBLE: return DOUBLE;
                case ValueTypeProto.STRING: return STRING;
                case ValueTypeProto.DATETIME: return DATETIME;
            }
        }
    }

    export class Transitivity {
        private readonly _transitivity: TransitivityProto;

        constructor(transitivity: TransitivityProto) {
            this._transitivity = transitivity;
        }

        proto(): TransitivityProto {
            return this._transitivity;
        }
    }

    export namespace Transitivity {
        export const TRANSITIVE = new Transitivity(TransitivityProto.TRANSITIVE);
        export const EXPLICIT = new Transitivity(TransitivityProto.EXPLICIT);
    }
}
