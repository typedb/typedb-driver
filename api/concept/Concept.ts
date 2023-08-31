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
    isType(): boolean;

    isRoleType(): boolean;
    isThingType(): boolean;

    isEntityType(): boolean;
    isRelationType(): boolean;
    isAttributeType(): boolean;

    isThing(): boolean;

    isEntity(): boolean;
    isRelation(): boolean;
    isAttribute(): boolean;

    isValue(): boolean;

    asType(): Type;

    asThingType(): ThingType;
    asRoleType(): RoleType;

    asEntityType(): EntityType;
    asRelationType(): RelationType;
    asAttributeType(): AttributeType;

    asThing(): Thing;

    asEntity(): Entity;
    asRelation(): Relation;
    asAttribute(): Attribute;

    asValue(): Value;

    equals(concept: Concept): boolean;

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
