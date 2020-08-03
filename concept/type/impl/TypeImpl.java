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
import grakn.client.concept.ConceptIID;
import grakn.client.concept.Label;
import grakn.client.concept.impl.ConceptImpl;
import grakn.client.concept.type.Type;
import grakn.client.exception.GraknClientException;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.ConceptProto;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public abstract class TypeImpl {

    public abstract static class Local<
            SomeSchemaConcept extends Type<SomeSchemaConcept>>
            extends ConceptImpl.Local<SomeSchemaConcept>
            implements Type.Local<SomeSchemaConcept> {

        private final Label label;

        protected Local(ConceptProto.Concept concept) {
            super(concept);
            this.label = Label.of(concept.getLabelRes().getLabel());
        }

        @Override
        public final Label label() {
            return label;
        }
    }

    public abstract static class Remote<
            BaseType extends Type<BaseType>>
            extends ConceptImpl.Remote<BaseType>
            implements Type.Remote<BaseType> {

        public Remote(GraknClient.Transaction tx, ConceptIID iid) {
            super(tx, iid);
        }

        public final Type.Remote<BaseType> sup(Type<?> type) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypeSetSupReq(ConceptProto.Type.SetSup.Req.newBuilder()
                                                       .setType(RequestBuilder.ConceptMessage.from(type))).build();

            runMethod(method);
            return this;
        }

        @Override
        public final Label label() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypeGetLabelReq(ConceptProto.Type.GetLabel.Req.getDefaultInstance()).build();

            return Label.of(runMethod(method).getTypeGetLabelRes().getLabel());
        }

        @Override
        public Type.Remote<BaseType> label(Label label) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypeSetLabelReq(ConceptProto.Type.SetLabel.Req.newBuilder()
                                                         .setLabel(label.getValue())).build();

            runMethod(method);
            return this;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        public Type.Remote<BaseType> sup() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypeGetSupReq(ConceptProto.Type.GetSup.Req.getDefaultInstance()).build();

            ConceptProto.Type.GetSup.Res response = runMethod(method).getTypeGetSupRes();

            switch (response.getResCase()) {
                case NULL:
                    return null;
                case TYPE:
                    return (Type.Remote<BaseType>) Concept.Remote.of(tx(), response.getType()).asSchemaConcept();
                default:
                    throw GraknClientException.unreachableStatement("Unexpected response " + response);
            }

        }

        @Override
        public Stream<? extends Type.Remote<BaseType>> sups() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setTypeSupsIterReq(ConceptProto.Type.Sups.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getTypeSubsIterRes().getType())
                    .filter(this::equalsCurrentBaseType).map(this::asCurrentBaseType);
        }

        @Override
        public Stream<? extends Type.Remote<BaseType>> subs() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setTypeSubsIterReq(ConceptProto.Type.Subs.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getTypeSubsIterRes().getType()).map(this::asCurrentBaseType);
        }

        @Override
        protected abstract Type.Remote<BaseType> asCurrentBaseType(Concept.Remote<?> other);

        protected abstract boolean equalsCurrentBaseType(Concept.Remote<?> other);
    }
}
