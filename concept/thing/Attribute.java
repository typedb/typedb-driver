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

import grakn.client.Grakn;
import grakn.client.common.exception.GraknException;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.ThingType;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;

public interface Attribute<VALUE> extends Thing {

    VALUE getValue();

    Attribute.Boolean asBoolean();

    Attribute.Long asLong();

    Attribute.Double asDouble();

    Attribute.String asString();

    Attribute.DateTime asDateTime();

    @Override
    Attribute.Remote<VALUE> asRemote(Grakn.Transaction transaction);

    interface Local<VALUE> extends Thing.Local, Attribute<VALUE> {

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

    interface Remote<VALUE> extends Thing.Remote, Attribute<VALUE> {

        Stream<? extends Thing.Remote> getOwners();

        Stream<? extends Thing.Remote> getOwners(ThingType ownerType);

        @Override
        default Attribute.Remote<VALUE> asRemote(Grakn.Transaction transaction) {
            return this;
        }

        @Override
        AttributeType.Local getType();

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

            @Override
            default Attribute.Boolean.Local asBoolean() {
                return this;
            }

            @Override
            Boolean.Remote asRemote(final Grakn.Transaction transaction);
        }

        interface Remote extends Attribute.Boolean, Attribute.Remote<java.lang.Boolean> {

            @Override
            default Attribute.Boolean.Remote asRemote(Grakn.Transaction transaction) {
                return this;
            }

            @Override
            default Attribute.Boolean.Remote asBoolean() {
                return this;
            }
        }
    }

    interface Long extends Attribute<java.lang.Long> {

        interface Local extends Attribute.Long, Attribute.Local<java.lang.Long> {

            @Override
            default Attribute.Long.Local asLong() {
                return this;
            }

            @Override
            Long.Remote asRemote(final Grakn.Transaction transaction);
        }

        interface Remote extends Attribute.Long, Attribute.Remote<java.lang.Long> {

            @Override
            default Attribute.Long.Remote asRemote(Grakn.Transaction transaction) {
                return this;
            }

            @Override
            default Attribute.Long.Remote asLong() {
                return this;
            }
        }
    }

    interface Double extends Attribute<java.lang.Double> {

        interface Local extends Attribute.Double, Attribute.Local<java.lang.Double> {

            @Override
            default Attribute.Double.Local asDouble() {
                return this;
            }

            @Override
            Double.Remote asRemote(final Grakn.Transaction transaction);
        }

        interface Remote extends Attribute.Double, Attribute.Remote<java.lang.Double> {

            @Override
            default Attribute.Double.Remote asRemote(Grakn.Transaction transaction) {
                return this;
            }

            @Override
            default Attribute.Double.Remote asDouble() {
                return this;
            }
        }
    }

    interface String extends Attribute<java.lang.String> {

        interface Local extends Attribute.String, Attribute.Local<java.lang.String> {

            @Override
            default Attribute.String.Local asString() {
                return this;
            }

            @Override
            String.Remote asRemote(final Grakn.Transaction transaction);
        }

        interface Remote extends Attribute.String, Attribute.Remote<java.lang.String> {

            @Override
            default Attribute.String.Remote asRemote(Grakn.Transaction transaction) {
                return this;
            }

            @Override
            default Attribute.String.Remote asString() {
                return this;
            }
        }
    }

    interface DateTime extends Attribute<LocalDateTime> {

        interface Local extends Attribute.DateTime, Attribute.Local<LocalDateTime> {

            @Override
            default Attribute.DateTime.Local asDateTime() {
                return this;
            }

            @Override
            DateTime.Remote asRemote(final Grakn.Transaction transaction);
        }

        interface Remote extends Attribute.DateTime, Attribute.Remote<LocalDateTime> {

            @Override
            default Attribute.DateTime.Remote asRemote(Grakn.Transaction transaction) {
                return this;
            }

            @Override
            default Attribute.DateTime.Remote asDateTime() {
                return this;
            }
        }
    }
}
