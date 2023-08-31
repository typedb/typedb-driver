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

import {EntityType as EntityTypeProto} from "typedb-protocol/proto/concept";
import {Entity} from "../../api/concept/thing/Entity";
import {EntityType} from "../../api/concept/type/EntityType";
import {TypeDBTransaction} from "../../api/connection/TypeDBTransaction";
import {RequestBuilder} from "../../common/rpc/RequestBuilder";
import {Stream} from "../../common/util/Stream";
import {EntityImpl, ThingTypeImpl} from "../../dependencies_internal";
import {Concept} from "../../api/concept/Concept";
import Transitivity = Concept.Transitivity;

export class EntityTypeImpl extends ThingTypeImpl implements EntityType {
    constructor(name: string, root: boolean, abstract: boolean) {
        super(name, root, abstract);
    }

    protected get className(): string {
        return "EntityType";
    }

    isEntityType(): boolean {
        return true;
    }

    asEntityType(): EntityType {
        return this;
    }

    async isDeleted(transaction: TypeDBTransaction): Promise<boolean> {
        return !(await transaction.concepts.getEntityType(this.label.name));
    }

    async create(transaction: TypeDBTransaction): Promise<Entity> {
        const res = await this.execute(transaction, RequestBuilder.Type.EntityType.createReq(this.label));
        return EntityImpl.ofEntityProto(res.entity_type_create_res.entity);
    }

    async getSupertype(transaction: TypeDBTransaction): Promise<EntityType> {
        const res = await this.execute(transaction, RequestBuilder.Type.EntityType.getSupertypeReq(this.label));
        return EntityTypeImpl.ofEntityTypeProto(res.entity_type_get_supertype_res.entity_type);
    }

    async setSupertype(transaction: TypeDBTransaction, superEntityType: EntityType): Promise<void> {
        await this.execute(transaction, RequestBuilder.Type.EntityType.setSupertypeReq(this.label, EntityType.proto(superEntityType)));
    }

    getSupertypes(transaction: TypeDBTransaction): Stream<EntityType> {
        return this.stream(transaction, RequestBuilder.Type.EntityType.getSupertypesReq(this.label)).flatMap(
            resPart => Stream.array(resPart.entity_type_get_supertypes_res_part.entity_types)
        ).map(EntityTypeImpl.ofEntityTypeProto);
    }

    getSubtypes(transaction: TypeDBTransaction): Stream<EntityType>;
    getSubtypes(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<EntityType>;
    getSubtypes(transaction: TypeDBTransaction, transitivity?: Transitivity): Stream<EntityType> {
        if (!transitivity) transitivity = Transitivity.TRANSITIVE;
        return this.stream(transaction, RequestBuilder.Type.EntityType.getSubtypesReq(this.label, transitivity.proto())).flatMap(
            resPart => Stream.array(resPart.entity_type_get_subtypes_res_part.entity_types)
        ).map(EntityTypeImpl.ofEntityTypeProto);
    }

    getInstances(transaction: TypeDBTransaction): Stream<Entity>;
    getInstances(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Entity>;
    getInstances(transaction: TypeDBTransaction, transitivity?: Transitivity): Stream<Entity> {
        if (!transitivity) transitivity = Transitivity.TRANSITIVE;
        return this.stream(transaction, RequestBuilder.Type.EntityType.getInstancesReq(this.label, transitivity.proto())).flatMap(
            resPart => Stream.array(resPart.entity_type_get_instances_res_part.entities)
        ).map(EntityImpl.ofEntityProto);
    }
}

export namespace EntityTypeImpl {
    export function ofEntityTypeProto(proto: EntityTypeProto): EntityType {
        if (!proto) return null;
        return new EntityTypeImpl(proto.label, proto.is_root, proto.is_abstract);
    }
}
