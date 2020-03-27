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

package grakn.client.concept.remote;

import grakn.client.GraknClient;
import grakn.client.concept.ConceptId;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.session.ConceptProto;

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
class RemoteRelationImpl extends RemoteThingImpl<RemoteRelation, RemoteRelationType> implements RemoteRelation {

    RemoteRelationImpl(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    @Override
    public final Map<RemoteRole, Set<RemoteThing>> rolePlayersMap() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRelationRolePlayersMapReq(ConceptProto.Relation.RolePlayersMap.Req.getDefaultInstance()).build();

        int iteratorId = runMethod(method).getRelationRolePlayersMapIter().getId();
        Iterable<ConceptProto.Relation.RolePlayersMap.Iter.Res> rolePlayers = () -> tx().iterator(
                iteratorId, res -> res.getConceptMethodIterRes().getRelationRolePlayersMapIterRes()
        );

        Map<RemoteRole, Set<RemoteThing>> rolePlayerMap = new HashMap<>();
        for (ConceptProto.Relation.RolePlayersMap.Iter.Res rolePlayer : rolePlayers) {
            RemoteRole role = RemoteConcept.of(rolePlayer.getRole(), tx()).asRole();
            RemoteThing player = RemoteConcept.of(rolePlayer.getPlayer(), tx()).asThing();
            if (rolePlayerMap.containsKey(role)) {
                rolePlayerMap.get(role).add(player);
            } else {
                rolePlayerMap.put(role, new HashSet<>(Collections.singletonList(player)));
            }
        }

        return rolePlayerMap;
    }

    @Override
    public final Stream<RemoteThing> rolePlayers(RemoteRole... roles) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRelationRolePlayersReq(ConceptProto.Relation.RolePlayers.Req.newBuilder()
                        .addAllRoles(RequestBuilder.ConceptMessage.concepts(Arrays.asList(roles)))).build();

        int iteratorId = runMethod(method).getRelationRolePlayersIter().getId();
        return conceptStream(iteratorId, res -> res.getRelationRolePlayersIterRes().getThing()).map(RemoteConcept::asThing);
    }

    @Override
    public final RemoteRelation assign(RemoteRole role, RemoteThing player) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRelationAssignReq(ConceptProto.Relation.Assign.Req.newBuilder()
                        .setRole(RequestBuilder.ConceptMessage.from(role))
                        .setPlayer(RequestBuilder.ConceptMessage.from(player))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    @Override
    public final void unassign(RemoteRole role, RemoteThing player) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRelationUnassignReq(ConceptProto.Relation.Unassign.Req.newBuilder()
                        .setRole(RequestBuilder.ConceptMessage.from(role))
                        .setPlayer(RequestBuilder.ConceptMessage.from(player))).build();

        runMethod(method);
    }

    @Override
    final RemoteRelationType asCurrentType(RemoteConcept concept) {
        return concept.asRelationType();
    }

    @Override
    final RemoteRelation asCurrentBaseType(RemoteConcept other) {
        return other.asRelation();
    }

}
