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

import {RelationType as RelationTypeProto} from "typedb-protocol/proto/concept";
import {Relation} from "../../api/concept/thing/Relation";
import {RelationType} from "../../api/concept/type/RelationType";
import {RoleType} from "../../api/concept/type/RoleType";
import {TypeDBTransaction} from "../../api/connection/TypeDBTransaction";
import {RequestBuilder} from "../../common/rpc/RequestBuilder";
import {Stream} from "../../common/util/Stream";
import {RelationImpl, RoleTypeImpl, ThingTypeImpl} from "../../dependencies_internal";
import {Concept} from "../../api/concept/Concept";
import Transitivity = Concept.Transitivity;

export class RelationTypeImpl extends ThingTypeImpl implements RelationType {
    constructor(label: string, root: boolean, abstract: boolean) {
        super(label, root, abstract);
    }

    protected get className(): string {
        return "RelationType";
    }

    isRelationType(): boolean {
        return true;
    }

    asRelationType(): RelationType {
        return this;
    }

    async isDeleted(transaction: TypeDBTransaction): Promise<boolean> {
        return !(await transaction.concepts.getRelationType(this.label.name));
    }

    async create(transaction: TypeDBTransaction): Promise<Relation> {
        const res = await this.execute(transaction, RequestBuilder.Type.RelationType.createReq(this.label));
        return RelationImpl.ofRelationProto(res.relation_type_create_res.relation);
    }

    async getSupertype(transaction: TypeDBTransaction): Promise<RelationType> {
        const res = await this.execute(transaction, RequestBuilder.Type.RelationType.getSupertypeReq(this.label));
        return RelationTypeImpl.ofRelationTypeProto(res.relation_type_get_supertype_res.relation_type);
    }

    async setSupertype(transaction: TypeDBTransaction, superRelationType: RelationType): Promise<void> {
        await this.execute(transaction, RequestBuilder.Type.RelationType.setSupertypeReq(this.label, RelationType.proto(superRelationType)));
    }

    getSupertypes(transaction: TypeDBTransaction): Stream<RelationType> {
        return this.stream(transaction, RequestBuilder.Type.RelationType.getSupertypesReq(this.label)).flatMap(
            resPart => Stream.array(resPart.relation_type_get_supertypes_res_part.relation_types)
        ).map(RelationTypeImpl.ofRelationTypeProto);
    }

    getSubtypes(transaction: TypeDBTransaction): Stream<RelationType>;
    getSubtypes(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<RelationType>;
    getSubtypes(transaction: TypeDBTransaction, transitivity?: Transitivity): Stream<RelationType> {
        if (!transitivity) transitivity = Transitivity.TRANSITIVE;
        return this.stream(transaction, RequestBuilder.Type.RelationType.getSubtypesReq(this.label, transitivity.proto())).flatMap(
            resPart => Stream.array(resPart.relation_type_get_subtypes_res_part.relation_types)
        ).map(RelationTypeImpl.ofRelationTypeProto);
    }

    getInstances(transaction: TypeDBTransaction): Stream<Relation>;
    getInstances(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Relation>;
    getInstances(transaction: TypeDBTransaction, transitivity?: Transitivity): Stream<Relation> {
        if (!transitivity) transitivity = Transitivity.TRANSITIVE;
        return this.stream(transaction, RequestBuilder.Type.RelationType.getInstancesReq(this.label, transitivity.proto())).flatMap(
            resPart => Stream.array(resPart.relation_type_get_instances_res_part.relations)
        ).map(RelationImpl.ofRelationProto);
    }

    getRelates(transaction: TypeDBTransaction): Stream<RoleType>;
    getRelates(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<RoleType>;
    getRelates(transaction: TypeDBTransaction, transitivity?: Transitivity): Stream<RoleType> {
        if (!transitivity) transitivity = Transitivity.TRANSITIVE;
        return this.stream(transaction, RequestBuilder.Type.RelationType.getRelatesReq(this.label, transitivity.proto())).flatMap(
            resPart => Stream.array(resPart.relation_type_get_relates_res_part.role_types)
        ).map(RoleTypeImpl.ofRoleTypeProto);
    }

    async getRelatesForRoleLabel(transaction: TypeDBTransaction, roleLabel: string): Promise<RoleType | null> {
        const res = await this.execute(transaction, RequestBuilder.Type.RelationType.getRelatesForRoleLabel(this.label, roleLabel));
        return RoleTypeImpl.ofRoleTypeProto(res.relation_type_get_relates_for_role_label_res.role_type);
    }

    async getRelatesOverridden(transaction: TypeDBTransaction, roleLabel: string): Promise<RoleType | null> {
        const res = await this.execute(transaction, RequestBuilder.Type.RelationType.getRelatesOverriddenReq(this.label, roleLabel));
        return RoleTypeImpl.ofRoleTypeProto(res.relation_type_get_relates_overridden_res.role_type);
    }

    async setRelates(transaction: TypeDBTransaction, roleLabel: string, overriddenLabel?: string): Promise<void> {
        await this.execute(transaction, RequestBuilder.Type.RelationType.setRelatesReq(this.label, roleLabel, overriddenLabel));
    }

    async unsetRelates(transaction: TypeDBTransaction, roleLabel: string): Promise<void> {
        await this.execute(transaction, RequestBuilder.Type.RelationType.unsetRelatesReq(this.label, roleLabel));
    }
}

export namespace RelationTypeImpl {
    export function ofRelationTypeProto(proto: RelationTypeProto): RelationType {
        if (!proto) return null;
        return new RelationTypeImpl(proto.label, proto.is_root, proto.is_abstract);
    }
}
