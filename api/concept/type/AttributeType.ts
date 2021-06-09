/*
 * Copyright (C) 2021 Vaticle
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


import {TypeDBTransaction} from "../../connection/TypeDBTransaction";
import {RemoteThingType, ThingType} from "./ThingType";
import {Attribute} from "../thing/Attribute";
import {Stream} from "../../../common/util/Stream";
import {AttributeType as AttributeTypeProto} from "typedb-protocol/common/concept_pb";

export interface AttributeType extends ThingType {

    getValueType(): AttributeType.ValueType;

    isKeyable(): boolean;

    isBoolean(): boolean;

    isLong(): boolean;

    isDouble(): boolean;

    isString(): boolean;

    isDateTime(): boolean;

    asBoolean(): AttributeType.Boolean;

    asLong(): AttributeType.Long;

    asDouble(): AttributeType.Double;

    asString(): AttributeType.String;

    asDateTime(): AttributeType.DateTime;

    asRemote(transaction: TypeDBTransaction): AttributeType.Remote;

}

/* eslint @typescript-eslint/ban-types: "off" */
export namespace AttributeType {

    export interface Remote extends RemoteThingType, AttributeType {

        setSupertype(type: AttributeType): Promise<void>;

        getSubtypes(): Stream<AttributeType>;

        getInstances(): Stream<Attribute<AttributeType.ValueClass>>;

        getOwners(): Stream<ThingType>;

        getOwners(onlyKey: boolean): Stream<ThingType>;

        asBoolean(): AttributeType.RemoteBoolean;

        asLong(): AttributeType.RemoteLong;

        asDouble(): AttributeType.RemoteDouble;

        asString(): AttributeType.RemoteString;

        asDateTime(): AttributeType.RemoteDateTime;

        asRemote(transaction: TypeDBTransaction): AttributeType.Remote;

    }

    export interface Boolean extends AttributeType {

        asRemote(transaction: TypeDBTransaction): AttributeType.RemoteBoolean;

    }

    export interface RemoteBoolean extends AttributeType.Remote, Boolean {

        asRemote(transaction: TypeDBTransaction): RemoteBoolean;

        // TODO avoid this re-declaration typing workaround
        asBoolean(): RemoteBoolean;

        asLong(): RemoteLong;

        asDouble(): RemoteDouble;

        asString(): RemoteString;

        asDateTime(): RemoteDateTime;

        setSupertype(type: Boolean): Promise<void>;

        getSubtypes(): Stream<Boolean>;

        getInstances(): Stream<Attribute.Boolean>;

        put(value: boolean): Promise<Attribute.Boolean>;

        get(value: boolean): Promise<Attribute.Boolean>;

    }

    export interface Long extends AttributeType {

        asRemote(transaction: TypeDBTransaction): AttributeType.RemoteLong;

    }

    export interface RemoteLong extends AttributeType.Remote, Long {

        asRemote(transaction: TypeDBTransaction): RemoteLong;

        // TODO avoid this re-declaration typing workaround
        asBoolean(): RemoteBoolean;

        asLong(): RemoteLong;

        asDouble(): RemoteDouble;

        asString(): RemoteString;

        asDateTime(): RemoteDateTime;

        setSupertype(type: Long): Promise<void>;

        getSubtypes(): Stream<Long>;

        getInstances(): Stream<Attribute.Long>;

        put(value: number): Promise<Attribute.Long>;

        get(value: number): Promise<Attribute.Long>;

    }

    export interface Double extends AttributeType {

        asRemote(transaction: TypeDBTransaction): AttributeType.RemoteDouble;

    }

    export interface RemoteDouble extends AttributeType.Remote, Double {
        asRemote(transaction: TypeDBTransaction): RemoteDouble;

        // TODO avoid this re-declaration typing workaround
        asBoolean(): RemoteBoolean;

        asLong(): RemoteLong;

        asDouble(): RemoteDouble;

        asString(): RemoteString;

        asDateTime(): RemoteDateTime;

        setSupertype(type: Double): Promise<void>;

        getSubtypes(): Stream<Double>;

        getInstances(): Stream<Attribute.Double>;

        put(value: number): Promise<Attribute.Double>;

        get(value: number): Promise<Attribute.Double>;
    }

    export interface String extends AttributeType {

        asRemote(transaction: TypeDBTransaction): AttributeType.RemoteString;

    }

    export interface RemoteString extends AttributeType.Remote, String {

        asRemote(transaction: TypeDBTransaction): RemoteString;

        // TODO avoid this re-declaration typing workaround
        asBoolean(): RemoteBoolean;

        asLong(): RemoteLong;

        asDouble(): RemoteDouble;

        asString(): RemoteString;

        asDateTime(): RemoteDateTime;

        setSupertype(type: String): Promise<void>;

        getSubtypes(): Stream<String>;

        getInstances(): Stream<Attribute.String>;

        put(value: string): Promise<Attribute.String>;

        get(value: string): Promise<Attribute.String>;

        getRegex(): Promise<string>;

        setRegex(regex: string): Promise<void>;

    }

    export interface DateTime extends AttributeType {

        asRemote(transaction: TypeDBTransaction): AttributeType.RemoteDateTime;

    }

    export interface RemoteDateTime extends AttributeType.Remote, DateTime {

        asRemote(transaction: TypeDBTransaction): RemoteDateTime;

        // TODO avoid this re-declaration typing workaround
        asBoolean(): RemoteBoolean;

        asLong(): RemoteLong;

        asDouble(): RemoteDouble;

        asString(): RemoteString;

        asDateTime(): RemoteDateTime;

        setSupertype(type: DateTime): Promise<void>;

        getSubtypes(): Stream<DateTime>;

        getInstances(): Stream<Attribute.DateTime>;

        put(value: Date): Promise<Attribute.DateTime>;

        get(value: Date): Promise<Attribute.DateTime>;

    }

    export interface ValueType {

        isKeyable(): boolean;

        isWritable(): boolean;

        proto(): AttributeTypeProto.ValueType;

    }

    export namespace ValueType {

        class ValueTypeImpl implements ValueType {

            private readonly _attrTypeProto: AttributeTypeProto.ValueType;
            private readonly _name: string;

            constructor(type: AttributeTypeProto.ValueType, name: string) {
                this._attrTypeProto = type;
                this._name = name;
            }

            proto(): AttributeTypeProto.ValueType {
                return this._attrTypeProto;
            }

            isKeyable(): boolean {
                return [AttributeTypeProto.ValueType.LONG, AttributeTypeProto.ValueType.STRING, AttributeTypeProto.ValueType.DATETIME].includes(this._attrTypeProto);
            }

            isWritable(): boolean {
                return this._attrTypeProto !== AttributeTypeProto.ValueType.OBJECT;
            }

            toString() {
                return "ValueType[" + this._name + "]";
            }
        }

        export const OBJECT = new ValueTypeImpl(AttributeTypeProto.ValueType.OBJECT, "OBJECT");
        export const BOOLEAN = new ValueTypeImpl(AttributeTypeProto.ValueType.BOOLEAN, "BOOLEAN");
        export const LONG = new ValueTypeImpl(AttributeTypeProto.ValueType.LONG, "LONG");
        export const DOUBLE = new ValueTypeImpl(AttributeTypeProto.ValueType.DOUBLE, "DOUBLE");
        export const STRING = new ValueTypeImpl(AttributeTypeProto.ValueType.STRING, "STRING");
        export const DATETIME = new ValueTypeImpl(AttributeTypeProto.ValueType.DATETIME, "DATETIME");

    }

    export type ValueClass = number | string | boolean | Date;
}
