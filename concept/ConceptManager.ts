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
    ConceptProtoBuilder,
    ThingType,
    EntityType,
    RelationType,
    AttributeType,
    EntityTypeImpl,
    TransactionRPC,
    RelationTypeImpl,
    AttributeTypeImpl,
    Thing,
    ThingImpl,
    Bytes,
    ThingTypeImpl,
} from "../dependencies_internal";
import ConceptProto from "grakn-protocol/protobuf/concept_pb";
import TransactionProto from "grakn-protocol/protobuf/transaction_pb";

export class ConceptManager {
    private readonly _transactionRPC: TransactionRPC;

    constructor (transactionRPC: TransactionRPC) {
        this._transactionRPC = transactionRPC;
    }

    async getRootThingType(): Promise<ThingType> {
        return await this.getThingType("thing");
    }

    async getRootEntityType(): Promise<EntityType> {
        return await this.getEntityType("entity");
    }

    async getRootRelationType(): Promise<RelationType> {
        return await this.getRelationType("relation");
    }

    async getRootAttributeType(): Promise<AttributeType> {
        return await this.getAttributeType("attribute");
    }

    async putEntityType(label: string): Promise<EntityType> {
        const req = new ConceptProto.ConceptManager.Req()
            .setPutEntityTypeReq(new ConceptProto.ConceptManager.PutEntityType.Req().setLabel(label));
        const res = await this.execute(req);
        return EntityTypeImpl.of(res.getPutEntityTypeRes().getEntityType());
    }

    async getEntityType(label: string): Promise<EntityType> {
        const type = await this.getThingType(label);
        if (type && type.isEntityType()) return type as EntityType;
        else return null;
    }

    async putRelationType(label: string): Promise<RelationType> {
        const req = new ConceptProto.ConceptManager.Req()
            .setPutRelationTypeReq(new ConceptProto.ConceptManager.PutRelationType.Req().setLabel(label));
        const res = await this.execute(req);
        return RelationTypeImpl.of(res.getPutRelationTypeRes().getRelationType());
    }

    async getRelationType(label: string): Promise<RelationType> {
        const type = await this.getThingType(label);
        if (type && type.isRelationType()) return type as RelationType;
        else return null;
    }

    async putAttributeType(label: string, valueType: AttributeType.ValueType): Promise<AttributeType> {
        const req = new ConceptProto.ConceptManager.Req()
            .setPutAttributeTypeReq(new ConceptProto.ConceptManager.PutAttributeType.Req()
                .setLabel(label)
                .setValueType(ConceptProtoBuilder.valueType(valueType)));
        const res = await this.execute(req);
        return AttributeTypeImpl.of(res.getPutAttributeTypeRes().getAttributeType());
    }

    async getAttributeType(label: string): Promise<AttributeType> {
        const type = await this.getThingType(label);
        if (type && type.isAttributeType()) return type as AttributeType;
        else return null;
    }

    async getThing(iid: string): Promise<Thing> {
        const req = new ConceptProto.ConceptManager.Req()
            .setGetThingReq(new ConceptProto.ConceptManager.GetThing.Req().setIid(Bytes.hexStringToBytes(iid)));
        const res = await this.execute(req);
        if (res.getGetThingRes().getResCase() === ConceptProto.ConceptManager.GetThing.Res.ResCase.THING)
            return ThingImpl.of(res.getGetThingRes().getThing());
        else
            return null;
    }

    async getThingType(label: string): Promise<ThingType> {
        const req = new ConceptProto.ConceptManager.Req()
            .setGetThingTypeReq(new ConceptProto.ConceptManager.GetThingType.Req().setLabel(label));
        const res = await this.execute(req);
        if (res.getGetThingTypeRes().getResCase() === ConceptProto.ConceptManager.GetThingType.Res.ResCase.THING_TYPE)
            return ThingTypeImpl.of(res.getGetThingTypeRes().getThingType());
        else
            return null;
    }

    private async execute(conceptManagerReq: ConceptProto.ConceptManager.Req): Promise<ConceptProto.ConceptManager.Res> {
        const transactionReq = new TransactionProto.Transaction.Req()
            .setConceptManagerReq(conceptManagerReq);
        return await this._transactionRPC.execute(transactionReq, res => res.getConceptManagerRes());
    }
}
