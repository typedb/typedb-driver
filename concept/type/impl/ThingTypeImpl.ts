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
    TypeImpl,
    RemoteTypeImpl,
    ThingType,
    RemoteThingType,
    AttributeType,
    RoleType,
    Grakn,
    Stream,
    ThingImpl,
    RoleTypeImpl,
    AttributeTypeImpl,
    ConceptProtoBuilder, EntityTypeImpl, RelationTypeImpl, GraknClientError, ErrorMessage,
} from "../../../dependencies_internal";
import Transaction = Grakn.Transaction;
import ConceptProto from "grakn-protocol/protobuf/concept_pb";
import assert from "assert";


export class ThingTypeImpl extends TypeImpl implements ThingType {
    constructor(label: string, isRoot: boolean) {
        super(label, isRoot);
    }

    asRemote(transaction: Transaction): RemoteThingType {
        return new RemoteThingTypeImpl(transaction, this.getLabel(), this.isRoot());
    }

    isThingType(): boolean {
        return true;
    }
}

export class RemoteThingTypeImpl extends RemoteTypeImpl implements RemoteThingType {
    constructor(transaction: Transaction, label: string, isRoot: boolean) {
        super(transaction, label, isRoot);
    }

    isThingType(): boolean {
        return true;
    }

    protected setSupertype(thingType: ThingType): Promise<void> {
        return super.setSupertype(thingType);
    }

    getSupertype(): Promise<ThingTypeImpl> {
        return super.getSupertype() as Promise<ThingTypeImpl>;
    }

    getSupertypes(): Stream<ThingTypeImpl> {
        return super.getSupertypes() as Stream<ThingTypeImpl>;
    }

    getSubtypes(): Stream<ThingTypeImpl> {
        return super.getSubtypes() as Stream<ThingTypeImpl>;
    }

    getInstances(): Stream<ThingImpl> {
        const request = new ConceptProto.Type.Req().setThingTypeGetInstancesReq(new ConceptProto.ThingType.GetInstances.Req());
        return this.thingStream(request, res => res.getThingTypeGetInstancesRes().getThingsList());
    }

    async setAbstract(): Promise<void> {
        await this.execute(new ConceptProto.Type.Req().setThingTypeSetAbstractReq(new ConceptProto.ThingType.SetAbstract.Req()));
    }

    async unsetAbstract(): Promise<void> {
        await this.execute(new ConceptProto.Type.Req().setThingTypeUnsetAbstractReq(new ConceptProto.ThingType.UnsetAbstract.Req()));
    }

    async setPlays(role: RoleType, overriddenType?: RoleType): Promise<void> {
        const setPlaysReq = new ConceptProto.ThingType.SetPlays.Req().setRole(ConceptProtoBuilder.type(role));
        if (overriddenType) setPlaysReq.setOverriddenRole(ConceptProtoBuilder.type(overriddenType));
        await this.execute(new ConceptProto.Type.Req().setThingTypeSetPlaysReq(setPlaysReq));
    }

    async setOwns(attributeType: AttributeType): Promise<void>;
    async setOwns(attributeType: AttributeType, isKey: boolean): Promise<void>;
    async setOwns(attributeType: AttributeType, overriddenType: AttributeType): Promise<void>;
    async setOwns(attributeType: AttributeType, isKey: boolean, overriddenType: AttributeType): Promise<void>;
    async setOwns(attributeType: AttributeType, isKeyOrOverriddenType?: boolean | AttributeType, overriddenType?: AttributeType): Promise<void> {
        const setOwnsReq = new ConceptProto.ThingType.SetOwns.Req().setAttributeType(ConceptProtoBuilder.type(attributeType))
            .setIsKey(typeof isKeyOrOverriddenType === "boolean" ? isKeyOrOverriddenType : false);
        let overriddenType1: AttributeType;
        if (isKeyOrOverriddenType instanceof AttributeTypeImpl) overriddenType1 = isKeyOrOverriddenType;
        else if (overriddenType) overriddenType1 = overriddenType;
        if (overriddenType1) setOwnsReq.setOverriddenType(ConceptProtoBuilder.type(overriddenType1));
        await this.execute(new ConceptProto.Type.Req().setThingTypeSetOwnsReq(setOwnsReq));
    }

    getPlays(): Stream<RoleTypeImpl> {
        const request = new ConceptProto.Type.Req().setThingTypeGetPlaysReq(new ConceptProto.ThingType.GetPlays.Req());
        return this.typeStream(request, res => res.getThingTypeGetPlaysRes().getRolesList()) as Stream<RoleTypeImpl>;
    }

    getOwns(): Stream<AttributeTypeImpl>;
    getOwns(valueType: AttributeType.ValueType): Stream<AttributeTypeImpl>;
    getOwns(keysOnly: boolean): Stream<AttributeTypeImpl>;
    getOwns(valueType: AttributeType.ValueType, keysOnly: boolean): Stream<AttributeTypeImpl>;
    getOwns(valueTypeOrKeysOnly?: AttributeType.ValueType | boolean, keysOnly?: boolean): Stream<AttributeTypeImpl> {
        const getOwnsReq = new ConceptProto.ThingType.GetOwns.Req()
            .setKeysOnly(typeof valueTypeOrKeysOnly === "boolean" ? valueTypeOrKeysOnly : typeof keysOnly === "boolean" ? keysOnly : false);
        // Here we take advantage of the fact that AttributeType.ValueType is a string enum
        if (typeof valueTypeOrKeysOnly === "string") getOwnsReq.setValueType(ConceptProtoBuilder.valueType(valueTypeOrKeysOnly));
        const request = new ConceptProto.Type.Req().setThingTypeGetOwnsReq(getOwnsReq);
        return this.typeStream(request, res => res.getThingTypeGetOwnsRes().getAttributeTypesList()) as Stream<AttributeTypeImpl>;
    }

    async unsetPlays(role: RoleType): Promise<void> {
        await this.execute(new ConceptProto.Type.Req().setThingTypeUnsetPlaysReq(
            new ConceptProto.ThingType.UnsetPlays.Req().setRole(ConceptProtoBuilder.type(role))));
    }

    async unsetOwns(attributeType: AttributeType): Promise<void> {
        await this.execute(new ConceptProto.Type.Req().setThingTypeUnsetOwnsReq(
            new ConceptProto.ThingType.UnsetOwns.Req().setAttributeType(ConceptProtoBuilder.type(attributeType))));
    }

    asRemote(transaction: Transaction): RemoteThingTypeImpl {
        return new RemoteThingTypeImpl(transaction, this.getLabel(), this.isRoot());
    }
}

export namespace ThingTypeImpl {
    export function of(typeProto: ConceptProto.Type): ThingTypeImpl {
        switch (typeProto.getEncoding()) {
            case ConceptProto.Type.Encoding.ENTITY_TYPE:
                return EntityTypeImpl.of(typeProto);
            case ConceptProto.Type.Encoding.RELATION_TYPE:
                return RelationTypeImpl.of(typeProto);
            case ConceptProto.Type.Encoding.ATTRIBUTE_TYPE:
                return AttributeTypeImpl.of(typeProto);
            case ConceptProto.Type.Encoding.THING_TYPE:
                assert(typeProto.getRoot());
                return new ThingTypeImpl(typeProto.getLabel(), typeProto.getRoot());
            default:
                throw new GraknClientError(ErrorMessage.Concept.BAD_ENCODING.message(typeProto.getEncoding()));
        }
    }
}
