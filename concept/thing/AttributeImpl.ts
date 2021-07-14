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

import { AttributeType as AttributeTypeProto, Thing as ThingProto } from "typedb-protocol/common/concept_pb";
import { Attribute } from "../../api/concept/thing/Attribute";
import { Thing } from "../../api/concept/thing/Thing";
import { AttributeType } from "../../api/concept/type/AttributeType";
import { ThingType } from "../../api/concept/type/ThingType";
import { TypeDBTransaction } from "../../api/connection/TypeDBTransaction";
import { ErrorMessage } from "../../common/errors/ErrorMessage";
import { TypeDBClientError } from "../../common/errors/TypeDBClientError";
import { RequestBuilder } from "../../common/rpc/RequestBuilder";
import { Bytes } from "../../common/util/Bytes";
import { Stream } from "../../common/util/Stream";
import { AttributeTypeImpl, ThingImpl } from "../../dependencies_internal";
import BAD_VALUE_TYPE = ErrorMessage.Concept.BAD_VALUE_TYPE;
import INVALID_CONCEPT_CASTING = ErrorMessage.Concept.INVALID_CONCEPT_CASTING;

export abstract class AttributeImpl extends ThingImpl implements Attribute {

    private readonly _type: AttributeType;

    protected constructor(iid: string, isInferred: boolean, type: AttributeType) {
        super(iid, isInferred);
        this._type = type;
    }

    abstract asRemote(transaction: TypeDBTransaction): Attribute.Remote;

    isAttribute(): boolean {
        return true;
    }

    getType(): AttributeType {
        return this._type;
    }

    abstract getValue(): boolean | string | number | Date;

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

    asAttribute(): Attribute {
        return this;
    }

    asBoolean(): Attribute.Boolean {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Attribute.Boolean"));
    }

    asLong(): Attribute.Long {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Attribute.Long"));
    }

    asDouble(): Attribute.Double {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Attribute.Double"));
    }

    asString(): Attribute.String {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Attribute.String"));
    }

    asDateTime(): Attribute.DateTime {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Attribute.DateTime"));
    }
}

export namespace AttributeImpl {

    export function of(thingProto: ThingProto): Attribute {
        if (!thingProto) return null;
        const attrType = AttributeTypeImpl.of(thingProto.getType());
        const iid = Bytes.bytesToHexString(thingProto.getIid_asU8());
        const isInferred = thingProto.getInferred();
        switch (thingProto.getType().getValueType()) {
            case AttributeTypeProto.ValueType.BOOLEAN:
                return new AttributeImpl.Boolean(iid, isInferred, attrType.asBoolean(), thingProto.getValue().getBoolean());
            case AttributeTypeProto.ValueType.LONG:
                return new AttributeImpl.Long(iid, isInferred, attrType.asLong(), thingProto.getValue().getLong());
            case AttributeTypeProto.ValueType.DOUBLE:
                return new AttributeImpl.Double(iid, isInferred, attrType.asDouble(), thingProto.getValue().getDouble());
            case AttributeTypeProto.ValueType.STRING:
                return new AttributeImpl.String(iid, isInferred, attrType.asString(), thingProto.getValue().getString());
            case AttributeTypeProto.ValueType.DATETIME:
                return new AttributeImpl.DateTime(iid, isInferred, attrType.asDateTime(), new Date(thingProto.getValue().getDateTime()));
            default:
                throw new TypeDBClientError(BAD_VALUE_TYPE.message(thingProto.getType().getValueType()));
        }
    }

    export abstract class Remote extends ThingImpl.Remote implements Attribute.Remote {

        private readonly _type: AttributeType;

        protected constructor(transaction: TypeDBTransaction.Extended, iid: string, isInferred: boolean, type: AttributeType, ..._: any) {
            super(transaction, iid, isInferred);
            this._type = type;
        }

        abstract asRemote(transaction: TypeDBTransaction): Attribute.Remote;

        getType(): AttributeType {
            return this._type;
        }

        abstract getValue(): boolean | string | number | Date;

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

        isAttribute(): boolean {
            return true;
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

        asAttribute(): Attribute.Remote {
            return this;
        }

        asBoolean(): Attribute.Boolean.Remote {
            throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Attribute.Boolean"));
        }

        asLong(): Attribute.Long.Remote {
            throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Attribute.Long"));
        }

        asDouble(): Attribute.Double.Remote {
            throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Attribute.Double"));
        }

        asString(): Attribute.String.Remote {
            throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Attribute.String"));
        }

        asDateTime(): Attribute.DateTime.Remote {
            throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Attribute.DateTime"));
        }
    }

