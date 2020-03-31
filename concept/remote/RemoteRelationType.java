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

package grakn.client.concept.remote;

import grakn.client.GraknClient;
import grakn.client.concept.AttributeType;
import grakn.client.concept.ConceptId;
import grakn.client.concept.Label;
import grakn.client.concept.Relation;
import grakn.client.concept.RelationType;
import grakn.client.concept.Role;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * An ontological element which categorises how Things may relate to each other.
 * A RelationType defines how Type may relate to one another.
 * They are used to model and categorise n-ary Relations.
 */
public interface RemoteRelationType extends RelationType<RemoteRelationType, RemoteRelation>,
        RemoteType<RemoteRelationType, RemoteRelation> {

    static RemoteRelationType of(GraknClient.Transaction tx, ConceptId id) {
        return new RemoteRelationTypeImpl(tx, id);
    }

    //------------------------------------- Modifiers ----------------------------------

    /**
     * Create a relation of this relation type.
     *
     * @return The newly created relation.
     */
    RemoteRelation create();

    /**
     * Set the super type of this relation type.
     *
     * @param superRelationType The super type to set.
     * @return This concept itself.
     */
    RemoteRelationType sup(RelationType<?, ?> superRelationType);

    /**
     * Changes the Label of this Concept to a new one.
     *
     * @param label The new Label.
     * @return The Concept itself
     */
    @Override
    RemoteRelationType label(Label label);

    /**
     * Creates a RelationType which allows this type and a resource type to be linked in a strictly one-to-one mapping.
     *
     * @param attributeType The resource type which instances of this type should be allowed to play.
     * @return The Type itself.
     */
    @Override
    RemoteRelationType key(AttributeType<?, ?, ?> attributeType);

    /**
     * Creates a RelationType which allows this type and a resource type to be linked.
     *
     * @param attributeType The resource type which instances of this type should be allowed to play.
     * @return The Type itself.
     */
    @Override
    RemoteRelationType has(AttributeType<?, ?, ?> attributeType);

    //------------------------------------- Accessors ----------------------------------

    /**
     * Retrieves a list of the RoleTypes that make up this RelationType.
     *
     * @return A list of the RoleTypes which make up this RelationType.
     * @see RemoteRole
     */
    @CheckReturnValue
    Stream<RemoteRole> roles();

    //------------------------------------- Edge Handling ----------------------------------

    /**
     * Sets a new Role for this RelationType.
     *
     * @param role A new role which is part of this relation.
     * @return The RelationType itself.
     * @see RemoteRole
     */
    RemoteRelationType relates(Role<?> role);

    //------------------------------------- Other ----------------------------------

    /**
     * Unrelates a Role from this RelationType
     *
     * @param role The Role to unrelate from the RelationType.
     * @return The RelationType itself.
     * @see RemoteRole
     */
    RemoteRelationType unrelate(Role<?> role);

    //---- Inherited Methods

    /**
     * Sets the RelationType to be abstract - which prevents it from having any instances.
     *
     * @param isAbstract Specifies if the concept is to be abstract (true) or not (false).
     * @return The RelationType itself.
     */
    @Override
    RemoteRelationType isAbstract(Boolean isAbstract);

    /**
     * Returns a collection of supertypes of this RelationType.
     *
     * @return All the supertypes of this RelationType
     */
    @Override
    Stream<RemoteRelationType> sups();

    /**
     * Returns a collection of subtypes of this RelationType.
     *
     * @return All the sub types of this RelationType
     */
    @Override
    Stream<RemoteRelationType> subs();

    /**
     * Sets the Role which instances of this RelationType may play.
     *
     * @param role The Role which the instances of this Type are allowed to play.
     * @return The RelationType itself.
     */
    @Override
    RemoteRelationType plays(Role<?> role);

    /**
     * Removes the ability of this RelationType to play a specific Role
     *
     * @param role The Role which the Things of this Rule should no longer be allowed to play.
     * @return The Rule itself.
     */
    @Override
    RemoteRelationType unplay(Role<?> role);

    /**
     * Removes the ability for Things of this RelationType to have Attributes of type AttributeType
     *
     * @param attributeType the AttributeType which this RelationType can no longer have
     * @return The RelationType itself.
     */
    @Override
    RemoteRelationType unhas(AttributeType<?, ?, ?> attributeType);

    /**
     * Removes AttributeType as a key to this RelationType
     *
     * @param attributeType the AttributeType which this RelationType can no longer have as a key
     * @return The RelationType itself.
     */
    @Override
    RemoteRelationType unkey(AttributeType<?, ?, ?> attributeType);

    /**
     * Retrieve all the Relation instances of this RelationType
     *
     * @return All the Relation instances of this RelationType
     * @see RemoteRelation
     */
    @Override
    Stream<RemoteRelation> instances();

    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default RemoteRelationType asRelationType() {
        return this;
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isRelationType() {
        return true;
    }
}
