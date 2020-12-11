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
    ThingTypeImpl,
    RemoteThingTypeImpl,
    RoleType,
    RemoteRoleType,
    Grakn,
    RelationTypeImpl,
    Stream,
    TypeImpl,
} from "../../../dependencies_internal";
import ConceptProto from "grakn-protocol/protobuf/concept_pb";
import Transaction = Grakn.Transaction;

export class RoleTypeImpl extends ThingTypeImpl implements RoleType {
    private readonly _scope: string;

    protected constructor(label: string, scope: string, isRoot: boolean) {
        super(label, isRoot);
        this._scope = scope;
    }

    static of(typeProto: ConceptProto.Type): RoleTypeImpl {
        return new RoleTypeImpl(typeProto.getLabel(), typeProto.getScope(), typeProto.getRoot());
    }

    getScope(): string {
        return this._scope;
    }

    getScopedLabel(): string {
        return `${this._scope}:${this.getLabel()}`;
    }

    asRemote(transaction: Transaction): RemoteRoleTypeImpl {
        return new RemoteRoleTypeImpl(transaction, this.getLabel(), this.getScope(), this.isRoot());
    }

    toString(): string {
        return `${this.constructor.name}[label: ${this._scope ? `${this._scope}:${this.getLabel()}` : this.getLabel()}]`;
    }
}

export class RemoteRoleTypeImpl extends RemoteThingTypeImpl implements RemoteRoleType {
    private readonly _scope: string;

    constructor(transaction: Transaction, label: string, scope: string, isRoot: boolean) {
        super(transaction, label, isRoot);
        this._scope = scope;
    }

    getScope(): string {
        return this._scope;
    }

    getScopedLabel(): string {
        return `${this._scope}:${this.getLabel()}`;
    }

    getSupertype(): Promise<RoleTypeImpl> {
        return super.getSupertype() as Promise<RoleTypeImpl>;
    }

    getSupertypes(): Stream<RoleTypeImpl> {
        return super.getSupertypes() as Stream<RoleTypeImpl>;
    }

    getSubtypes(): Stream<RoleTypeImpl> {
        return super.getSubtypes() as Stream<RoleTypeImpl>;
    }

    asRemote(transaction: Transaction): RemoteRoleTypeImpl {
        return new RemoteRoleTypeImpl(transaction, this.getLabel(), this._scope, this.isRoot())
    }

    async getRelationType(): Promise<RelationTypeImpl> {
        const method = new ConceptProto.Type.Req().setRoleTypeGetRelationTypeReq(new ConceptProto.RoleType.GetRelationType.Req());
        const response = (await this.execute(method)).getRoleTypeGetRelationTypeRes();
        return TypeImpl.of(response.getRelationType()) as RelationTypeImpl;
    }

    getRelationTypes(): Stream<RelationTypeImpl> {
        return this.typeStream(
            new ConceptProto.Type.Req().setRoleTypeGetRelationTypesReq(new ConceptProto.RoleType.GetRelationTypes.Req()),
            res => res.getRoleTypeGetRelationTypesRes().getRelationTypesList()) as Stream<RelationTypeImpl>;
    }

    getPlayers(): Stream<ThingTypeImpl> {
        return this.typeStream(
            new ConceptProto.Type.Req().setRoleTypeGetPlayersReq(new ConceptProto.RoleType.GetPlayers.Req()),
            res => res.getRoleTypeGetPlayersRes().getThingTypesList()) as Stream<ThingTypeImpl>;
    }

    protected typeStream(method: ConceptProto.Type.Req, typeGetter: (res: ConceptProto.Type.Res) => ConceptProto.Type[]): Stream<TypeImpl> {
        return super.typeStream(method.setScope(this._scope), typeGetter);
    }

    protected execute(method: ConceptProto.Type.Req): Promise<ConceptProto.Type.Res> {
        return super.execute(method.setScope(this._scope));
    }

    toString(): string {
        return `${this.constructor.name}[label: ${this._scope ? `${this._scope}:${this.getLabel()}` : this.getLabel()}]`;
    }
}
