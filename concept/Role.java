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

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * An SchemaConcept which defines a role which can be played in a RelationType
 * This ontological element defines the Role which make up a RelationType.
 * It behaves similarly to SchemaConcept when relating to other types.
 */
public interface Role extends SchemaConcept {
    //------------------------------------- Modifiers ----------------------------------

    /**
     * Changes the Label of this Concept to a new one.
     *
     * @param label The new Label.
     * @return The Concept itself
     */
    Role label(Label label);

    /**
     * Sets the super of this Role.
     *
     * @param type The super of this Role
     * @return The Role itself
     */
    Role sup(Role type);

    //------------------------------------- Accessors ----------------------------------

    /**
     * @return All the super-types of this this Role
     */
    @Override
    Stream<Role> sups();

    /**
     * Returns the sub of this Role.
     *
     * @return The sub of this Role
     */
    @Override
    Stream<Role> subs();

    /**
     * Returns the RelationTypes that this Role takes part in.
     *
     * @return The RelationType which this Role takes part in.
     * @see RelationType
     */
    @CheckReturnValue
    Stream<RelationType> relations();

    /**
     * Returns a collection of the Types that can play this Role.
     *
     * @return A list of all the Types which can play this Role.
     * @see Type
     */
    @CheckReturnValue
    Stream<Type> players();

    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default Role asRole() {
        return this;
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isRole() {
        return true;
    }
}

