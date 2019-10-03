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

package grakn.client.concept.api;

import grakn.core.concept.Label;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * An ontological element which categorises how Things may relate to each other.
 * A RelationType defines how Type may relate to one another.
 * They are used to model and categorise n-ary Relations.
 */
public interface RelationType extends Type {
    //------------------------------------- Modifiers ----------------------------------

    /**
     * Changes the Label of this Concept to a new one.
     *
     * @param label The new Label.
     * @return The Concept itself
     */
    RelationType label(Label label);

    /**
     * Creates and returns a new Relation instance, whose direct type will be this type.
     *
     * @return a new empty relation.
     * @see Relation
     */
    Relation create();

    /**
     * Sets the supertype of the RelationType to be the RelationType specified.
     *
     * @param type The supertype of this RelationType
     * @return The RelationType itself.
     */
    RelationType sup(RelationType type);

    /**
     * Creates a RelationType which allows this type and a resource type to be linked in a strictly one-to-one mapping.
     *
     * @param attributeType The resource type which instances of this type should be allowed to play.
     * @return The Type itself.
     */
    @Override
    RelationType key(AttributeType attributeType);

    /**
     * Creates a RelationType which allows this type and a resource type to be linked.
     *
     * @param attributeType The resource type which instances of this type should be allowed to play.
     * @return The Type itself.
     */
    @Override
    RelationType has(AttributeType attributeType);

    //------------------------------------- Accessors ----------------------------------

    /**
     * Retrieves a list of the RoleTypes that make up this RelationType.
     *
     * @return A list of the RoleTypes which make up this RelationType.
     * @see Role
     */
    @CheckReturnValue
    Stream<Role> roles();

    //------------------------------------- Edge Handling ----------------------------------

    /**
     * Sets a new Role for this RelationType.
     *
     * @param role A new role which is part of this relation.
     * @return The RelationType itself.
     * @see Role
     */
    RelationType relates(Role role);

    //------------------------------------- Other ----------------------------------

    /**
     * Unrelates a Role from this RelationType
     *
     * @param role The Role to unrelate from the RelationType.
     * @return The RelationType itself.
     * @see Role
     */
    RelationType unrelate(Role role);

    //---- Inherited Methods

    /**
     * Sets the RelationType to be abstract - which prevents it from having any instances.
     *
     * @param isAbstract Specifies if the concept is to be abstract (true) or not (false).
     * @return The RelationType itself.
     */
    @Override
    RelationType isAbstract(Boolean isAbstract);

    /**
     * Returns the direct supertype of this RelationType.
     *
     * @return The direct supertype of this RelationType
     */
    @Override
    @Nullable
    RelationType sup();

    /**
     * Returns a collection of supertypes of this RelationType.
     *
     * @return All the supertypes of this RelationType
     */
    @Override
    Stream<RelationType> sups();

    /**
     * Returns a collection of subtypes of this RelationType.
     *
     * @return All the sub types of this RelationType
     */
    @Override
    Stream<RelationType> subs();

    /**
     * Sets the Role which instances of this RelationType may play.
     *
     * @param role The Role which the instances of this Type are allowed to play.
     * @return The RelationType itself.
     */
    @Override
    RelationType plays(Role role);

    /**
     * Removes the ability of this RelationType to play a specific Role
     *
     * @param role The Role which the Things of this Rule should no longer be allowed to play.
     * @return The Rule itself.
     */
    @Override
    RelationType unplay(Role role);

    /**
     * Removes the ability for Things of this RelationType to have Attributes of type AttributeType
     *
     * @param attributeType the AttributeType which this RelationType can no longer have
     * @return The RelationType itself.
     */
    @Override
    RelationType unhas(AttributeType attributeType);

    /**
     * Removes AttributeType as a key to this RelationType
     *
     * @param attributeType the AttributeType which this RelationType can no longer have as a key
     * @return The RelationType itself.
     */
    @Override
    RelationType unkey(AttributeType attributeType);

    /**
     * Retrieve all the Relation instances of this RelationType
     *
     * @return All the Relation instances of this RelationType
     * @see Relation
     */
    @Override
    Stream<Relation> instances();

    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default RelationType asRelationType() {
        return this;
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isRelationType() {
        return true;
    }
}
