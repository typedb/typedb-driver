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

package com.vaticle.typedb.client.api.concept;

import com.eclipsesource.json.JsonObject;
import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.value.Value;
import com.vaticle.typedb.client.api.concept.thing.Attribute;
import com.vaticle.typedb.client.api.concept.thing.Entity;
import com.vaticle.typedb.client.api.concept.thing.Relation;
import com.vaticle.typedb.client.api.concept.thing.Thing;
import com.vaticle.typedb.client.api.concept.type.AttributeType;
import com.vaticle.typedb.client.api.concept.type.EntityType;
import com.vaticle.typedb.client.api.concept.type.RelationType;
import com.vaticle.typedb.client.api.concept.type.RoleType;
import com.vaticle.typedb.client.api.concept.type.ThingType;
import com.vaticle.typedb.client.api.concept.type.Type;

import javax.annotation.CheckReturnValue;

public interface Concept {

    @CheckReturnValue
    default boolean isType() {
        return false;
    }

    @CheckReturnValue
    default boolean isThingType() {
        return false;
    }

    @CheckReturnValue
    default boolean isEntityType() {
        return false;
    }

    @CheckReturnValue
    default boolean isAttributeType() {
        return false;
    }

    @CheckReturnValue
    default boolean isRelationType() {
        return false;
    }

    @CheckReturnValue
    default boolean isRoleType() {
        return false;
    }

    @CheckReturnValue
    default boolean isThing() {
        return false;
    }

    @CheckReturnValue
    default boolean isEntity() {
        return false;
    }

    @CheckReturnValue
    default boolean isAttribute() {
        return false;
    }

    @CheckReturnValue
    default boolean isRelation() {
        return false;
    }

    @CheckReturnValue
    default boolean isValue() {
        return false;
    }

    @CheckReturnValue
    Type asType();

    @CheckReturnValue
    ThingType asThingType();

    @CheckReturnValue
    EntityType asEntityType();

    @CheckReturnValue
    AttributeType asAttributeType();

    @CheckReturnValue
    RelationType asRelationType();

    @CheckReturnValue
    RoleType asRoleType();

    @CheckReturnValue
    Thing asThing();

    @CheckReturnValue
    Entity asEntity();

    @CheckReturnValue
    Attribute<?> asAttribute();

    @CheckReturnValue
    Relation asRelation();

    @CheckReturnValue
    Value<?> asValue();

    @CheckReturnValue
    Remote asRemote(TypeDBTransaction transaction);

    @CheckReturnValue
    boolean isRemote();

    @CheckReturnValue
    JsonObject toJSON();

    interface Remote extends Concept {

        void delete();

        @CheckReturnValue
        boolean isDeleted();

        @Override
        @CheckReturnValue
        Type.Remote asType();

        @Override
        @CheckReturnValue
        ThingType.Remote asThingType();

        @Override
        @CheckReturnValue
        EntityType.Remote asEntityType();

        @Override
        @CheckReturnValue
        RelationType.Remote asRelationType();

        @Override
        @CheckReturnValue
        AttributeType.Remote asAttributeType();

        @Override
        @CheckReturnValue
        RoleType.Remote asRoleType();

        @Override
        @CheckReturnValue
        Thing.Remote asThing();

        @Override
        @CheckReturnValue
        Entity.Remote asEntity();

        @Override
        @CheckReturnValue
        Relation.Remote asRelation();

        @Override
        @CheckReturnValue
        Attribute.Remote<?> asAttribute();
    }
}
