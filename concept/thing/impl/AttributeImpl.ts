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

import {
    Attribute,
    RemoteAttribute,
    BooleanAttribute,
    DateTimeAttribute,
    DoubleAttribute,
    LongAttribute,
    StringAttribute,
    RemoteBooleanAttribute,
    RemoteLongAttribute,
    RemoteStringAttribute,
    RemoteDoubleAttribute,
    RemoteDateTimeAttribute,
    ThingImpl,
    RemoteThingImpl,
    ThingType,
    AttributeTypeImpl,
    BooleanAttributeTypeImpl,
    DateTimeAttributeTypeImpl,
    DoubleAttributeTypeImpl,
    LongAttributeTypeImpl,
    StringAttributeTypeImpl,
    AttributeType,
    Grakn,
    Merge,
    Stream, ConceptProtoBuilder, ConceptProtoReader,
} from "../../../dependencies_internal";
import ValueClass = AttributeType.ValueClass;
import Transaction = Grakn.Transaction;
import ConceptProto from "graknlabs-protocol/protobuf/concept_pb";

export abstract class AttributeImpl<T extends ValueClass> extends ThingImpl implements Attribute<T> {
    protected constructor(iid: string) {
        super(iid);
    }

    abstract asRemote(transaction: Transaction): RemoteAttribute<T>;

    abstract getValue(): T;
}

export abstract class RemoteAttributeImpl<T extends ValueClass> extends RemoteThingImpl implements RemoteAttribute<T> {
    protected constructor(transaction: Transaction, iid: string) {
        super(transaction, iid);
    }

    getOwners(ownerType: ThingType): Stream<ThingImpl> {
        const method = new ConceptProto.Thing.Req().setAttributeGetOwnersReq(
            new ConceptProto.Attribute.GetOwners.Req().setThingType(ConceptProtoBuilder.type(ownerType))
        );
        return this.thingStream(method, res => res.getAttributeGetOwnersRes().getThingList()) as Stream<ThingImpl>;
    }

    async getType(): Promise<AttributeTypeImpl> {
        const res = await this.execute(new ConceptProto.Thing.Req().setThingGetTypeReq(new ConceptProto.Thing.GetType.Req()));
        return ConceptProtoReader.thingType(res.getThingGetTypeRes().getThingType()) as AttributeTypeImpl;
    }

    abstract asRemote(transaction: Transaction): RemoteAttribute<T>;

    abstract getValue(): T;
}

export class BooleanAttributeImpl extends AttributeImpl<boolean> implements BooleanAttribute {
    private readonly _value: boolean;

    constructor(iid: string, value: boolean) {
        super(iid);
        this._value = value;
    }

    static of(protoThing: ConceptProto.Thing): BooleanAttributeImpl {
        return new BooleanAttributeImpl(protoThing.getIid_asB64(), protoThing.getValue().getBoolean());
    }

    asRemote(transaction: Transaction): RemoteBooleanAttributeImpl {
        return new RemoteBooleanAttributeImpl(transaction, this.getIID(), this._value);
    }

    getValue(): boolean {
        return this._value;
    }
}

export class RemoteBooleanAttributeImpl extends RemoteAttributeImpl<boolean> implements Merge<RemoteBooleanAttribute, BooleanAttribute> {
    private readonly _value: boolean;

    constructor(transaction: Transaction, iid: string, value: boolean){
        super(transaction, iid);
        this._value = value;
    }

    getValue(): boolean {
        return this._value;
    }

    async getType(): Promise<BooleanAttributeTypeImpl> {
        const res = await this.execute(new ConceptProto.Thing.Req().setThingGetTypeReq(new ConceptProto.Thing.GetType.Req()));
        return BooleanAttributeTypeImpl.of(res.getThingGetTypeRes().getThingType());
    }

    asRemote(transaction: Transaction): RemoteBooleanAttributeImpl {
        return new RemoteBooleanAttributeImpl(transaction, this.getIID(), this._value);
    }
}

export class LongAttributeImpl extends AttributeImpl<number> implements LongAttribute {
    private readonly _value: number;

    constructor(iid: string, value: number) {
        super(iid);
        this._value = value;
    }

    static of(protoThing: ConceptProto.Thing): LongAttributeImpl {
        return new LongAttributeImpl(protoThing.getIid_asB64(), protoThing.getValue().getLong());
    }

    asRemote(transaction: Transaction): RemoteLongAttributeImpl {
        return new RemoteLongAttributeImpl(transaction, this.getIID(), this._value);
    }

    getValue(): number {
        return this._value;
    }
}

export class RemoteLongAttributeImpl extends RemoteAttributeImpl<number> implements Merge<RemoteLongAttribute, LongAttribute> {
    private readonly _value: number;

    constructor(transaction: Transaction, iid: string, value: number){
        super(transaction, iid);
        this._value = value;
    }

    getValue(): number {
        return this._value;
    }

