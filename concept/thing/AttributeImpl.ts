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

import {TypeDBTransaction} from "../../api/connection/TypeDBTransaction";
import {ThingType} from "../../api/concept/type/ThingType";
import {Attribute} from "../../api/concept/thing/Attribute";
import {Thing} from "../../api/concept/thing/Thing";
import {AttributeType} from "../../api/concept/type/AttributeType";
import {AttributeTypeImpl, RemoteThingImpl, ThingImpl} from "../../dependencies_internal";
import {Bytes} from "../../common/util/Bytes";
import {Stream} from "../../common/util/Stream";
import {RequestBuilder} from "../../common/rpc/RequestBuilder";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import {TypeDBClientError} from "../../common/errors/TypeDBClientError";
import {AttributeType as AttributeTypeProto, Thing as ThingProto} from "typedb-protocol/common/concept_pb";
import BAD_VALUE_TYPE = ErrorMessage.Concept.BAD_VALUE_TYPE;

export abstract class AttributeImpl<T extends AttributeType.ValueClass> extends ThingImpl implements Attribute<T> {

    private readonly _type: AttributeType;

    protected constructor(iid: string, isInferred: boolean, type: AttributeType) {
        super(iid, isInferred);
        this._type = type;
    }

    abstract asRemote(transaction: TypeDBTransaction): Attribute.Remote<T>;

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
        const isInferred = thingProto.getInferred();
        switch (thingProto.getType().getValueType()) {
            case AttributeTypeProto.ValueType.BOOLEAN:
                return new AttributeImpl.Boolean(iid, isInferred, attrType as AttributeType.Boolean, thingProto.getValue().getBoolean());
            case AttributeTypeProto.ValueType.LONG:
                return new AttributeImpl.Long(iid, isInferred, attrType as AttributeType.Long, thingProto.getValue().getLong());
            case AttributeTypeProto.ValueType.DOUBLE:
                return new AttributeImpl.Double(iid, isInferred, attrType as AttributeType.Double, thingProto.getValue().getDouble());
            case AttributeTypeProto.ValueType.STRING:
                return new AttributeImpl.String(iid, isInferred, attrType as AttributeType.String, thingProto.getValue().getString());
            case AttributeTypeProto.ValueType.DATETIME:
                return new AttributeImpl.DateTime(iid, isInferred, attrType as AttributeType.DateTime, new Date(thingProto.getValue().getDateTime()));
            default:
                throw new TypeDBClientError(BAD_VALUE_TYPE.message(thingProto.getType().getValueType()));
        }
    }

    export abstract class RemoteImpl<T extends AttributeType.ValueClass> extends RemoteThingImpl implements Attribute.Remote<T> {

        private readonly _type: AttributeType;

        protected constructor(transaction: TypeDBTransaction.Extended, iid: string, isInferred: boolean, type: AttributeType) {
            super(transaction, iid, isInferred);
            this._type = type;
        }

        abstract getValue(): T;

        abstract asRemote(transaction: TypeDBTransaction): Attribute.Remote<T>;

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
        private readonly _value: boolean;

        constructor(iid: string, isInferred: boolean, type: AttributeType.Boolean, value: boolean) {
            super(iid, isInferred, type);
            this._value = value;
        }

        asRemote(transaction: TypeDBTransaction): Attribute.RemoteBoolean {
            return new AttributeImpl.RemoteImpl.Boolean(transaction as TypeDBTransaction.Extended, this.getIID(), this.isInferred(), this.getType(), this.getValue());
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
        private readonly _value: number;

        constructor(iid: string, isInferred: boolean, type: AttributeType.Long, value: number) {
            super(iid, isInferred, type);
            this._value = value;
        }

        asRemote(transaction: TypeDBTransaction): Attribute.RemoteLong {
            return new AttributeImpl.RemoteImpl.Long(transaction as TypeDBTransaction.Extended, this.getIID(), this.isInferred(), this.getType(), this.getValue());
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
        private readonly _value: number;

        constructor(iid: string, isInferred: boolean, type: AttributeType.Double, value: number) {
            super(iid, isInferred, type);
            this._value = value;
        }

        asRemote(transaction: TypeDBTransaction): Attribute.RemoteDouble {
            return new AttributeImpl.RemoteImpl.Double(transaction as TypeDBTransaction.Extended, this.getIID(), this.isInferred(), this.getType(), this.getValue());
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

        constructor(iid: string, isInferred: boolean, type: AttributeType.String, value: string) {
            super(iid, isInferred, type);
            this._value = value;
        }

        asRemote(transaction: TypeDBTransaction): Attribute.RemoteString {
            return new AttributeImpl.RemoteImpl.String(transaction as TypeDBTransaction.Extended, this.getIID(), this.isInferred(), this.getType(), this.getValue());
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
        private readonly _value: Date;

        constructor(iid: string, isInferred: boolean, type: AttributeType.DateTime, value: Date) {
            super(iid, isInferred, type);
            this._value = value;
        }

        asRemote(transaction: TypeDBTransaction): Attribute.RemoteDateTime {
            return new AttributeImpl.RemoteImpl.DateTime(transaction as TypeDBTransaction.Extended, this.getIID(), this.isInferred(), this.getType(), this.getValue());
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
            private readonly _value: boolean;

            constructor(transaction: TypeDBTransaction.Extended, iid: string, isInferred: boolean, type: AttributeType.Boolean, value: boolean) {
                super(transaction, iid, isInferred, type);
                this._value = value;
            }

            asRemote(transaction: TypeDBTransaction): Attribute.RemoteBoolean {
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
            private readonly _value: number;

            constructor(transaction: TypeDBTransaction.Extended, iid: string, isInferred: boolean, type: AttributeType.Double, value: number) {
                super(transaction, iid, isInferred, type);
                this._value = value;
            }

            asRemote(transaction: TypeDBTransaction): Attribute.RemoteDouble {
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
            private readonly _value: number;

            constructor(transaction: TypeDBTransaction.Extended, iid: string, isInferred: boolean, type: AttributeType.Long, value: number) {
                super(transaction, iid, isInferred, type);
                this._value = value;
            }

            asRemote(transaction: TypeDBTransaction): Attribute.RemoteLong {
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
            private readonly _value: string;

            constructor(transaction: TypeDBTransaction.Extended, iid: string, isInferred: boolean, type: AttributeType.String, value: string) {
                super(transaction, iid, isInferred, type);
                this._value = value;
            }

            asRemote(transaction: TypeDBTransaction): Attribute.RemoteString {
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
            private readonly _value: Date;

            constructor(transaction: TypeDBTransaction.Extended, iid: string, isInferred: boolean, type: AttributeType.DateTime, value: Date) {
                super(transaction, iid, isInferred, type);
                this._value = value;
            }

            asRemote(transaction: TypeDBTransaction): Attribute.RemoteDateTime {
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
