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

package grakn.client.concept.thing;

import grakn.client.api.GraknTransaction;
import grakn.client.api.concept.thing.Relation;
import grakn.client.api.concept.thing.Thing;
import grakn.client.api.concept.type.RoleType;
import grakn.client.concept.type.RelationTypeImpl;
import grakn.client.concept.type.RoleTypeImpl;
import grakn.client.concept.type.TypeImpl;
import grakn.common.collection.Bytes;
import grakn.protocol.ConceptProto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static grakn.client.common.rpc.RequestBuilder.Thing.Relation.addPlayerReq;
import static grakn.client.common.rpc.RequestBuilder.Thing.Relation.getPlayersByRoleTypeReq;
import static grakn.client.common.rpc.RequestBuilder.Thing.Relation.getPlayersReq;
import static grakn.client.common.rpc.RequestBuilder.Thing.Relation.getRelatingReq;
import static grakn.client.common.rpc.RequestBuilder.Thing.Relation.removePlayerReq;
import static grakn.client.common.rpc.RequestBuilder.Thing.protoThing;
import static grakn.client.concept.type.RoleTypeImpl.protoRoleType;
import static grakn.client.concept.type.TypeImpl.protoTypes;
import static java.util.Arrays.asList;

public class RelationImpl extends ThingImpl implements Relation {

    private final RelationTypeImpl type;

    RelationImpl(String iid, boolean isInferred, RelationTypeImpl type) {
        super(iid, isInferred);
        this.type = type;
    }

    public static RelationImpl of(ConceptProto.Thing protoThing) {
        return new RelationImpl(Bytes.bytesToHexString(protoThing.getIid().toByteArray()),
                protoThing.getInferred(),
                RelationTypeImpl.of(protoThing.getType()));
    }

    @Override
    public RelationImpl.Remote asRemote(GraknTransaction transaction) {
        return new RelationImpl.Remote(transaction, getIID(), isInferred(), type);
    }

    @Override
    public RelationTypeImpl getType() {
        return type;
    }

    @Override
    public final RelationImpl asRelation() {
        return this;
    }

    public static class Remote extends ThingImpl.Remote implements Relation.Remote {

        private final RelationTypeImpl type;

        public Remote(GraknTransaction transaction, String iid, boolean isInferred, RelationTypeImpl type) {
            super(transaction, iid, isInferred);
            this.type = type;
        }

        @Override
        public RelationImpl.Remote asRemote(GraknTransaction transaction) {
            return new RelationImpl.Remote(transaction, getIID(), isInferred(), type);
        }

        @Override
        public RelationTypeImpl getType() {
            return type;
        }

        @Override
        public void addPlayer(RoleType roleType, Thing player) {
            execute(addPlayerReq(getIID(), protoRoleType(roleType), protoThing(player.getIID())));
        }

        @Override
        public void removePlayer(RoleType roleType, Thing player) {
            execute(removePlayerReq(getIID(), protoRoleType(roleType), protoThing(player.getIID())));
        }

        @Override
        public Stream<ThingImpl> getPlayers(RoleType... roleTypes) {
            return stream(getPlayersReq(getIID(), protoTypes(asList(roleTypes))))
                    .flatMap(rp -> rp.getRelationGetPlayersResPart().getThingsList().stream())
                    .map(ThingImpl::of);
        }

        @Override
        public Map<RoleTypeImpl, List<ThingImpl>> getPlayersByRoleType() {
            Map<RoleTypeImpl, List<ThingImpl>> rolePlayerMap = new HashMap<>();
            stream(getPlayersByRoleTypeReq(getIID()))
                    .flatMap(rp -> rp.getRelationGetPlayersByRoleTypeResPart().getRoleTypesWithPlayersList().stream())
                    .forEach(rolePlayer -> {
                        RoleTypeImpl role = TypeImpl.of(rolePlayer.getRoleType()).asRoleType();
                        ThingImpl player = ThingImpl.of(rolePlayer.getPlayer());
                        if (rolePlayerMap.containsKey(role)) rolePlayerMap.get(role).add(player);
                        else rolePlayerMap.put(role, new ArrayList<>(Collections.singletonList(player)));
                    });
            return rolePlayerMap;
        }

        @Override
        public Stream<? extends RoleType> getRelating() {
            return stream(getRelatingReq(getIID()))
                    .flatMap(rp -> rp.getRelationGetRelatingResPart().getRoleTypesList().stream())
                    .map(RoleTypeImpl::of);
        }

        @Override
        public final RelationImpl.Remote asRelation() {
            return this;
        }
    }
}
