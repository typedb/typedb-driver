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
    RemoteThingTypeImpl,
    ThingTypeImpl,
    AttributeType,
    BooleanAttributeType,
    DateTimeAttributeType,
    DoubleAttributeType,
    LongAttributeType,
    RemoteAttributeType,
    RemoteBooleanAttributeType,
    RemoteDateTimeAttributeType,
    RemoteDoubleAttributeType,
    RemoteLongAttributeType,
    RemoteStringAttributeType,
    StringAttributeType,
    Grakn,
    AttributeImpl,
    BooleanAttributeImpl,
    DateTimeAttributeImpl,
    DoubleAttributeImpl,
    LongAttributeImpl,
    StringAttributeImpl,
    Stream,
    ConceptProtoBuilder,
    GraknClientError, ErrorMessage,
} from "../../../dependencies_internal";
import ConceptProto from "grakn-protocol/protobuf/concept_pb";
import Transaction = Grakn.Transaction;
import ValueType = AttributeType.ValueType;
import ValueClass = AttributeType.ValueClass;
import isKeyable = AttributeType.ValueType.isKeyable;
import assert from "assert";

export class AttributeTypeImpl extends ThingTypeImpl implements AttributeType {

    private static ROOT_LABEL = "attribute";

    constructor(label: string, isRoot: boolean) {
        super(label, isRoot);
    }

    getValueType(): ValueType {
        return ValueType.OBJECT;
    }

    isKeyable(): boolean {
        return isKeyable(this.getValueType());
    }

    asRemote(transaction: Transaction): RemoteAttributeTypeImpl {
        return new RemoteAttributeTypeImpl(transaction, this.getLabel(), this.isRoot())
    }

    isAttributeType(): boolean {
        return true;
    }

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
}

export class RemoteAttributeTypeImpl extends RemoteThingTypeImpl implements RemoteAttributeType {

    constructor(transaction: Transaction, label: string, isRoot: boolean) {
        super(transaction, label, isRoot);
    }

    isAttributeType(): boolean {
        return true;
    }

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

    getValueType(): ValueType {
        return ValueType.OBJECT;
    }

    isKeyable(): boolean {
        return isKeyable(this.getValueType());
    }

    setSupertype(attributeType: AttributeType): Promise<void> {
        return super.setSupertype(attributeType);
    }

    getSubtypes(): Stream<AttributeTypeImpl> {
        return super.getSubtypes() as Stream<AttributeTypeImpl>;
    }

    getInstances(): Stream<AttributeImpl<ValueClass>> {
        return super.getInstances() as Stream<AttributeImpl<ValueClass>>;
    }

    getOwners(onlyKey?: boolean): Stream<ThingTypeImpl> {
        const method = new ConceptProto.Type.Req()
            .setAttributeTypeGetOwnersReq(new ConceptProto.AttributeType.GetOwners.Req().setOnlyKey(onlyKey || false));
        return this.typeStream(method, res => res.getAttributeTypeGetOwnersRes().getOwnersList()) as Stream<ThingTypeImpl>;
    }

    protected async putInternal(valueProto: ConceptProto.Attribute.Value): Promise<AttributeImpl<ValueClass>> {
        const method = new ConceptProto.Type.Req().setAttributeTypePutReq(new ConceptProto.AttributeType.Put.Req().setValue(valueProto));
        return AttributeImpl.of(await this.execute(method).then(res => res.getAttributeTypePutRes().getAttribute()));
    }

    protected async getInternal(valueProto: ConceptProto.Attribute.Value): Promise<AttributeImpl<ValueClass>> {
        const method = new ConceptProto.Type.Req().setAttributeTypeGetReq(new ConceptProto.AttributeType.Get.Req().setValue(valueProto));
        const response = await this.execute(method).then(res => res.getAttributeTypeGetRes());
        return response.getResCase() === ConceptProto.AttributeType.Get.Res.ResCase.ATTRIBUTE ? AttributeImpl.of(response.getAttribute()) : null;
    }

    asRemote(transaction: Transaction): RemoteAttributeTypeImpl {
        return new RemoteAttributeTypeImpl(transaction, this.getLabel(), this.isRoot());
    }
}

