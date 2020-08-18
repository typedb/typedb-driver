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

package grakn.client.concept.thing.impl;

import grakn.client.Grakn.Transaction;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.ThingType;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.ConceptProto;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public abstract class ThingImpl {

    /**
     * Client implementation of Thing
     */
    public abstract static class Local implements Thing.Local {

        private final ConceptIID iid;
        private final ThingType.Local type;
        private final boolean inferred;

        protected Local(ConceptProto.Concept concept) {
            this.iid = ConceptIID.of(concept.getIid());
            this.type = Concept.Local.of(concept.getTypeRes().getThingType()).asType().asThingType();
            this.inferred = concept.getInferredRes().getInferred();
        }

        @Override
        public ConceptIID getIID() {
            return iid;
        }

        @Override
        public ThingType.Local getType() {
            return type;
        }

        @Override
        public final boolean isInferred() {
            return inferred;
        }
    }

    /**
     * Client implementation of Thing
     */
    public abstract static class Remote implements Thing.Remote {

        private final Transaction tx;
        private final ConceptIID iid;

        protected Remote(Transaction tx, ConceptIID iid) {
            this.tx = requireNonNull(tx, "Null tx");
            if (iid == null || iid.getValue().isEmpty()) {
                throw new IllegalArgumentException("Null or empty iid");
            }
            this.iid = iid;
        }

        @Override
        public ConceptIID getIID() {
            return iid;
        }

        @Override
        public ThingType.Remote getType() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingGetTypeReq(ConceptProto.Thing.GetType.Req.getDefaultInstance()).build();

            return Concept.Remote.of(tx(), runMethod(method).getThingGetTypeRes().getThingType()).asType().asThingType();
        }

        @Override
        public final boolean isInferred() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingIsInferredReq(ConceptProto.Thing.IsInferred.Req.getDefaultInstance()).build();

            return runMethod(method).getThingIsInferredRes().getInferred();
        }

        @Override
        public final Stream<Attribute.Remote> getHas(AttributeType... attributeTypes) {
            final ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setThingGetHasIterReq(ConceptProto.Thing.GetHas.Iter.Req.newBuilder()
                                                   .addAllAttributeTypes(RequestBuilder.ConceptMessage.concepts(Arrays.asList(attributeTypes)))).build();

            return conceptStream(method, res -> res.getThingGetHasIterRes().getAttribute()).map(x -> x.asThing().asAttribute());
        }

        @Override
        public Stream<? extends Attribute.Boolean.Remote> getHas(AttributeType.Boolean attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Remote::asBoolean);
        }

        @Override
        public Stream<? extends Attribute.Long.Remote> getHas(AttributeType.Long attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Remote::asLong);
        }

        @Override
        public Stream<? extends Attribute.Double.Remote> getHas(AttributeType.Double attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Remote::asDouble);
        }

        @Override
        public Stream<? extends Attribute.String.Remote> getHas(AttributeType.String attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Remote::asString);
        }

        @Override
        public Stream<? extends Attribute.DateTime.Remote> getHas(AttributeType.DateTime attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Remote::asDateTime);
        }

        @Override
        public final Stream<Attribute.Remote> getHas(boolean onlyKey) {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setThingGetHasIterReq(ConceptProto.Thing.GetHas.Iter.Req.newBuilder().setKeysOnly(onlyKey)).build();
            return conceptStream(method, res -> res.getThingGetHasIterRes().getAttribute()).map(x -> x.asThing().asAttribute());
        }

        @Override
        public final Stream<RoleType.Remote> getPlays() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setThingGetPlaysIterReq(ConceptProto.Thing.GetPlays.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getThingGetPlaysIterRes().getRoleType()).map(x -> x.asType().asRoleType());
        }

        @Override
        public Stream<? extends Relation> getRelations(RoleType... roleTypes) {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setThingGetRelationsIterReq(ConceptProto.Thing.GetRelations.Iter.Req.newBuilder()
                                                  .addAllRoleTypes(RequestBuilder.ConceptMessage.concepts(Arrays.asList(roleTypes)))).build();

            return conceptStream(method, res -> res.getThingGetRelationsIterRes().getRelation()).map(x -> x.asThing().asRelation());
        }

        @Override
        public void setHas(Attribute attribute) {
            // TODO: replace usage of this method as a getter, with relations(Attribute attribute)
            // TODO: then remove this method altogether and just use has(Attribute attribute)
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingSetHasReq(ConceptProto.Thing.SetHas.Req.newBuilder()
                                               .setAttribute(RequestBuilder.ConceptMessage.from(attribute))).build();
            runMethod(method);
        }

        @Override
        public void unsetHas(Attribute attribute) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingUnsetHasReq(ConceptProto.Thing.UnsetHas.Req.newBuilder()
                                              .setAttribute(RequestBuilder.ConceptMessage.from(attribute))).build();
            runMethod(method);
        }

        @Override
        public final void delete() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setConceptDeleteReq(ConceptProto.Concept.Delete.Req.getDefaultInstance())
                    .build();

            runMethod(method);
        }

        @Override
        public final boolean isDeleted() {
            return tx().getConcept(getIID()) == null;
        }

        protected Transaction tx() {
            return tx;
        }

        protected Stream<Concept.Remote> conceptStream
                (ConceptProto.Method.Iter.Req request, Function<ConceptProto.Method.Iter.Res, ConceptProto.Concept> conceptGetter) {
            return tx.iterateConceptMethod(iid, request, response -> Concept.Remote.of(tx, conceptGetter.apply(response)));
        }

        protected ConceptProto.Method.Res runMethod(ConceptProto.Method.Req method) {
            return runMethod(getIID(), method);
        }

        private ConceptProto.Method.Res runMethod(ConceptIID iid, ConceptProto.Method.Req method) {
            return tx().runConceptMethod(iid, method).getConceptMethodRes().getResponse();
        }
    }
}
