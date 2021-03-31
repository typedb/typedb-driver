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


import {GraknTransaction} from "../../api/GraknTransaction";
import {RelationType, RemoteRelationType} from "../../api/concept/type/RelationType";
import {Relation} from "../../api/concept/thing/Relation";
import {RoleType} from "../../api/concept/type/RoleType";
import {RelationImpl, RoleTypeImpl, ThingTypeImpl} from "../../dependencies_internal";
import {Label} from "../../common/Label";
import {Stream} from "../../common/util/Stream";
import {RequestBuilder} from "../../common/rpc/RequestBuilder";
import {Type as TypeProto} from "grakn-protocol/common/concept_pb";

export class RelationTypeImpl extends ThingTypeImpl implements RelationType {

    constructor(label: string, isRoot: boolean) {
        super(label, isRoot);
    }

    asRemote(transaction: GraknTransaction): RemoteRelationType {
        return new RelationTypeImpl.RemoteImpl(transaction as GraknTransaction.Extended, this.getLabel(), this.isRoot());
    }

    isRelationType(): boolean {
        return true;
    }

}

export namespace RelationTypeImpl {

    export function of(relationTypeProto: TypeProto) {
        return new RelationTypeImpl(relationTypeProto.getLabel(), relationTypeProto.getRoot());
    }

    export class RemoteImpl extends ThingTypeImpl.RemoteImpl implements RemoteRelationType {

        constructor(transaction: GraknTransaction.Extended, label: Label, isRoot: boolean) {
            super(transaction, label, isRoot);
        }

        asRemote(transaction: GraknTransaction): RemoteRelationType {
            return this;
        }

        isRelationType(): boolean {
            return true;
        }

        async create(): Promise<Relation> {
            const request = RequestBuilder.Type.RelationType.createReq(this.getLabel());
            return this.execute(request).then((res) => RelationImpl.of(res.getRelationTypeCreateRes().getRelation()));
        }

        getSubtypes(): Stream<RelationType> {
            return super.getSubtypes() as Stream<RelationType>;
        }

        setSupertype(relationType: RelationType): Promise<void> {
            return super.setSupertype(relationType);
        }

        getInstances(): Stream<Relation> {
            return super.getInstances() as Stream<Relation>;
        }

        getRelates(): Stream<RoleType>;
        getRelates(roleLabel: string): Promise<RoleType>;
        getRelates(roleLabel?: string): Promise<RoleType> | Stream<RoleType> {
            if (roleLabel) {
                const request = RequestBuilder.Type.RelationType.getRelatesByRoleReq(this.getLabel(), roleLabel);
                return this.execute(request)
                    .then((res) => RoleTypeImpl.of(res.getRelationTypeGetRelatesForRoleLabelRes().getRoleType()));
            } else {
                const request = RequestBuilder.Type.RelationType.getRelatesReq(this.getLabel());
                return this.stream(request)
                    .flatMap((resPart) => {
                        return Stream.array(resPart.getRelationTypeGetRelatesResPart().getRolesList())
                    })
                    .map((roleProto) => {
                        return RoleTypeImpl.of(roleProto)
                    });
            }
        }

        async setRelates(roleLabel: string, overriddenLabel?: string): Promise<void> {
            let request;
            if (overriddenLabel) {
                request = RequestBuilder.Type.RelationType.setRelatesOverriddenReq(this.getLabel(), roleLabel, overriddenLabel);
            } else {
                request = RequestBuilder.Type.RelationType.setRelatesReq(this.getLabel(), roleLabel);
            }
            await this.execute(request);
        }

        async unsetRelates(roleLabel: string): Promise<void> {
            const request = RequestBuilder.Type.RelationType.unsetRelatesReq(this.getLabel(), roleLabel);
            await this.execute(request);
        }

    }

}
