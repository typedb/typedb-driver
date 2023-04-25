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

package com.vaticle.typedb.client.concept.thing;

import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.thing.Relation;
import com.vaticle.typedb.client.api.concept.thing.Thing;
import com.vaticle.typedb.client.api.concept.type.RoleType;
import com.vaticle.typedb.client.concept.ConceptManagerImpl;
import com.vaticle.typedb.client.concept.type.RelationTypeImpl;
import com.vaticle.typedb.client.concept.type.RoleTypeImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_add_role_player;
import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_get_players_by_role_type;
import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_get_relating;
import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_get_role_players;
import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_get_type;
import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_remove_role_player;
import static com.vaticle.typedb.client.jni.typedb_client_jni.role_player_get_player;
import static com.vaticle.typedb.client.jni.typedb_client_jni.role_player_get_role_type;

public class RelationImpl extends ThingImpl implements Relation {

    public RelationImpl(com.vaticle.typedb.client.jni.Concept concept) {
        super(concept);
    }

    @Override
    public RelationTypeImpl getType() {
        return new RelationTypeImpl(relation_get_type(concept));
    }

    @Override
    public final RelationImpl asRelation() {
        return this;
    }

    @Override
    public void addPlayer(TypeDBTransaction transaction, RoleType roleType, Thing player) {
        relation_add_role_player(((ConceptManagerImpl) transaction.concepts()).transaction, concept, ((RoleTypeImpl) roleType).concept, ((ThingImpl) player).concept);
    }

    @Override
    public void removePlayer(TypeDBTransaction transaction, RoleType roleType, Thing player) {
        relation_remove_role_player(((ConceptManagerImpl) transaction.concepts()).transaction, concept, ((RoleTypeImpl) roleType).concept, ((ThingImpl) player).concept);
    }

    @Override
    public Stream<ThingImpl> getPlayers(TypeDBTransaction transaction, RoleType... roleTypes) {
        return relation_get_players_by_role_type(((ConceptManagerImpl) transaction.concepts()).transaction, concept, Arrays.stream(roleTypes).map(rt -> ((RoleTypeImpl) rt).concept).toArray(com.vaticle.typedb.client.jni.Concept[]::new)).stream().map(ThingImpl::of);
    }

    @Override
    public Map<RoleTypeImpl, List<ThingImpl>> getPlayersByRoleType(TypeDBTransaction transaction) {
        Map<RoleTypeImpl, List<ThingImpl>> rolePlayerMap = new HashMap<>();
        relation_get_role_players(((ConceptManagerImpl) transaction.concepts()).transaction, concept).stream().forEach(rolePlayer -> {
            RoleTypeImpl role = new RoleTypeImpl(role_player_get_role_type(rolePlayer));
            ThingImpl player = ThingImpl.of(role_player_get_player(rolePlayer));
            if (rolePlayerMap.containsKey(role)) rolePlayerMap.get(role).add(player);
            else rolePlayerMap.put(role, new ArrayList<>(Collections.singletonList(player)));
        });
        return rolePlayerMap;
    }

    @Override
    public Stream<? extends RoleType> getRelating(TypeDBTransaction transaction) {
        return relation_get_relating(((ConceptManagerImpl) transaction.concepts()).transaction, concept).stream().map(RoleTypeImpl::new);
    }
}
