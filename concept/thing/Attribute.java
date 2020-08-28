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

package grakn.client.concept.thing;

import grakn.client.common.exception.GraknException;
import grakn.client.concept.Concepts;
import grakn.client.concept.thing.impl.AttributeImpl;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.ThingType;

import javax.annotation.CheckReturnValue;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;

public interface Attribute<VALUE> extends Thing {

    /**
     * Retrieves the type of the Attribute, that is, the AttributeType of which this resource is a Thing.
     *
     * @return The AttributeType of which this resource is a Thing.
     */
    @Override
    AttributeType getType();

    /**
     * Retrieves the value of the Attribute.
     *
     * @return The value itself
     */
    @CheckReturnValue
    VALUE getValue();

    @CheckReturnValue
    Attribute.Boolean asBoolean();

    @CheckReturnValue
    Attribute.Long asLong();

    @CheckReturnValue
    Attribute.Double asDouble();

    @CheckReturnValue
    Attribute.String asString();

    @CheckReturnValue
    Attribute.DateTime asDateTime();

    @CheckReturnValue
    @Override
    Attribute.Remote<VALUE> asRemote(Concepts concepts);

    /**
     * Represent a literal Attribute in the graph.
     * Acts as a Thing when relating to other instances except it has the added functionality of:
     * 1. It is unique to its AttributeType based on its value.
     * 2. It has an AttributeType.ValueType associated with it which constrains the allowed values.
     */
    interface Local<VALUE> extends Thing.Local, Attribute<VALUE> {

        @CheckReturnValue
        @Override
        default Attribute.Local<VALUE> asAttribute() {
            return this;
        }

