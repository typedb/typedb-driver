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

package com.vaticle.typedb.client.concept.value;

import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.Concept;
import com.vaticle.typedb.client.api.concept.type.Type;
import com.vaticle.typedb.client.api.concept.type.Type.ValueType;
import com.vaticle.typedb.client.api.concept.value.Value;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.concept.ConceptImpl;
import com.vaticle.typedb.protocol.ConceptProto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.BAD_VALUE_TYPE;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.VALUE_HAS_NO_REMOTE;
import static com.vaticle.typedb.common.util.Objects.className;

public abstract class ValueImpl<VALUE> extends ConceptImpl implements Value<VALUE> {

    protected final VALUE value;
    private final ValueType valueType;

    public ValueImpl(ValueType valueType, VALUE value) {
        this.valueType = valueType;
        this.value = value;
    }

    public static ValueImpl<?> of(ConceptProto.Value valueProto) {
        switch (valueProto.getValueType()) {
            case BOOLEAN:
                return ValueImpl.Boolean.of(valueProto);
            case LONG:
                return ValueImpl.Long.of(valueProto);
            case DOUBLE:
                return ValueImpl.Double.of(valueProto);
            case STRING:
                return ValueImpl.String.of(valueProto);
            case DATETIME:
                return ValueImpl.DateTime.of(valueProto);
            case UNRECOGNIZED:
            default:
                throw new TypeDBClientException(BAD_VALUE_TYPE, valueProto.getValueType());
        }
    }

    @Override
    public ValueImpl<?> asValue() {
        return this;
    }

    @Override
    public Concept.Remote asRemote(TypeDBTransaction transaction) {
        throw new TypeDBClientException(VALUE_HAS_NO_REMOTE);
    }

    @Override
    public ValueType getValueType() {
        return valueType;
    }

    @Override
    public VALUE getValue() {
        return this.value;
    }

    @Override
    public Value<java.lang.Boolean> asBoolean() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Value.Boolean.class));
    }

    @Override
    public Value<java.lang.Long> asLong() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Value.Long.class));
    }

    @Override
    public Value<java.lang.Double> asDouble() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Value.Double.class));
    }

    @Override
    public Value<java.lang.String> asString() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Value.String.class));
    }

    @Override
    public Value<LocalDateTime> asDateTime() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Value.DateTime.class));
    }

    public static class Boolean extends ValueImpl<java.lang.Boolean> implements Value.Boolean {
        public Boolean(ValueType valueType, java.lang.Boolean value) {
            super(valueType, value);
        }

        public static ValueImpl.Boolean of(ConceptProto.Value valueProto) {
            assert valueProto.getValueType() == ConceptProto.ValueType.BOOLEAN;
            return new ValueImpl.Boolean(ValueType.of(valueProto.getValueType()), valueProto.getValue().getBoolean());
        }

        public ValueImpl.Boolean asBoolean() {
            return this;
        }
    }

    public static class Long extends ValueImpl<java.lang.Long> implements Value.Long {
        public Long(ValueType valueType, java.lang.Long value) {
            super(valueType, value);
        }

        public static ValueImpl.Long of(ConceptProto.Value valueProto) {
            assert valueProto.getValueType() == ConceptProto.ValueType.LONG;
            return new ValueImpl.Long(ValueType.of(valueProto.getValueType()), valueProto.getValue().getLong());
        }

        public ValueImpl.Long asLong() {
            return this;
        }
    }

    public static class Double extends ValueImpl<java.lang.Double>  implements Value.Double {
        public Double(ValueType valueType, java.lang.Double value) {
            super(valueType, value);
        }

        public static ValueImpl.Double of(ConceptProto.Value valueProto) {
            assert valueProto.getValueType() == ConceptProto.ValueType.DOUBLE;
            return new ValueImpl.Double(ValueType.of(valueProto.getValueType()), valueProto.getValue().getDouble());
        }

        public ValueImpl.Double asDouble() {
            return this;
        }
    }

    public static class String extends ValueImpl<java.lang.String> implements Value.String {
        public String(ValueType valueType, java.lang.String value) {
            super(valueType, value);
        }

        public static ValueImpl.String of(ConceptProto.Value valueProto) {
            assert valueProto.getValueType() == ConceptProto.ValueType.STRING;
            return new ValueImpl.String(ValueType.of(valueProto.getValueType()), valueProto.getValue().getString());
        }

        public ValueImpl.String asString() {
            return this;
        }
    }

    public static class DateTime extends ValueImpl<LocalDateTime> implements Value.DateTime {
        public DateTime(ValueType valueType, LocalDateTime value) {
            super(valueType, value);
        }

        public static ValueImpl.DateTime of(ConceptProto.Value valueProto) {
            assert valueProto.getValueType() == ConceptProto.ValueType.DATETIME;
            return new ValueImpl.DateTime(ValueType.of(valueProto.getValueType()),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(valueProto.getValue().getDateTime()), ZoneOffset.UTC));
        }

        public ValueImpl.DateTime asDateTime() {
            return this;
        }
    }
}
