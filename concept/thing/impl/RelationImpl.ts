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
    ThingImpl,
    RemoteThingImpl,
    Relation,
    RemoteRelation,
    Thing,
    RelationTypeImpl,
    RoleType,
    Grakn,
    Stream,
    ConceptProtoReader,
    RPCTransaction,
    RoleTypeImpl, ConceptProtoBuilder,
} from "../../../dependencies_internal";
import Transaction = Grakn.Transaction;
import ConceptProto from "graknlabs-protocol/protobuf/concept_pb";
import TransactionProto from "graknlabs-protocol/protobuf/transaction_pb";

export class RelationImpl extends ThingImpl implements Relation {
    protected constructor(iid: string) {
        super(iid)
    }

    static of(protoThing: ConceptProto.Thing): RelationImpl {
        return new RelationImpl(protoThing.getIid_asB64());
    }

    asRemote(transaction: Transaction): RemoteRelationImpl {
        return new RemoteRelationImpl(transaction, this.getIID());
    }
}

export class RemoteRelationImpl extends RemoteThingImpl implements RemoteRelation {
    constructor(transaction: Transaction, iid: string) {
        super(transaction, iid);
    }

    asRemote(transaction: Transaction): RemoteRelationImpl {
        return new RemoteRelationImpl(transaction, this.getIID());
    }

    async getType(): Promise<RelationTypeImpl> {
        const res = await this.execute(new ConceptProto.Thing.Req().setThingGetTypeReq(new ConceptProto.Thing.GetType.Req()));
        return ConceptProtoReader.thingType(res.getThingGetTypeRes().getThingType()) as RelationTypeImpl;
    }

    async getPlayersByRoleType(): Promise<Map<RoleType, Thing[]>> {
        const method = new ConceptProto.Thing.Req()
            .setRelationGetPlayersByRoleTypeReq(new ConceptProto.Relation.GetPlayersByRoleType.Req())
            .setIid(this.getIID());
        const request = new TransactionProto.Transaction.Req().setThingReq(method);
        const stream = (this.transaction as RPCTransaction).stream(request, res => res.getThingRes().getRelationGetPlayersByRoleTypeRes().getRoleTypeWithPlayerList())
        const rolePlayerMap = new Map<RoleTypeImpl, ThingImpl[]>();
        for await (const rolePlayer of stream) {
            const role = ConceptProtoReader.type(rolePlayer.getRoleType()) as RoleTypeImpl;
            const player = ConceptProtoReader.thing(rolePlayer.getPlayer());
            if (!rolePlayerMap.has(role)) {
                rolePlayerMap.set(role, []);
            }
            rolePlayerMap.get(role).push(player)
        }
        return rolePlayerMap;
    }

    getPlayers(): Stream<ThingImpl>;
    getPlayers(roleTypes: RoleType[] = []): Stream<ThingImpl> {
        const method = new ConceptProto.Thing.Req().setRelationGetPlayersReq(
            new ConceptProto.Relation.GetPlayers.Req().setRoleTypesList(roleTypes.map(roleType => ConceptProtoBuilder.type(roleType))));
        return this.thingStream(method, res => res.getRelationGetPlayersRes().getThingList()) as Stream<ThingImpl>;
    }

    async addPlayer(roleType: RoleType, player: Thing): Promise<void> {
        await this.execute(new ConceptProto.Thing.Req().setRelationAddPlayerReq(
            new ConceptProto.Relation.AddPlayer.Req()
                .setPlayer(ConceptProtoBuilder.thing(player))
                .setRoleType(ConceptProtoBuilder.type(roleType))
        ));
    }

    async removePlayer(roleType: RoleType, player: Thing): Promise<void> {
        await this.execute(new ConceptProto.Thing.Req().setRelationRemovePlayerReq(
            new ConceptProto.Relation.RemovePlayer.Req()
                .setPlayer(ConceptProtoBuilder.thing(player))
                .setRoleType(ConceptProtoBuilder.type(roleType))
        ));
    }
}
