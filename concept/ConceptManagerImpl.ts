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

import { ConceptManager as ConceptProto } from "typedb-protocol/common/concept_pb";
import { Transaction as TransactionProto } from "typedb-protocol/common/transaction_pb";
import { ConceptManager } from "../api/concept/ConceptManager";
import { Thing } from "../api/concept/thing/Thing";
import { AttributeType } from "../api/concept/type/AttributeType";
import { EntityType } from "../api/concept/type/EntityType";
import { RelationType } from "../api/concept/type/RelationType";
import { ThingType } from "../api/concept/type/ThingType";
import { TypeDBTransaction } from "../api/connection/TypeDBTransaction";
import { RequestBuilder } from "../common/rpc/RequestBuilder";
import { AttributeTypeImpl, EntityTypeImpl, RelationTypeImpl, ThingImpl, ThingTypeImpl, } from "../dependencies_internal";
import {Concept} from "../api/concept/Concept";

export class ConceptManagerImpl implements ConceptManager {

    private _transaction: TypeDBTransaction.Extended;

    constructor(client: TypeDBTransaction.Extended) {
        this._transaction = client;
    }

    async getRootThingType(): Promise<ThingType> {
        return this.getThingType("thing");
    }

    async getRootEntityType(): Promise<EntityType> {
        return this.getEntityType("entity");
    }

    async getRootRelationType(): Promise<RelationType> {
        return this.getRelationType("relation");
    }

    async getRootAttributeType(): Promise<AttributeType> {
        return this.getAttributeType("attribute");
    }

    async getThingType(label: string): Promise<ThingType> {
        const request = RequestBuilder.ConceptManager.getThingTypeReq(label);
        const response = await this.execute(request);
        if (response.getGetThingTypeRes().getResCase() == ConceptProto.GetThingType.Res.ResCase.THING_TYPE) {
            return ThingTypeImpl.of(response.getGetThingTypeRes().getThingType());
        } else {
            return null;
        }
    }

    async getEntityType(label: string): Promise<EntityType> {
        const type = await this.getThingType(label);
        if (type?.isEntityType()) return type.asEntityType();
        else return null;
    }

    async getRelationType(label: string): Promise<RelationType> {
        const type = await this.getThingType(label);
        if (type?.isRelationType()) return type.asRelationType();
        else return null;
    }

    async getAttributeType(label: string): Promise<AttributeType> {
        const type = await this.getThingType(label);
        if (type?.isAttributeType()) return type.asAttributeType();
        else return null;
    }

    async getThing(iid: string): Promise<Thing> {
        const request = RequestBuilder.ConceptManager.getThingReq(iid);
        const response = await this.execute(request);
        if (response.getGetThingRes().getResCase() === ConceptProto.GetThing.Res.ResCase.THING) {
            return ThingImpl.of(response.getGetThingRes().getThing());
        } else {
            return null;
        }
    }

    async putEntityType(label: string): Promise<EntityType> {
        const request = RequestBuilder.ConceptManager.putEntityTypeReq(label);
        const response = await this.execute(request);
        return EntityTypeImpl.of(response.getPutEntityTypeRes().getEntityType());
    }

    async putRelationType(label: string): Promise<RelationType> {
        const request = RequestBuilder.ConceptManager.putRelationTypeReq(label);
        const response = await this.execute(request);
        return RelationTypeImpl.of(response.getPutRelationTypeRes().getRelationType());
    }

    async putAttributeType(label: string, valueType: Concept.ValueType): Promise<AttributeType> {
        const request = RequestBuilder.ConceptManager.putAttributeTypeReq(label, valueType.proto());
        const response = await this.execute(request);
        return AttributeTypeImpl.of(response.getPutAttributeTypeRes().getAttributeType());
    }

    private execute(request: TransactionProto.Req): Promise<ConceptProto.Res> {
        return this._transaction.rpcExecute(request).then((res) => res.getConceptManagerRes());
    }

}