    export class Boolean extends AttributeImpl implements Attribute.Boolean {
        private readonly _value: boolean;

        constructor(iid: string, isInferred: boolean, type: AttributeType.Boolean, value: boolean) {
            super(iid, isInferred, type);
            this._value = value;
        }

        protected get className(): string {
            return "Attribute.Boolean";
        }

        asRemote(transaction: TypeDBTransaction): Attribute.Boolean.Remote {
            return new AttributeImpl.Boolean.Remote(transaction as TypeDBTransaction.Extended, this.getIID(), this.isInferred(), this.getType(), this.getValue());
        }

        getType(): AttributeType.Boolean {
            return super.getType().asBoolean();
        }

        getValue(): boolean {
            return this._value;
        }

        isBoolean(): boolean {
            return true;
        }

        asBoolean(): Attribute.Boolean {
            return this;
        }
    }

    export namespace Boolean {

        export class Remote extends AttributeImpl.Remote implements Attribute.Boolean.Remote {

            private readonly _value: boolean;

            constructor(transaction: TypeDBTransaction.Extended, iid: string, isInferred: boolean, type: AttributeType.Boolean, value: boolean) {
                super(transaction, iid, isInferred, type);
                this._value = value;
            }

            protected get className(): string {
                return "Attribute.Boolean";
            }

            asRemote(transaction: TypeDBTransaction): Attribute.Boolean.Remote {
                return new AttributeImpl.Boolean.Remote(transaction as TypeDBTransaction.Extended, this.getIID(), this.isInferred(), this.getType(), this.getValue());
            }

            getType(): AttributeType.Boolean {
                return super.getType().asBoolean();
            }

            getValue(): boolean {
                return this._value;
            }

            isBoolean(): boolean {
                return true;
            }

            asBoolean(): Attribute.Boolean.Remote {
                return this;
            }
        }
    }

    export class Long extends AttributeImpl implements Attribute.Long {
        private readonly _value: number;

        constructor(iid: string, isInferred: boolean, type: AttributeType.Long, value: number) {
            super(iid, isInferred, type);
            this._value = value;
        }

        protected get className(): string {
            return "Attribute.Long";
        }

        asRemote(transaction: TypeDBTransaction): Attribute.Long.Remote {
            return new AttributeImpl.Long.Remote(transaction as TypeDBTransaction.Extended, this.getIID(), this.isInferred(), this.getType(), this.getValue());
        }

        getType(): AttributeType.Long {
            return super.getType().asLong();
        }

        getValue(): number {
            return this._value;
        }

        isLong(): boolean {
            return true;
        }

        asLong(): Attribute.Long {
            return this;
        }
    }

    export namespace Long {

        export class Remote extends AttributeImpl.Remote implements Attribute.Long.Remote {
            private readonly _value: number;

            constructor(transaction: TypeDBTransaction.Extended, iid: string, isInferred: boolean, type: AttributeType.Long, value: number) {
                super(transaction, iid, isInferred, type);
                this._value = value;
            }

            protected get className(): string {
                return "Attribute.Long";
            }

            asRemote(transaction: TypeDBTransaction): Attribute.Long.Remote {
                return new AttributeImpl.Long.Remote(transaction as TypeDBTransaction.Extended, this.getIID(), this.isInferred(), this.getType(), this.getValue());
            }

            getType(): AttributeType.Long {
                return super.getType().asLong();
            }

            getValue(): number {
                return this._value;
            }

            isLong(): boolean {
                return true;
            }

            asLong(): Attribute.Long.Remote {
                return this;
            }
        }
    }

    export class Double extends AttributeImpl implements Attribute.Double {
        private readonly _value: number;

        constructor(iid: string, isInferred: boolean, type: AttributeType.Double, value: number) {
            super(iid, isInferred, type);
            this._value = value;
        }

        protected get className(): string {
            return "Attribute.Double";
        }

        asRemote(transaction: TypeDBTransaction): Attribute.Double.Remote {
            return new AttributeImpl.Double.Remote(transaction as TypeDBTransaction.Extended, this.getIID(), this.isInferred(), this.getType(), this.getValue());
        }

        getType(): AttributeType.Double {
            return super.getType().asDouble();
        }

        getValue(): number {
            return this._value;
        }

        isDouble(): boolean {
            return true;
        }

