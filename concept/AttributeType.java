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

package grakn.client.concept;

import com.google.common.collect.Sets;
import grakn.client.GraknClient;
import grakn.client.exception.GraknClientException;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.session.ConceptProto;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Client implementation of AttributeType
 *
 * @param <D> The data type of this attribute type
 */
public class AttributeType<D> extends Type<AttributeType, Attribute<D>> {

    AttributeType(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    static <D> AttributeType<D> construct(GraknClient.Transaction tx, ConceptId id) {
        return new AttributeType<>(tx, id);
    }

    public final Attribute<D> create(D value) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setAttributeTypeCreateReq(ConceptProto.AttributeType.Create.Req.newBuilder()
                                                   .setValue(RequestBuilder.Concept.attributeValue(value))).build();

        Concept concept = Concept.of(runMethod(method).getAttributeTypeCreateRes().getAttribute(), tx());
        return asInstance(concept);
    }

    @Nullable
    public final Attribute<D> attribute(D value) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setAttributeTypeAttributeReq(ConceptProto.AttributeType.Attribute.Req.newBuilder()
                                                      .setValue(RequestBuilder.Concept.attributeValue(value))).build();

        ConceptProto.AttributeType.Attribute.Res response = runMethod(method).getAttributeTypeAttributeRes();
        switch (response.getResCase()) {
            case NULL:
                return null;
            case ATTRIBUTE:
                return Concept.of(response.getAttribute(), tx()).asAttribute();
            default:
                throw GraknClientException.unreachableStatement("Unexpected response " + response);
        }
    }

    @Nullable
    public final AttributeType.DataType<D> dataType() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setAttributeTypeDataTypeReq(ConceptProto.AttributeType.DataType.Req.getDefaultInstance()).build();

        ConceptProto.AttributeType.DataType.Res response = runMethod(method).getAttributeTypeDataTypeRes();
        switch (response.getResCase()) {
            case NULL:
                return null;
            case DATATYPE:
                return (AttributeType.DataType<D>) RequestBuilder.Concept.dataType(response.getDataType());
            default:
                throw GraknClientException.unreachableStatement("Unexpected response " + response);
        }
    }

    @Nullable
    public final String regex() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setAttributeTypeGetRegexReq(ConceptProto.AttributeType.GetRegex.Req.getDefaultInstance()).build();

        String regex = runMethod(method).getAttributeTypeGetRegexRes().getRegex();
        return regex.isEmpty() ? null : regex;
    }

    public final AttributeType regex(String regex) {
        if (regex == null) regex = "";
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setAttributeTypeSetRegexReq(ConceptProto.AttributeType.SetRegex.Req.newBuilder()
                                                     .setRegex(regex)).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    @Override
    final AttributeType asCurrentBaseType(Concept other) {
        return other.asAttributeType();
    }

    @Override
    final boolean equalsCurrentBaseType(Concept other) {
        return other.isAttributeType();
    }

    @Override
    protected final Attribute<D> asInstance(Concept concept) {
        return concept.asAttribute();
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    @CheckReturnValue
    @Override
    public AttributeType asAttributeType() {
        return this;
    }

    @Deprecated
    @CheckReturnValue
    @Override
    public boolean isAttributeType() {
        return true;
    }


    /**
     * A class used to hold the supported data types of resources and any other concepts.
     * This is used tp constrain value data types to only those we explicitly support.
     *
     * @param <D> The data type.
     */
    public abstract static class DataType<D> {
        public static final AttributeType.DataType<Boolean> BOOLEAN = new AttributeType.DataType<Boolean>(Boolean.class){
            @Override
            public Set<AttributeType.DataType<?>> comparableDataTypes() { return Collections.singleton(AttributeType.DataType.BOOLEAN); }
        };
        public static final AttributeType.DataType<LocalDateTime> DATE = new AttributeType.DataType<LocalDateTime>(LocalDateTime.class){
            @Override
            public Set<AttributeType.DataType<?>> comparableDataTypes() { return Collections.singleton(AttributeType.DataType.DATE); }
        };
        public static final AttributeType.DataType<Double> DOUBLE = new AttributeType.DataType<Double>(Double.class){
            @Override
            public Set<AttributeType.DataType<?>> comparableDataTypes() {
                return java.util.Collections.unmodifiableSet(Sets.newHashSet(AttributeType.DataType.DOUBLE,
                        //DataType.FLOAT,
                        //DataType.INTEGER,
                        AttributeType.DataType.LONG));
            }
        };

        public static final AttributeType.DataType<Float> FLOAT = new AttributeType.DataType<Float>(Float.class){
            @Override
            public Set<AttributeType.DataType<?>> comparableDataTypes() { return new HashSet<>(); }
        };
        public static final AttributeType.DataType<Integer> INTEGER = new AttributeType.DataType<Integer>(Integer.class){
            @Override
            public Set<AttributeType.DataType<?>> comparableDataTypes() { return new HashSet<>(); }
        };
        public static final AttributeType.DataType<Long> LONG = new AttributeType.DataType<Long>(Long.class){
            @Override
            public Set<AttributeType.DataType<?>> comparableDataTypes() {
                return java.util.Collections.unmodifiableSet(Sets.newHashSet(AttributeType.DataType.DOUBLE,
                        //DataType.FLOAT,
                        //DataType.INTEGER,
                        AttributeType.DataType.LONG));
            }
        };
        public static final AttributeType.DataType<String> STRING = new AttributeType.DataType<String>(String.class){
            @Override
            public Set<AttributeType.DataType<?>> comparableDataTypes() { return Collections.singleton(AttributeType.DataType.STRING); }
        };

        private static final List<AttributeType.DataType<?>> values = java.util.Collections.unmodifiableList(
                Arrays.asList(BOOLEAN, DATE, DOUBLE, FLOAT, INTEGER, LONG, STRING));

        private final Class<D> dataClass;

        private DataType(Class<D> dataClass) {
            this.dataClass = dataClass;
        }

        @CheckReturnValue
        public Class<D> dataClass() {
            return dataClass;
        }

        @CheckReturnValue
        public String name() {
            return dataClass.getName();
        }

        @Override
        public String toString() {
            return name();
        }

        @CheckReturnValue
        public static List<AttributeType.DataType<?>> values() {
            return values;
        }

        @CheckReturnValue
        public abstract Set<AttributeType.DataType<?>> comparableDataTypes();

        @SuppressWarnings("unchecked")
        @CheckReturnValue
        public static <D> AttributeType.DataType<D> of(Class<D> name) {
            for (AttributeType.DataType<?> dc : AttributeType.DataType.values()) {
                if (dc.dataClass.equals(name)) {
                    return (AttributeType.DataType<D>) dc;
                }
            }
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AttributeType.DataType<?> that = (AttributeType.DataType<?>) o;

            return (this.dataClass().equals(that.dataClass()));
        }

        @Override
        public int hashCode() {
            int h = 1;
            h *= 1000003;
            h ^=  dataClass.hashCode();
            return h;
        }
    }
}
