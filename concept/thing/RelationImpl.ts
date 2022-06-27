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

import { Thing as ThingProto } from "typedb-protocol/common/concept_pb";
import { Relation } from "../../api/concept/thing/Relation";
import { Thing } from "../../api/concept/thing/Thing";
import { RelationType } from "../../api/concept/type/RelationType";
import { RoleType } from "../../api/concept/type/RoleType";
import { TypeDBTransaction } from "../../api/connection/TypeDBTransaction";
import { RequestBuilder } from "../../common/rpc/RequestBuilder";
import { Bytes } from "../../common/util/Bytes";
import { Stream } from "../../common/util/Stream";
import { RelationTypeImpl, RoleTypeImpl, ThingImpl } from "../../dependencies_internal";

export class RelationImpl extends ThingImpl implements Relation {

    private readonly _type: RelationType;

    constructor(iid: string, inferred: boolean, type: RelationType) {
        super(iid, inferred);
        this._type = type;
    }

    protected get className(): string {
        return "Relation";
    }

    asRemote(transaction: TypeDBTransaction): Relation.Remote {
        return new RelationImpl.Remote((transaction as TypeDBTransaction.Extended), this.iid, this.inferred, this.type);
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
}

export namespace RelationImpl {

    export function of(thingProto: ThingProto) {
        if (!thingProto) return null;
        const iid = Bytes.bytesToHexString(thingProto.getIid_asU8());
        return new RelationImpl(iid, thingProto.getInferred(), RelationTypeImpl.of(thingProto.getType()));
    }

    export class Remote extends ThingImpl.Remote implements Relation.Remote {

        private _type: RelationType;

        constructor(transaction: TypeDBTransaction.Extended, iid: string, inferred: boolean, type: RelationType) {
            super(transaction, iid, inferred);
            this._type = type;
        }

        protected get className(): string {
            return "Relation";
        }

        asRemote(transaction: TypeDBTransaction): Relation.Remote {
            return new RelationImpl.Remote((transaction as TypeDBTransaction.Extended), this.iid, this.inferred, this.type);
        }

        get type(): RelationType {
            return this._type;
        }

        isRelation(): boolean {
            return true;
        }

        asRelation(): Relation.Remote {
            return this;
        }

        async addPlayer(roleType: RoleType, player: Thing): Promise<void> {
            const request = RequestBuilder.Thing.Relation.addPlayerReq(this.iid, RoleType.proto(roleType), Thing.proto(player));
            await this.execute(request);
        }

        getPlayers(roleTypes?: RoleType[]): Stream<Thing> {
            if (!roleTypes) roleTypes = []
            const roleTypesProtos = roleTypes.map((roleType) => RoleType.proto(roleType));
            const request = RequestBuilder.Thing.Relation.getPlayersReq(this.iid, roleTypesProtos);
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getRelationGetPlayersResPart().getThingsList()))
                .map((thingProto) => ThingImpl.of(thingProto));
        }

        async getPlayersByRoleType(): Promise<Map<RoleType, Thing[]>> {
            const request = RequestBuilder.Thing.Relation.getPlayersByRoleTypeReq(this.iid);
            const rolePlayersMap = new Map<RoleType, Thing[]>();
            await this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getRelationGetPlayersByRoleTypeResPart().getRoleTypesWithPlayersList()))
                .forEach((roleTypeWithPlayerList) => {
                    const role = RoleTypeImpl.of(roleTypeWithPlayerList.getRoleType());
                    const player = ThingImpl.of(roleTypeWithPlayerList.getPlayer());
                    let key = this.findRole(rolePlayersMap, role);
                    if (key == null) {
                        rolePlayersMap.set(role, []);
                        key = role;
                    }
                    rolePlayersMap.get(key).push(player);
                })
            return rolePlayersMap;
        }

        async removePlayer(roleType: RoleType, player: Thing): Promise<void> {
            const request = RequestBuilder.Thing.Relation.removePlayerReq(this.iid, RoleType.proto(roleType), Thing.proto(player));
            await this.execute(request);
        }

        getRelating(): Stream<RoleType> {
            const request = RequestBuilder.Thing.Relation.getRelatingReq(this.iid);
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getRelationGetRelatingResPart().getRoleTypesList()))
                .map((roleTypeProto) => RoleTypeImpl.of(roleTypeProto));
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
}
