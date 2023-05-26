/*
 * Copyright (C) 2022 Vaticle
 *
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

package com.vaticle.typedb.client.api.concept.type;

import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.thing.Attribute;
import com.vaticle.typeql.lang.common.TypeQLToken;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;

public interface AttributeType extends ThingType {

    @CheckReturnValue
    default ValueType getValueType() {
        return ValueType.OBJECT;
    }

    @Override
    @CheckReturnValue
    default boolean isAttributeType() {
        return true;
    }

    @CheckReturnValue
    default boolean isBoolean() {
        return false;
    }

    @CheckReturnValue
    default boolean isLong() {
        return false;
    }

    @CheckReturnValue
    default boolean isDouble() {
        return false;
    }

    @CheckReturnValue
    default boolean isString() {
        return false;
    }

    @CheckReturnValue
    default boolean isDateTime() {
        return false;
    }

    @Override
    @CheckReturnValue
    AttributeType.Remote asRemote(TypeDBTransaction transaction);

    @CheckReturnValue
    AttributeType.Boolean asBoolean();

    @CheckReturnValue
    AttributeType.Long asLong();

    @CheckReturnValue
    AttributeType.Double asDouble();

    @CheckReturnValue
    AttributeType.String asString();

    @CheckReturnValue
    AttributeType.DateTime asDateTime();

    interface Remote extends ThingType.Remote, AttributeType {

        void setSupertype(AttributeType attributeType);

        @Override
        @CheckReturnValue
        Stream<? extends AttributeType> getSubtypes();

        @Override
        @CheckReturnValue
        Stream<? extends AttributeType> getSubtypesExplicit();

        @Override
        @CheckReturnValue
        Stream<? extends Attribute<?>> getInstances();

        @Override
        @CheckReturnValue
        Stream<? extends Attribute<?>> getInstancesExplicit();

        @CheckReturnValue
        Stream<? extends ThingType> getOwners();

        @CheckReturnValue
        Stream<? extends ThingType> getOwners(Set<TypeQLToken.Annotation> annotations);

        @CheckReturnValue
        Stream<? extends ThingType> getOwnersExplicit();

        @CheckReturnValue
        Stream<? extends ThingType> getOwnersExplicit(Set<TypeQLToken.Annotation> annotations);

        @Override
        @CheckReturnValue
        AttributeType.Remote asAttributeType();

        @Override
        @CheckReturnValue
        AttributeType.Boolean.Remote asBoolean();

        @Override
        @CheckReturnValue
        AttributeType.Long.Remote asLong();

        @Override
        @CheckReturnValue
        AttributeType.Double.Remote asDouble();

        @Override
        @CheckReturnValue
        AttributeType.String.Remote asString();

        @Override
        @CheckReturnValue
        AttributeType.DateTime.Remote asDateTime();
    }

    interface Boolean extends AttributeType {

        @Override
        @CheckReturnValue
        default ValueType getValueType() {
            return ValueType.BOOLEAN;
        }

        @Override
        @CheckReturnValue
        default boolean isBoolean() {
            return true;
        }

        @Override
        @CheckReturnValue
        AttributeType.Boolean.Remote asRemote(TypeDBTransaction transaction);

        interface Remote extends AttributeType.Boolean, AttributeType.Remote {

            Attribute.Boolean put(boolean value);

            @Nullable
            @CheckReturnValue
            Attribute.Boolean get(boolean value);

            @Override
            @CheckReturnValue
            Stream<? extends Attribute.Boolean> getInstances();

            @Override
            @CheckReturnValue
            Stream<? extends Attribute.Boolean> getInstancesExplicit();

            @Override
            @CheckReturnValue
            Stream<? extends AttributeType.Boolean> getSubtypes();

            @Override
            @CheckReturnValue
            Stream<? extends AttributeType.Boolean> getSubtypesExplicit();

            void setSupertype(AttributeType.Boolean booleanAttributeType);
        }
    }

    interface Long extends AttributeType {

        @Override
        @CheckReturnValue
        default ValueType getValueType() {
            return ValueType.LONG;
        }

        @Override
        @CheckReturnValue
        default boolean isLong() {
            return true;
        }

        @Override
        @CheckReturnValue
        AttributeType.Long.Remote asRemote(TypeDBTransaction transaction);

        interface Remote extends AttributeType.Long, AttributeType.Remote {

            Attribute.Long put(long value);

            @Nullable
            @CheckReturnValue
            Attribute.Long get(long value);

            @Override
            @CheckReturnValue
            Stream<? extends Attribute.Long> getInstances();

            @Override
            @CheckReturnValue
            Stream<? extends Attribute.Long> getInstancesExplicit();

            @Override
            @CheckReturnValue
            Stream<? extends AttributeType.Long> getSubtypes();

            @Override
            @CheckReturnValue
            Stream<? extends AttributeType.Long> getSubtypesExplicit();

            void setSupertype(AttributeType.Long longAttributeType);
        }
    }

    interface Double extends AttributeType {

        @Override
        @CheckReturnValue
        default ValueType getValueType() {
            return ValueType.DOUBLE;
        }

        @Override
        @CheckReturnValue
        default boolean isDouble() {
            return true;
        }

        @Override
        @CheckReturnValue
        AttributeType.Double.Remote asRemote(TypeDBTransaction transaction);

        interface Remote extends AttributeType.Double, AttributeType.Remote {

            Attribute.Double put(double value);

            @Nullable
            @CheckReturnValue
            Attribute.Double get(double value);

            @Override
            @CheckReturnValue
            Stream<? extends Attribute.Double> getInstances();

            @Override
            @CheckReturnValue
            Stream<? extends Attribute.Double> getInstancesExplicit();

            @Override
            @CheckReturnValue
            Stream<? extends AttributeType.Double> getSubtypes();

            @Override
            @CheckReturnValue
            Stream<? extends AttributeType.Double> getSubtypesExplicit();

            void setSupertype(AttributeType.Double doubleAttributeType);
        }
    }

    interface String extends AttributeType {

        @Override
        @CheckReturnValue
        default ValueType getValueType() {
            return ValueType.STRING;
        }

        @Override
        @CheckReturnValue
        default boolean isString() {
            return true;
        }

        @Override
        @CheckReturnValue
        AttributeType.String.Remote asRemote(TypeDBTransaction transaction);

        interface Remote extends AttributeType.String, AttributeType.Remote {

            Attribute.String put(java.lang.String value);

            @Nullable
            @CheckReturnValue
            Attribute.String get(java.lang.String value);

            @Override
            @CheckReturnValue
            Stream<? extends Attribute.String> getInstances();

            @Override
            @CheckReturnValue
            Stream<? extends Attribute.String> getInstancesExplicit();

            @Nullable
            @CheckReturnValue
            java.lang.String getRegex();

            void setRegex(java.lang.String regex);

            @Override
            @CheckReturnValue
            Stream<? extends AttributeType.String> getSubtypes();

            @Override
            @CheckReturnValue
            Stream<? extends AttributeType.String> getSubtypesExplicit();

            void setSupertype(AttributeType.String stringAttributeType);
        }
    }

    interface DateTime extends AttributeType {

        @Override
        @CheckReturnValue
        default ValueType getValueType() {
            return ValueType.DATETIME;
        }

        @Override
        @CheckReturnValue
        default boolean isDateTime() {
            return true;
        }

        @Override
        @CheckReturnValue
        AttributeType.DateTime.Remote asRemote(TypeDBTransaction transaction);

        interface Remote extends AttributeType.DateTime, AttributeType.Remote {

            Attribute.DateTime put(LocalDateTime value);

            @Nullable
            @CheckReturnValue
            Attribute.DateTime get(LocalDateTime value);

            @Override
            @CheckReturnValue
            Stream<? extends Attribute.DateTime> getInstances();

            @Override
            @CheckReturnValue
            Stream<? extends Attribute.DateTime> getInstancesExplicit();

            @Override
            @CheckReturnValue
            Stream<? extends AttributeType.DateTime> getSubtypes();

            @Override
            @CheckReturnValue
            Stream<? extends AttributeType.DateTime> getSubtypesExplicit();

            void setSupertype(AttributeType.DateTime dateTimeAttributeType);
        }
    }
}
