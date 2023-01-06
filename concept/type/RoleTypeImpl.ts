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

import { Type as TypeProto } from "typedb-protocol/common/concept_pb";
import { Relation } from "../../api/concept/thing/Relation";
import { Thing } from "../../api/concept/thing/Thing";
import { RelationType } from "../../api/concept/type/RelationType";
import { RoleType } from "../../api/concept/type/RoleType";
import { ThingType } from "../../api/concept/type/ThingType";
import { TypeDBTransaction } from "../../api/connection/TypeDBTransaction";
import { Label } from "../../common/Label";
import { RequestBuilder } from "../../common/rpc/RequestBuilder";
import { Stream } from "../../common/util/Stream";
import { RelationImpl, RelationTypeImpl, ThingImpl, ThingTypeImpl, TypeImpl } from "../../dependencies_internal";

export class RoleTypeImpl extends TypeImpl implements RoleType {

    constructor(scope: string, label: string, root: boolean, abstract: boolean) {
        super(Label.scoped(scope, label), root, abstract);
    }

    protected get className(): string {
        return "RoleType";
    }

    asRemote(transaction: TypeDBTransaction): RoleType.Remote {
        return new RoleTypeImpl.Remote(transaction as TypeDBTransaction.Extended, this.label, this.root, this.abstract);
    }

    isRoleType(): boolean {
        return true;
    }

    asRoleType(): RoleType {
        return this;
    }
}

export namespace RoleTypeImpl {

    export function of(typeProto: TypeProto) {
        if (!typeProto) return null;
        return new RoleTypeImpl(typeProto.getScope(), typeProto.getLabel(), typeProto.getIsRoot(), typeProto.getIsAbstract());
    }

    export class Remote extends TypeImpl.Remote implements RoleType.Remote {

        constructor(transaction: TypeDBTransaction.Extended, label: Label, root: boolean, abstract: boolean) {
            super(transaction, label, root, abstract);
        }

        protected get className(): string {
            return "RoleType";
        }

        asRemote(transaction: TypeDBTransaction): RoleType.Remote {
            return new RoleTypeImpl.Remote(transaction as TypeDBTransaction.Extended, this.label, this.root, this.abstract);
        }

        isRoleType(): boolean {
            return true;
        }

        asRoleType(): RoleType.Remote {
            return this;
        }

        getSubtypes(): Stream<RoleType> {
            return super.getSubtypes() as Stream<RoleType>;
        }

        getSupertype(): Promise<RoleType> {
            return super.getSupertype() as Promise<RoleType>;
        }

        getSupertypes(): Stream<RoleType> {
            return super.getSupertypes() as Stream<RoleType>;
        }

        getRelationType(): Promise<RelationType> {
            return this.transaction.concepts.getRelationType(this.label.scope);
        }

        getRelationTypes(): Stream<RelationType> {
            const request = RequestBuilder.Type.RoleType.getRelationTypesReq(this.label);
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getRoleTypeGetRelationTypesResPart().getRelationTypesList()))
                .map((res) => RelationTypeImpl.of(res));
        }

        getPlayerTypes(): Stream<ThingType> {
            const request = RequestBuilder.Type.RoleType.getPlayerTypesReq(this.label);
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getRoleTypeGetPlayerTypesResPart().getThingTypesList()))
                .map((thingType) => ThingTypeImpl.of(thingType));
        }

        getPlayerTypesExplicit(): Stream<ThingType> {
            const request = RequestBuilder.Type.RoleType.getPlayerTypesExplicitReq(this.label);
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getRoleTypeGetPlayerTypesExplicitResPart().getThingTypesList()))
                .map((thingType) => ThingTypeImpl.of(thingType));
        }

        getRelationInstances(): Stream<Relation> {
            const request = RequestBuilder.Type.RoleType.getRelationInstancesReq(this.label);
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getRoleTypeGetRelationInstancesResPart().getRelationsList()))
                .map((rel) => RelationImpl.of(rel));
        }

        getRelationInstancesExplicit(): Stream<Relation> {
            const request = RequestBuilder.Type.RoleType.getRelationInstancesExplicitReq(this.label);
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getRoleTypeGetRelationInstancesExplicitResPart().getRelationsList()))
                .map((rel) => RelationImpl.of(rel));
        }

        getPlayerInstances(): Stream<Thing> {
            const request = RequestBuilder.Type.RoleType.getPlayerInstancesReq(this.label);
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getRoleTypeGetPlayerInstancesResPart().getThingsList()))
                .map((thing) => ThingImpl.of(thing));
        }

        getPlayerInstancesExplicit(): Stream<Thing> {
            const request = RequestBuilder.Type.RoleType.getPlayerInstancesExplicitReq(this.label);
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getRoleTypeGetPlayerInstancesExplicitResPart().getThingsList()))
                .map((thing) => ThingImpl.of(thing));
        }

        async isDeleted(): Promise<boolean> {
            const relationType = await this.getRelationType();
            return !(relationType) || (!(await relationType.asRemote(this.transaction).getRelates(this.label.name)));
        }
    }
}