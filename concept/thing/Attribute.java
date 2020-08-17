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

import grakn.client.Grakn.Transaction;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.GraknConceptException;
import grakn.client.concept.thing.impl.AttributeImpl;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.ThingType;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

public interface Attribute extends Thing {

    /**
     * Retrieves the type of the Attribute, that is, the AttributeType of which this resource is a Thing.
     *
     * @return The AttributeType of which this resource is a Thing.
     */
    @Override
    AttributeType getType();

    @CheckReturnValue
    default Attribute.Boolean asBoolean() {
        throw GraknConceptException.invalidCasting(this, java.lang.Boolean.class);
    }

    @CheckReturnValue
    default Attribute.Long asLong() {
        throw GraknConceptException.invalidCasting(this, java.lang.Long.class);
    }

    @CheckReturnValue
    default Attribute.Double asDouble() {
        throw GraknConceptException.invalidCasting(this, java.lang.Double.class);
    }

    @CheckReturnValue
    default Attribute.String asString() {
        throw GraknConceptException.invalidCasting(this, java.lang.String.class);
    }

    @CheckReturnValue
    default Attribute.DateTime asDateTime() {
        throw GraknConceptException.invalidCasting(this, java.time.LocalDateTime.class);
    }

    /**
     * Represent a literal Attribute in the graph.
     * Acts as a Thing when relating to other instances except it has the added functionality of:
     * 1. It is unique to its AttributeType based on its value.
     * 2. It has an AttributeType.ValueType associated with it which constrains the allowed values.
     */
    interface Local extends Thing.Local, Attribute {

        @CheckReturnValue
        @Override
        default Attribute.Local asAttribute() {
            return this;
        }

        @CheckReturnValue
        @Override
        Attribute.Boolean.Local asBoolean();

        @CheckReturnValue
        @Override
        Attribute.Long.Local asLong();

        @CheckReturnValue
        @Override
        Attribute.Double.Local asDouble();

        @CheckReturnValue
        @Override
        Attribute.String.Local asString();

        @CheckReturnValue
        @Override
        Attribute.DateTime.Local asDateTime();
    }

    /**
     * Represent a literal Attribute in the graph.
     * Acts as an Thing when relating to other instances except it has the added functionality of:
     * 1. It is unique to its AttributeType based on it's value.
     * 2. It has an AttributeType.ValueType associated with it which constrains the allowed values.
     */
    interface Remote extends Thing.Remote, Attribute {

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
        default Attribute.Remote asAttribute() {
            return this;
        }

        @CheckReturnValue
        @Override
        Attribute.Boolean.Remote asBoolean();

        @CheckReturnValue
        @Override
        Attribute.Long.Remote asLong();

        @CheckReturnValue
        @Override
        Attribute.Double.Remote asDouble();

        @CheckReturnValue
        @Override
        Attribute.String.Remote asString();

        @CheckReturnValue
        @Override
        Attribute.DateTime.Remote asDateTime();
    }

    interface Boolean extends Attribute {

        @CheckReturnValue
        @Override
        default Remote asRemote(Transaction tx) {
            return Remote.of(tx, getIID());
        }

        /**
         * Retrieves the value of the Attribute.
         *
         * @return The value itself
         */
        @CheckReturnValue
        java.lang.Boolean getValue();

        interface Local extends Attribute.Boolean, Attribute.Local {

            @CheckReturnValue
            @Override
            default Attribute.Boolean.Local asBoolean() {
                return this;
            }
        }

        interface Remote extends Attribute.Boolean, Attribute.Remote {

            static Boolean.Remote of(Transaction tx, ConceptIID iid) {
                return new AttributeImpl.Boolean.Remote(tx, iid);
            }

            @CheckReturnValue
            @Override
            default Attribute.Boolean.Remote asBoolean() {
                return this;
            }
        }
    }

    interface Long extends Attribute {

        @CheckReturnValue
        @Override
        default Remote asRemote(Transaction tx) {
            return Remote.of(tx, getIID());
        }

        /**
         * Retrieves the value of the Attribute.
         *
         * @return The value itself
         */
        @CheckReturnValue
        java.lang.Long getValue();

        interface Local extends Attribute.Long, Attribute.Local {

            @CheckReturnValue
            @Override
            default Attribute.Long.Local asLong() {
                return this;
            }
        }

        interface Remote extends Attribute.Long, Attribute.Remote {

            static Long.Remote of(Transaction tx, ConceptIID iid) {
                return new AttributeImpl.Long.Remote(tx, iid);
            }

            @CheckReturnValue
            @Override
            default Attribute.Long.Remote asLong() {
                return this;
            }
        }
    }

    interface Double extends Attribute {

        @CheckReturnValue
        @Override
        default Remote asRemote(Transaction tx) {
            return Remote.of(tx, getIID());
        }

        /**
         * Retrieves the value of the Attribute.
         *
         * @return The value itself
         */
        @CheckReturnValue
        java.lang.Double getValue();

        interface Local extends Attribute.Double, Attribute.Local {

            @CheckReturnValue
            @Override
            default Attribute.Double.Local asDouble() {
                return this;
            }
        }

        interface Remote extends Attribute.Double, Attribute.Remote {

            static Double.Remote of(Transaction tx, ConceptIID iid) {
                return new AttributeImpl.Double.Remote(tx, iid);
            }

            @CheckReturnValue
            @Override
            default Attribute.Double.Remote asDouble() {
                return this;
            }
        }
    }

    interface String extends Attribute {

        @CheckReturnValue
        @Override
        default Remote asRemote(Transaction tx) {
            return Remote.of(tx, getIID());
        }

        /**
         * Retrieves the value of the Attribute.
         *
         * @return The value itself
         */
        @CheckReturnValue
        java.lang.String getValue();

        interface Local extends Attribute.String, Attribute.Local {

            @CheckReturnValue
            @Override
            default Attribute.String.Local asString() {
                return this;
            }
        }

        interface Remote extends Attribute.String, Attribute.Remote {

            static String.Remote of(Transaction tx, ConceptIID iid) {
                return new AttributeImpl.String.Remote(tx, iid);
            }

            @CheckReturnValue
            @Override
            default Attribute.String.Remote asString() {
                return this;
            }
        }
    }

    interface DateTime extends Attribute {

        @CheckReturnValue
        @Override
        default Remote asRemote(Transaction tx) {
            return Remote.of(tx, getIID());
        }

        /**
         * Retrieves the value of the Attribute.
         *
         * @return The value itself
         */
        @CheckReturnValue
        java.time.LocalDateTime getValue();

        interface Local extends Attribute.DateTime, Attribute.Local {

            @CheckReturnValue
            @Override
            default Attribute.DateTime.Local asDateTime() {
                return this;
            }
        }

        interface Remote extends Attribute.DateTime, Attribute.Remote {

            static DateTime.Remote of(Transaction tx, ConceptIID iid) {
                return new AttributeImpl.DateTime.Remote(tx, iid);
            }

            @CheckReturnValue
            @Override
            default Attribute.DateTime.Remote asDateTime() {
                return this;
            }
        }
    }
}
