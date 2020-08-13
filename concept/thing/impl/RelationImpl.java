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
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.RelationType;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.ConceptProto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class RelationImpl {
    /**
     * Client implementation of Relation
     */
    public static class Local extends ThingImpl.Local<Relation, RelationType> implements Relation.Local {

        public Local(ConceptProto.Concept concept) {
            super(concept);
        }
    }

    /**
     * Client implementation of Relation
     */
    public static class Remote extends ThingImpl.Local.Remote<Relation, RelationType> implements Relation.Remote {

        public Remote(Transaction tx, ConceptIID iid) {
            super(tx, iid);
        }

        @Override
        public RelationType.Remote getType() {
            return (RelationType.Remote) super.getType();
        }

        @Override
        public Relation.Remote setHas(Attribute<?> attribute) {
            return (Relation.Remote) super.setHas(attribute);
        }

        @Override
        public Relation.Remote unsetHas(Attribute<?> attribute) {
            return (Relation.Remote) super.unsetHas(attribute);
        }

        @Override
        public Map<RoleType.Remote, List<Thing.Remote<?, ?>>> getPlayersByRoleType() {
            final ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setRelationGetPlayersByRoleTypeIterReq(ConceptProto.Relation.GetPlayersByRoleType.Iter.Req.getDefaultInstance()).build();

            final Stream<ConceptProto.Relation.GetPlayersByRoleType.Iter.Res> stream = tx().iterateConceptMethod(iid(), method, ConceptProto.Method.Iter.Res::getRelationGetPlayersByRoleTypeIterRes);

            final Map<RoleType.Remote, List<Thing.Remote<?, ?>>> rolePlayerMap = new HashMap<>();
            stream.forEach(rolePlayer -> {
                final RoleType.Remote role = Concept.Remote.of(tx(), rolePlayer.getRoleType()).asRoleType();
                final Thing.Remote<?, ?> player = Concept.Remote.of(tx(), rolePlayer.getPlayer()).asThing();
                if (rolePlayerMap.containsKey(role)) {
                    rolePlayerMap.get(role).add(player);
                } else {
                    rolePlayerMap.put(role, new ArrayList<>(Collections.singletonList(player)));
                }
            });

            return rolePlayerMap;
        }

        @Override
        public Stream<Thing.Remote<?, ?>> getPlayers() {
            final ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setRelationGetPlayersIterReq(ConceptProto.Relation.GetPlayers.Iter.Req.newBuilder()).build();

            return conceptStream(method, res -> res.getRelationGetPlayersIterRes().getThing()).map(Concept.Remote::asThing);
        }

        @Override
        public Stream<Thing.Remote<?, ?>> getPlayers(final List<RoleType> roleTypes) {
            final ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setRelationGetPlayersForRoleTypesIterReq(ConceptProto.Relation.GetPlayersForRoleTypes.Iter.Req.newBuilder()
                            .addAllRoleTypes(RequestBuilder.ConceptMessage.concepts(roleTypes))).build();

            return conceptStream(method, res -> res.getRelationGetPlayersForRoleTypesIterRes().getThing()).map(Concept.Remote::asThing);
        }

        @Override
        public void addPlayer(RoleType roleType, Thing<?, ?> player) {
            final ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setRelationAddPlayerReq(ConceptProto.Relation.AddPlayer.Req.newBuilder()
                            .setRoleType(RequestBuilder.ConceptMessage.from(roleType))
                            .setPlayer(RequestBuilder.ConceptMessage.from(player))).build();

            runMethod(method);
        }

        @Override
        public void removePlayer(RoleType roleType, Thing<?, ?> player) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setRelationRemovePlayerReq(ConceptProto.Relation.RemovePlayer.Req.newBuilder()
                            .setRoleType(RequestBuilder.ConceptMessage.from(roleType))
                            .setPlayer(RequestBuilder.ConceptMessage.from(player))).build();

            runMethod(method);
        }

        @Override
        protected Relation.Remote asCurrentBaseType(Concept.Remote<?> other) {
            return other.asRelation();
        }

    }
}
