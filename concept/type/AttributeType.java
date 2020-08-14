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
import grakn.client.concept.ConceptIID;
import grakn.client.concept.GraknConceptException;
import grakn.client.concept.Label;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.type.impl.AttributeTypeImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.stream.Stream;

public interface AttributeType extends ThingType {

    @CheckReturnValue
    @Override
    default AttributeType asAttributeType() {
        return this;
    }

    boolean isKeyable();

    /**
     * Get the data type to which instances of the AttributeType must conform.
     *
     * @return The data type to which instances of this Attribute  must conform.
     */
    @Nullable
    @CheckReturnValue
    ValueType getValueType();

    default AttributeType asObject() {
        throw GraknConceptException.invalidCasting(this, java.lang.Object.class);
    }

    AttributeType.Boolean asBoolean();

    AttributeType.Long asLong();

    AttributeType.Double asDouble();

    AttributeType.String asString();

    AttributeType.DateTime asDateTime();

    @Override
    default AttributeType.Remote asRemote(Transaction tx) {
        return AttributeType.Remote.of(tx, getIID());
    }

    /**
     * A class used to hold the supported data types of attributes.
     * This is used tp constrain value data types to only those we explicitly support.
     */
    enum ValueType {
        /**
         * A special value type used only as the return type when retrieving instances of meta 'attribute'.
         */
        OBJECT(Object.class, false, false),
        BOOLEAN(Boolean.class, true, false),
        LONG(Long.class, true, true),
        DOUBLE(Double.class, true, false),
        STRING(String.class, true, true),
        DATETIME(LocalDateTime.class, true, true);

        private final Class<?> valueClass;
        private final boolean isWritable;
        private final boolean isKeyable;

        ValueType(Class<?> valueClass, boolean isWritable, boolean isKeyable) {
            this.valueClass = valueClass;
            this.isWritable = isWritable;
            this.isKeyable = isKeyable;
        }

        public static ValueType of(Class<?> valueClass) {
            for (final ValueType t : ValueType.values()) {
                if (t.valueClass == valueClass) {
                    return t;
                }
            }
            // TODO: extract hardcoded string to ErrorMessage.Internal.UNRECOGNISED_VALUE
            throw GraknConceptException.create("Unrecognised schema value!");
        }

        @CheckReturnValue
        public Class<?> valueClass() {
            return valueClass;
        }

        @CheckReturnValue
        public boolean isWritable() {
            return isWritable;
        }

        @CheckReturnValue
        public boolean isKeyable() {
            return isKeyable;
        }

        @Override
        public java.lang.String toString() {
            return valueClass.getName();
        }
    }

    /**
     * An ontological element which models and categorises the various Attribute in the graph.
     * This ontological element behaves similarly to Type when defining how it relates to other
     * types. It has two additional functions to be aware of:
     * 1. It has a ValueType constraining the data types of the values it's instances may take.
     * 2. Any of it's instances are unique to the type.
     * For example if you have an AttributeType modelling month throughout the year there can only be one January.
     */
    interface Local extends ThingType.Local, AttributeType {

        @Override
        default AttributeType.Boolean.Local asBoolean() {
            throw GraknConceptException.invalidCasting(this, java.lang.Boolean.class);
        }

        @Override
        default AttributeType.Long.Local asLong() {
            throw GraknConceptException.invalidCasting(this, java.lang.Long.class);
        }

        @Override
        default AttributeType.Double.Local asDouble() {
            throw GraknConceptException.invalidCasting(this, java.lang.Double.class);
        }

        @Override
        default AttributeType.String.Local asString() {
            throw GraknConceptException.invalidCasting(this, java.lang.String.class);
        }

        @Override
        default AttributeType.DateTime.Local asDateTime() {
            throw GraknConceptException.invalidCasting(this, LocalDateTime.class);
        }
    }

    /**
     * An ontological element which models and categorises the various Attribute in the graph.
     * This ontological element behaves similarly to Type when defining how it relates to other
     * types. It has two additional functions to be aware of:
     * 1. It has a ValueType constraining the data types of the values it's instances may take.
     * 2. Any of it's instances are unique to the type.
     * For example if you have an AttributeType modelling month throughout the year there can only be one January.
     */
    interface Remote extends ThingType.Remote, AttributeType {

        static AttributeType.Remote of(Transaction tx, ConceptIID iid) {
            return new AttributeTypeImpl.Remote(tx, iid);
        }

        @CheckReturnValue
        @Override
        default AttributeType.Remote asAttributeType() {
            return this;
        }