export class BooleanAttributeTypeImpl extends AttributeTypeImpl implements BooleanAttributeType {

    constructor(label: string, isRoot: boolean) {
        super(label, isRoot);
    }

    isBoolean(): boolean {
        return true;
    }

    static of(typeProto: ConceptProto.Type): BooleanAttributeTypeImpl {
        return new BooleanAttributeTypeImpl(typeProto.getLabel(), typeProto.getRoot());
    }

    getValueType(): ValueType {
        return ValueType.BOOLEAN;
    }

    asRemote(transaction: Transaction): RemoteBooleanAttributeTypeImpl {
        return new RemoteBooleanAttributeTypeImpl(transaction, this.getLabel(), this.isRoot())
    }
}

export class RemoteBooleanAttributeTypeImpl extends RemoteAttributeTypeImpl implements RemoteBooleanAttributeType {

    constructor(transaction: Transaction, label: string, isRoot: boolean) {
        super(transaction, label, isRoot);
    }

    isBoolean(): boolean {
        return true;
    }

    getValueType(): ValueType {
        return ValueType.BOOLEAN;
    }

    asRemote(transaction: Transaction): RemoteBooleanAttributeTypeImpl {
        return new RemoteBooleanAttributeTypeImpl(transaction, this.getLabel(), this.isRoot());
    }

    getSubtypes(): Stream<BooleanAttributeTypeImpl> {
        return super.getSubtypes() as Stream<BooleanAttributeTypeImpl>;
    }

    getInstances(): Stream<BooleanAttributeImpl> {
        return super.getInstances() as Stream<BooleanAttributeImpl>;
    }

    setSupertype(type: BooleanAttributeType): Promise<void> {
        return super.setSupertype(type);
    }

    put(value: boolean): Promise<BooleanAttributeImpl> {
        return this.putInternal(ConceptProtoBuilder.booleanAttributeValue(value)) as Promise<BooleanAttributeImpl>;
    }

    get(value: boolean): Promise<BooleanAttributeImpl> {
        return this.getInternal(ConceptProtoBuilder.booleanAttributeValue(value)) as Promise<BooleanAttributeImpl>;
    }
}

export class LongAttributeTypeImpl extends AttributeTypeImpl implements LongAttributeType {

    constructor(label: string, isRoot: boolean) {
        super(label, isRoot);
    }

    static of(typeProto: ConceptProto.Type): LongAttributeTypeImpl {
        return new LongAttributeTypeImpl(typeProto.getLabel(), typeProto.getRoot());
    }

    getValueType(): ValueType {
        return ValueType.LONG;
    }

    isLong(): boolean {
        return true;
    }

    asRemote(transaction: Transaction): RemoteLongAttributeTypeImpl {
        return new RemoteLongAttributeTypeImpl(transaction, this.getLabel(), this.isRoot())
    }
}

export class RemoteLongAttributeTypeImpl extends RemoteAttributeTypeImpl implements RemoteLongAttributeType {

    constructor(transaction: Transaction, label: string, isRoot: boolean) {
        super(transaction, label, isRoot);
    }

    getValueType(): ValueType {
        return ValueType.LONG;
    }

    isLong(): boolean {
        return true;
    }

    asRemote(transaction: Transaction): RemoteLongAttributeTypeImpl {
        return new RemoteLongAttributeTypeImpl(transaction, this.getLabel(), this.isRoot());
    }

    getSubtypes(): Stream<LongAttributeTypeImpl> {
        return super.getSubtypes() as Stream<LongAttributeTypeImpl>;
    }

    getInstances(): Stream<LongAttributeImpl> {
        return super.getInstances() as Stream<LongAttributeImpl>;
    }

    setSupertype(type: LongAttributeType): Promise<void> {
        return super.setSupertype(type);
    }

    put(value: number): Promise<LongAttributeImpl> {
        return this.putInternal(ConceptProtoBuilder.longAttributeValue(value)) as Promise<LongAttributeImpl>;
    }

    get(value: number): Promise<LongAttributeImpl> {
        return this.getInternal(ConceptProtoBuilder.longAttributeValue(value)) as Promise<LongAttributeImpl>;
    }
}

