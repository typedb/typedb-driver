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
import {AttributeType} from "../../api/concept/type/AttributeType";
import {Attribute} from "../../api/concept/thing/Attribute";
import {ThingType} from "../../api/concept/type/ThingType";
import {AttributeImpl, ThingTypeImpl} from "../../dependencies_internal";
import {Label} from "../../common/Label";
import {Stream} from "../../common/util/Stream";
import {RequestBuilder} from "../../common/rpc/RequestBuilder";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import {GraknClientError} from "../../common/errors/GraknClientError";
import {
    Attribute as AttributeProto,
    AttributeType as AttributeTypeProto,
    Type as TypeProto
} from "grakn-protocol/common/concept_pb";
import INVALID_CONCEPT_CASTING = ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import BAD_VALUE_TYPE = ErrorMessage.Concept.BAD_VALUE_TYPE;

export class AttributeTypeImpl extends ThingTypeImpl implements AttributeType {

    constructor(name: string, isRoot: boolean) {
        super(name, isRoot);
    }

    asRemote(transaction: GraknTransaction): AttributeType.Remote {
        return new AttributeTypeImpl.RemoteImpl(transaction as GraknTransaction.Extended, this.getLabel(), this.isRoot());
    }

    isAttributeType(): boolean {
        return true;
    }

    getValueType(): AttributeType.ValueType {
        return AttributeType.ValueType.OBJECT;
    }

    isKeyable(): boolean {
        return this.getValueType().isKeyable();
    }

    asBoolean(): AttributeType.Boolean {
        if (this.isRoot()) {
            return new AttributeTypeImpl.Boolean(this.getLabel().name(), this.isRoot());
        }
        throw new GraknClientError(INVALID_CONCEPT_CASTING.message("AttributeType", "AttributeType.Boolean"));
    }

    asLong(): AttributeType.Long {
        if (this.isRoot()) {
            return new AttributeTypeImpl.Long(this.getLabel().name(), this.isRoot());
        }
        throw new GraknClientError(INVALID_CONCEPT_CASTING.message("AttributeType", "AttributeType.Long"));
    }

    asDouble(): AttributeType.Double {
        if (this.isRoot()) {
            return new AttributeTypeImpl.Double(this.getLabel().name(), this.isRoot());
        }
        throw new GraknClientError(INVALID_CONCEPT_CASTING.message("AttributeType", "AttributeType.Double"));
    }

    asString(): AttributeType.String {
        if (this.isRoot()) {
            return new AttributeTypeImpl.String(this.getLabel().name(), this.isRoot());
        }
        throw new GraknClientError(INVALID_CONCEPT_CASTING.message("AttributeType", "AttributeType.String"));
    }

