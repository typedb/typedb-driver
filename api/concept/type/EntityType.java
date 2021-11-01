/*
 * Copyright (C) 2021 Vaticle
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

import com.vaticle.typedb.client.api.concept.thing.Entity;
import com.vaticle.typedb.client.api.connection.TypeDBTransaction;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

public interface EntityType extends ThingType {

    @Override
    @CheckReturnValue
    default boolean isEntityType() {
        return true;
    }

    @Override
    @CheckReturnValue
    EntityType.Remote asRemote(TypeDBTransaction transaction);

    interface Remote extends ThingType.Remote, EntityType {

        @CheckReturnValue
        Entity create();

        @Override
        @CheckReturnValue
        Stream<? extends Entity> getInstances();

        @Override
        @CheckReturnValue
        Stream<? extends EntityType> getSubtypes();

        void setSupertype(EntityType superEntityType);
    }
}
