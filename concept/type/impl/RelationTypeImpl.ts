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
    RelationType,
    RemoteRelationType,
    Grakn,
    Stream,
    RelationImpl,
    RoleTypeImpl, TypeImpl,
} from "../../../dependencies_internal";
import Transaction = Grakn.Transaction;
import ConceptProto, { Type as TypeProto } from "grakn-protocol/protobuf/concept_pb";

export class RelationTypeImpl extends ThingTypeImpl implements RelationType {
    protected constructor(label: string, isRoot: boolean) {
        super(label, isRoot);
    }

    static of(typeProto: TypeProto): RelationTypeImpl {
        return new RelationTypeImpl(typeProto.getLabel(), typeProto.getRoot());
    }

    asRemote(transaction: Transaction): RemoteRelationTypeImpl {
        return new RemoteRelationTypeImpl(transaction, this.getLabel(), this.isRoot())
    }
}

export class RemoteRelationTypeImpl extends RemoteThingTypeImpl implements RemoteRelationType {
    constructor(transaction: Transaction, label: string, isRoot: boolean) {
        super(transaction, label, isRoot);
    }

    asRemote(transaction: Transaction): RemoteRelationTypeImpl {
        return new RemoteRelationTypeImpl(transaction, this.getLabel(), this.isRoot())
    }

    create(): Promise<RelationImpl> {
        const method = new ConceptProto.Type.Req().setRelationTypeCreateReq(new ConceptProto.RelationType.Create.Req());
        return this.execute(method).then(res => RelationImpl.of(res.getRelationTypeCreateRes().getRelation()));
    }

    getRelates(roleLabel: string): Promise<RoleTypeImpl>;
    getRelates(): Stream<RoleTypeImpl>;
    getRelates(roleLabel?: string): Promise<RoleTypeImpl> | Stream<RoleTypeImpl> {
        if (roleLabel != null) {
            const method = new ConceptProto.Type.Req().setRelationTypeGetRelatesForRoleLabelReq(
                new ConceptProto.RelationType.GetRelatesForRoleLabel.Req().setLabel(roleLabel));
            return this.execute(method).then(res => {
                const getRelatesRes = res.getRelationTypeGetRelatesForRoleLabelRes();
                if (getRelatesRes.hasRoleType()) return TypeImpl.of(getRelatesRes.getRoleType()) as RoleTypeImpl;
                else return null;
            });
        }

        return this.typeStream(
            new ConceptProto.Type.Req().setRelationTypeGetRelatesReq(new ConceptProto.RelationType.GetRelates.Req()),
            res => res.getRelationTypeGetRelatesRes().getRoleList()) as Stream<RoleTypeImpl>;
    }

    setRelates(roleLabel: string): Promise<void>;
    setRelates(roleLabel: string, overriddenLabel: string): Promise<void>;
    async setRelates(roleLabel: string, overriddenLabel?: string): Promise<void> {
        const setRelatesReq = new ConceptProto.RelationType.SetRelates.Req().setLabel(roleLabel);
        if (overriddenLabel != null) setRelatesReq.setOverriddenLabel(overriddenLabel);
        await this.execute(new ConceptProto.Type.Req().setRelationTypeSetRelatesReq(setRelatesReq));
    }

    async unsetRelates(roleLabel: string): Promise<void> {
        await this.execute(new ConceptProto.Type.Req()
            .setRelationTypeUnsetRelatesReq(new ConceptProto.RelationType.UnsetRelates.Req().setLabel(roleLabel)));
    }

    setSupertype(relationType: RelationType): Promise<void> {
        return super.setSupertype(relationType);
    }

    getSupertype(): Promise<RelationTypeImpl> {
        return super.getSupertype() as Promise<RelationTypeImpl>;
    }

    getSupertypes(): Stream<RelationTypeImpl> {
        return super.getSupertypes() as Stream<RelationTypeImpl>;
    }

    getSubtypes(): Stream<RelationTypeImpl> {
        return super.getSubtypes() as Stream<RelationTypeImpl>;
    }

    getInstances(): Stream<RelationImpl> {
        return super.getInstances() as Stream<RelationImpl>;
    }
}