    asDateTime(): AttributeType.DateTime {
        if (this.isRoot()) {
            return new AttributeTypeImpl.DateTime(this.getLabel().name(), this.isRoot());
        }
        throw new GraknClientError(INVALID_CONCEPT_CASTING.message("AttributeType", "AttributeType.DateTime"));
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

export namespace AttributeTypeImpl {

    export function of(attributeTypeProto: TypeProto): AttributeType {
        switch (attributeTypeProto.getValueType()) {
            case AttributeTypeProto.ValueType.BOOLEAN:
                return new AttributeTypeImpl.Boolean(attributeTypeProto.getLabel(), attributeTypeProto.getRoot());
            case AttributeTypeProto.ValueType.LONG:
                return new AttributeTypeImpl.Long(attributeTypeProto.getLabel(), attributeTypeProto.getRoot());
            case AttributeTypeProto.ValueType.DOUBLE:
                return new AttributeTypeImpl.Double(attributeTypeProto.getLabel(), attributeTypeProto.getRoot());
            case AttributeTypeProto.ValueType.STRING:
                return new AttributeTypeImpl.String(attributeTypeProto.getLabel(), attributeTypeProto.getRoot());
            case AttributeTypeProto.ValueType.DATETIME:
                return new AttributeTypeImpl.DateTime(attributeTypeProto.getLabel(), attributeTypeProto.getRoot());
            case AttributeTypeProto.ValueType.OBJECT:
                return new AttributeTypeImpl(attributeTypeProto.getLabel(), attributeTypeProto.getRoot());
            default:
                throw new GraknClientError(BAD_VALUE_TYPE.message(attributeTypeProto.getValueType()));
        }
    }

    export class RemoteImpl extends ThingTypeImpl.RemoteImpl implements AttributeType.Remote {

        constructor(transaction: GraknTransaction.Extended, label: Label, isRoot: boolean) {
            super(transaction, label, isRoot);
        }

        asRemote(transaction: GraknTransaction): AttributeType.Remote {
            return this;
        }

        isAttributeType(): boolean {
            return true;
        }

        isKeyable(): boolean {
            return this.getValueType().isKeyable();
        }

        getValueType(): AttributeType.ValueType {
            return AttributeType.ValueType.OBJECT;
        }

        setSupertype(type: AttributeType): Promise<void> {
            return super.setSupertype(type);
        }

        getSubtypes(): Stream<AttributeType> {
            return super.getSupertypes() as Stream<AttributeType>;
        }

        getInstances(): Stream<Attribute<AttributeType.ValueClass>> {
            return super.getInstances() as Stream<Attribute<AttributeType.ValueClass>>;
        }

        getOwners(onlyKey?: boolean): Stream<ThingType> {
            // TODO check this is the right way to check onlyKey is unset
            if (!onlyKey) onlyKey = false;

            const request = RequestBuilder.Type.AttributeType.getOwnersReq(this.getLabel(), onlyKey);
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getAttributeTypeGetOwnersResPart().getOwnersList()))
                .map((thingTypeProto) => ThingTypeImpl.of(thingTypeProto));
        }

        protected getImpl(valueProto: AttributeProto.Value): Promise<Attribute<any>> {
            const request = RequestBuilder.Type.AttributeType.getReq(this.getLabel(), valueProto);
            return this.execute(request)
                .then((attrProto) => AttributeImpl.of(attrProto.getAttributeTypeGetRes().getAttribute()));
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

        asBoolean(): AttributeType.RemoteBoolean {
            if (this.isRoot()) {
                return new AttributeTypeImpl.RemoteBoolean(this._transaction, this.getLabel(), this.isRoot());
            }
            throw new GraknClientError(INVALID_CONCEPT_CASTING.message("AttributeType.Remote", "AttributeType.RemoteBoolean"));
        }

        asLong(): AttributeType.RemoteLong {
            if (this.isRoot()) {
                return new AttributeTypeImpl.RemoteLong(this._transaction, this.getLabel(), this.isRoot());
            }
            throw new GraknClientError(INVALID_CONCEPT_CASTING.message("AttributeType.Remote", "AttributeType.RemoteLong"));
        }

        asDouble(): AttributeType.RemoteDouble {
            if (this.isRoot()) {
                return new AttributeTypeImpl.RemoteDouble(this._transaction, this.getLabel(), this.isRoot());
            }
            throw new GraknClientError(INVALID_CONCEPT_CASTING.message("AttributeType.Remote", "AttributeType.RemoteDouble"));
        }

        asString(): AttributeType.RemoteString {
            if (this.isRoot()) {
                return new AttributeTypeImpl.RemoteString(this._transaction, this.getLabel(), this.isRoot());
            }
            throw new GraknClientError(INVALID_CONCEPT_CASTING.message("AttributeType.Remote", "AttributeType.RemoteString"));

        }

        asDateTime(): AttributeType.RemoteDateTime {
            if (this.isRoot()) {
                return new AttributeTypeImpl.RemoteDateTime(this._transaction, this.getLabel(), this.isRoot());
            }
            throw new GraknClientError(INVALID_CONCEPT_CASTING.message("AttributeType.Remote", "AttributeType.RemoteDateTime"));

        }
    }

    export class Boolean extends AttributeTypeImpl implements AttributeType.Boolean {

        constructor(label: string, isRoot: boolean) {
            super(label, isRoot);
        }

        asRemote(transaction: GraknTransaction): AttributeType.RemoteBoolean {
            return new AttributeTypeImpl.RemoteBoolean(transaction as GraknTransaction.Extended, this.getLabel(), this.isRoot());
        }

        getValueType(): AttributeType.ValueType {
            return AttributeType.ValueType.BOOLEAN;
        }

        isBoolean(): boolean {
            return true;
        }

        asBoolean(): AttributeType.Boolean {
            return this;
        }

    }

    export class RemoteBoolean extends AttributeTypeImpl.RemoteImpl implements AttributeType.RemoteBoolean {

        constructor(transaction: GraknTransaction.Extended, label: Label, isRoot: boolean) {
            super(transaction, label, isRoot);
        }

        asRemote(transaction: GraknTransaction): AttributeType.RemoteBoolean {
            return this;
        }

        async get(value: boolean): Promise<Attribute.Boolean> {
            return await (super.getImpl(RequestBuilder.Thing.Attribute.attributeValueBooleanReq(value)) as Promise<Attribute.Boolean>);
        }

        getInstances(): Stream<Attribute.Boolean> {
            return super.getInstances() as Stream<Attribute.Boolean>;
        }

        getSubtypes(): Stream<AttributeType.RemoteBoolean> {
            return super.getSubtypes() as Stream<AttributeType.RemoteBoolean>;
        }

        getValueType(): AttributeType.ValueType {
            return AttributeType.ValueType.BOOLEAN;
        }

        async put(value: boolean): Promise<Attribute.Boolean> {
            const request = RequestBuilder.Type.AttributeType.putReq(this.getLabel(), RequestBuilder.Thing.Attribute.attributeValueBooleanReq(value));
            return await (this.execute(request).then((res) => AttributeImpl.of(res.getAttributeTypePutRes().getAttribute())) as Promise<Attribute.Boolean>);
        }

        isBoolean(): boolean {
            return true;
        }

        asBoolean(): AttributeType.RemoteBoolean {
            return this;
        }

    }

    export class Long extends AttributeTypeImpl implements AttributeType.Long {

        constructor(label: string, isRoot: boolean) {
            super(label, isRoot);
        }

        asRemote(transaction: GraknTransaction): AttributeType.RemoteLong {
            return new AttributeTypeImpl.RemoteLong(transaction as GraknTransaction.Extended, this.getLabel(), this.isRoot());
        }

        getValueType(): AttributeType.ValueType {
            return AttributeType.ValueType.LONG;
        }

        isLong(): boolean {
            return true;
        }

        asLong(): AttributeType.Long {
            return this;
        }
    }

    export class RemoteLong extends AttributeTypeImpl.RemoteImpl implements AttributeType.RemoteLong {

        constructor(transaction: GraknTransaction.Extended, label: Label, isRoot: boolean) {
            super(transaction, label, isRoot);
        }

        asRemote(transaction: GraknTransaction): AttributeType.RemoteLong {
            return this;
        }

        getInstances(): Stream<Attribute.Long> {
            return super.getInstances() as Stream<Attribute.Long>;
        }

        getSubtypes(): Stream<AttributeType.RemoteLong> {
            return super.getSubtypes() as Stream<AttributeType.RemoteLong>;
        }

        async get(value: number): Promise<Attribute.Long> {
            return await (super.getImpl(RequestBuilder.Thing.Attribute.attributeValueLongReq(value)) as Promise<Attribute.Long>);
        }

        getValueType(): AttributeType.ValueType {
            return AttributeType.ValueType.LONG;
        }

        async put(value: number): Promise<Attribute.Long> {
            const request = RequestBuilder.Type.AttributeType.putReq(this.getLabel(), RequestBuilder.Thing.Attribute.attributeValueLongReq(value));
            return await (this.execute(request).then((res) => AttributeImpl.of(res.getAttributeTypePutRes().getAttribute())) as Promise<Attribute.Long>);
        }

        isLong(): boolean {
            return true;
        }

        asLong(): AttributeType.RemoteLong {
            return this;
        }

    }

    export class Double extends AttributeTypeImpl implements AttributeType.Double {

        constructor(label: string, isRoot: boolean) {
            super(label, isRoot);
        }

        asRemote(transaction: GraknTransaction): AttributeType.RemoteDouble {
            return new AttributeTypeImpl.RemoteDouble(transaction as GraknTransaction.Extended, this.getLabel(), this.isRoot());
        }

        getValueType(): AttributeType.ValueType {
            return AttributeType.ValueType.DOUBLE;
        }

        isDouble(): boolean {
            return true;
        }

        asDouble(): AttributeType.Double {
            return this;
        }
    }

    export class RemoteDouble extends AttributeTypeImpl.RemoteImpl implements AttributeType.RemoteDouble {

        constructor(transaction: GraknTransaction.Extended, label: Label, isRoot: boolean) {
            super(transaction, label, isRoot);
        }

        asRemote(transaction: GraknTransaction): AttributeType.RemoteDouble {
            return this;
        }

        getInstances(): Stream<Attribute.Double> {
            return super.getInstances() as Stream<Attribute.Double>;
        }

        getSubtypes(): Stream<AttributeType.RemoteDouble> {
            return super.getSubtypes() as Stream<AttributeType.RemoteDouble>;
        }

        async get(value: number): Promise<Attribute.Double> {
            return await (super.getImpl(RequestBuilder.Thing.Attribute.attributeValueDoubleReq(value)) as Promise<Attribute.Double>);
        }

        getValueType(): AttributeType.ValueType {
            return AttributeType.ValueType.LONG;
        }

        async put(value: number): Promise<Attribute.Double> {
            const request = RequestBuilder.Type.AttributeType.putReq(this.getLabel(), RequestBuilder.Thing.Attribute.attributeValueDoubleReq(value));
            return await (this.execute(request).then((res) => AttributeImpl.of(res.getAttributeTypePutRes().getAttribute())) as Promise<Attribute.Double>);
        }

        isDouble(): boolean {
            return true;
        }

        asDouble(): AttributeType.RemoteDouble {
            return this;
        }

    }

    export class String extends AttributeTypeImpl implements AttributeType.String {

        constructor(label: string, isRoot: boolean) {
            super(label, isRoot);
        }

        asRemote(transaction: GraknTransaction): AttributeType.RemoteString {
            return new AttributeTypeImpl.RemoteString(transaction as GraknTransaction.Extended, this.getLabel(), this.isRoot())
        }

        getValueType(): AttributeType.ValueType {
            return AttributeType.ValueType.STRING;
        }

        isString(): boolean {
            return true;
        }

        asString(): AttributeType.String {
            return this;
        }
    }

    export class RemoteString extends AttributeTypeImpl.RemoteImpl implements AttributeType.RemoteString {

        constructor(transaction: GraknTransaction.Extended, label: Label, isRoot: boolean) {
            super(transaction, label, isRoot);
        }

        asRemote(transaction: GraknTransaction): AttributeType.RemoteString {
            return this;
        }

        getInstances(): Stream<Attribute.String> {
            return super.getInstances() as Stream<Attribute.String>;
        }

        getSubtypes(): Stream<AttributeType.RemoteString> {
            return super.getSubtypes() as Stream<AttributeType.RemoteString>;
        }

        async get(value: string): Promise<Attribute.String> {
            return await (super.getImpl(RequestBuilder.Thing.Attribute.attributeValueStringReq(value)) as Promise<Attribute.String>);
        }

        getValueType(): AttributeType.ValueType {
            return AttributeType.ValueType.STRING;
        }

        async put(value: string): Promise<Attribute.String> {
            const request = RequestBuilder.Type.AttributeType.putReq(this.getLabel(), RequestBuilder.Thing.Attribute.attributeValueStringReq(value));
            return await (this.execute(request).then((res) => AttributeImpl.of(res.getAttributeTypePutRes().getAttribute())) as Promise<Attribute.String>);
        }

        isString(): boolean {
            return true;
        }

        asString(): AttributeType.RemoteString {
            return this;
        }

        async getRegex(): Promise<string> {
            const request = RequestBuilder.Type.AttributeType.getRegexReq(this.getLabel());
            return await this.execute(request).then((res) => res.getAttributeTypeGetRegexRes().getRegex());
        }

        async setRegex(regex: string): Promise<void> {
            const request = RequestBuilder.Type.AttributeType.setRegexReq(this.getLabel(), regex);
            await this.execute(request);
        }

    }

    export class DateTime extends AttributeTypeImpl implements AttributeType.DateTime {

        constructor(label: string, isRoot: boolean) {
            super(label, isRoot);
        }

        asRemote(transaction: GraknTransaction): AttributeType.RemoteDateTime {
            return new AttributeTypeImpl.RemoteDateTime(transaction as GraknTransaction.Extended, this.getLabel(), this.isRoot());
        }

        getValueType(): AttributeType.ValueType {
            return AttributeType.ValueType.DATETIME;
        }

        isDateTime(): boolean {
            return true;
        }

        asDateTime(): AttributeType.DateTime {
            return this;
        }
    }

    export class RemoteDateTime extends AttributeTypeImpl.RemoteImpl implements AttributeType.RemoteDateTime {

        constructor(transaction: GraknTransaction.Extended, label: Label, isRoot: boolean) {
            super(transaction, label, isRoot);
        }

        asRemote(transaction: GraknTransaction): AttributeType.RemoteDateTime {
            return this;
        }

        getInstances(): Stream<Attribute.DateTime> {
            return super.getInstances() as Stream<Attribute.DateTime>;
        }

        getSubtypes(): Stream<AttributeType.RemoteDateTime> {
            return super.getSubtypes() as Stream<AttributeType.RemoteDateTime>;
        }

        async get(value: Date): Promise<Attribute.DateTime> {
            return await (super.getImpl(RequestBuilder.Thing.Attribute.attributeValueDateTimeReq(value)) as Promise<Attribute.DateTime>);
        }

        getValueType(): AttributeType.ValueType {
            return AttributeType.ValueType.DATETIME;
        }

        async put(value: Date): Promise<Attribute.DateTime> {
            const request = RequestBuilder.Type.AttributeType.putReq(this.getLabel(), RequestBuilder.Thing.Attribute.attributeValueDateTimeReq(value));
            return await (this.execute(request).then((res) => AttributeImpl.of(res.getAttributeTypePutRes().getAttribute())) as Promise<Attribute.DateTime>);
        }

        isDateTime(): boolean {
            return true;
        }

        asDateTime(): AttributeType.RemoteDateTime {
            return this;
        }

    }
}



