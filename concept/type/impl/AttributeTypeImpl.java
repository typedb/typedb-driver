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

package grakn.client.concept.type.impl;

import grakn.client.GraknClient;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.DataType;
import grakn.client.concept.Label;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.Role;
import grakn.client.exception.GraknClientException;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.session.ConceptProto;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class AttributeTypeImpl {
    /**
     * Client implementation of AttributeType
     *
     * @param <D> The data type of this attribute type
     */
    public static class Local<D> extends TypeImpl.Local<AttributeType<D>, Attribute<D>> implements AttributeType.Local<D> {

        private final DataType<D> dataType;

        public Local(ConceptProto.Concept concept) {
            super(concept);
            this.dataType = RequestBuilder.ConceptMessage.dataType(concept.getDataTypeRes().getDataType());
        }

        @Override
        @Nullable
        public DataType<D> dataType() {
            return dataType;
        }
    }

    /**
     * Client implementation of AttributeType
     *
     * @param <D> The data type of this attribute type
     */
    public static class Remote<D> extends TypeImpl.Remote<AttributeType<D>, Attribute<D>> implements AttributeType.Remote<D> {

        public Remote(GraknClient.Transaction tx, ConceptId id) {
            super(tx, id);
        }

        @Override
        public final AttributeType.Remote<D> key(AttributeType<?> attributeType) {
            return (AttributeType.Remote<D>) super.key(attributeType);
        }

        @Override
        public final AttributeType.Remote<D> has(AttributeType<?> attributeType) {
            return (AttributeType.Remote<D>) super.has(attributeType);
        }

        @Override
        public final AttributeType.Remote<D> plays(Role role) {
            return (AttributeType.Remote<D>) super.plays(role);
        }

        @Override
        public final AttributeType.Remote<D> unkey(AttributeType<?> attributeType) {
            return (AttributeType.Remote<D>) super.unkey(attributeType);
        }

        @Override
        public final AttributeType.Remote<D> unhas(AttributeType<?> attributeType) {
            return (AttributeType.Remote<D>) super.unhas(attributeType);
        }

        @Override
        public final AttributeType.Remote<D> unplay(Role role) {
            return (AttributeType.Remote<D>) super.unplay(role);
        }

        @Override
        public final AttributeType.Remote<D> isAbstract(Boolean isAbstract) {
            return (AttributeType.Remote<D>) super.isAbstract(isAbstract);
        }

        @Override
        public final Stream<Attribute.Remote<D>> instances() {
            return super.instances().map(this::asInstance);
        }

        @Override
        public final Stream<AttributeType.Remote<D>> sups() {
            return super.sups().map(this::asCurrentBaseType);
        }

        @Override
        public final Stream<AttributeType.Remote<D>> subs() {
            return super.subs().map(this::asCurrentBaseType);
        }

        @Override
        public final AttributeType.Remote<D> label(Label label) {
            return (AttributeType.Remote<D>) super.label(label);
        }

        @Override
        public final AttributeType.Remote<D> sup(AttributeType<D> type) {
            return (AttributeType.Remote<D>) super.sup(type);
        }

        @SuppressWarnings("unchecked")
        @Override
        public final Attribute.Remote<D> create(D value) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setAttributeTypeCreateReq(ConceptProto.AttributeType.Create.Req.newBuilder()
                                                       .setValue(RequestBuilder.ConceptMessage.attributeValue(value))).build();

            return (Attribute.Remote<D>) Concept.Remote.of(runMethod(method).getAttributeTypeCreateRes().getAttribute(), tx()).asAttribute();
        }

        @SuppressWarnings("unchecked")
        @Override
        @Nullable
        public final Attribute.Remote<D> attribute(D value) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setAttributeTypeAttributeReq(ConceptProto.AttributeType.Attribute.Req.newBuilder()
                                                          .setValue(RequestBuilder.ConceptMessage.attributeValue(value))).build();

            ConceptProto.AttributeType.Attribute.Res response = runMethod(method).getAttributeTypeAttributeRes();
            switch (response.getResCase()) {
                case NULL:
                    return null;
                case ATTRIBUTE:
                    return (Attribute.Remote<D>) Concept.Remote.of(response.getAttribute(), tx()).asAttribute();
                default:
                    throw GraknClientException.unreachableStatement("Unexpected response " + response);
            }
        }

        @Override
        @Nullable
        public final DataType<D> dataType() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setAttributeTypeDataTypeReq(ConceptProto.AttributeType.DataType.Req.getDefaultInstance()).build();

            ConceptProto.AttributeType.DataType.Res response = runMethod(method).getAttributeTypeDataTypeRes();
            switch (response.getResCase()) {
                case NULL:
                    return null;
                case DATATYPE:
                    return RequestBuilder.ConceptMessage.dataType(response.getDataType());
                default:
                    throw GraknClientException.unreachableStatement("Unexpected response " + response);
            }
        }

        @Override
        @Nullable
        public final String regex() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setAttributeTypeGetRegexReq(ConceptProto.AttributeType.GetRegex.Req.getDefaultInstance()).build();

            String regex = runMethod(method).getAttributeTypeGetRegexRes().getRegex();
            return regex.isEmpty() ? null : regex;
        }

        @Override
        public final AttributeType.Remote<D> regex(String regex) {
            if (regex == null) regex = "";
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setAttributeTypeSetRegexReq(ConceptProto.AttributeType.SetRegex.Req.newBuilder()
                                                         .setRegex(regex)).build();

            runMethod(method);
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected final Attribute.Remote<D> asInstance(Concept.Remote<?> concept) {
            return (Attribute.Remote<D>) concept.asAttribute();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected final AttributeType.Remote<D> asCurrentBaseType(Concept.Remote<?> other) {
            return (AttributeType.Remote<D>) other.asAttributeType();
        }

        @Override
        protected final boolean equalsCurrentBaseType(Concept.Remote<?> other) {
            return other.isAttributeType();
        }
    }
}