        @Override
        default AttributeType.Boolean.Remote asBoolean() {
            throw GraknConceptException.invalidCasting(this, java.lang.Boolean.class);
        }

        @Override
        default AttributeType.Long.Remote asLong() {
            throw GraknConceptException.invalidCasting(this, java.lang.Long.class);
        }

        @Override
        default AttributeType.Double.Remote asDouble() {
            throw GraknConceptException.invalidCasting(this, java.lang.Double.class);
        }

        @Override
        default AttributeType.String.Remote asString() {
            throw GraknConceptException.invalidCasting(this, java.lang.String.class);
        }

        @Override
        default AttributeType.DateTime.Remote asDateTime() {
            throw GraknConceptException.invalidCasting(this, LocalDateTime.class);
        }

        /**
         * @return The direct supertype of this concept
         */
        @Override
        AttributeType.Remote getSupertype();

        /**
         * Returns a collection of super-types of this AttributeType.
         *
         * @return The super-types of this AttributeType
         */
        @Override
        Stream<? extends AttributeType.Remote> getSupertypes();

        /**
         * Returns a collection of subtypes of this AttributeType.
         *
         * @return The subtypes of this AttributeType
         */
        @Override
        Stream<? extends AttributeType.Remote> getSubtypes();

        /**
         * Returns a collection of all Attribute of this AttributeType.
         *
         * @return The resource instances of this AttributeType
         */
        @Override
        Stream<? extends Attribute.Remote> getInstances();

        /**
         * Sets the supertype of the AttributeType to be the AttributeType specified.
         *
         * @param type The super type of this AttributeType.
         * @return The AttributeType itself.
         */
        AttributeType.Remote setSupertype(AttributeType type);

        /**
         * Changes the Label of this Concept to a new one.
         *
         * @param label The new Label.
         * @return The Concept itself
         */
        AttributeType.Remote setLabel(Label label);

        /**
         * Sets the AttributeType to be abstract - which prevents it from having any instances.
         *
         * @param isAbstract Specifies if the AttributeType is to be abstract (true) or not (false).
         * @return The AttributeType itself.
         */
        @Override
        AttributeType.Remote setAbstract(boolean isAbstract);

        /**
         * Sets the RoleType which instances of this AttributeType may play.
         *
         * @param role The RoleType which the instances of this AttributeType are allowed to play.
         * @return The AttributeType itself.
         */
        @Override
        AttributeType.Remote setPlays(RoleType role);

        /**
         * Set the regular expression that instances of the AttributeType must conform to.
         *
         * @param regex The regular expression that instances of this AttributeType must conform to.
         * @return The AttributeType itself.
         */
        AttributeType.Remote setRegex(String regex);

        /**
         * Creates a RelationType which allows this type and a resource type to be linked.
         *
         * @param attributeType The resource type which instances of this type should be allowed to play.
         * @return The Type itself.
         */
        @Override
        AttributeType.Remote setOwns(AttributeType attributeType);
        @Override
        AttributeType.Remote setOwns(AttributeType attributeType, boolean isKey);
        @Override
        AttributeType.Remote setOwns(AttributeType attributeType, AttributeType overriddenType);
        @Override
        AttributeType.Remote setOwns(AttributeType attributeType, AttributeType overriddenType, boolean isKey);

        /**
         * Retrieve the regular expression to which instances of this AttributeType must conform, or {@code null} if no
         * regular expression is set.
         * By default, an AttributeType does not have a regular expression set.
         *
         * @return The regular expression to which instances of this AttributeType must conform.
         */
        @CheckReturnValue
        @Nullable
        String getRegex();
    }

    interface Boolean extends AttributeType {

        interface Local extends AttributeType.Boolean, AttributeType.Local {
        }

        interface Remote extends AttributeType.Boolean, AttributeType.Remote {

            @Override
            AttributeType.Boolean.Remote getSupertype();

            @Override
            Stream<? extends AttributeType.Boolean.Remote> getSupertypes();

            @Override
            Stream<? extends AttributeType.Boolean.Remote> getSubtypes();

            @Override
            Stream<? extends Attribute.Boolean.Remote> getInstances();

            /**
             * Set the value for the Attribute, unique to its type.
             *
             * @param value A value for the Attribute which is unique to its type
             * @return new or existing Attribute of this type with the provided value.
             */
            Attribute.Boolean.Remote put(boolean value);

            /**
             * Get the Attribute with the value provided, and its type, or return NULL
             *
             * @param value A value which an Attribute in the graph may be holding
             * @return The Attribute with the provided value and type or null if no such Attribute exists.
             * @see Attribute.Boolean.Remote
             */
            @CheckReturnValue
            @Nullable
            Attribute.Boolean.Remote get(boolean value);
        }
    }

