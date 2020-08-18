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

package grakn.client.concept.type;

import grakn.client.Grakn.Transaction;
import grakn.client.concept.Concept;
import grakn.client.concept.GraknConceptException;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * Facilitates construction of ontological elements.
 * Allows you to create schema or ontological elements.
 * These differ from normal graph constructs in two ways:
 * 1. They have a unique Label which identifies them
 * 2. You can link them together into a hierarchical structure
 */
public interface Type extends Concept {

    /**
     * Returns the unique label of this Type.
     *
     * @return The unique label of this type
     */
    @CheckReturnValue
    String getLabel();

    /**
     * Return as a ThingType if the Concept is a ThingType.
     *
     * @return A ThingType if the Concept is a ThingType
     */
    @CheckReturnValue
    ThingType asThingType();

    /**
     * Return as an EntityType if the Concept is an EntityType.
     *
     * @return A EntityType if the Concept is an EntityType
     */
    @CheckReturnValue
    EntityType asEntityType();

    /**
     * Return as an AttributeType if the Concept is an AttributeType
     *
     * @return An AttributeType if the Concept is an AttributeType
     */
    @CheckReturnValue
    AttributeType asAttributeType();

    /**
     * Return as a RelationType if the Concept is a RelationType.
     *
     * @return A RelationType if the Concept is a RelationType
     */
    @CheckReturnValue
    RelationType asRelationType();

    /**
     * Return as a RoleType if the Concept is a RoleType.
     *
     * @return A RoleType if the Concept is a RoleType
     */
    @CheckReturnValue
    RoleType asRoleType();

    @Override
    Remote asRemote(Transaction tx);

    interface Local extends Type, Concept.Local {

        @CheckReturnValue
        @Override
        default Type.Local asType() {
            return this;
        }

        /**
         * Return as a ThingType if the Type is a ThingType.
         *
         * @return A ThingType if the Type is a ThingType
         */
        @CheckReturnValue
        @Override
        default ThingType.Local asThingType() {
            throw GraknConceptException.invalidCasting(this, ThingType.class);
        }

        /**
         * Return as an EntityType if the Type is an EntityType.
         *
         * @return A EntityType if the Type is an EntityType
         */
        @CheckReturnValue
        @Override
        default EntityType.Local asEntityType() {
            throw GraknConceptException.invalidCasting(this, EntityType.class);
        }

        /**
         * Return as an AttributeType if the Type is an AttributeType
         *
         * @return An AttributeType if the Type is an AttributeType
         */
        @CheckReturnValue
        @Override
        default AttributeType.Local asAttributeType() {
            throw GraknConceptException.invalidCasting(this, AttributeType.class);
        }

        /**
         * Return as a RelationType if the Type is a RelationType.
         *
         * @return A RelationType if the Type is a RelationType
         */
        @CheckReturnValue
        @Override
        default RelationType.Local asRelationType() {
            throw GraknConceptException.invalidCasting(this, RelationType.class);
        }

        /**
         * Return as a RoleType if the Type is a RoleType.
         *
         * @return A RoleType if the Type is a RoleType
         */
        @CheckReturnValue
        @Override
        default RoleType.Local asRoleType() {
            throw GraknConceptException.invalidCasting(this, RoleType.class);
        }
    }

    /**
     * Facilitates construction of ontological elements.
     * Allows you to create schema or ontological elements.
     * These differ from normal graph constructs in two ways:
     * 1. They have a unique Label which identifies them
     * 2. You can link them together into a hierarchical structure
     */
    interface Remote extends Type, Concept.Remote {

        /**
         * Changes the Label of this Concept to a new one.
         *
         * @param label The new Label.
         */
        void setLabel(String label);

        /**
         * Return if the type is set to abstract.
         * By default, types are not abstract.
         *
         * @return returns true if the type is set to be abstract.
         */
        @CheckReturnValue
        boolean isAbstract();

        /**
         * @return The direct supertype of this concept
         */
        @CheckReturnValue
        @Nullable
        Type.Remote getSupertype();

        /**
         * @return All super-concepts of this Type, including itself and excluding the meta type THING.
         * If you want to include THING, use Transaction.sups().
         */
        Stream<? extends Type.Remote> getSupertypes();

        /**
         * Get all indirect subs of this concept.
         * The indirect subs are the concept itself and all indirect subs of direct subs.
         *
         * @return All the indirect sub-types of this Type
         */
        @CheckReturnValue
        Stream<? extends Type.Remote> getSubtypes();

        @CheckReturnValue
        @Override
        default Type.Remote asType() {
            return this;
        }

        /**
         * Return as a ThingType if the Type is a ThingType.
         *
         * @return A ThingType if the Type is a ThingType
         */
        @Override
        @CheckReturnValue
        default ThingType.Remote asThingType() {
            throw GraknConceptException.invalidCasting(this, ThingType.Remote.class);
        }

        /**
         * Return as an EntityType if the Type is an EntityType.
         *
         * @return A EntityType if the Type is an EntityType
         */
        @Override
        @CheckReturnValue
        default EntityType.Remote asEntityType() {
            throw GraknConceptException.invalidCasting(this, EntityType.Remote.class);
        }

        /**
         * Return as a RelationType if the Type is a RelationType.
         *
         * @return A RelationType if the Type is a RelationType
         */
        @Override
        @CheckReturnValue
        default RelationType.Remote asRelationType() {
            throw GraknConceptException.invalidCasting(this, RelationType.Remote.class);
        }

        /**
         * Return as a AttributeType if the Type is a AttributeType
         *
         * @return A AttributeType if the Type is a AttributeType
         */
        @Override
        @CheckReturnValue
        default AttributeType.Remote asAttributeType() {
            throw GraknConceptException.invalidCasting(this, AttributeType.class);
        }

        /**
         * Return as a RoleType if the Type is a RoleType.
         *
         * @return A RoleType if the Type is a RoleType
         */
        @Override
        @CheckReturnValue
        default RoleType.Remote asRoleType() {
            throw GraknConceptException.invalidCasting(this, RoleType.Remote.class);
        }
    }
}