export class DoubleAttributeTypeImpl extends AttributeTypeImpl implements DoubleAttributeType {

    constructor(label: string, isRoot: boolean) {
        super(label, isRoot);
    }

    static of(typeProto: ConceptProto.Type): DoubleAttributeTypeImpl {
        return new DoubleAttributeTypeImpl(typeProto.getLabel(), typeProto.getRoot());
    }

    getValueType(): ValueType {
        return ValueType.DOUBLE;
    }

    isDouble(): boolean {
        return true;
    }

    asRemote(transaction: Transaction): RemoteDoubleAttributeTypeImpl {
        return new RemoteDoubleAttributeTypeImpl(transaction, this.getLabel(), this.isRoot())
    }
}

export class RemoteDoubleAttributeTypeImpl extends RemoteAttributeTypeImpl implements RemoteDoubleAttributeType {

    constructor(transaction: Transaction, label: string, isRoot: boolean) {
        super(transaction, label, isRoot);
    }

    getValueType(): ValueType {
        return ValueType.DOUBLE;
    }

    asRemote(transaction: Transaction): RemoteDoubleAttributeTypeImpl {
        return new RemoteDoubleAttributeTypeImpl(transaction, this.getLabel(), this.isRoot());
    }

    isDouble(): boolean {
        return true;
    }

    getSubtypes(): Stream<DoubleAttributeTypeImpl> {
        return super.getSubtypes() as Stream<DoubleAttributeTypeImpl>;
    }

    getInstances(): Stream<DoubleAttributeImpl> {
        return super.getInstances() as Stream<DoubleAttributeImpl>;
    }

    setSupertype(type: DoubleAttributeType): Promise<void> {
        return super.setSupertype(type);
    }

    put(value: number): Promise<DoubleAttributeImpl> {
        return this.putInternal(ConceptProtoBuilder.doubleAttributeValue(value)) as Promise<DoubleAttributeImpl>;
    }

    get(value: number): Promise<DoubleAttributeImpl> {
        return this.getInternal(ConceptProtoBuilder.doubleAttributeValue(value)) as Promise<DoubleAttributeImpl>;
    }
}

export class StringAttributeTypeImpl extends AttributeTypeImpl implements StringAttributeType {

    constructor(label: string, isRoot: boolean) {
        super(label, isRoot);
    }

    static of(typeProto: ConceptProto.Type): StringAttributeTypeImpl {
        return new StringAttributeTypeImpl(typeProto.getLabel(), typeProto.getRoot());
    }

    getValueType(): ValueType {
        return ValueType.STRING;
    }

    isString(): boolean {
        return true;
    }

    asRemote(transaction: Transaction): RemoteStringAttributeTypeImpl {
        return new RemoteStringAttributeTypeImpl(transaction, this.getLabel(), this.isRoot())
    }
}

export class RemoteStringAttributeTypeImpl extends RemoteAttributeTypeImpl implements RemoteStringAttributeType {

    constructor(transaction: Transaction, label: string, isRoot: boolean) {
        super(transaction, label, isRoot);
    }

    getValueType(): ValueType {
        return ValueType.STRING;
    }

    asRemote(transaction: Transaction): RemoteStringAttributeTypeImpl {
        return new RemoteStringAttributeTypeImpl(transaction, this.getLabel(), this.isRoot());
    }

    isString(): boolean {
        return true;
    }

    getSubtypes(): Stream<StringAttributeTypeImpl> {
        return super.getSubtypes() as Stream<StringAttributeTypeImpl>;
    }

    getInstances(): Stream<StringAttributeImpl> {
        return super.getInstances() as Stream<StringAttributeImpl>;
    }

    setSupertype(type: StringAttributeType): Promise<void> {
        return super.setSupertype(type);
    }

    put(value: string): Promise<StringAttributeImpl> {
        return this.putInternal(ConceptProtoBuilder.stringAttributeValue(value)) as Promise<StringAttributeImpl>;
    }

    get(value: string): Promise<StringAttributeImpl> {
        return this.getInternal(ConceptProtoBuilder.stringAttributeValue(value)) as Promise<StringAttributeImpl>;
    }

