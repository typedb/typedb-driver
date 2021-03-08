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

import { Attribute, RemoteAttribute, BooleanAttribute, DateTimeAttribute, DoubleAttribute, LongAttribute,
    StringAttribute, RemoteBooleanAttribute, RemoteLongAttribute, RemoteStringAttribute, RemoteDoubleAttribute,
    RemoteDateTimeAttribute, ThingImpl, RemoteThingImpl, ThingType, AttributeTypeImpl, BooleanAttributeTypeImpl,
    DateTimeAttributeTypeImpl, DoubleAttributeTypeImpl, LongAttributeTypeImpl, StringAttributeTypeImpl,
    AttributeType, GraknClient, Merge, Stream, ConceptProtoBuilder, GraknClientError, ErrorMessage,
    Bytes } from "../../../dependencies_internal";
import ValueClass = AttributeType.ValueClass;
import Transaction = GraknClient.Transaction;
import ConceptProto from "grakn-protocol/protobuf/concept_pb";

export abstract class AttributeImpl<T extends ValueClass> extends ThingImpl implements Attribute<T> {
    protected constructor(iid: string) {
        super(iid);
    }

    abstract asRemote(transaction: Transaction): RemoteAttribute<T>;

    abstract getType(): AttributeTypeImpl;

    abstract getValue(): T;

    isBoolean(): boolean {
        return false;
    }

    isString(): boolean {
        return false;
    }

    isDouble(): boolean {
        return false;
    }

    isLong(): boolean {
        return false;
    }

    isDateTime(): boolean {
        return false;
    }

    isAttribute(): boolean {
        return true;
    }
}

export abstract class RemoteAttributeImpl<T extends ValueClass> extends RemoteThingImpl implements RemoteAttribute<T> {
    protected constructor(transaction: Transaction, iid: string) {
        super(transaction, iid);
    }

    getOwners(ownerType?: ThingType): Stream<ThingImpl> {
        const getOwnersReq = new ConceptProto.Attribute.GetOwners.Req();
        if (ownerType) getOwnersReq.setThingType(ConceptProtoBuilder.type(ownerType));
        const method = new ConceptProto.Thing.Req().setAttributeGetOwnersReq(getOwnersReq);
        return this.thingStream(method, res => res.getAttributeGetOwnersRes().getThingsList()) as Stream<ThingImpl>;
    }

    abstract getType(): AttributeTypeImpl;

    isBoolean(): boolean {
        return false;
    }

    isString(): boolean {
        return false;
    }

    isDouble(): boolean {
        return false;
    }

    isLong(): boolean {
        return false;
    }

    isDateTime(): boolean {
        return false;
    }

    isAttribute(): boolean {
        return true;
    }

    abstract asRemote(transaction: Transaction): RemoteAttribute<T>;

    abstract getValue(): T;
}

export class BooleanAttributeImpl extends AttributeImpl<boolean> implements BooleanAttribute {
    private readonly _type: BooleanAttributeTypeImpl;
    private readonly _value: boolean;

    constructor(iid: string, type: BooleanAttributeTypeImpl, value: boolean) {
        super(iid);
        this._type = type;
        this._value = value;
    }

    static of(protoThing: ConceptProto.Thing): BooleanAttributeImpl {
        return new BooleanAttributeImpl(
            Bytes.bytesToHexString(protoThing.getIid_asU8()),
            BooleanAttributeTypeImpl.of(protoThing.getType()),
            protoThing.getValue().getBoolean());
    }

    asRemote(transaction: Transaction): RemoteBooleanAttributeImpl {
        return new RemoteBooleanAttributeImpl(transaction, this.getIID(), this._type, this._value);
    }

    getType(): BooleanAttributeTypeImpl {
        return this._type;
    }

    getValue(): boolean {
        return this._value;
    }

    isBoolean(): boolean {
        return true;
    }
}

export class RemoteBooleanAttributeImpl extends RemoteAttributeImpl<boolean> implements Merge<RemoteBooleanAttribute, BooleanAttribute> {
    private readonly _type: BooleanAttributeTypeImpl;
    private readonly _value: boolean;

    constructor(transaction: Transaction, iid: string, type: BooleanAttributeTypeImpl, value: boolean){
        super(transaction, iid);
        this._type = type;
        this._value = value;
    }

