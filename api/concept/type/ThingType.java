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

import com.vaticle.typedb.client.api.concept.thing.Thing;
import com.vaticle.typedb.client.api.concept.type.AttributeType.ValueType;
import com.vaticle.typedb.client.api.connection.TypeDBTransaction;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

public interface ThingType extends Type {

    @Override
    @CheckReturnValue
    default boolean isThingType() {
        return true;
    }

    @Override
    ThingType.Remote asRemote(TypeDBTransaction transaction);

    interface Remote extends Type.Remote, ThingType {

        @Override
        @CheckReturnValue
        ThingType getSupertype();

        @Override
        @CheckReturnValue
        Stream<? extends ThingType> getSupertypes();

        @Override
        @CheckReturnValue
        Stream<? extends ThingType> getSubtypes();

        @CheckReturnValue
        Stream<? extends Thing> getInstances();

        void setAbstract();

        void unsetAbstract();

        void setPlays(RoleType roleType);

        void setPlays(RoleType roleType, RoleType overriddenType);

        void setOwns(AttributeType attributeType, AttributeType overriddenType, boolean isKey);

        void setOwns(AttributeType attributeType, AttributeType overriddenType);

        void setOwns(AttributeType attributeType, boolean isKey);

        void setOwns(AttributeType attributeType);

        @CheckReturnValue
        Stream<? extends RoleType> getPlays();

        @CheckReturnValue
        Stream<? extends AttributeType> getOwns();

        @CheckReturnValue
        Stream<? extends AttributeType> getOwns(ValueType valueType);

        @CheckReturnValue
        Stream<? extends AttributeType> getOwns(boolean keysOnly);

        @CheckReturnValue
        Stream<? extends AttributeType> getOwns(ValueType valueType, boolean keysOnly);

        void unsetPlays(RoleType roleType);

        void unsetOwns(AttributeType attributeType);
    }
}
