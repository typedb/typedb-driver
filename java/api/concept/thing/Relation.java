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

package com.vaticle.typedb.client.api.concept.thing;

import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.type.RelationType;
import com.vaticle.typedb.client.api.concept.type.RoleType;

import javax.annotation.CheckReturnValue;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface Relation extends Thing {
    @Override
    @CheckReturnValue
    default boolean isRelation() {
        return true;
    }

    @Override
    @CheckReturnValue
    default Relation asRelation() {
        return this;
    }

    @Override
    @CheckReturnValue
    RelationType getType();

    void addPlayer(TypeDBTransaction transaction, RoleType roleType, Thing player);

    void removePlayer(TypeDBTransaction transaction, RoleType roleType, Thing player);

    @CheckReturnValue
    Stream<? extends Thing> getPlayers(TypeDBTransaction transaction, RoleType... roleTypes);

    @CheckReturnValue
    Map<? extends RoleType, ? extends List<? extends Thing>> getPlayersByRoleType(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends RoleType> getRelating(TypeDBTransaction transaction);
}