    async getRegex(): Promise<string> {
        return (await this.execute(new ConceptProto.Type.Req().setAttributeTypeGetRegexReq(
            new ConceptProto.AttributeType.GetRegex.Req()
        ))).getAttributeTypeGetRegexRes().getRegex();
    }

    async setRegex(regex: string): Promise<void> {
        await this.execute(new ConceptProto.Type.Req().setAttributeTypeSetRegexReq(
            new ConceptProto.AttributeType.SetRegex.Req()
                .setRegex(regex)
        ));
    }

}

export class DateTimeAttributeTypeImpl extends AttributeTypeImpl implements DateTimeAttributeType {

    constructor(label: string, isRoot: boolean) {
        super(label, isRoot);
    }

    static of(typeProto: ConceptProto.Type): DateTimeAttributeTypeImpl {
        return new DateTimeAttributeTypeImpl(typeProto.getLabel(), typeProto.getRoot());
    }

    getValueType(): ValueType {
        return ValueType.DATETIME;
    }

    isDateTime(): boolean {
        return true;
    }

    asRemote(transaction: Transaction): RemoteDateTimeAttributeTypeImpl {
        return new RemoteDateTimeAttributeTypeImpl(transaction, this.getLabel(), this.isRoot())
    }
}

export class RemoteDateTimeAttributeTypeImpl extends RemoteAttributeTypeImpl implements RemoteDateTimeAttributeType {

    constructor(transaction: Transaction, label: string, isRoot: boolean) {
        super(transaction, label, isRoot);
    }

    getValueType(): ValueType {
        return ValueType.DATETIME;
    }

    isDateTime(): boolean {
        return true;
    }

    asRemote(transaction: Transaction): RemoteDateTimeAttributeTypeImpl {
        return new RemoteDateTimeAttributeTypeImpl(transaction, this.getLabel(), this.isRoot());
    }

    getSubtypes(): Stream<DateTimeAttributeTypeImpl> {
        return super.getSubtypes() as Stream<DateTimeAttributeTypeImpl>;
    }

    getInstances(): Stream<DateTimeAttributeImpl> {
        return super.getInstances() as Stream<DateTimeAttributeImpl>;
    }

    setSupertype(type: DateTimeAttributeType): Promise<void> {
        return super.setSupertype(type);
    }

    put(value: Date): Promise<DateTimeAttributeImpl> {
        return this.putInternal(ConceptProtoBuilder.dateTimeAttributeValue(value)) as Promise<DateTimeAttributeImpl>;
    }

    get(value: Date): Promise<DateTimeAttributeImpl> {
        return this.getInternal(ConceptProtoBuilder.dateTimeAttributeValue(value)) as Promise<DateTimeAttributeImpl>;
    }
}

export namespace AttributeTypeImpl {
    export function of(typeProto: ConceptProto.Type): AttributeTypeImpl {
        switch (typeProto.getValueType()) {
            case ConceptProto.AttributeType.ValueType.BOOLEAN:
                return new BooleanAttributeTypeImpl(typeProto.getLabel(), typeProto.getRoot());
            case ConceptProto.AttributeType.ValueType.LONG:
                return new LongAttributeTypeImpl(typeProto.getLabel(), typeProto.getRoot());
            case ConceptProto.AttributeType.ValueType.DOUBLE:
                return new DoubleAttributeTypeImpl(typeProto.getLabel(), typeProto.getRoot());
            case ConceptProto.AttributeType.ValueType.STRING:
                return new StringAttributeTypeImpl(typeProto.getLabel(), typeProto.getRoot());
            case ConceptProto.AttributeType.ValueType.DATETIME:
                return new DateTimeAttributeTypeImpl(typeProto.getLabel(), typeProto.getRoot());
            case ConceptProto.AttributeType.ValueType.OBJECT:
                assert(typeProto.getRoot());
                return new AttributeTypeImpl(typeProto.getLabel(), typeProto.getRoot());
            default:
                throw new GraknClientError(ErrorMessage.Concept.BAD_VALUE_TYPE.message(typeProto.getValueType()));
        }
    }
}
