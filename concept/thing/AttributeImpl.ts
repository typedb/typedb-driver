/*
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

import {GraknTransaction} from "../../api/GraknTransaction";
import {ThingType} from "../../api/concept/type/ThingType";
import {Attribute} from "../../api/concept/thing/Attribute";
import {Thing} from "../../api/concept/thing/Thing";
import {AttributeType} from "../../api/concept/type/AttributeType";
import {AttributeTypeImpl, RemoteThingImpl, ThingImpl} from "../../dependencies_internal";
import {Bytes} from "../../common/util/Bytes";
import {Stream} from "../../common/util/Stream";
import {RequestBuilder} from "../../common/rpc/RequestBuilder";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import {GraknClientError} from "../../common/errors/GraknClientError";
import {AttributeType as AttributeTypeProto, Thing as ThingProto} from "grakn-protocol/common/concept_pb";
import BAD_VALUE_TYPE = ErrorMessage.Concept.BAD_VALUE_TYPE;

export abstract class AttributeImpl<T extends AttributeType.ValueClass> extends ThingImpl implements Attribute<T> {

    private _type: AttributeType;

    constructor(iid: string, type: AttributeType) {
        super(iid);
        this._type = type;
    }

    abstract asRemote(transaction: GraknTransaction): Attribute.Remote<T>;

    abstract getValue(): T;

    isAttribute(): boolean {
        return true;
    }

    getType(): AttributeType {
        return this._type;
    }

    isBoolean(): boolean {
        return false;
    }

    isDateTime(): boolean {
        return false;
    }

    isDouble(): boolean {
        return false;
    }

    isLong(): boolean {
        return false;
    }

    isString(): boolean {
        return false;
    }

}

export namespace AttributeImpl {

    export function of(thingProto: ThingProto): Attribute<any> {
        if (!thingProto) return null;
        const attrType = AttributeTypeImpl.of(thingProto.getType());
        const iid = Bytes.bytesToHexString(thingProto.getIid_asU8());
        switch (thingProto.getType().getValueType()) {
            case AttributeTypeProto.ValueType.BOOLEAN:
                return new AttributeImpl.Boolean(iid, attrType as AttributeType.Boolean, thingProto.getValue().getBoolean());
            case AttributeTypeProto.ValueType.LONG:
                return new AttributeImpl.Long(iid, attrType as AttributeType.Long, thingProto.getValue().getLong());
            case AttributeTypeProto.ValueType.DOUBLE:
                return new AttributeImpl.Double(iid, attrType as AttributeType.Double, thingProto.getValue().getDouble());
            case AttributeTypeProto.ValueType.STRING:
                return new AttributeImpl.String(iid, attrType as AttributeType.String, thingProto.getValue().getString());
            case AttributeTypeProto.ValueType.DATETIME:
                return new AttributeImpl.DateTime(iid, attrType as AttributeType.DateTime, new Date(thingProto.getValue().getDateTime()));
            default:
                throw new GraknClientError(BAD_VALUE_TYPE.message(thingProto.getType().getValueType()));
        }
    }

    export abstract class RemoteImpl<T extends AttributeType.ValueClass> extends RemoteThingImpl implements Attribute.Remote<T> {

        private _type: AttributeType;

        constructor(transaction: GraknTransaction.Extended, iid: string, type: AttributeType) {
            super(transaction, iid);
            this._type = type;
        }

        abstract getValue(): T;

        abstract asRemote(transaction: GraknTransaction): Attribute.Remote<T>;

        isAttribute(): boolean {
            return true;
        }

        getType(): AttributeType {
            return this._type;
        }

        getOwners(ownerType?: ThingType): Stream<Thing> {
            let request;
            if (!ownerType) {
                request = RequestBuilder.Thing.Attribute.getOwnersReq(this.getIID());
            } else {
                request = RequestBuilder.Thing.Attribute.getOwnersByTypeReq(this.getIID(), ThingType.proto(ownerType));
            }
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getAttributeGetOwnersResPart().getThingsList()))
                .map((thingProto) => ThingImpl.of(thingProto));
        }

        isBoolean(): boolean {
            return false;
        }

        isDateTime(): boolean {
            return false;
        }

        isDouble(): boolean {
            return false;
        }

        isLong(): boolean {
            return false;
        }

        isString(): boolean {
            return false;
        }

    }

    export class Boolean extends AttributeImpl<boolean> implements Attribute.Boolean {
        private _value: boolean;

        constructor(iid: string, type: AttributeType.Boolean, value: boolean) {
            super(iid, type);
            this._value = value;
        }

        asRemote(transaction: GraknTransaction): Attribute.RemoteBoolean {
            return new AttributeImpl.RemoteImpl.Boolean(transaction as GraknTransaction.Extended, this.getIID(), this.getType(), this.getValue());
        }

        getType(): AttributeType.Boolean {
            return super.getType() as AttributeType.Boolean;
        }

        getValue(): boolean {
            return this._value;
        }

        isBoolean(): boolean {
            return true;
        }

    }


    export class Long extends AttributeImpl<number> implements Attribute.Long {
        private _value: number;

        constructor(iid: string, type: AttributeType.Long, value: number) {
            super(iid, type);
            this._value = value;
        }

        asRemote(transaction: GraknTransaction): Attribute.RemoteLong {
            return new AttributeImpl.RemoteImpl.Long(transaction as GraknTransaction.Extended, this.getIID(), this.getType(), this.getValue());
        }

        getType(): AttributeType.Long {
            return super.getType() as AttributeType.Long;
        }

        getValue(): number {
            return this._value;
        }

        isLong(): boolean {
            return true;
        }

    }


    export class Double extends AttributeImpl<number> implements Attribute.Double {
        private _value: number;

        constructor(iid: string, type: AttributeType.Double, value: number) {
            super(iid, type);
            this._value = value;
        }

        asRemote(transaction: GraknTransaction): Attribute.RemoteDouble {
            return new AttributeImpl.RemoteImpl.Double(transaction as GraknTransaction.Extended, this.getIID(), this.getType(), this.getValue());
        }

        getType(): AttributeType.Double {
            return super.getType() as AttributeType.Double;
        }

        getValue(): number {
            return this._value;
        }

        isDouble(): boolean {
            return true;
        }

    }


    export class String extends AttributeImpl<string> implements Attribute.String {
        private _value: string;

        constructor(iid: string, type: AttributeType.String, value: string) {
            super(iid, type);
            this._value = value;
        }

        asRemote(transaction: GraknTransaction): Attribute.RemoteString {
            return new AttributeImpl.RemoteImpl.String(transaction as GraknTransaction.Extended, this.getIID(), this.getType(), this.getValue());
        }

        getType(): AttributeType.String {
            return super.getType() as AttributeType.String;
        }

        getValue(): string {
            return this._value;
        }

        isString(): boolean {
            return true;
        }

    }


    export class DateTime extends AttributeImpl<Date> implements Attribute.DateTime {
        private _value: Date;

        constructor(iid: string, type: AttributeType.DateTime, value: Date) {
            super(iid, type);
            this._value = value;
        }

        asRemote(transaction: GraknTransaction): Attribute.RemoteDateTime {
            return new AttributeImpl.RemoteImpl.DateTime(transaction as GraknTransaction.Extended, this.getIID(), this.getType(), this.getValue());
        }

        getType(): AttributeType.DateTime {
            return super.getType() as AttributeType.DateTime;
        }

        getValue(): Date {
            return this._value;
        }

        isDateTime(): boolean {
            return true;
        }

    }

    export namespace RemoteImpl {

        export class Boolean extends AttributeImpl.RemoteImpl<boolean> implements Attribute.RemoteBoolean {
            private _value: boolean;

            constructor(transaction: GraknTransaction.Extended, iid: string, type: AttributeType.Boolean, value: boolean) {
                super(transaction, iid, type);
                this._value = value;
            }

            asRemote(transaction: GraknTransaction): Attribute.RemoteBoolean {
                return this;
            }

            getType(): AttributeType.Boolean {
                return super.getType() as AttributeType.Boolean;
            }

            getValue(): boolean {
                return this._value;
            }

            isBoolean(): boolean {
                return true;
            }
        }


        export class Double extends AttributeImpl.RemoteImpl<number> implements Attribute.RemoteDouble {
            private _value: number;

            constructor(transaction: GraknTransaction.Extended, iid: string, type: AttributeType.Double, value: number) {
                super(transaction, iid, type);
                this._value = value;
            }

            asRemote(transaction: GraknTransaction): Attribute.RemoteDouble {
                return this;
            }

            getType(): AttributeType.Double {
                return super.getType() as AttributeType.Double;
            }

            getValue(): number {
                return this._value;
            }

            isDouble(): boolean {
                return true;
            }

        }

        export class Long extends AttributeImpl.RemoteImpl<number> implements Attribute.RemoteLong {
            private _value: number;

            constructor(transaction: GraknTransaction.Extended, iid: string, type: AttributeType.Long, value: number) {
                super(transaction, iid, type);
                this._value = value;
            }

            asRemote(transaction: GraknTransaction): Attribute.RemoteLong {
                return this;
            }

            getType(): AttributeType.Long {
                return super.getType() as AttributeType.Long;
            }

            getValue(): number {
                return this._value;
            }

            isLong(): boolean {
                return true;
            }

        }

        export class String extends AttributeImpl.RemoteImpl<string> implements Attribute.RemoteString {
            private _value: string;

            constructor(transaction: GraknTransaction.Extended, iid: string, type: AttributeType.String, value: string) {
                super(transaction, iid, type);
                this._value = value;
            }

            asRemote(transaction: GraknTransaction): Attribute.RemoteString {
                return this;
            }

            getType(): AttributeType.String {
                return super.getType() as AttributeType.String;
            }

            getValue(): string {
                return this._value;
            }

            isString(): boolean {
                return true;
            }

        }


        export class DateTime extends AttributeImpl.RemoteImpl<Date> implements Attribute.RemoteDateTime {
            private _value: Date;

            constructor(transaction: GraknTransaction.Extended, iid: string, type: AttributeType.DateTime, value: Date) {
                super(transaction, iid, type);
                this._value = value;
            }

            asRemote(transaction: GraknTransaction): Attribute.RemoteDateTime {
                return this;
            }

            getType(): AttributeType.DateTime {
                return super.getType() as AttributeType.DateTime;
            }

            getValue(): Date {
                return this._value;
            }

            isDateTime(): boolean {
                return true;
            }

        }

    }

}