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


import { Attribute as AttributeProto, AttributeType as AttributeTypeProto, Type as TypeProto } from "typedb-protocol/common/concept_pb";
import { Attribute } from "../../api/concept/thing/Attribute";
import { AttributeType } from "../../api/concept/type/AttributeType";
import { ThingType } from "../../api/concept/type/ThingType";
import { TypeDBTransaction } from "../../api/connection/TypeDBTransaction";
import { ErrorMessage } from "../../common/errors/ErrorMessage";
import { TypeDBClientError } from "../../common/errors/TypeDBClientError";
import { Label } from "../../common/Label";
import { RequestBuilder } from "../../common/rpc/RequestBuilder";
import { Stream } from "../../common/util/Stream";
import { AttributeImpl, ThingTypeImpl } from "../../dependencies_internal";
import BAD_VALUE_TYPE = ErrorMessage.Concept.BAD_VALUE_TYPE;
import INVALID_CONCEPT_CASTING = ErrorMessage.Concept.INVALID_CONCEPT_CASTING;

export class AttributeTypeImpl extends ThingTypeImpl implements AttributeType {

    constructor(name: string, root: boolean, abstract: boolean) {
        super(name, root, abstract);
    }

    protected get className(): string {
        return "AttributeType";
    }

    asRemote(transaction: TypeDBTransaction): AttributeType.Remote {
        return new AttributeTypeImpl.Remote(transaction as TypeDBTransaction.Extended, this.label, this.root, this.abstract);
    }

    get valueType(): AttributeType.ValueType {
        return AttributeType.ValueType.OBJECT;
    }

    isAttributeType(): boolean {
        return true;
    }

    asAttributeType(): AttributeType {
        return this;
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

    asBoolean(): AttributeType.Boolean {
        if (this.root) {
            return new AttributeTypeImpl.Boolean(this.label.name, this.root, this.abstract);
        }
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "AttributeType.Boolean"));
    }

    asLong(): AttributeType.Long {
        if (this.root) {
            return new AttributeTypeImpl.Long(this.label.name, this.root, this.abstract);
        }
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "AttributeType.Long"));
    }

    asDouble(): AttributeType.Double {
        if (this.root) {
            return new AttributeTypeImpl.Double(this.label.name, this.root, this.abstract);
        }
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "AttributeType.Double"));
    }

    asString(): AttributeType.String {
        if (this.root) {
            return new AttributeTypeImpl.String(this.label.name, this.root, this.abstract);
        }
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "AttributeType.String"));
    }

    asDateTime(): AttributeType.DateTime {
        if (this.root) {
            return new AttributeTypeImpl.DateTime(this.label.name, this.root, this.abstract);
        }
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "AttributeType.DateTime"));
    }
}

export namespace AttributeTypeImpl {

    export function of(attributeTypeProto: TypeProto): AttributeType {
        if (!attributeTypeProto) return null;
        switch (attributeTypeProto.getValueType()) {
            case AttributeTypeProto.ValueType.BOOLEAN:
                return new AttributeTypeImpl.Boolean(attributeTypeProto.getLabel(), attributeTypeProto.getIsRoot(), attributeTypeProto.getIsAbstract());
            case AttributeTypeProto.ValueType.LONG:
                return new AttributeTypeImpl.Long(attributeTypeProto.getLabel(), attributeTypeProto.getIsRoot(), attributeTypeProto.getIsAbstract());
            case AttributeTypeProto.ValueType.DOUBLE:
                return new AttributeTypeImpl.Double(attributeTypeProto.getLabel(), attributeTypeProto.getIsRoot(), attributeTypeProto.getIsAbstract());
            case AttributeTypeProto.ValueType.STRING:
                return new AttributeTypeImpl.String(attributeTypeProto.getLabel(), attributeTypeProto.getIsRoot(), attributeTypeProto.getIsAbstract());
            case AttributeTypeProto.ValueType.DATETIME:
                return new AttributeTypeImpl.DateTime(attributeTypeProto.getLabel(), attributeTypeProto.getIsRoot(), attributeTypeProto.getIsAbstract());
            case AttributeTypeProto.ValueType.OBJECT:
                return new AttributeTypeImpl(attributeTypeProto.getLabel(), attributeTypeProto.getIsRoot(), attributeTypeProto.getIsAbstract());
            default:
                throw new TypeDBClientError(BAD_VALUE_TYPE.message(attributeTypeProto.getValueType()));
        }
    }

