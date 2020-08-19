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

import grakn.client.Grakn.Transaction;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.rpc.ConceptMessage;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class RelationImpl {
    /**
     * Client implementation of Relation
     */
    public static class Local extends ThingImpl.Local implements Relation.Local {

        public Local(ConceptProto.Thing thing) {
            super(thing);
        }

        public RelationType.Local getType() {
            return super.getType().asRelationType();
        }
    }

    /**
     * Client implementation of Relation
     */
    public static class Remote extends ThingImpl.Remote implements Relation.Remote {

        public Remote(Transaction tx, ConceptIID iid) {
            super(tx, iid);
        }

        @Override
        public RelationType.Remote getType() {
            return (RelationType.Remote) super.getType();
        }

        @Override
        public Map<? extends RoleType.Remote, List<? extends Thing.Remote>> getPlayersByRoleType() {
            final ConceptProto.ThingMethod.Iter.Req method = ConceptProto.ThingMethod.Iter.Req.newBuilder()
                    .setRelationGetPlayersByRoleTypeIterReq(ConceptProto.Relation.GetPlayersByRoleType.Iter.Req.getDefaultInstance()).build();

            final Stream<ConceptProto.Relation.GetPlayersByRoleType.Iter.Res> stream = tx().iterateConceptMethod(getIID(), method, ConceptProto.ThingMethod.Iter.Res::getRelationGetPlayersByRoleTypeIterRes);

            final Map<RoleType.Remote, List<Thing.Remote>> rolePlayerMap = new HashMap<>();
            stream.forEach(rolePlayer -> {
                final RoleType.Remote role = Type.Remote.of(tx(), rolePlayer.getRoleType()).asRoleType();
                final Thing.Remote player = Thing.Remote.of(tx(), rolePlayer.getPlayer());
                if (rolePlayerMap.containsKey(role)) {
                    rolePlayerMap.get(role).add(player);
                } else {
                    rolePlayerMap.put(role, new ArrayList<>(Collections.singletonList(player)));
                }
            });

            final Map<RoleType.Remote, List<? extends Thing.Remote>> result = new HashMap<>();
            for (Map.Entry<RoleType.Remote, List<Thing.Remote>> entry : rolePlayerMap.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
            return result;
        }

        @Override
        public Stream<Thing.Remote> getPlayers(RoleType... roleTypes) {
            final ConceptProto.ThingMethod.Iter.Req method = ConceptProto.ThingMethod.Iter.Req.newBuilder()
                    .setRelationGetPlayersIterReq(ConceptProto.Relation.GetPlayers.Iter.Req.newBuilder()
                            .addAllRoleTypes(ConceptMessage.types(Arrays.asList(roleTypes)))).build();

            return thingStream(method, res -> res.getRelationGetPlayersIterRes().getThing());
        }

        @Override
        public void addPlayer(RoleType roleType, Thing player) {
            final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                    .setRelationAddPlayerReq(ConceptProto.Relation.AddPlayer.Req.newBuilder()
                            .setRoleType(ConceptMessage.type(roleType))
                            .setPlayer(ConceptMessage.thing(player))).build();
            runMethod(method);
        }

        @Override
        public void removePlayer(RoleType roleType, Thing player) {
            final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                    .setRelationRemovePlayerReq(ConceptProto.Relation.RemovePlayer.Req.newBuilder()
                            .setRoleType(ConceptMessage.type(roleType))
                            .setPlayer(ConceptMessage.thing(player))).build();
            runMethod(method);
        }
    }
}