        asDouble(): Attribute.Double {
            return this;
        }
    }

    export namespace Double {

        export class Remote extends AttributeImpl.Remote implements Attribute.Double.Remote {
            private readonly _value: number;

            constructor(transaction: TypeDBTransaction.Extended, iid: string, isInferred: boolean, type: AttributeType.Double, value: number) {
                super(transaction, iid, isInferred, type);
                this._value = value;
            }

            protected get className(): string {
                return "Attribute.Double";
            }

            asRemote(transaction: TypeDBTransaction): Attribute.Double.Remote {
                return new AttributeImpl.Double.Remote(transaction as TypeDBTransaction.Extended, this.getIID(), this.isInferred(), this.getType(), this.getValue());
            }

            getType(): AttributeType.Double {
                return super.getType().asDouble();
            }

            getValue(): number {
                return this._value;
            }

            isDouble(): boolean {
                return true;
            }

            asDouble(): Attribute.Double.Remote {
                return this;
            }
        }
    }

    export class String extends AttributeImpl implements Attribute.String {
        private _value: string;

        constructor(iid: string, isInferred: boolean, type: AttributeType.String, value: string) {
            super(iid, isInferred, type);
            this._value = value;
        }

        protected get className(): string {
            return "Attribute.String";
        }

        asRemote(transaction: TypeDBTransaction): Attribute.String.Remote {
            return new AttributeImpl.String.Remote(transaction as TypeDBTransaction.Extended, this.getIID(), this.isInferred(), this.getType(), this.getValue());
        }

        getType(): AttributeType.String {
            return super.getType().asString();
        }

        getValue(): string {
            return this._value;
        }

        isString(): boolean {
            return true;
        }

        asString(): Attribute.String {
            return this;
        }
    }

    export namespace String {

        export class Remote extends AttributeImpl.Remote implements Attribute.String.Remote {
            private readonly _value: string;

            constructor(transaction: TypeDBTransaction.Extended, iid: string, isInferred: boolean, type: AttributeType.String, value: string) {
                super(transaction, iid, isInferred, type);
                this._value = value;
            }

            protected get className(): string {
                return "Attribute.String";
            }

            asRemote(transaction: TypeDBTransaction): Attribute.String.Remote {
                return new AttributeImpl.String.Remote(transaction as TypeDBTransaction.Extended, this.getIID(), this.isInferred(), this.getType(), this.getValue());
            }

            getType(): AttributeType.String {
                return super.getType().asString();
            }

            getValue(): string {
                return this._value;
            }

            isString(): boolean {
                return true;
            }

            asString(): Attribute.String.Remote {
                return this;
            }
        }
    }

    export class DateTime extends AttributeImpl implements Attribute.DateTime {
        private readonly _value: Date;

        constructor(iid: string, isInferred: boolean, type: AttributeType.DateTime, value: Date) {
            super(iid, isInferred, type);
            this._value = value;
        }

        protected get className(): string {
            return "Attribute.DateTime";
        }

        asRemote(transaction: TypeDBTransaction): Attribute.DateTime.Remote {
            return new AttributeImpl.DateTime.Remote(transaction as TypeDBTransaction.Extended, this.getIID(), this.isInferred(), this.getType(), this.getValue());
        }

        getType(): AttributeType.DateTime {
            return super.getType().asDateTime();
        }

        getValue(): Date {
            return this._value;
        }

        isDateTime(): boolean {
            return true;
        }

        asDateTime(): Attribute.DateTime {
            return this;
        }
    }

    export namespace DateTime {

        export class Remote extends AttributeImpl.Remote implements Attribute.DateTime.Remote {
            private readonly _value: Date;

            constructor(transaction: TypeDBTransaction.Extended, iid: string, isInferred: boolean, type: AttributeType.DateTime, value: Date) {
                super(transaction, iid, isInferred, type);
                this._value = value;
            }

            protected get className(): string {
                return "Attribute.DateTime.Remote";
            }

            asRemote(transaction: TypeDBTransaction): Attribute.DateTime.Remote {
                return new AttributeImpl.DateTime.Remote(transaction as TypeDBTransaction.Extended, this.getIID(), this.isInferred(), this.getType(), this.getValue());
            }

            getType(): AttributeType.DateTime {
                return super.getType().asDateTime();
            }

            getValue(): Date {
                return this._value;
            }

            isDateTime(): boolean {
                return true;
            }

            asDateTime(): Attribute.DateTime.Remote {
                return this;
            }
        }
    }
}
