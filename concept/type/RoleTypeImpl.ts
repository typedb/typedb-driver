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
import { RelationType } from "../../api/concept/type/RelationType";
import { RoleType } from "../../api/concept/type/RoleType";
import { ThingType } from "../../api/concept/type/ThingType";
import { TypeDBTransaction } from "../../api/connection/TypeDBTransaction";
import { Label } from "../../common/Label";
import { RequestBuilder } from "../../common/rpc/RequestBuilder";
import { Stream } from "../../common/util/Stream";
import { RelationTypeImpl, ThingTypeImpl, TypeImpl } from "../../dependencies_internal";

export class RoleTypeImpl extends TypeImpl implements RoleType {

    constructor(scope: string, label: string, root: boolean) {
        super(Label.scoped(scope, label), root);
    }

    protected get className(): string {
        return "RoleType";
    }

    asRemote(transaction: TypeDBTransaction): RoleType.Remote {
        return new RoleTypeImpl.Remote(transaction as TypeDBTransaction.Extended, this.label, this.root);
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
        return new RoleTypeImpl(typeProto.getScope(), typeProto.getLabel(), typeProto.getRoot());
    }

    export class Remote extends TypeImpl.Remote implements RoleType.Remote {

        constructor(transaction: TypeDBTransaction.Extended, label: Label, root: boolean) {
            super(transaction, label, root);
        }

        protected get className(): string {
            return "RoleType";
        }

        asRemote(transaction: TypeDBTransaction): RoleType.Remote {
            return new RoleTypeImpl.Remote(transaction as TypeDBTransaction.Extended, this.label, this.root);
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

        getPlayers(): Stream<ThingType> {
            const request = RequestBuilder.Type.RoleType.getPlayersReq(this.label);
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getRoleTypeGetPlayersResPart().getThingTypesList()))
                .map((thing) => ThingTypeImpl.of(thing));
        }

        async isDeleted(): Promise<boolean> {
            const relationType = await this.getRelationType();
            return !(relationType) || (!(await relationType.asRemote(this.transaction).getRelates(this.label.name)));
        }
    }
}