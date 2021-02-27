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

package grakn.client.concept.thing.impl;

import grakn.client.GraknClient;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.impl.RelationTypeImpl;
import grakn.client.concept.type.impl.RoleTypeImpl;
import grakn.client.concept.type.impl.TypeImpl;
import grakn.common.collection.Bytes;
import grakn.protocol.ConceptProto;
import grakn.protocol.TransactionProto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static grakn.client.concept.proto.ConceptProtoBuilder.iid;
import static grakn.client.concept.proto.ConceptProtoBuilder.thing;
import static grakn.client.concept.proto.ConceptProtoBuilder.type;
import static grakn.client.concept.proto.ConceptProtoBuilder.types;

public class RelationImpl extends ThingImpl implements Relation {

    RelationImpl(String iid) {
        super(iid);
    }

    public static RelationImpl of(ConceptProto.Thing protoThing) {
        return new RelationImpl(Bytes.bytesToHexString(protoThing.getIid().toByteArray()));
    }

    @Override
    public RelationImpl.Remote asRemote(GraknClient.Transaction transaction) {
        return new RelationImpl.Remote(transaction, getIID());
    }

    @Override
    public final RelationImpl asRelation() {
        return this;
    }

    public static class Remote extends ThingImpl.Remote implements Relation.Remote {

        public Remote(GraknClient.Transaction transaction, String iid) {
            super(transaction, iid);
        }

        @Override
        public RelationImpl.Remote asRemote(GraknClient.Transaction transaction) {
            return new RelationImpl.Remote(transaction, getIID());
        }

        @Override
        public RelationTypeImpl getType() {
            return super.getType().asRelationType();
        }

        @Override
        public Map<RoleTypeImpl, List<ThingImpl>> getPlayersByRoleType() {
            ConceptProto.Thing.Req.Builder method = ConceptProto.Thing.Req.newBuilder()
                    .setRelationGetPlayersByRoleTypeReq(ConceptProto.Relation.GetPlayersByRoleType.Req.getDefaultInstance());

            TransactionProto.Transaction.Req.Builder request = TransactionProto.Transaction.Req.newBuilder()
                    .setThingReq(method.setIid(iid(getIID())));
            Stream<ConceptProto.Relation.GetPlayersByRoleType.RoleTypeWithPlayer> stream = transactionRPC.stream(
                    request, res -> res.getThingRes().getRelationGetPlayersByRoleTypeRes().getRoleTypesWithPlayersList().stream());

            Map<RoleTypeImpl, List<ThingImpl>> rolePlayerMap = new HashMap<>();
            stream.forEach(rolePlayer -> {
                RoleTypeImpl role = TypeImpl.of(rolePlayer.getRoleType()).asRoleType();
                ThingImpl player = ThingImpl.of(rolePlayer.getPlayer());
                if (rolePlayerMap.containsKey(role)) {
                    rolePlayerMap.get(role).add(player);
                } else {
                    rolePlayerMap.put(role, new ArrayList<>(Collections.singletonList(player)));
                }
            });
            return rolePlayerMap;
        }

        @Override
        public Stream<ThingImpl> getPlayers(RoleType... roleTypes) {
            return thingStream(
                    ConceptProto.Thing.Req.newBuilder().setRelationGetPlayersReq(
                            ConceptProto.Relation.GetPlayers.Req.newBuilder().addAllRoleTypes(types(Arrays.asList(roleTypes)))),
                    res -> res.getRelationGetPlayersRes().getThingsList());
        }

        @Override
        public void addPlayer(RoleType roleType, Thing player) {
            execute(ConceptProto.Thing.Req.newBuilder().setRelationAddPlayerReq(
                    ConceptProto.Relation.AddPlayer.Req.newBuilder().setRoleType(type(roleType)).setPlayer(thing(player))));
        }

        @Override
        public void removePlayer(RoleType roleType, Thing player) {
            execute(ConceptProto.Thing.Req.newBuilder().setRelationRemovePlayerReq(
                    ConceptProto.Relation.RemovePlayer.Req.newBuilder().setRoleType(type(roleType)).setPlayer(thing(player))));
        }

        @Override
        public final RelationImpl.Remote asRelation() {
            return this;
        }
    }
}