    export class Remote extends ThingTypeImpl.Remote implements AttributeType.Remote {

        constructor(transaction: TypeDBTransaction.Extended, label: Label, root: boolean, abstract: boolean) {
            super(transaction, label, root, abstract);
        }

        protected get className(): string {
            return "AttributeType";
        }

        asRemote(transaction: TypeDBTransaction): AttributeType.Remote {
            return new AttributeTypeImpl.Remote(transaction as TypeDBTransaction.Extended, this.label, this.root, this.abstract);
        }

        get valueType(): AttributeType.ValueType {
            return AttributeType.ValueType.OBJECT;
        }

        isAttributeType(): boolean {
            return true;
        }

        asAttributeType(): AttributeType.Remote {
            return this;
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

        asBoolean(): AttributeType.Boolean.Remote {
            if (this.root) {
                return new AttributeTypeImpl.Boolean.Remote(this.transaction, this.label, this.root, this.abstract);
            }
            throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "AttributeType.Boolean"));
        }

        asLong(): AttributeType.Long.Remote {
            if (this.root) {
                return new AttributeTypeImpl.Long.Remote(this.transaction, this.label, this.root, this.abstract);
            }
            throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "AttributeType.Long"));
        }

        asDouble(): AttributeType.Double.Remote {
            if (this.root) {
                return new AttributeTypeImpl.Double.Remote(this.transaction, this.label, this.root, this.abstract);
            }
            throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "AttributeType.Double"));
        }

        asString(): AttributeType.String.Remote {
            if (this.root) {
                return new AttributeTypeImpl.String.Remote(this.transaction, this.label, this.root, this.abstract);
            }
            throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "AttributeType.String"));
        }

        asDateTime(): AttributeType.DateTime.Remote {
            if (this.root) {
                return new AttributeTypeImpl.DateTime.Remote(this.transaction, this.label, this.root, this.abstract);
            }
            throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "AttributeType.DateTime"));
        }

        setSupertype(type: AttributeType): Promise<void> {
            return super.setSupertype(type);
        }

        getSubtypes(): Stream<AttributeType> {
            return super.getSupertypes() as Stream<AttributeType>;
        }

        getInstances(): Stream<Attribute> {
            return super.getInstances() as Stream<Attribute>;
        }

        getOwners(onlyKey?: boolean): Stream<ThingType> {
            const request = RequestBuilder.Type.AttributeType.getOwnersReq(this.label, !!onlyKey);
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getAttributeTypeGetOwnersResPart().getThingTypesList()))
                .map((thingTypeProto) => ThingTypeImpl.of(thingTypeProto));
        }

        getOwnersExplicit(onlyKey?: boolean): Stream<ThingType> {
            const request = RequestBuilder.Type.AttributeType.getOwnersExplicitReq(this.label, !!onlyKey);
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getAttributeTypeGetOwnersExplicitResPart().getThingTypesList()))
                .map((thingTypeProto) => ThingTypeImpl.of(thingTypeProto));
        }

        protected getImpl(valueProto: AttributeProto.Value): Promise<Attribute> {
            const request = RequestBuilder.Type.AttributeType.getReq(this.label, valueProto);
            return this.execute(request)
                .then((attrProto) => AttributeImpl.of(attrProto.getAttributeTypeGetRes().getAttribute()));
        }
    }

    export class Boolean extends AttributeTypeImpl implements AttributeType.Boolean {

        constructor(label: string, root: boolean, abstract: boolean) {
            super(label, root, abstract);
        }

        protected get className(): string {
            return "AttributeType.Boolean";
        }

        asRemote(transaction: TypeDBTransaction): AttributeType.Boolean.Remote {
            return new AttributeTypeImpl.Boolean.Remote(transaction as TypeDBTransaction.Extended, this.label, this.root, this.abstract);
        }

        get valueType(): AttributeType.ValueType {
            return AttributeType.ValueType.BOOLEAN;
        }

        isBoolean(): boolean {
            return true;
        }

        asBoolean(): AttributeType.Boolean {
            return this;
        }
    }

    export namespace Boolean {

        export class Remote extends AttributeTypeImpl.Remote implements AttributeType.Boolean.Remote {

            constructor(transaction: TypeDBTransaction.Extended, label: Label, root: boolean, abstract: boolean) {
                super(transaction, label, root, abstract);
            }

            protected get className(): string {
                return "AttributeType.Boolean";
            }

            asRemote(transaction: TypeDBTransaction): AttributeType.Boolean.Remote {
                return new AttributeTypeImpl.Boolean.Remote(transaction as TypeDBTransaction.Extended, this.label, this.root, this.abstract);
            }

            get valueType(): AttributeType.ValueType {
                return AttributeType.ValueType.BOOLEAN;
            }

            isBoolean(): boolean {
                return true;
            }

            asBoolean(): AttributeType.Boolean.Remote {
                return this;
            }

            async get(value: boolean): Promise<Attribute.Boolean> {
                return await (super.getImpl(RequestBuilder.Thing.Attribute.attributeValueBooleanReq(value)) as Promise<Attribute.Boolean>);
            }

            getInstances(): Stream<Attribute.Boolean> {
                return super.getInstances() as Stream<Attribute.Boolean>;
            }

            getSubtypes(): Stream<AttributeType.Boolean.Remote> {
                return super.getSubtypes() as Stream<AttributeType.Boolean.Remote>;
            }

            async put(value: boolean): Promise<Attribute.Boolean> {
                const request = RequestBuilder.Type.AttributeType.putReq(this.label, RequestBuilder.Thing.Attribute.attributeValueBooleanReq(value));
                return await (this.execute(request).then((res) => AttributeImpl.of(res.getAttributeTypePutRes().getAttribute())) as Promise<Attribute.Boolean>);
            }
        }
    }

    export class Long extends AttributeTypeImpl implements AttributeType.Long {

        constructor(label: string, root: boolean, abstract: boolean) {
            super(label, root, abstract);
        }

        protected get className(): string {
            return "AttributeType.Long";
        }

        asRemote(transaction: TypeDBTransaction): AttributeType.Long.Remote {
            return new AttributeTypeImpl.Long.Remote(transaction as TypeDBTransaction.Extended, this.label, this.root, this.abstract);
        }

        get valueType(): AttributeType.ValueType {
            return AttributeType.ValueType.LONG;
        }

        isLong(): boolean {
            return true;
        }

        asLong(): AttributeType.Long {
            return this;
        }
    }

    export namespace Long {

        export class Remote extends AttributeTypeImpl.Remote implements AttributeType.Long.Remote {

            constructor(transaction: TypeDBTransaction.Extended, label: Label, root: boolean, abstract: boolean) {
                super(transaction, label, root, abstract);
            }

            protected get className(): string {
                return "AttributeType.Long";
            }

            asRemote(transaction: TypeDBTransaction): AttributeType.Long.Remote {
                return new AttributeTypeImpl.Long.Remote(transaction as TypeDBTransaction.Extended, this.label, this.root, this.abstract);
            }

            get valueType(): AttributeType.ValueType {
                return AttributeType.ValueType.LONG;
            }

            isLong(): boolean {
                return true;
            }

            asLong(): AttributeType.Long.Remote {
                return this;
            }

            getInstances(): Stream<Attribute.Long> {
                return super.getInstances() as Stream<Attribute.Long>;
            }

            getSubtypes(): Stream<AttributeType.Long.Remote> {
                return super.getSubtypes() as Stream<AttributeType.Long.Remote>;
            }

            async get(value: number): Promise<Attribute.Long> {
                return await (super.getImpl(RequestBuilder.Thing.Attribute.attributeValueLongReq(value)) as Promise<Attribute.Long>);
            }

            async put(value: number): Promise<Attribute.Long> {
                const request = RequestBuilder.Type.AttributeType.putReq(this.label, RequestBuilder.Thing.Attribute.attributeValueLongReq(value));
                return await (this.execute(request).then((res) => AttributeImpl.of(res.getAttributeTypePutRes().getAttribute())) as Promise<Attribute.Long>);
            }
        }
    }

    export class Double extends AttributeTypeImpl implements AttributeType.Double {

        constructor(label: string, root: boolean, abstract: boolean) {
            super(label, root, abstract);
        }

        protected get className(): string {
            return "AttributeType.Double";
        }

        asRemote(transaction: TypeDBTransaction): AttributeType.Double.Remote {
            return new AttributeTypeImpl.Double.Remote(transaction as TypeDBTransaction.Extended, this.label, this.root, this.abstract);
        }

        get valueType(): AttributeType.ValueType {
            return AttributeType.ValueType.DOUBLE;
        }

        isDouble(): boolean {
            return true;
        }

        asDouble(): AttributeType.Double {
            return this;
        }
    }

    export namespace Double {

        export class Remote extends AttributeTypeImpl.Remote implements AttributeType.Double.Remote {

            constructor(transaction: TypeDBTransaction.Extended, label: Label, root: boolean, abstract: boolean) {
                super(transaction, label, root, abstract);
            }

            protected get className(): string {
                return "AttributeType.Double";
            }

            asRemote(transaction: TypeDBTransaction): AttributeType.Double.Remote {
                return new AttributeTypeImpl.Double.Remote(transaction as TypeDBTransaction.Extended, this.label, this.root, this.abstract);
            }

            get valueType(): AttributeType.ValueType {
                return AttributeType.ValueType.DOUBLE;
            }

            isDouble(): boolean {
                return true;
            }

            asDouble(): AttributeType.Double.Remote {
                return this;
            }

            getInstances(): Stream<Attribute.Double> {
                return super.getInstances() as Stream<Attribute.Double>;
            }

            getSubtypes(): Stream<AttributeType.Double.Remote> {
                return super.getSubtypes() as Stream<AttributeType.Double.Remote>;
            }

            async get(value: number): Promise<Attribute.Double> {
                return await (super.getImpl(RequestBuilder.Thing.Attribute.attributeValueDoubleReq(value)) as Promise<Attribute.Double>);
            }

            async put(value: number): Promise<Attribute.Double> {
                const request = RequestBuilder.Type.AttributeType.putReq(this.label, RequestBuilder.Thing.Attribute.attributeValueDoubleReq(value));
                return await (this.execute(request).then((res) => AttributeImpl.of(res.getAttributeTypePutRes().getAttribute())) as Promise<Attribute.Double>);
            }
        }
    }

    export class String extends AttributeTypeImpl implements AttributeType.String {

        constructor(label: string, root: boolean, abstract: boolean) {
            super(label, root, abstract);
        }

        protected get className(): string {
            return "AttributeType.String";
        }

        asRemote(transaction: TypeDBTransaction): AttributeType.String.Remote {
            return new AttributeTypeImpl.String.Remote(transaction as TypeDBTransaction.Extended, this.label, this.root, this.abstract)
        }

        get valueType(): AttributeType.ValueType {
            return AttributeType.ValueType.STRING;
        }

        isString(): boolean {
            return true;
        }

        asString(): AttributeType.String {
            return this;
        }
    }

    export namespace String {

        export class Remote extends AttributeTypeImpl.Remote implements AttributeType.String.Remote {

            constructor(transaction: TypeDBTransaction.Extended, label: Label, root: boolean, abstract: boolean) {
                super(transaction, label, root, abstract);
            }

            protected get className(): string {
                return "AttributeType.String";
            }

            asRemote(transaction: TypeDBTransaction): AttributeType.String.Remote {
                return new AttributeTypeImpl.String.Remote(transaction as TypeDBTransaction.Extended, this.label, this.root, this.abstract)
            }

            get valueType(): AttributeType.ValueType {
                return AttributeType.ValueType.STRING;
            }

            isString(): boolean {
                return true;
            }

            asString(): AttributeType.String.Remote {
                return this;
            }

            getInstances(): Stream<Attribute.String> {
                return super.getInstances() as Stream<Attribute.String>;
            }

            getSubtypes(): Stream<AttributeType.String.Remote> {
                return super.getSubtypes() as Stream<AttributeType.String.Remote>;
            }

            async get(value: string): Promise<Attribute.String> {
                return await (super.getImpl(RequestBuilder.Thing.Attribute.attributeValueStringReq(value)) as Promise<Attribute.String>);
            }

            async put(value: string): Promise<Attribute.String> {
                const request = RequestBuilder.Type.AttributeType.putReq(this.label, RequestBuilder.Thing.Attribute.attributeValueStringReq(value));
                return await (this.execute(request).then((res) => AttributeImpl.of(res.getAttributeTypePutRes().getAttribute())) as Promise<Attribute.String>);
            }

            async getRegex(): Promise<string> {
                const request = RequestBuilder.Type.AttributeType.getRegexReq(this.label);
                return await this.execute(request).then((res) => res.getAttributeTypeGetRegexRes().getRegex());
            }

            async setRegex(regex: string): Promise<void> {
                const request = RequestBuilder.Type.AttributeType.setRegexReq(this.label, regex);
                await this.execute(request);
            }
        }
    }

    export class DateTime extends AttributeTypeImpl implements AttributeType.DateTime {

        constructor(label: string, root: boolean, abstract: boolean) {
            super(label, root, abstract);
        }

        protected get className(): string {
            return "AttributeType.DateTime";
        }

        asRemote(transaction: TypeDBTransaction): AttributeType.DateTime.Remote {
            return new AttributeTypeImpl.DateTime.Remote(transaction as TypeDBTransaction.Extended, this.label, this.root, this.abstract);
        }

        get valueType(): AttributeType.ValueType {
            return AttributeType.ValueType.DATETIME;
        }

        isDateTime(): boolean {
            return true;
        }

        asDateTime(): AttributeType.DateTime {
            return this;
        }
    }

    export namespace DateTime {

        export class Remote extends AttributeTypeImpl.Remote implements AttributeType.DateTime.Remote {

            constructor(transaction: TypeDBTransaction.Extended, label: Label, root: boolean, abstract: boolean) {
                super(transaction, label, root, abstract);
            }

            protected get className(): string {
                return "AttributeType.DateTime";
            }

            asRemote(transaction: TypeDBTransaction): AttributeType.DateTime.Remote {
                return new AttributeTypeImpl.DateTime.Remote(transaction as TypeDBTransaction.Extended, this.label, this.root, this.abstract);
            }

            get valueType(): AttributeType.ValueType {
                return AttributeType.ValueType.DATETIME;
            }

            isDateTime(): boolean {
                return true;
            }

            asDateTime(): AttributeType.DateTime.Remote {
                return this;
            }

            getInstances(): Stream<Attribute.DateTime> {
                return super.getInstances() as Stream<Attribute.DateTime>;
            }

            getSubtypes(): Stream<AttributeType.DateTime.Remote> {
                return super.getSubtypes() as Stream<AttributeType.DateTime.Remote>;
            }

            async get(value: Date): Promise<Attribute.DateTime> {
                return await (super.getImpl(RequestBuilder.Thing.Attribute.attributeValueDateTimeReq(value)) as Promise<Attribute.DateTime>);
            }

            async put(value: Date): Promise<Attribute.DateTime> {
                const request = RequestBuilder.Type.AttributeType.putReq(this.label, RequestBuilder.Thing.Attribute.attributeValueDateTimeReq(value));
                return await (this.execute(request).then((res) => AttributeImpl.of(res.getAttributeTypePutRes().getAttribute())) as Promise<Attribute.DateTime>);
            }
        }
    }
}
