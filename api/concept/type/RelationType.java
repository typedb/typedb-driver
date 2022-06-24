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
    default boolean isRelationType() {
        return true;
    }

    @Override
    RelationType.Remote asRemote(TypeDBTransaction transaction);

    interface Remote extends ThingType.Remote, RelationType {

        @CheckReturnValue
        Relation create();

        @Override
        @CheckReturnValue
        Stream<? extends Relation> getInstances();

        @Override
        @CheckReturnValue
        Stream<? extends Relation> getInstancesExplicit();

        @CheckReturnValue
        Stream<? extends RoleType> getRelates();

        @CheckReturnValue
        Stream<? extends RoleType> getRelatesExplicit();

        @Nullable
        @CheckReturnValue
        RoleType getRelates(String roleLabel);

        @Nullable
        @CheckReturnValue
        RoleType getRelatesOverridden(RoleType roleType);

        @Nullable
        @CheckReturnValue
        RoleType getRelatesOverridden(String roleLabel);

        void setRelates(String roleLabel);

        void setRelates(String roleLabel, RoleType overriddenType);

        void setRelates(String roleLabel, String overriddenLabel);

        void unsetRelates(RoleType roleType);

        void unsetRelates(String roleLabel);

        @Override
        @CheckReturnValue
        Stream<? extends RelationType> getSubtypes();

        @Override
        @CheckReturnValue
        Stream<? extends RelationType> getSubtypesExplicit();

        void setSupertype(RelationType superRelationType);
    }
}
