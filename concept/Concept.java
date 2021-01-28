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

package grakn.client.concept;

import grakn.client.GraknClient;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Entity;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;

import javax.annotation.CheckReturnValue;

public interface Concept {

    default boolean isType() {
        return false;
    }

    default boolean isThingType() {
        return false;
    }

    default boolean isEntityType() {
        return false;
    }

    default boolean isAttributeType() {
        return false;
    }

    default boolean isRelationType() {
        return false;
    }

    default boolean isRoleType() {
        return false;
    }

    default boolean isThing() {
        return false;
    }

    default boolean isEntity() {
        return false;
    }

    default boolean isAttribute() {
        return false;
    }

    default boolean isRelation() {
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
    Remote asRemote(GraknClient.Transaction transaction);

    @CheckReturnValue
    boolean isRemote();

    interface Remote extends Concept {

        void delete();

        @CheckReturnValue
        boolean isDeleted();

        @Override
        Type.Remote asType();

        @Override
        ThingType.Remote asThingType();

        @Override
        EntityType.Remote asEntityType();

        @Override
        RelationType.Remote asRelationType();

        @Override
        AttributeType.Remote asAttributeType();

        @Override
        RoleType.Remote asRoleType();

        @Override
        Thing.Remote asThing();

        @Override
        Entity.Remote asEntity();

        @Override
        Relation.Remote asRelation();

        @Override
        Attribute.Remote<?> asAttribute();
    }
}
