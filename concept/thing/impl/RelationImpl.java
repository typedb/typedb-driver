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

import grakn.client.Grakn;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.impl.RelationTypeImpl;
import grakn.client.concept.type.impl.RoleTypeImpl;
import grakn.client.concept.type.impl.TypeImpl;
import grakn.common.collection.Bytes;
import grakn.protocol.ConceptProto;
import grakn.protocol.ConceptProto.Relation.AddPlayer;
import grakn.protocol.ConceptProto.Relation.GetPlayers;
import grakn.protocol.ConceptProto.Relation.RemovePlayer;
import grakn.protocol.ConceptProto.ThingMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static grakn.client.concept.proto.ConceptProtoBuilder.thing;
import static grakn.client.concept.proto.ConceptProtoBuilder.type;
import static grakn.client.concept.proto.ConceptProtoBuilder.types;

public abstract class RelationImpl {

    public static class Local extends ThingImpl.Local implements Relation.Local {

        Local(String iid) {
            super(iid);
        }

        public static RelationImpl.Local of(final ConceptProto.Thing protoThing) {
            return new RelationImpl.Local(Bytes.bytesToHexString(protoThing.getIid().toByteArray()));
        }

        @Override
        public RelationImpl.Remote asRemote(Grakn.Transaction transaction) {
            return new RelationImpl.Remote(transaction, getIID());
        }
    }

    public static class Remote extends ThingImpl.Remote implements Relation.Remote {

        public Remote(final Grakn.Transaction transaction, final String iid) {
            super(transaction, iid);
        }

        public static RelationImpl.Remote of(final Grakn.Transaction transaction, final ConceptProto.Thing protoThing) {
            return new RelationImpl.Remote(transaction, Bytes.bytesToHexString(protoThing.getIid().toByteArray()));
        }

        @Override
        public RelationImpl.Remote asRemote(final Grakn.Transaction transaction) {
            return new RelationImpl.Remote(transaction, getIID());
        }

        @Override
        public RelationTypeImpl.Local getType() {
            return super.getType().asRelationType();
        }

        @Override
        public Map<RoleTypeImpl.Local, List<ThingImpl.Local>> getPlayersByRoleType() {
            final ThingMethod.Iter.Req method = ThingMethod.Iter.Req.newBuilder()
                    .setRelationGetPlayersByRoleTypeIterReq(ConceptProto.Relation.GetPlayersByRoleType.Iter.Req.getDefaultInstance()).build();

            final Stream<ConceptProto.Relation.GetPlayersByRoleType.Iter.Res> stream = tx().concepts().iterateThingMethod(getIID(), method, ThingMethod.Iter.Res::getRelationGetPlayersByRoleTypeIterRes);

            final Map<RoleTypeImpl.Local, List<ThingImpl.Local>> rolePlayerMap = new HashMap<>();
            stream.forEach(rolePlayer -> {
                final RoleTypeImpl.Local role = TypeImpl.Local.of(rolePlayer.getRoleType()).asRoleType();
                final ThingImpl.Local player = ThingImpl.Local.of(rolePlayer.getPlayer());
                if (rolePlayerMap.containsKey(role)) {
                    rolePlayerMap.get(role).add(player);
                } else {
                    rolePlayerMap.put(role, new ArrayList<>(Collections.singletonList(player)));
                }
            });

            final Map<RoleTypeImpl.Local, List<ThingImpl.Local>> result = new HashMap<>();
            for (Map.Entry<RoleTypeImpl.Local, List<ThingImpl.Local>> entry : rolePlayerMap.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
            return result;
        }

        @Override
        public Stream<ThingImpl.Local> getPlayers(RoleType... roleTypes) {
            return stream(
                    ThingMethod.Iter.Req.newBuilder().setRelationGetPlayersIterReq(
                            GetPlayers.Iter.Req.newBuilder().addAllRoleTypes(types(Arrays.asList(roleTypes)))).build(),
                    res -> res.getRelationGetPlayersIterRes().getThing()
            );
        }

        @Override
        public void addPlayer(RoleType roleType, Thing player) {
            execute(ThingMethod.Req.newBuilder().setRelationAddPlayerReq(
                    AddPlayer.Req.newBuilder().setRoleType(type(roleType)).setPlayer(thing(player))
            ).build());
        }

        @Override
        public void removePlayer(RoleType roleType, Thing player) {
            execute(ThingMethod.Req.newBuilder().setRelationRemovePlayerReq(
                    RemovePlayer.Req.newBuilder().setRoleType(type(roleType)).setPlayer(thing(player))
            ).build());
        }
    }
}