        @Override
        default Attribute.Boolean.Local asBoolean() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, Attribute.Boolean.class.getCanonicalName()));
        }

        @Override
        default Attribute.Long.Local asLong() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, Attribute.Long.class.getCanonicalName()));
        }

        @Override
        default Attribute.Double.Local asDouble() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, Attribute.Double.class.getCanonicalName()));
        }

        @Override
        default Attribute.String.Local asString() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, Attribute.String.class.getCanonicalName()));
        }

        @Override
        default Attribute.DateTime.Local asDateTime() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, Attribute.DateTime.class.getCanonicalName()));
        }
    }

    /**
     * Represent a literal Attribute in the graph.
     * Acts as an Thing when relating to other instances except it has the added functionality of:
     * 1. It is unique to its AttributeType based on it's value.
     * 2. It has an AttributeType.ValueType associated with it which constrains the allowed values.
     */
    interface Remote<VALUE> extends Thing.Remote, Attribute<VALUE> {

        /**
         * Retrieves the type of the Attribute, that is, the AttributeType of which this resource is an Thing.
         *
         * @return The AttributeType of which this resource is an Thing.
         */
        @Override
        AttributeType.Remote getType();

        /**
         * Retrieves the set of all Instances that possess this Attribute.
         *
         * @return The list of all Instances that possess this Attribute.
         */
        @CheckReturnValue
        Stream<? extends Thing.Remote> getOwners();

        /**
         * Retrieves the set of all Instances of the specified type that possess this Attribute.
         *
         * @return The list of all Instances of the specified type that possess this Attribute.
         */
        @CheckReturnValue
        Stream<? extends Thing.Remote> getOwners(ThingType ownerType);

        @CheckReturnValue
        @Override
        default Attribute.Remote<VALUE> asRemote(Concepts concepts) {
            return this;
        }

        @CheckReturnValue
        @Override
        default Attribute.Remote<VALUE> asAttribute() {
            return this;
        }

        @Override
        default Attribute.Boolean.Remote asBoolean() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, Attribute.Boolean.class.getCanonicalName()));
        }

        @Override
        default Attribute.Long.Remote asLong() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, Attribute.Long.class.getCanonicalName()));
        }

        @Override
        default Attribute.Double.Remote asDouble() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, Attribute.Double.class.getCanonicalName()));
        }

        @Override
        default Attribute.String.Remote asString() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, Attribute.String.class.getCanonicalName()));
        }

        @Override
        default Attribute.DateTime.Remote asDateTime() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, Attribute.DateTime.class.getCanonicalName()));
        }
    }

    interface Boolean extends Attribute<java.lang.Boolean> {

        interface Local extends Attribute.Boolean, Attribute.Local<java.lang.Boolean> {

            @CheckReturnValue
            @Override
            default Attribute.Boolean.Local asBoolean() {
                return this;
            }

            @CheckReturnValue
            @Override
            default Boolean.Remote asRemote(final Concepts concepts) {
                return Boolean.Remote.of(concepts, getIID());
            }
        }

        interface Remote extends Attribute.Boolean, Attribute.Remote<java.lang.Boolean> {

            static Boolean.Remote of(final Concepts concepts, final java.lang.String iid) {
                return new AttributeImpl.Boolean.Remote(concepts, iid);
            }

            @CheckReturnValue
            @Override
            default Attribute.Boolean.Remote asRemote(Concepts concepts) {
                return this;
            }

            @CheckReturnValue
            @Override
            default Attribute.Boolean.Remote asBoolean() {
                return this;
            }
        }
    }

    interface Long extends Attribute<java.lang.Long> {

        interface Local extends Attribute.Long, Attribute.Local<java.lang.Long> {

            @CheckReturnValue
            @Override
            default Attribute.Long.Local asLong() {
                return this;
            }

            @CheckReturnValue
            @Override
            default Long.Remote asRemote(final Concepts concepts) {
                return Long.Remote.of(concepts, getIID());
            }
        }

        interface Remote extends Attribute.Long, Attribute.Remote<java.lang.Long> {

            static Long.Remote of(final Concepts concepts, final java.lang.String iid) {
                return new AttributeImpl.Long.Remote(concepts, iid);
            }

            @CheckReturnValue
            @Override
            default Attribute.Long.Remote asRemote(Concepts concepts) {
                return this;
            }

            @CheckReturnValue
            @Override
            default Attribute.Long.Remote asLong() {
                return this;
            }
        }
    }

    interface Double extends Attribute<java.lang.Double> {

        interface Local extends Attribute.Double, Attribute.Local<java.lang.Double> {

            @CheckReturnValue
            @Override
            default Attribute.Double.Local asDouble() {
                return this;
            }

            @CheckReturnValue
            @Override
            default Double.Remote asRemote(final Concepts concepts) {
                return Double.Remote.of(concepts, getIID());
            }
        }

        interface Remote extends Attribute.Double, Attribute.Remote<java.lang.Double> {

            static Double.Remote of(final Concepts concepts, final java.lang.String iid) {
                return new AttributeImpl.Double.Remote(concepts, iid);
            }

            @CheckReturnValue
            @Override
            default Attribute.Double.Remote asRemote(Concepts concepts) {
                return this;
            }

            @CheckReturnValue
            @Override
            default Attribute.Double.Remote asDouble() {
                return this;
            }
        }
    }

    interface String extends Attribute<java.lang.String> {

        interface Local extends Attribute.String, Attribute.Local<java.lang.String> {

            @CheckReturnValue
            @Override
            default Attribute.String.Local asString() {
                return this;
            }

            @CheckReturnValue
            @Override
            default String.Remote asRemote(final Concepts concepts) {
                return String.Remote.of(concepts, getIID());
            }
        }

        interface Remote extends Attribute.String, Attribute.Remote<java.lang.String> {

            static String.Remote of(final Concepts concepts, final java.lang.String iid) {
                return new AttributeImpl.String.Remote(concepts, iid);
            }

            @CheckReturnValue
            @Override
            default Attribute.String.Remote asRemote(Concepts concepts) {
                return this;
            }

            @CheckReturnValue
            @Override
            default Attribute.String.Remote asString() {
                return this;
            }
        }
    }

    interface DateTime extends Attribute<LocalDateTime> {

        interface Local extends Attribute.DateTime, Attribute.Local<LocalDateTime> {

            @CheckReturnValue
            @Override
            default Attribute.DateTime.Local asDateTime() {
                return this;
            }

            @CheckReturnValue
            @Override
            default DateTime.Remote asRemote(final Concepts concepts) {
                return DateTime.Remote.of(concepts, getIID());
            }
        }

        interface Remote extends Attribute.DateTime, Attribute.Remote<LocalDateTime> {

            static DateTime.Remote of(final Concepts concepts, final java.lang.String iid) {
                return new AttributeImpl.DateTime.Remote(concepts, iid);
            }

            @CheckReturnValue
            @Override
            default Attribute.DateTime.Remote asRemote(Concepts concepts) {
                return this;
            }

            @CheckReturnValue
            @Override
            default Attribute.DateTime.Remote asDateTime() {
                return this;
            }
        }
    }
}
