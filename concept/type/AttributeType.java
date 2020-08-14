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
import grakn.client.concept.ValueTypeOld;
import grakn.client.concept.GraknConceptException;
import grakn.client.concept.Label;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.type.impl.AttributeTypeImpl;
import grakn.protocol.ConceptProto;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;

public interface AttributeType extends ThingType {

    /**
     * Get the data type to which instances of the AttributeType must conform.
     *
     * @return The data type to which instances of this Attribute  must conform.
     */
    @Nullable
    @CheckReturnValue
    ValueTypeOld getValueType();

    @CheckReturnValue
    @Override
    default AttributeType asAttributeType() {
        return this;
    }

    @CheckReturnValue
    default private AttributeType asAttributeType(ValueTypeOld valueType) {
        if (!valueType.equals(getValueType())) {
            throw GraknConceptException.invalidCasting(this, valueType.getClass());
        }
        return this;
    }

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
        public String toString() {
            return valueClass.getName();
        }

        /**
         * Obtains the value from the given value protocol and casts it to a generic Java type D.
         *
         * @param value The value protocol object.
         * @return the value cast to D Java type.
         * @throws IllegalArgumentException if the value type is not recognised or does not match the type of this ValueTypeOld.
         */
        @SuppressWarnings("unchecked")
        public static <D> D staticCastValue(ConceptProto.ValueObject value) {
            try {
                switch (value.getValueCase()) {
                    case DATETIME:
                        return (D) LocalDateTime.ofInstant(Instant.ofEpochMilli(value.getDatetime()), ZoneId.of("Z"));
                    case STRING:
                        return (D) value.getString();
                    case BOOLEAN:
                        return (D) (Boolean) value.getBoolean();
                    case LONG:
                        return (D) (Long) value.getLong();
                    case DOUBLE:
                        return (D) (Double) value.getDouble();
                    case VALUE_NOT_SET:
                        return null;
                    default:
                        throw new IllegalArgumentException("Unexpected value for attribute: " + value);
                }
            } catch (ClassCastException ex) {
                throw new IllegalArgumentException("Value type did not match ValueTypeOld ", ex);
            }
        }
    }


    /**
     * An ontological element which models and categorises the various Attribute in the graph.
     * This ontological element behaves similarly to Type when defining how it relates to other
     * types. It has two additional functions to be aware of:
     * 1. It has a ValueTypeOld constraining the data types of the values it's instances may take.
     * 2. Any of it's instances are unique to the type.
     * For example if you have an AttributeType modelling month throughout the year there can only be one January.
     */
    interface Local extends ThingType.Local, AttributeType {
    }

    /**
     * An ontological element which models and categorises the various Attribute in the graph.
     * This ontological element behaves similarly to Type when defining how it relates to other
     * types. It has two additional functions to be aware of:
     * 1. It has a ValueTypeOld constraining the data types of the values it's instances may take.
     * 2. Any of it's instances are unique to the type.
     * For example if you have an AttributeType modelling month throughout the year there can only be one January.
     */
    interface Remote extends ThingType.Remote, AttributeType {

        static AttributeType.Remote of(Transaction tx, ConceptIID iid) {
            return new AttributeTypeImpl.Remote(tx, iid);
        }

        //------------------------------------- Modifiers ----------------------------------

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
        AttributeType.Remote isAbstract(Boolean isAbstract);

        /**
         * Sets the supertype of the AttributeType to be the AttributeType specified.
         *
         * @param type The super type of this AttributeType.
         * @return The AttributeType itself.
         */
        AttributeType.Remote setSupertype(AttributeType type);

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
         * Set the value for the Attribute, unique to its type.
         *
         * @param value A value for the Attribute which is unique to its type
         * @return new or existing Attribute of this type with the provided value.
         */
        Attribute.Remote put(D value);

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

        //------------------------------------- Accessors ---------------------------------

        /**
         * Get the Attribute with the value provided, and its type, or return NULL
         *
         * @param value A value which an Attribute in the graph may be holding
         * @return The Attribute with the provided value and type or null if no such Attribute exists.
         * @see Attribute.Remote
         */
        @CheckReturnValue
        @Nullable
        Attribute.Remote get(D value);

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
         * Retrieve the regular expression to which instances of this AttributeType must conform, or {@code null} if no
         * regular expression is set.
         * By default, an AttributeType does not have a regular expression set.
         *
         * @return The regular expression to which instances of this AttributeType must conform.
         */
        @CheckReturnValue
        @Nullable
        String getRegex();

        @CheckReturnValue
        @Override
        default AttributeType.Remote asAttributeType() {
            return this;
        }
    }
}
