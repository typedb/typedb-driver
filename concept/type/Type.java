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
import grakn.client.common.exception.GraknConceptException;
import grakn.client.concept.type.impl.AttributeTypeImpl;
import grakn.client.concept.type.impl.EntityTypeImpl;
import grakn.client.concept.type.impl.RelationTypeImpl;
import grakn.client.concept.type.impl.RoleTypeImpl;
import grakn.client.concept.type.impl.RuleImpl;
import grakn.client.concept.type.impl.ThingTypeImpl;
import grakn.protocol.ConceptProto;

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
     * Return if the type is a root type.
     * @return Returns true if the type is a root type.
     */
    @CheckReturnValue
    boolean isRoot();

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

        static Type.Local of(ConceptProto.Type type) {
            switch (type.getSchema()) {
                case ENTITY_TYPE:
                    return new EntityTypeImpl.Local(type);
                case RELATION_TYPE:
                    return new RelationTypeImpl.Local(type);
                case ATTRIBUTE_TYPE:
                    switch (type.getValueType()) {
                        case BOOLEAN:
                            return new AttributeTypeImpl.Boolean.Local(type);
                        case LONG:
                            return new AttributeTypeImpl.Long.Local(type);
                        case DOUBLE:
                            return new AttributeTypeImpl.Double.Local(type);
                        case STRING:
                            return new AttributeTypeImpl.String.Local(type);
                        case DATETIME:
                            return new AttributeTypeImpl.DateTime.Local(type);
                        default:
                        case UNRECOGNIZED:
                            throw new IllegalArgumentException("Unrecognised value type " + type.getValueType() + " for concept " + type);
                    }
                case ROLE_TYPE:
                    return new RoleTypeImpl.Local(type);
                case RULE:
                    return new RuleImpl.Local(type);
                case THING_TYPE:
                    return new ThingTypeImpl.Local(type);
                default:
                case UNRECOGNIZED:
                    throw new IllegalArgumentException("Unrecognised " + type);
            }
        }

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

        static Type.Remote of(Transaction tx, ConceptProto.Type type) {
            final String label = type.getLabel();
            switch (type.getSchema()) {
                case ENTITY_TYPE:
                    return new EntityTypeImpl.Remote(tx, label);
                case RELATION_TYPE:
                    return new RelationTypeImpl.Remote(tx, label);
                case ATTRIBUTE_TYPE:
                    switch (type.getValueType()) {
                        case BOOLEAN:
                            return new AttributeTypeImpl.Boolean.Remote(tx, label);
                        case LONG:
                            return new AttributeTypeImpl.Long.Remote(tx, label);
                        case DOUBLE:
                            return new AttributeTypeImpl.Double.Remote(tx, label);
                        case STRING:
                            return new AttributeTypeImpl.String.Remote(tx, label);
                        case DATETIME:
                            return new AttributeTypeImpl.DateTime.Remote(tx, label);
                        default:
                        case UNRECOGNIZED:
                            throw new IllegalArgumentException("Unrecognised value type " + type.getValueType() + " for concept " + type);
                    }
                case ROLE_TYPE:
                    final String scopedLabel = type.getScopedLabel();
                    return new RoleTypeImpl.Remote(tx, label, scopedLabel);
                case RULE:
                    return new RuleImpl.Remote(tx, label);
                case THING_TYPE:
                    return new ThingTypeImpl.Remote(tx, label);
                default:
                case UNRECOGNIZED:
                    throw new IllegalArgumentException("Unrecognised " + type);
            }
        }

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
         */
        @CheckReturnValue
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
        // TODO: remove @deprecated
        @Deprecated
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
