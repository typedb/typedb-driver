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

package com.vaticle.typedb.client.api.concept.type;

import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.thing.Relation;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface RelationType extends ThingType {
    @Override
    @CheckReturnValue
    default boolean isRelationType() {
        return true;
    }

    @Override
    @CheckReturnValue
    default RelationType asRelationType() {
        return this;
    }

    @CheckReturnValue
    Relation create(TypeDBTransaction transaction);

    @Override
    @CheckReturnValue
    Stream<? extends Relation> getInstances(TypeDBTransaction transaction);

    @Override
    @CheckReturnValue
    Stream<? extends Relation> getInstances(TypeDBTransaction transaction, Transitivity transitivity);

    @CheckReturnValue
    Stream<? extends RoleType> getRelates(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends RoleType> getRelates(TypeDBTransaction transaction, Transitivity transitivity);

    @Nullable
    @CheckReturnValue
    RoleType getRelates(TypeDBTransaction transaction, String roleLabel);

    @Nullable
    @CheckReturnValue
    RoleType getRelatesOverridden(TypeDBTransaction transaction, RoleType roleType);

    @Nullable
    @CheckReturnValue
    RoleType getRelatesOverridden(TypeDBTransaction transaction, String roleLabel);

    void setRelates(TypeDBTransaction transaction, String roleLabel);

    void setRelates(TypeDBTransaction transaction, String roleLabel, RoleType overriddenType);

    void setRelates(TypeDBTransaction transaction, String roleLabel, String overriddenLabel);

    void unsetRelates(TypeDBTransaction transaction, RoleType roleType);

    void unsetRelates(TypeDBTransaction transaction, String roleLabel);

    @Override
    @CheckReturnValue
    Stream<? extends RelationType> getSubtypes(TypeDBTransaction transaction);

    @Override
    @CheckReturnValue
    Stream<? extends RelationType> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity);

    void setSupertype(TypeDBTransaction transaction, RelationType superRelationType);
}
