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

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.Concept;
import com.vaticle.typedb.client.common.Label;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.protocol.ConceptProto;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.BAD_VALUE_TYPE;

public interface Type extends Concept {

    @CheckReturnValue
    Label getLabel();

    @CheckReturnValue
    boolean isRoot();

    @CheckReturnValue
    boolean isAbstract();

    @Override
    default boolean isType() {
        return true;
    }

    @Override
    Remote asRemote(TypeDBTransaction transaction);

    @Override
    default JsonObject toJSON() {
        return Json.object().add("label", getLabel().scopedName());
    }

    interface Remote extends Type, Concept.Remote {


        void setLabel(String label);

        @Nullable
        @CheckReturnValue
        Type getSupertype();

        @CheckReturnValue
        Stream<? extends Type> getSupertypes();

        @CheckReturnValue
        Stream<? extends Type> getSubtypes();

        @CheckReturnValue
        Stream<? extends Type> getSubtypesExplicit();
    }

    enum ValueType {

        OBJECT(Object.class),
        BOOLEAN(AttributeType.Boolean.class),
        LONG(AttributeType.Long.class),
        DOUBLE(AttributeType.Double.class),
        STRING(AttributeType.String.class),
        DATETIME(LocalDateTime.class);

        private final Class<?> valueClass;

        ValueType(Class<?> valueClass) {
            this.valueClass = valueClass;
        }

        @CheckReturnValue
        public static ValueType of(ConceptProto.ValueType valueType) {
            switch (valueType) {
                case BOOLEAN:
                    return BOOLEAN;
                case LONG:
                    return LONG;
                case DOUBLE:
                    return DOUBLE;
                case STRING:
                    return STRING;
                case DATETIME:
                    return DATETIME;
                default:
                    throw new TypeDBClientException(BAD_VALUE_TYPE, valueType);
            }
        }

        @CheckReturnValue
        public static ValueType of(Class<?> valueClass) {
            for (ValueType t : ValueType.values()) {
                if (t.valueClass == valueClass) {
                    return t;
                }
            }
            throw new TypeDBClientException(BAD_VALUE_TYPE);
        }

        @CheckReturnValue
        public Class<?> valueClass() {
            return valueClass;
        }

        @CheckReturnValue
        public ConceptProto.ValueType proto() {
            switch (this) {
                case OBJECT:
                    return ConceptProto.ValueType.OBJECT;
                case BOOLEAN:
                    return ConceptProto.ValueType.BOOLEAN;
                case LONG:
                    return ConceptProto.ValueType.LONG;
                case DOUBLE:
                    return ConceptProto.ValueType.DOUBLE;
                case STRING:
                    return ConceptProto.ValueType.STRING;
                case DATETIME:
                    return ConceptProto.ValueType.DATETIME;
                default:
                    return ConceptProto.ValueType.UNRECOGNIZED;
            }
        }
    }
}
