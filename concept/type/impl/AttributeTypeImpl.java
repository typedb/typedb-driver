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

import grakn.client.Grakn.Transaction;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.ValueType;
import grakn.client.concept.Label;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.RoleType;
import grakn.client.exception.GraknClientException;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.ConceptProto;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class AttributeTypeImpl {
    /**
     * Client implementation of AttributeType
     *
     * @param <D> The data type of this attribute type
     */
    public static class Local<D> extends ThingTypeImpl.Local<AttributeType<D>, Attribute<D>> implements AttributeType.Local<D> {

        private final ValueType<D> valueType;

        public Local(ConceptProto.Concept concept) {
            super(concept);
            this.valueType = RequestBuilder.ConceptMessage.valueType(concept.getValueTypeRes().getValueType());
        }

        @Override
        @Nullable
        public ValueType<D> getValueType() {
            return valueType;
        }
    }

    /**
     * Client implementation of AttributeType
     *
     * @param <D> The data type of this attribute type
     */
    public static class Remote<D> extends ThingTypeImpl.Remote<AttributeType<D>, Attribute<D>> implements AttributeType.Remote<D> {

        public Remote(Transaction tx, ConceptIID iid) {
            super(tx, iid);
        }

        @Override
        public final AttributeType.Remote<D> setOwns(AttributeType<?> attributeType) {
            return (AttributeType.Remote<D>) super.setOwns(attributeType);
        }

        @Override
        public final AttributeType.Remote<D> setOwns(AttributeType<?> attributeType, boolean isKey) {
            return (AttributeType.Remote<D>) super.setOwns(attributeType, isKey);
        }

        @Override
        public final AttributeType.Remote<D> setOwns(AttributeType<?> attributeType, AttributeType<?> overriddenType) {
            return (AttributeType.Remote<D>) super.setOwns(attributeType, overriddenType);
        }

        @Override
        public final AttributeType.Remote<D> setOwns(AttributeType<?> attributeType, AttributeType<?> overriddenType, boolean isKey) {
            return (AttributeType.Remote<D>) super.setOwns(attributeType, overriddenType, isKey);
        }

        @Override
        public Stream<? extends AttributeType.Remote<?>> getOwns(boolean keysOnly) {
            return super.getOwns(keysOnly);
        }

        @Override
        public final AttributeType.Remote<D> unsetOwns(AttributeType<?> attributeType) {
            return (AttributeType.Remote<D>) super.unsetOwns(attributeType);
        }

        @Override
        public final AttributeType.Remote<D> setPlays(RoleType role) {
            return (AttributeType.Remote<D>) super.setPlays(role);
        }

        @Override
        public final AttributeType.Remote<D> unsetPlays(RoleType role) {
            return (AttributeType.Remote<D>) super.unsetPlays(role);
        }

        @Override
        public final AttributeType.Remote<D> isAbstract(Boolean isAbstract) {
            return (AttributeType.Remote<D>) super.isAbstract(isAbstract);
        }

        @Override
        public final Stream<Attribute.Remote<D>> getInstances() {
            return super.getInstances().map(this::asInstance);
        }

        @Override
        public final Stream<AttributeType.Remote<D>> getSupertypes() {
            return super.getSupertypes().map(this::asCurrentBaseType);
        }

        @Override
        public final Stream<AttributeType.Remote<D>> getSubtypes() {
            return super.getSubtypes().map(this::asCurrentBaseType);
        }

        @Override
        public final AttributeType.Remote<D> setLabel(Label label) {
            return (AttributeType.Remote<D>) super.setLabel(label);
        }

        @Override
        public final AttributeType.Remote<D> setSupertype(AttributeType<D> type) {
            return (AttributeType.Remote<D>) super.setSupertype(type);
        }

        @SuppressWarnings("unchecked")
        @Override
        public final Attribute.Remote<D> put(D value) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setAttributeTypePutReq(ConceptProto.AttributeType.Put.Req.newBuilder()
                                                       .setValue(RequestBuilder.ConceptMessage.attributeValue(value))).build();

            return (Attribute.Remote<D>) Concept.Remote.of(tx(), runMethod(method).getAttributeTypePutRes().getAttribute()).asAttribute();
        }

        @SuppressWarnings("unchecked")
        @Override
        @Nullable
        public final Attribute.Remote<D> get(D value) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setAttributeTypeGetReq(ConceptProto.AttributeType.Get.Req.newBuilder()
                                                          .setValue(RequestBuilder.ConceptMessage.attributeValue(value))).build();

            ConceptProto.AttributeType.Get.Res response = runMethod(method).getAttributeTypeGetRes();
            switch (response.getResCase()) {
                case NULL:
                    return null;
                case ATTRIBUTE:
                    return (Attribute.Remote<D>) Concept.Remote.of(tx(), response.getAttribute()).asAttribute();
                default:
                    throw GraknClientException.unreachableStatement("Unexpected response " + response);
            }
        }

        @Override
        @Nullable
        public final ValueType<D> getValueType() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setAttributeTypeGetValueTypeReq(ConceptProto.AttributeType.GetValueType.Req.getDefaultInstance()).build();

            ConceptProto.AttributeType.GetValueType.Res response = runMethod(method).getAttributeTypeGetValueTypeRes();
            switch (response.getResCase()) {
                case NULL:
                    return null;
                case VALUETYPE:
                    return RequestBuilder.ConceptMessage.valueType(response.getValueType());
                default:
                    throw GraknClientException.unreachableStatement("Unexpected response " + response);
            }
        }

        @Override
        @Nullable
        public final String getRegex() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setAttributeTypeGetRegexReq(ConceptProto.AttributeType.GetRegex.Req.getDefaultInstance()).build();

            String regex = runMethod(method).getAttributeTypeGetRegexRes().getRegex();
            return regex.isEmpty() ? null : regex;
        }

        @Override
        public final AttributeType.Remote<D> setRegex(String regex) {
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
