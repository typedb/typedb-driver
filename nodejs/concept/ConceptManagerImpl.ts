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

import {ConceptManager} from "../api/concept/ConceptManager";
import {AttributeType} from "../api/concept/type/AttributeType";
import {EntityType} from "../api/concept/type/EntityType";
import {RelationType} from "../api/concept/type/RelationType";
import {ThingType} from "../api/concept/type/ThingType";
import {TypeDBTransaction} from "../api/connection/TypeDBTransaction";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import {
    AttributeImpl,
    AttributeTypeImpl,
    EntityImpl,
    EntityTypeImpl,
    RelationImpl,
    RelationTypeImpl,
    ThingTypeImpl,
} from "../dependencies_internal";
import {Concept} from "../api/concept/Concept";
import {ConceptManagerRes} from "typedb-protocol/proto/concept";
import {TransactionReq} from "typedb-protocol/proto/transaction";
import {Entity} from "../api/concept/thing/Entity";
import {Attribute} from "../api/concept/thing/Attribute";
import {Relation} from "../api/concept/thing/Relation";
import {TypeDBClientError} from "../common/errors/TypeDBClientError";

export class ConceptManagerImpl implements ConceptManager {
    private _transaction: TypeDBTransaction.Extended;

    constructor(client: TypeDBTransaction.Extended) {
        this._transaction = client;
    }

    async getRootThingType(): Promise<ThingType> {
        return new ThingTypeImpl.Root();
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

    async getEntityType(label: string): Promise<EntityType> {
        const response = await this.execute(RequestBuilder.ConceptManager.getEntityTypeReq(label));
        if (response.has_get_entity_type_res) {
            return EntityTypeImpl.ofEntityTypeProto(response.get_entity_type_res.entity_type);
        } else return null;
    }

    async getRelationType(label: string): Promise<RelationType> {
        const response = await this.execute(RequestBuilder.ConceptManager.getRelationTypeReq(label));
        if (response.has_get_relation_type_res) {
            return RelationTypeImpl.ofRelationTypeProto(response.get_relation_type_res.relation_type);
        } else return null;
    }

    async getAttributeType(label: string): Promise<AttributeType> {
        const response = await this.execute(RequestBuilder.ConceptManager.getAttributeTypeReq(label));
        if (response.has_get_attribute_type_res) {
            return AttributeTypeImpl.ofAttributeTypeProto(response.get_attribute_type_res.attribute_type);
        } else return null;
    }

    async getEntity(iid: string): Promise<Entity> {
        const response = await this.execute(RequestBuilder.ConceptManager.getEntityReq(iid));
        if (response.has_get_entity_res) {
            return EntityImpl.ofEntityProto(response.get_entity_res.entity);
        } else return null;
    }

    async getRelation(iid: string): Promise<Relation> {
        const response = await this.execute(RequestBuilder.ConceptManager.getRelationReq(iid));
        if (response.has_get_relation_res) {
            return RelationImpl.ofRelationProto(response.get_relation_res.relation);
        } else return null;
    }

    async getAttribute(iid: string): Promise<Attribute> {
        const response = await this.execute(RequestBuilder.ConceptManager.getAttributeReq(iid));
        if (response.has_get_attribute_res) {
            return AttributeImpl.ofAttributeProto(response.get_attribute_res.attribute);
        } else return null;
    }

    async putEntityType(label: string): Promise<EntityType> {
        const response = await this.execute(RequestBuilder.ConceptManager.putEntityTypeReq(label));
        return EntityTypeImpl.ofEntityTypeProto(response.put_entity_type_res.entity_type);
    }

    async putRelationType(label: string): Promise<RelationType> {
        const response = await this.execute(RequestBuilder.ConceptManager.putRelationTypeReq(label));
        return RelationTypeImpl.ofRelationTypeProto(response.put_relation_type_res.relation_type);
    }

    async putAttributeType(label: string, valueType: Concept.ValueType): Promise<AttributeType> {
        const response = await this.execute(RequestBuilder.ConceptManager.putAttributeTypeReq(label, valueType.proto()));
        return AttributeTypeImpl.ofAttributeTypeProto(response.put_attribute_type_res.attribute_type);
    }

    async getSchemaExceptions(): Promise<TypeDBClientError[]> {
        const response = await this.execute(RequestBuilder.ConceptManager.getSchemaExceptions());
        return response.get_schema_exceptions_res.exceptions.map(schemaException =>
            new TypeDBClientError(`${schemaException.code} ${schemaException.message}`)
        );
    }

    private execute(request: TransactionReq): Promise<ConceptManagerRes> {
        return this._transaction.rpcExecute(request).then((res) => res.concept_manager_res);
    }
}