    getValue(): boolean {
        return this._value;
    }

    getType(): BooleanAttributeTypeImpl {
        return this._type;
    }

    asRemote(transaction: Transaction): RemoteBooleanAttributeImpl {
        return new RemoteBooleanAttributeImpl(transaction, this.getIID(), this._type, this._value);
    }

    isBoolean(): boolean {
        return true;
    }
}

export class LongAttributeImpl extends AttributeImpl<number> implements LongAttribute {
    private readonly _type: LongAttributeTypeImpl;
    private readonly _value: number;

    constructor(iid: string, type: LongAttributeTypeImpl, value: number) {
        super(iid);
        this._type = type;
        this._value = value;
    }

    static of(protoThing: ConceptProto.Thing): LongAttributeImpl {
        return new LongAttributeImpl(
            Bytes.bytesToHexString(protoThing.getIid_asU8()),
            LongAttributeTypeImpl.of(protoThing.getType()),
            protoThing.getValue().getLong());
    }

    asRemote(transaction: Transaction): RemoteLongAttributeImpl {
        return new RemoteLongAttributeImpl(transaction, this.getIID(), this._type, this._value);
    }

    getType(): LongAttributeTypeImpl {
        return this._type;
    }

    getValue(): number {
        return this._value;
    }

    isLong(): boolean {
        return true;
    }
}

export class RemoteLongAttributeImpl extends RemoteAttributeImpl<number> implements Merge<RemoteLongAttribute, LongAttribute> {
    private readonly _type: LongAttributeTypeImpl;
    private readonly _value: number;

    constructor(transaction: Transaction, iid: string, type: LongAttributeTypeImpl, value: number) {
        super(transaction, iid);
        this._type = type;
        this._value = value;
    }

    getValue(): number {
        return this._value;
    }

    getType(): LongAttributeTypeImpl {
        return this._type;
    }

    asRemote(transaction: Transaction): RemoteLongAttributeImpl {
        return new RemoteLongAttributeImpl(transaction, this.getIID(), this._type, this._value);
    }

    isLong(): boolean {
        return true;
    }
}

export class DoubleAttributeImpl extends AttributeImpl<number> implements DoubleAttribute {
    private readonly _type: DoubleAttributeTypeImpl;
    private readonly _value: number;

    constructor(iid: string, type: DoubleAttributeTypeImpl, value: number) {
        super(iid);
        this._type = type;
        this._value = value;
    }

    static of(protoThing: ConceptProto.Thing): DoubleAttributeImpl {
        return new DoubleAttributeImpl(
            Bytes.bytesToHexString(protoThing.getIid_asU8()),
            DoubleAttributeTypeImpl.of(protoThing.getType()),
            protoThing.getValue().getDouble());
    }

    asRemote(transaction: Transaction): RemoteDoubleAttributeImpl {
        return new RemoteDoubleAttributeImpl(transaction, this.getIID(), this._type, this._value);
    }

    getType(): DoubleAttributeTypeImpl {
        return this._type;
    }

    getValue(): number {
        return this._value;
    }

    isDouble(): boolean {
        return true;
    }
}

export class RemoteDoubleAttributeImpl extends RemoteAttributeImpl<number> implements Merge<RemoteDoubleAttribute, DoubleAttribute> {
    private readonly _type: DoubleAttributeTypeImpl;
    private readonly _value: number;

    constructor(transaction: Transaction, iid: string, type: DoubleAttributeTypeImpl, value: number) {
        super(transaction, iid);
        this._type = type;
        this._value = value;
    }

    getValue(): number {
        return this._value;
    }

    getType(): DoubleAttributeTypeImpl {
        return this._type;
    }

    asRemote(transaction: Transaction): RemoteDoubleAttributeImpl {
        return new RemoteDoubleAttributeImpl(transaction, this.getIID(), this._type, this._value);
    }

    isDouble(): boolean {
        return true;
    }
}

export class StringAttributeImpl extends AttributeImpl<string> implements StringAttribute {
    private readonly _type: StringAttributeTypeImpl;
    private readonly _value: string;

    constructor(iid: string, type: StringAttributeTypeImpl, value: string) {
        super(iid);
        this._type = type;
        this._value = value;
    }

