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

import {Relation as RelationProto} from "typedb-protocol/proto/concept";
import {Relation} from "../../api/concept/thing/Relation";
import {Thing} from "../../api/concept/thing/Thing";
import {RelationType} from "../../api/concept/type/RelationType";
import {RoleType} from "../../api/concept/type/RoleType";
import {TypeDBTransaction} from "../../api/connection/TypeDBTransaction";
import {RequestBuilder} from "../../common/rpc/RequestBuilder";
import {Bytes} from "../../common/util/Bytes";
import {Stream} from "../../common/util/Stream";
import {RelationTypeImpl, RoleTypeImpl, ThingImpl} from "../../dependencies_internal";

export class RelationImpl extends ThingImpl implements Relation {
    private readonly _type: RelationType;

    constructor(iid: string, inferred: boolean, type: RelationType) {
        super(iid, inferred);
        this._type = type;
    }

    protected get className(): string {
        return "Relation";
    }

    get type(): RelationType {
        return this._type;
    }

    isRelation(): boolean {
        return true;
    }

    asRelation(): Relation {
        return this;
    }

    async isDeleted(transaction: TypeDBTransaction): Promise<boolean> {
        return !(await transaction.concepts.getRelation(this.iid));
    }

    async addRolePlayer(transaction: TypeDBTransaction, roleType: RoleType, player: Thing): Promise<void> {
        const request = RequestBuilder.Thing.Relation.addRolePlayerReq(this.iid, RoleType.proto(roleType), Thing.proto(player));
        await this.execute(transaction, request);
    }

    async removeRolePlayer(transaction: TypeDBTransaction, roleType: RoleType, player: Thing): Promise<void> {
        const request = RequestBuilder.Thing.Relation.removeRolePlayerReq(this.iid, RoleType.proto(roleType), Thing.proto(player));
        await this.execute(transaction, request);
    }

    getPlayersByRoleType(transaction: TypeDBTransaction, roleTypes?: RoleType[]): Stream<Thing> {
        if (!roleTypes) roleTypes = []
        const request = RequestBuilder.Thing.Relation.getPlayersByRoleTypeReq(this.iid, roleTypes.map(RoleType.proto));
        return this.stream(transaction, request)
            .flatMap((resPart) => Stream.array(resPart.relation_get_players_by_role_type_res_part.things))
            .map((thingProto) => ThingImpl.ofThingProto(thingProto));
    }

    async getRolePlayers(transaction: TypeDBTransaction): Promise<Map<RoleType, Thing[]>> {
        const request = RequestBuilder.Thing.Relation.getRolePlayersReq(this.iid);
        const rolePlayersMap = new Map<RoleType, Thing[]>();
        await this.stream(transaction, request)
            .flatMap((resPart) => Stream.array(resPart.relation_get_role_players_res_part.role_players))
            .forEach((roleTypeWithPlayerList) => {
                const role = RoleTypeImpl.ofRoleTypeProto(roleTypeWithPlayerList.role_type);
                const player = ThingImpl.ofThingProto(roleTypeWithPlayerList.player);
                let key = this.findRole(rolePlayersMap, role);
                if (key == null) {
                    rolePlayersMap.set(role, []);
                    key = role;
                }
                rolePlayersMap.get(key).push(player);
            })
        return rolePlayersMap;
    }

    getRelating(transaction: TypeDBTransaction): Stream<RoleType> {
        const request = RequestBuilder.Thing.Relation.getRelatingReq(this.iid);
        return this.stream(transaction, request)
            .flatMap((resPart) => Stream.array(resPart.relation_get_relating_res_part.role_types))
            .map((roleTypeProto) => RoleTypeImpl.ofRoleTypeProto(roleTypeProto));
    }

    private findRole(map: Map<RoleType, Thing[]>, role: RoleType) {
        const iter = map.keys();
        let next = iter.next();
        while (!next.done) {
            const roleType = next.value;
            if (roleType.label.scopedName === role.label.scopedName) {
                return roleType;
            }
            next = iter.next();
        }
        return null;
    }
}

export namespace RelationImpl {
    export function ofRelationProto(proto: RelationProto) {
        if (!proto) return null;
        const iid = Bytes.bytesToHexString(proto.iid);
        return new RelationImpl(iid, proto.inferred, RelationTypeImpl.ofRelationTypeProto(proto.relation_type));
    }
}
