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
import com.vaticle.typedb.client.api.concept.thing.Thing;
import com.vaticle.typedb.client.jni.ValueType;
import com.vaticle.typeql.lang.common.TypeQLToken;

import javax.annotation.CheckReturnValue;
import java.util.Set;
import java.util.stream.Stream;

public interface ThingType extends Type {

    @Override
    @CheckReturnValue
    default boolean isThingType() {
        return true;
    }

    @Override
    @CheckReturnValue
    ThingType getSupertype(TypeDBTransaction transaction);

    @Override
    @CheckReturnValue
    Stream<? extends ThingType> getSupertypes(TypeDBTransaction transaction);

    @Override
    @CheckReturnValue
    Stream<? extends ThingType> getSubtypes(TypeDBTransaction transaction);

    @Override
    @CheckReturnValue
    Stream<? extends ThingType> getSubtypesExplicit(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends Thing> getInstances(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends Thing> getInstancesExplicit(TypeDBTransaction transaction);

    void setAbstract(TypeDBTransaction transaction);

    void unsetAbstract(TypeDBTransaction transaction);

    void setPlays(TypeDBTransaction transaction, RoleType roleType);

    void setPlays(TypeDBTransaction transaction, RoleType roleType, RoleType overriddenType);

    void setOwns(TypeDBTransaction transaction, AttributeType attributeType, AttributeType overriddenType, Set<TypeQLToken.Annotation> annotations);

    void setOwns(TypeDBTransaction transaction, AttributeType attributeType, AttributeType overriddenType);

    void setOwns(TypeDBTransaction transaction, AttributeType attributeType, Set<TypeQLToken.Annotation> annotations);

    void setOwns(TypeDBTransaction transaction, AttributeType attributeType);

    @CheckReturnValue
    Stream<? extends RoleType> getPlays(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends RoleType> getPlaysExplicit(TypeDBTransaction transaction);

    @CheckReturnValue
    RoleType getPlaysOverridden(TypeDBTransaction transaction, RoleType roleType);

    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, ValueType valueType);

    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Set<TypeQLToken.Annotation> annotations);

    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, ValueType valueType, Set<TypeQLToken.Annotation> annotations);

    @CheckReturnValue
    Stream<? extends AttributeType> getOwnsExplicit(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends AttributeType> getOwnsExplicit(TypeDBTransaction transaction, ValueType valueType);

    @CheckReturnValue
    Stream<? extends AttributeType> getOwnsExplicit(TypeDBTransaction transaction, Set<TypeQLToken.Annotation> annotations);

    @CheckReturnValue
    Stream<? extends AttributeType> getOwnsExplicit(TypeDBTransaction transaction, ValueType valueType, Set<TypeQLToken.Annotation> annotations);

    @CheckReturnValue
    AttributeType getOwnsOverridden(TypeDBTransaction transaction, AttributeType attributeType);

    void unsetPlays(TypeDBTransaction transaction, RoleType roleType);

    void unsetOwns(TypeDBTransaction transaction, AttributeType attributeType);

    @CheckReturnValue
    String getSyntax(TypeDBTransaction transaction);
}
