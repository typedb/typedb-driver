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

import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.Concepts;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.type.impl.AttributeTypeImpl;
import grakn.protocol.ConceptProto;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.ClientInternal.UNRECOGNISED_VALUE;
import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static grakn.client.common.exception.ErrorMessage.Protocol.UNRECOGNISED_FIELD;

public interface AttributeType extends ThingType {

    /**
     * Get the data type to which instances of the AttributeType must conform.
     *
     * @return The data type to which instances of this Attribute  must conform.
     */
    @CheckReturnValue
    default ValueType getValueType() {
        return ValueType.OBJECT;
    }

    @CheckReturnValue
    default boolean isKeyable() {
        return getValueType().isKeyable();
    }

    AttributeType.Boolean asBoolean();

    AttributeType.Long asLong();

    AttributeType.Double asDouble();

    AttributeType.String asString();

    AttributeType.DateTime asDateTime();

    @CheckReturnValue
    @Override
    Remote asRemote(Concepts concepts);

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
            throw new GraknClientException(UNRECOGNISED_VALUE);
        }

        public static ValueType of(ConceptProto.AttributeType.VALUE_TYPE valueType) {
            switch (valueType) {
                case STRING:
                    return AttributeType.ValueType.STRING;
                case BOOLEAN:
                    return AttributeType.ValueType.BOOLEAN;
                case LONG:
                    return AttributeType.ValueType.LONG;
                case DOUBLE:
                    return AttributeType.ValueType.DOUBLE;
                case DATETIME:
                    return AttributeType.ValueType.DATETIME;
                default:
                case UNRECOGNIZED:
                    throw new GraknClientException(UNRECOGNISED_FIELD.message(ConceptProto.AttributeType.VALUE_TYPE.class.getCanonicalName(), valueType));
            }
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

        @CheckReturnValue
        @Override
        default AttributeType.Local asAttributeType() {
            return this;
        }

        @Override
        default AttributeType.Boolean.Local asBoolean() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, AttributeType.Boolean.class.getCanonicalName()));
        }

        @Override
        default AttributeType.Long.Local asLong() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, AttributeType.Long.class.getCanonicalName()));
        }

        @Override
        default AttributeType.Double.Local asDouble() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, AttributeType.Double.class.getCanonicalName()));
        }

        @Override
        default AttributeType.String.Local asString() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, AttributeType.String.class.getCanonicalName()));
        }

        @Override
        default AttributeType.DateTime.Local asDateTime() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, AttributeType.DateTime.class.getCanonicalName()));
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

        /**
         * Sets the supertype of the AttributeType to be the AttributeType specified.
         *
         * @param type The super type of this AttributeType.
         */
        void setSupertype(AttributeType type);

        @CheckReturnValue
        @Override
        default AttributeType.Remote asAttributeType() {
            return this;
        }

        @Override
        AttributeType.Boolean.Remote asBoolean();

        @Override
        AttributeType.Long.Remote asLong();

        @Override
        AttributeType.Double.Remote asDouble();

        @Override
        AttributeType.String.Remote asString();

        @Override
        AttributeType.DateTime.Remote asDateTime();

        /**
         * Retrieve all the Attribute instances of this AttributeType
         *
         * @return All the Attribute instances of this AttributeType
         * @see Attribute.Remote
         */
        @Override
        Stream<? extends Attribute.Remote<?>> getInstances();

        Stream<? extends ThingType> getOwners();

        Stream<? extends ThingType> getOwners(boolean onlyKey);
    }

    interface Boolean extends AttributeType {

        @CheckReturnValue
        @Override
        default ValueType getValueType() {
            return ValueType.BOOLEAN;
        }

        @CheckReturnValue
        @Override
        default AttributeType.Boolean.Remote asRemote(final Concepts concepts) {
            return AttributeType.Boolean.Remote.of(concepts, getLabel());
        }

        interface Local extends AttributeType.Boolean, AttributeType.Local {

            @CheckReturnValue
            @Override
            default AttributeType.Boolean.Local asBoolean() {
                return this;
            }
        }

        interface Remote extends AttributeType.Boolean, AttributeType.Remote {

            static AttributeType.Boolean.Remote of(final Concepts concepts, java.lang.String label) {
                return new AttributeTypeImpl.Boolean.Remote(concepts, label);
            }

            /**
             * @return The direct supertype of this concept
             */
            @Override
            AttributeType.Boolean.Remote getSupertype();

            /**
             * Returns a collection of super-types of this AttributeType.
             *
             * @return The super-types of this AttributeType
             */
            @Override
            Stream<? extends AttributeType.Boolean.Remote> getSupertypes();

            /**
             * Returns a collection of subtypes of this AttributeType.
             *
             * @return The subtypes of this AttributeType
             */
            @Override
            Stream<? extends AttributeType.Boolean.Remote> getSubtypes();

            /**
             * Returns a collection of all Attribute of this AttributeType.
             *
             * @return The resource instances of this AttributeType
             */
            @Override
            Stream<? extends Attribute.Boolean.Remote> getInstances();

            /**
             * Sets the supertype of the AttributeType to be the AttributeType specified.
             *
             * @param type The super type of this AttributeType.
             * @return The AttributeType itself.
             */
            void setSupertype(AttributeType.Boolean type);

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

            @CheckReturnValue
            @Override
            default AttributeType.Boolean.Remote asBoolean() {
                return this;
            }
        }
    }

    interface Long extends AttributeType {

        @CheckReturnValue
        @Override
        default ValueType getValueType() {
            return ValueType.LONG;
        }

        @CheckReturnValue
        @Override
        default AttributeType.Long.Remote asRemote(final Concepts concepts) {
            return AttributeType.Long.Remote.of(concepts, getLabel());
        }

        interface Local extends AttributeType.Long, AttributeType.Local {

            @CheckReturnValue
            @Override
            default AttributeType.Long.Local asLong() {
                return this;
            }
        }

        interface Remote extends AttributeType.Long, AttributeType.Remote {

            static AttributeType.Long.Remote of(final Concepts concepts, java.lang.String label) {
                return new AttributeTypeImpl.Long.Remote(concepts, label);
            }

            /**
             * @return The direct supertype of this concept
             */
            @Override
            AttributeType.Long.Remote getSupertype();

            /**
             * Returns a collection of super-types of this AttributeType.
             *
             * @return The super-types of this AttributeType
             */
            @Override
            Stream<? extends AttributeType.Long.Remote> getSupertypes();

            /**
             * Returns a collection of subtypes of this AttributeType.
             *
             * @return The subtypes of this AttributeType
             */
            @Override
            Stream<? extends AttributeType.Long.Remote> getSubtypes();

            /**
             * Returns a collection of all Attribute of this AttributeType.
             *
             * @return The resource instances of this AttributeType
             */
            @Override
            Stream<? extends Attribute.Long.Remote> getInstances();

            /**
             * Sets the supertype of the AttributeType to be the AttributeType specified.
             *
             * @param type The super type of this AttributeType.
             */
            void setSupertype(AttributeType.Long type);

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

            @CheckReturnValue
            @Override
            default AttributeType.Long.Remote asLong() {
                return this;
            }
        }
    }

    interface Double extends AttributeType {

        @CheckReturnValue
        @Override
        default ValueType getValueType() {
            return ValueType.DOUBLE;
        }

        @CheckReturnValue
        @Override
        default AttributeType.Double.Remote asRemote(final Concepts concepts) {
            return AttributeType.Double.Remote.of(concepts, getLabel());
        }

        interface Local extends AttributeType.Double, AttributeType.Local {

            @CheckReturnValue
            @Override
            default AttributeType.Double.Local asDouble() {
                return this;
            }
        }

        interface Remote extends AttributeType.Double, AttributeType.Remote {

            static AttributeType.Double.Remote of(final Concepts concepts, java.lang.String label) {
                return new AttributeTypeImpl.Double.Remote(concepts, label);
            }

            /**
             * @return The direct supertype of this concept
             */
            @Override
            AttributeType.Double.Remote getSupertype();

            /**
             * Returns a collection of super-types of this AttributeType.
             *
             * @return The super-types of this AttributeType
             */
            @Override
            Stream<? extends AttributeType.Double.Remote> getSupertypes();

            /**
             * Returns a collection of subtypes of this AttributeType.
             *
             * @return The subtypes of this AttributeType
             */
            @Override
            Stream<? extends AttributeType.Double.Remote> getSubtypes();

            /**
             * Returns a collection of all Attribute of this AttributeType.
             *
             * @return The resource instances of this AttributeType
             */
            @Override
            Stream<? extends Attribute.Double.Remote> getInstances();

            /**
             * Sets the supertype of the AttributeType to be the AttributeType specified.
             *
             * @param type The super type of this AttributeType.
             */
            void setSupertype(AttributeType.Double type);

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

            @CheckReturnValue
            @Override
            default AttributeType.Double.Remote asDouble() {
                return this;
            }
        }
    }

    interface String extends AttributeType {

        @CheckReturnValue
        @Override
        default ValueType getValueType() {
            return ValueType.STRING;
        }

        @CheckReturnValue
        @Override
        default AttributeType.String.Remote asRemote(final Concepts concepts) {
            return AttributeType.String.Remote.of(concepts, getLabel());
        }

        interface Local extends AttributeType.String, AttributeType.Local {

            @CheckReturnValue
            @Override
            default AttributeType.String.Local asString() {
                return this;
            }
        }

        interface Remote extends AttributeType.String, AttributeType.Remote {

            static AttributeType.String.Remote of(final Concepts concepts, java.lang.String label) {
                return new AttributeTypeImpl.String.Remote(concepts, label);
            }

            /**
             * @return The direct supertype of this concept
             */
            @Override
            AttributeType.String.Remote getSupertype();

            /**
             * Returns a collection of super-types of this AttributeType.
             *
             * @return The super-types of this AttributeType
             */
            @Override
            Stream<? extends AttributeType.String.Remote> getSupertypes();

            /**
             * Returns a collection of subtypes of this AttributeType.
             *
             * @return The subtypes of this AttributeType
             */
            @Override
            Stream<? extends AttributeType.String.Remote> getSubtypes();

            /**
             * Returns a collection of all Attribute of this AttributeType.
             *
             * @return The resource instances of this AttributeType
             */
            @Override
            Stream<? extends Attribute.String.Remote> getInstances();

            /**
             * Sets the supertype of the AttributeType to be the AttributeType specified.
             *
             * @param type The super type of this AttributeType.
             */
            void setSupertype(AttributeType.String type);

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

            @CheckReturnValue
            @Nullable
            java.lang.String getRegex();

            void setRegex(java.lang.String regex);
        }
    }

    interface DateTime extends AttributeType {

        @CheckReturnValue
        @Override
        default ValueType getValueType() {
            return ValueType.DATETIME;
        }

        @CheckReturnValue
        @Override
        default AttributeType.DateTime.Remote asRemote(final Concepts concepts) {
            return AttributeType.DateTime.Remote.of(concepts, getLabel());
        }

        interface Local extends AttributeType.DateTime, AttributeType.Local {

            @CheckReturnValue
            @Override
            default AttributeType.DateTime.Local asDateTime() {
                return this;
            }
        }

        interface Remote extends AttributeType.DateTime, AttributeType.Remote {

            static AttributeType.DateTime.Remote of(final Concepts concepts, java.lang.String label) {
                return new AttributeTypeImpl.DateTime.Remote(concepts, label);
            }

            /**
             * @return The direct supertype of this concept
             */
            @Override
            AttributeType.DateTime.Remote getSupertype();

            /**
             * Returns a collection of super-types of this AttributeType.
             *
             * @return The super-types of this AttributeType
             */
            @Override
            Stream<? extends AttributeType.DateTime.Remote> getSupertypes();

            /**
             * Returns a collection of subtypes of this AttributeType.
             *
             * @return The subtypes of this AttributeType
             */
            @Override
            Stream<? extends AttributeType.DateTime.Remote> getSubtypes();

            /**
             * Returns a collection of all Attribute of this AttributeType.
             *
             * @return The resource instances of this AttributeType
             */
            @Override
            Stream<? extends Attribute.DateTime.Remote> getInstances();

            /**
             * Sets the supertype of the AttributeType to be the AttributeType specified.
             *
             * @param type The super type of this AttributeType.
             */
            void setSupertype(AttributeType.DateTime type);

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

            @CheckReturnValue
            @Override
            default AttributeType.DateTime.Remote asDateTime() {
                return this;
            }
        }
    }
}