    static of(protoThing: ConceptProto.Thing): StringAttributeImpl {
        return new StringAttributeImpl(
            Bytes.bytesToHexString(protoThing.getIid_asU8()),
            StringAttributeTypeImpl.of(protoThing.getType()),
            protoThing.getValue().getString());
    }

    asRemote(transaction: Transaction): RemoteStringAttributeImpl {
        return new RemoteStringAttributeImpl(transaction, this.getIID(), this._type, this._value);
    }

    getType(): StringAttributeTypeImpl {
        return this._type;
    }

    getValue(): string {
        return this._value;
    }

    isString(): boolean {
        return true;
    }
}

export class RemoteStringAttributeImpl extends RemoteAttributeImpl<string> implements Merge<RemoteStringAttribute, StringAttribute> {
    private readonly _type: StringAttributeTypeImpl;
    private readonly _value: string;

    constructor(transaction: Transaction, iid: string, type: StringAttributeTypeImpl, value: string) {
        super(transaction, iid);
        this._type = type;
        this._value = value;
    }

    getValue(): string {
        return this._value;
    }

    getType(): StringAttributeTypeImpl {
        return this._type;
    }

    asRemote(transaction: Transaction): RemoteStringAttributeImpl {
        return new RemoteStringAttributeImpl(transaction, this.getIID(), this._type, this._value);
    }

    isString(): boolean {
        return true;
    }
}

export class DateTimeAttributeImpl extends AttributeImpl<Date> implements DateTimeAttribute {
    private readonly _type: DateTimeAttributeTypeImpl;
    private readonly _value: Date;

    constructor(iid: string, type: DateTimeAttributeTypeImpl, value: Date) {
        super(iid);
        this._type = type;
        this._value = value;
    }

    static of(protoThing: ConceptProto.Thing): DateTimeAttributeImpl {
        return new DateTimeAttributeImpl(
            Bytes.bytesToHexString(protoThing.getIid_asU8()),
            DateTimeAttributeTypeImpl.of(protoThing.getType()),
            new Date(protoThing.getValue().getDateTime()));
    }

    asRemote(transaction: Transaction): RemoteDateTimeAttributeImpl {
        return new RemoteDateTimeAttributeImpl(transaction, this.getIID(), this._type, this._value);
    }

    getType(): DateTimeAttributeTypeImpl {
        return this._type;
    }

    getValue(): Date {
        return this._value;
    }

    isDateTime(): boolean {
        return true;
    }
}

export class RemoteDateTimeAttributeImpl extends RemoteAttributeImpl<Date> implements Merge<RemoteDateTimeAttribute, DateTimeAttribute> {
    private readonly _type: DateTimeAttributeTypeImpl;
    private readonly _value: Date;

    constructor(transaction: Transaction, iid: string, type: DateTimeAttributeTypeImpl, value: Date) {
        super(transaction, iid);
        this._type = type;
        this._value = value;
    }

    getValue(): Date {
        return this._value;
    }

    getType(): DateTimeAttributeTypeImpl {
        return this._type;
    }

    asRemote(transaction: Transaction): RemoteDateTimeAttributeImpl {
        return new RemoteDateTimeAttributeImpl(transaction, this.getIID(), this._type, this._value);
    }

    isDateTime(): boolean {
        return true;
    }
}

export namespace AttributeImpl {
    export function of(thingProto: ConceptProto.Thing): AttributeImpl<ValueClass> {
        switch (thingProto.getType().getValueType()) {
            case ConceptProto.AttributeType.ValueType.BOOLEAN:
                return BooleanAttributeImpl.of(thingProto);
            case ConceptProto.AttributeType.ValueType.LONG:
                return LongAttributeImpl.of(thingProto);
            case ConceptProto.AttributeType.ValueType.DOUBLE:
                return DoubleAttributeImpl.of(thingProto);
            case ConceptProto.AttributeType.ValueType.STRING:
                return StringAttributeImpl.of(thingProto);
            case ConceptProto.AttributeType.ValueType.DATETIME:
                return DateTimeAttributeImpl.of(thingProto);
            default:
                throw new GraknClientError(ErrorMessage.Concept.BAD_VALUE_TYPE.message(thingProto.getType().getValueType()));
        }
    }
}