    async getType(): Promise<LongAttributeTypeImpl> {
        const res = await this.execute(new ConceptProto.Thing.Req().setThingGetTypeReq(new ConceptProto.Thing.GetType.Req()));
        return LongAttributeTypeImpl.of(res.getThingGetTypeRes().getThingType());
    }

    asRemote(transaction: Transaction): RemoteLongAttributeImpl {
        return new RemoteLongAttributeImpl(transaction, this.getIID(), this._value);
    }
}

export class DoubleAttributeImpl extends AttributeImpl<number> implements Attribute<number> {
    private readonly _value: number;

    constructor(iid: string, value: number) {
        super(iid);
        this._value = value;
    }

    static of(protoThing: ConceptProto.Thing): DoubleAttributeImpl {
        return new DoubleAttributeImpl(protoThing.getIid_asB64(), protoThing.getValue().getDouble());
    }

    asRemote(transaction: Transaction): RemoteDoubleAttributeImpl {
        return new RemoteDoubleAttributeImpl(transaction, this.getIID(), this._value);
    }

    getValue(): number {
        return this._value;
    }
}


export class RemoteDoubleAttributeImpl extends RemoteAttributeImpl<number> implements Merge<RemoteDoubleAttribute, DoubleAttribute> {
    private readonly _value: number;

    constructor(transaction: Transaction, iid: string, value: number){
        super(transaction, iid);
        this._value = value;
    }

    getValue(): number {
        return this._value;
    }

    async getType(): Promise<DoubleAttributeTypeImpl> {
        const res = await this.execute(new ConceptProto.Thing.Req().setThingGetTypeReq(new ConceptProto.Thing.GetType.Req()));
        return DoubleAttributeTypeImpl.of(res.getThingGetTypeRes().getThingType());
    }

    asRemote(transaction: Transaction): RemoteDoubleAttributeImpl {
        return new RemoteDoubleAttributeImpl(transaction, this.getIID(), this._value);
    }
}

export class StringAttributeImpl extends AttributeImpl<string> implements Attribute<string> {
    private readonly _value: string;

    constructor(iid: string, value: string) {
        super(iid);
        this._value = value;
    }

    static of(protoThing: ConceptProto.Thing): StringAttributeImpl {
        return new StringAttributeImpl(protoThing.getIid_asB64(), protoThing.getValue().getString());
    }

    asRemote(transaction: Transaction): RemoteStringAttributeImpl {
        return new RemoteStringAttributeImpl(transaction, this.getIID(), this._value);
    }

    getValue(): string {
        return this._value;
    }
}

export class RemoteStringAttributeImpl extends RemoteAttributeImpl<string> implements Merge<RemoteStringAttribute, StringAttribute> {
    private readonly _value: string;

    constructor(transaction: Transaction, iid: string, value: string){
        super(transaction, iid);
        this._value = value;
    }

    getValue(): string {
        return this._value;
    }

    async getType(): Promise<StringAttributeTypeImpl> {
        const res = await this.execute(new ConceptProto.Thing.Req().setThingGetTypeReq(new ConceptProto.Thing.GetType.Req()));
        return StringAttributeTypeImpl.of(res.getThingGetTypeRes().getThingType());
    }

    asRemote(transaction: Transaction): RemoteStringAttributeImpl {
        return new RemoteStringAttributeImpl(transaction, this.getIID(), this._value);
    }
}

export class DateTimeAttributeImpl extends AttributeImpl<Date> implements DateTimeAttribute {
    private readonly _value: Date;

    constructor(iid: string, value: Date) {
        super(iid);
        this._value = value;
    }

    static of(protoThing: ConceptProto.Thing): DateTimeAttributeImpl {
        return new DateTimeAttributeImpl(protoThing.getIid_asB64(), new Date(protoThing.getValue().getDateTime()));
    }

    asRemote(transaction: Transaction): RemoteDateTimeAttributeImpl {
        return new RemoteDateTimeAttributeImpl(transaction, this.getIID(), this._value);
    }

    getValue(): Date {
        return this._value;
    }
}


class RemoteDateTimeAttributeImpl extends RemoteAttributeImpl<Date> implements Merge<RemoteDateTimeAttribute, DateTimeAttribute> {
    private readonly _value: Date;

    constructor(transaction: Transaction, iid: string, value: Date){
        super(transaction, iid);
        this._value = value;
    }

    getValue(): Date {
        return this._value;
    }

    async getType(): Promise<DateTimeAttributeTypeImpl> {
        const res = await this.execute(new ConceptProto.Thing.Req().setThingGetTypeReq(new ConceptProto.Thing.GetType.Req()));
        return DateTimeAttributeTypeImpl.of(res.getThingGetTypeRes().getThingType());
    }

    asRemote(transaction: Transaction): RemoteDateTimeAttributeImpl {
        return new RemoteDateTimeAttributeImpl(transaction, this.getIID(), this._value);
    }
}