    interface Long extends AttributeType {

        interface Local extends AttributeType.Long, AttributeType.Local {
        }

        interface Remote extends AttributeType.Long, AttributeType.Remote {

            @Override
            AttributeType.Long.Remote getSupertype();

            @Override
            Stream<? extends AttributeType.Long.Remote> getSupertypes();

            @Override
            Stream<? extends AttributeType.Long.Remote> getSubtypes();

            @Override
            Stream<? extends Attribute.Long.Remote> getInstances();

            /**
             * Set the value for the Attribute, unique to its type.
             *
             * @param value A value for the Attribute which is unique to its type
             * @return new or existing Attribute of this type with the provided value.
             */
            Attribute.Long.Remote put(long value);

            /**
             * Get the Attribute with the value provided, and its type, or return NULL
             *
             * @param value A value which an Attribute in the graph may be holding
             * @return The Attribute with the provided value and type or null if no such Attribute exists.
             * @see Attribute.Long.Remote
             */
            @CheckReturnValue
            @Nullable
            Attribute.Long.Remote get(long value);
        }
    }

    interface Double extends AttributeType {

        interface Local extends AttributeType.Double, AttributeType.Local {
        }

        interface Remote extends AttributeType.Double, AttributeType.Remote {

            @Override
            AttributeType.Double.Remote getSupertype();

            @Override
            Stream<? extends AttributeType.Double.Remote> getSupertypes();

            @Override
            Stream<? extends AttributeType.Double.Remote> getSubtypes();

            @Override
            Stream<? extends Attribute.Double.Remote> getInstances();

            /**
             * Set the value for the Attribute, unique to its type.
             *
             * @param value A value for the Attribute which is unique to its type
             * @return new or existing Attribute of this type with the provided value.
             */
            Attribute.Double.Remote put(double value);

            /**
             * Get the Attribute with the value provided, and its type, or return NULL
             *
             * @param value A value which an Attribute in the graph may be holding
             * @return The Attribute with the provided value and type or null if no such Attribute exists.
             * @see Attribute.Double.Remote
             */
            @CheckReturnValue
            @Nullable
            Attribute.Double.Remote get(double value);
        }
    }

    interface String extends AttributeType {

        interface Local extends AttributeType.String, AttributeType.Local {
        }

        interface Remote extends AttributeType.String, AttributeType.Remote {

            @Override
            AttributeType.String.Remote getSupertype();

            @Override
            Stream<? extends AttributeType.String.Remote> getSupertypes();

            @Override
            Stream<? extends AttributeType.String.Remote> getSubtypes();

            @Override
            Stream<? extends Attribute.String.Remote> getInstances();

            /**
             * Set the value for the Attribute, unique to its type.
             *
             * @param value A value for the Attribute which is unique to its type
             * @return new or existing Attribute of this type with the provided value.
             */
            Attribute.String.Remote put(java.lang.String value);

            /**
             * Get the Attribute with the value provided, and its type, or return NULL
             *
             * @param value A value which an Attribute in the graph may be holding
             * @return The Attribute with the provided value and type or null if no such Attribute exists.
             * @see Attribute.String.Remote
             */
            @CheckReturnValue
            @Nullable
            Attribute.String.Remote get(java.lang.String value);
        }
    }

    interface DateTime extends AttributeType {

        interface Local extends AttributeType.DateTime, AttributeType.Local {
        }

        interface Remote extends AttributeType.DateTime, AttributeType.Remote {

            @Override
            AttributeType.DateTime.Remote getSupertype();

            @Override
            Stream<? extends AttributeType.DateTime.Remote> getSupertypes();

            @Override
            Stream<? extends AttributeType.DateTime.Remote> getSubtypes();

            @Override
            Stream<? extends Attribute.DateTime.Remote> getInstances();

            /**
             * Set the value for the Attribute, unique to its type.
             *
             * @param value A value for the Attribute which is unique to its type
             * @return new or existing Attribute of this type with the provided value.
             */
            Attribute.DateTime.Remote put(LocalDateTime value);

            /**
             * Get the Attribute with the value provided, and its type, or return NULL
             *
             * @param value A value which an Attribute in the graph may be holding
             * @return The Attribute with the provided value and type or null if no such Attribute exists.
             * @see Attribute.DateTime.Remote
             */
            @CheckReturnValue
            @Nullable
            Attribute.DateTime.Remote get(LocalDateTime value);
        }
    }
}
