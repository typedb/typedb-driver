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

package grakn.client.concept;

import grakn.client.GraknClient;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.session.ConceptProto;

import javax.annotation.CheckReturnValue;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Client implementation of Relation
 */
public class Relation extends Thing<Relation, RelationType> {

    Relation(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    public final Map<Role, Set<Thing>> rolePlayersMap() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRelationRolePlayersMapReq(ConceptProto.Relation.RolePlayersMap.Req.getDefaultInstance()).build();

        int iteratorId = runMethod(method).getRelationRolePlayersMapIter().getId();
        Iterable<ConceptProto.Relation.RolePlayersMap.Iter.Res> rolePlayers = () -> tx().iterator(
                iteratorId, res -> res.getConceptMethodIterRes().getRelationRolePlayersMapIterRes()
        );

        Map<Role, Set<Thing>> rolePlayerMap = new HashMap<>();
        for (ConceptProto.Relation.RolePlayersMap.Iter.Res rolePlayer : rolePlayers) {
            Role role = Concept.of(rolePlayer.getRole(), tx()).asRole();
            Thing player = Concept.of(rolePlayer.getPlayer(), tx()).asThing();
            if (rolePlayerMap.containsKey(role)) {
                rolePlayerMap.get(role).add(player);
            } else {
                rolePlayerMap.put(role, new HashSet<>(Collections.singletonList(player)));
            }
        }

        return rolePlayerMap;
    }

    public final Stream<Thing> rolePlayers(Role... roles) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRelationRolePlayersReq(ConceptProto.Relation.RolePlayers.Req.newBuilder()
                        .addAllRoles(RequestBuilder.ConceptMessage.concepts(Arrays.asList(roles)))).build();

        int iteratorId = runMethod(method).getRelationRolePlayersIter().getId();
        return conceptStream(iteratorId, res -> res.getRelationRolePlayersIterRes().getThing()).map(Concept::asThing);
    }

    public final Relation assign(Role role, Thing player) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRelationAssignReq(ConceptProto.Relation.Assign.Req.newBuilder()
                        .setRole(RequestBuilder.ConceptMessage.from(role))
                        .setPlayer(RequestBuilder.ConceptMessage.from(player))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    public final void unassign(Role role, Thing player) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRelationUnassignReq(ConceptProto.Relation.Unassign.Req.newBuilder()
                        .setRole(RequestBuilder.ConceptMessage.from(role))
                        .setPlayer(RequestBuilder.ConceptMessage.from(player))).build();

        runMethod(method);
    }

    final RelationType asCurrentType(Concept concept) {
        return concept.asRelationType();
    }

    final Relation asCurrentBaseType(Concept other) {
        return other.asRelation();
    }


    @Deprecated
    @CheckReturnValue
    @Override
    public Relation asRelation() {
        return this;
    }

    @Deprecated
    @CheckReturnValue
    @Override
    public boolean isRelation() {
        return true;
    }
}
