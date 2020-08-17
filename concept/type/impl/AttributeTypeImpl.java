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
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;
import grakn.client.exception.GraknClientException;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.ConceptProto;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class AttributeTypeImpl {

    /**
     * Client implementation of AttributeType
     */
    public abstract static class Local extends ThingTypeImpl.Local implements AttributeType.Local {

        private final ValueType valueType;

        public Local(ConceptProto.Concept concept) {
            super(concept);
            this.valueType = RequestBuilder.ConceptMessage.valueType(concept.getValueTypeRes().getValueType());
        }

        @Override
        @Nullable
        public ValueType getValueType() {
            return valueType;
        }
    }

    /**
     * Client implementation of AttributeType
     */
    public abstract static class Remote extends ThingTypeImpl.Remote implements AttributeType.Remote {

        public Remote(Transaction tx, java.lang.String label) {
            super(tx, label);
        }

        public final Attribute.Remote put(Object value) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setAttributeTypePutReq(ConceptProto.AttributeType.Put.Req.newBuilder()
                                                       .setValue(RequestBuilder.ConceptMessage.attributeValue(value))).build();

            return Concept.Remote.of(tx(), runMethod(method).getAttributeTypePutRes().getAttribute()).asAttribute();
        }

        @Nullable
        public final Attribute.Remote get(Object value) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setAttributeTypeGetReq(ConceptProto.AttributeType.Get.Req.newBuilder()
                                                          .setValue(RequestBuilder.ConceptMessage.attributeValue(value))).build();

            ConceptProto.AttributeType.Get.Res response = runMethod(method).getAttributeTypeGetRes();
            switch (response.getResCase()) {
                case NULL:
                    return null;
                case ATTRIBUTE:
                    return Concept.Remote.of(tx(), response.getAttribute()).asAttribute();
                default:
                    throw GraknClientException.unreachableStatement("Unexpected response " + response);
            }
        }

        @Override
        @Nullable
        public final ValueType getValueType() {
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
        protected final Attribute.Remote asInstance(Concept.Remote concept) {
            return concept.asAttribute();
        }
    }

    public static abstract class Boolean implements AttributeType.Boolean {

        /**
         * Client implementation of AttributeType.Boolean
         */
        public static class Local extends AttributeTypeImpl.Local implements AttributeType.Boolean.Local {

            private final ValueType valueType;

            public Local(ConceptProto.Concept concept) {
                super(concept);
                this.valueType = RequestBuilder.ConceptMessage.valueType(concept.getValueTypeRes().getValueType());
            }

            @Override
            @Nullable
            public ValueType getValueType() {
                return valueType;
            }
        }

        /**
         * Client implementation of AttributeType.Boolean
         */
        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.Boolean.Remote {

            public Remote(Transaction tx, java.lang.String label) {
                super(tx, label);
            }

            @Override
            public AttributeType.Boolean.Remote getSupertype() {
                return super.getSupertype().asAttributeType().asBoolean();
            }

            @Override
            public final Stream<AttributeType.Boolean.Remote> getSupertypes() {
                return super.getSupertypes().map(x -> x.asAttributeType().asBoolean());
            }

            @Override
            public final Stream<AttributeType.Boolean.Remote> getSubtypes() {
                return super.getSubtypes().map(x -> x.asAttributeType().asBoolean());
            }

            @Override
            public final Stream<Attribute.Boolean.Remote> getInstances() {
                return super.getInstances().map(x -> x.asAttribute().asBoolean());
            }

            @Override
            public final void setSupertype(AttributeType.Boolean type) {
                super.setSupertype(type);
            }

            public final Attribute.Boolean.Remote put(boolean value) {
                return super.put(value).asBoolean();
            }

            @Nullable
            public final Attribute.Boolean.Remote get(boolean value) {
                final Attribute.Remote attr = super.get(value);
                return attr != null ? attr.asBoolean() : null;
            }

            // TODO: move to AttributeType.String.Remote
            @Nullable
            public final java.lang.String getRegex() {
                ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                        .setAttributeTypeGetRegexReq(ConceptProto.AttributeType.GetRegex.Req.getDefaultInstance()).build();

                java.lang.String regex = runMethod(method).getAttributeTypeGetRegexRes().getRegex();
                return regex.isEmpty() ? null : regex;
            }

            public final AttributeType.Remote setRegex(java.lang.String regex) {
                if (regex == null) regex = "";
                ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                        .setAttributeTypeSetRegexReq(ConceptProto.AttributeType.SetRegex.Req.newBuilder()
                                                             .setRegex(regex)).build();

                runMethod(method);
                return this;
            }
        }
    }
}
