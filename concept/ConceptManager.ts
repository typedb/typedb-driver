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
    Type,
    RPCTransaction,
    RelationTypeImpl,
    AttributeTypeImpl,
    Thing, ThingImpl, TypeImpl,
} from "../dependencies_internal";
import ConceptProto from "grakn-protocol/protobuf/concept_pb";
import TransactionProto from "grakn-protocol/protobuf/transaction_pb";

export class ConceptManager {
    private readonly _rpcTransaction: RPCTransaction;

    constructor (rpcTransaction: RPCTransaction) {
        this._rpcTransaction = rpcTransaction;
    }

    async getRootThingType(): Promise<ThingType> {
        return await this.getType("thing") as ThingType;
    }

    async getRootEntityType(): Promise<EntityType> {
        return await this.getType("entity") as EntityType;
    }

    async getRootRelationType(): Promise<RelationType> {
        return await this.getType("relation") as RelationType;
    }

    async getRootAttributeType(): Promise<AttributeType> {
        return await this.getType("attribute") as AttributeType;
    }

    async putEntityType(label: string): Promise<EntityType> {
        const req = new ConceptProto.ConceptManager.Req()
            .setPutEntityTypeReq(new ConceptProto.ConceptManager.PutEntityType.Req().setLabel(label));
        const res = await this.execute(req);
        return EntityTypeImpl.of(res.getPutEntityTypeRes().getEntityType());
    }

    async getEntityType(label: string): Promise<EntityType> {
        const type = await this.getType(label);
        if (type instanceof EntityTypeImpl) return type as EntityType;
        else return null;
    }

    async putRelationType(label: string): Promise<RelationType> {
        const req = new ConceptProto.ConceptManager.Req()
            .setPutRelationTypeReq(new ConceptProto.ConceptManager.PutRelationType.Req().setLabel(label));
        const res = await this.execute(req);
        return RelationTypeImpl.of(res.getPutRelationTypeRes().getRelationType());
    }

    async getRelationType(label: string): Promise<RelationType> {
        const type = await this.getType(label);
        if (type instanceof RelationTypeImpl) return type as RelationType;
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
        const type = await this.getType(label);
        if (type instanceof AttributeTypeImpl) return type as AttributeType;
        else return null;
    }

    async getThing(iid: string): Promise<Thing> {
        const req = new ConceptProto.ConceptManager.Req()
            .setGetThingReq(new ConceptProto.ConceptManager.GetThing.Req().setIid(iid));
        const res = await this.execute(req);
        if (res.getGetThingRes().getResCase() === ConceptProto.ConceptManager.GetThing.Res.ResCase.THING)
            return ThingImpl.of(res.getGetThingRes().getThing());
        else
            return null;
    }

    async getType(label: string): Promise<Type> {
        const req = new ConceptProto.ConceptManager.Req()
            .setGetTypeReq(new ConceptProto.ConceptManager.GetType.Req().setLabel(label));
        const res = await this.execute(req);
        if (res.getGetTypeRes().getResCase() === ConceptProto.ConceptManager.GetType.Res.ResCase.TYPE)
            return TypeImpl.of(res.getGetTypeRes().getType());
        else
            return null;
    }

    private async execute(conceptManagerReq: ConceptProto.ConceptManager.Req): Promise<ConceptProto.ConceptManager.Res> {
        const transactionReq = new TransactionProto.Transaction.Req()
            .setConceptManagerReq(conceptManagerReq);
        return await this._rpcTransaction.execute(transactionReq, res => res.getConceptManagerRes());
    }
}
