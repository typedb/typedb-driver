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

import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.Concepts;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.ClientInternal.ILLEGAL_ARGUMENT_NULL;
import static grakn.client.common.exception.ErrorMessage.ClientInternal.ILLEGAL_ARGUMENT_NULL_OR_EMPTY;
import static grakn.client.concept.ConceptMessageWriter.thing;
import static grakn.client.concept.ConceptMessageWriter.types;

public abstract class ThingImpl {

    /**
     * Client implementation of Thing
     */
    public abstract static class Local implements Thing.Local {

        private final String iid;
        private final ThingType.Local type;

        protected Local(final ConceptProto.Thing thing) {
            // TODO we (probably) do need the Type, but we should have a better way of retrieving it.
            // In 1.8 it was in a "pre-filled response" in ConceptProto.Concept, which was highly confusing as it was
            // not actually prefilled when using the Concept API - only when using the Query API.
            // We should probably create a dedicated Proto class for Graql (AnswerProto or QueryProto) and keep the code clean.
            throw new GraknClientException(new UnsupportedOperationException());
            //this.iid = thing.getIid();
            //this.type = Type.Local.of(thing.getType()).asThingType();
            //this.inferred = thing.getInferredRes().getInferred();
        }

        @Override
        public String getIID() {
            return iid;
        }

        @Override
        public ThingType.Local getType() {
            return type;
        }
    }

    /**
     * Client implementation of Thing
     */
    public abstract static class Remote implements Thing.Remote {

        private final Concepts concepts;
        private final String iid;

        protected Remote(final Concepts concepts, final String iid) {
            if (concepts == null) {
                throw new GraknClientException(ILLEGAL_ARGUMENT_NULL.message("concepts"));
            }
            this.concepts = concepts;
            if (iid == null || iid.isEmpty()) {
                throw new GraknClientException(ILLEGAL_ARGUMENT_NULL_OR_EMPTY.message("iid"));
            }
            this.iid = iid;
        }

        @Override
        public String getIID() {
            return iid;
        }

        @Override
        public ThingType.Remote getType() {
            final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                    .setThingGetTypeReq(ConceptProto.Thing.GetType.Req.getDefaultInstance()).build();

            return Type.Remote.of(concepts, runMethod(method).getThingGetTypeRes().getThingType()).asThingType();
        }

        @Override
        public final boolean isInferred() {
            final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                    .setThingIsInferredReq(ConceptProto.Thing.IsInferred.Req.getDefaultInstance()).build();

            return runMethod(method).getThingIsInferredRes().getInferred();
        }

        @Override
        public final Stream<? extends Attribute.Remote<?>> getHas(AttributeType... attributeTypes) {
            final ConceptProto.ThingMethod.Iter.Req method = ConceptProto.ThingMethod.Iter.Req.newBuilder()
                    .setThingGetHasIterReq(ConceptProto.Thing.GetHas.Iter.Req.newBuilder()
                            .addAllAttributeTypes(types(Arrays.asList(attributeTypes)))).build();
            return thingStream(method, res -> res.getThingGetHasIterRes().getAttribute()).map(Thing.Remote::asAttribute);
        }

        @Override
        public final Stream<? extends Attribute.Boolean.Remote> getHas(AttributeType.Boolean attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Remote::asBoolean);
        }

        @Override
        public final Stream<? extends Attribute.Long.Remote> getHas(AttributeType.Long attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Remote::asLong);
        }

        @Override
        public final Stream<? extends Attribute.Double.Remote> getHas(AttributeType.Double attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Remote::asDouble);
        }

        @Override
        public final Stream<? extends Attribute.String.Remote> getHas(AttributeType.String attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Remote::asString);
        }

        @Override
        public final Stream<? extends Attribute.DateTime.Remote> getHas(AttributeType.DateTime attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Remote::asDateTime);
        }

        @Override
        public final Stream<? extends Attribute.Remote<?>> getHas(boolean onlyKey) {
            final ConceptProto.ThingMethod.Iter.Req method = ConceptProto.ThingMethod.Iter.Req.newBuilder()
                    .setThingGetHasIterReq(ConceptProto.Thing.GetHas.Iter.Req.newBuilder().setKeysOnly(onlyKey)).build();
            return thingStream(method, res -> res.getThingGetHasIterRes().getAttribute()).map(Thing.Remote::asAttribute);
        }

        @Override
        public final Stream<RoleType.Remote> getPlays() {
            final ConceptProto.ThingMethod.Iter.Req method = ConceptProto.ThingMethod.Iter.Req.newBuilder()
                    .setThingGetPlaysIterReq(ConceptProto.Thing.GetPlays.Iter.Req.getDefaultInstance()).build();
            return typeStream(method, res -> res.getThingGetPlaysIterRes().getRoleType()).map(Type.Remote::asRoleType);
        }

        @Override
        public final Stream<? extends Relation> getRelations(RoleType... roleTypes) {
            final ConceptProto.ThingMethod.Iter.Req method = ConceptProto.ThingMethod.Iter.Req.newBuilder()
                    .setThingGetRelationsIterReq(ConceptProto.Thing.GetRelations.Iter.Req.newBuilder()
                            .addAllRoleTypes(types(Arrays.asList(roleTypes)))).build();
            return thingStream(method, res -> res.getThingGetRelationsIterRes().getRelation()).map(Thing.Remote::asRelation);
        }

        @Override
        public final void setHas(Attribute<?> attribute) {
            final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                    .setThingSetHasReq(ConceptProto.Thing.SetHas.Req.newBuilder()
                            .setAttribute(thing(attribute))).build();
            runMethod(method);
        }

        @Override
        public final void unsetHas(Attribute<?> attribute) {
            final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                    .setThingUnsetHasReq(ConceptProto.Thing.UnsetHas.Req.newBuilder()
                            .setAttribute(thing(attribute))).build();
            runMethod(method);
        }

        @Override
        public final void delete() {
            final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                    .setThingDeleteReq(ConceptProto.Thing.Delete.Req.getDefaultInstance())
                    .build();
            runMethod(method);
        }

        @Override
        public final boolean isDeleted() {
            return concepts.getThing(getIID()) == null;
        }

        @Override
        public String toString() {
            return this.getClass().getCanonicalName() + "{concepts=" + concepts + ", iid=" + iid + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ThingImpl.Remote that = (ThingImpl.Remote) o;

            return this.concepts.equals(that.concepts) &&
                    this.iid.equals(that.iid);
        }

        @Override
        public int hashCode() {
            int h = 1;
            h *= 1000003;
            h ^= concepts.hashCode();
            h *= 1000003;
            h ^= iid.hashCode();
            return h;
        }

        protected final Concepts concepts() {
            return concepts;
        }

        protected Stream<Thing.Remote> thingStream(final ConceptProto.ThingMethod.Iter.Req request, final Function<ConceptProto.ThingMethod.Iter.Res, ConceptProto.Thing> thingGetter) {
            return concepts.iterateThingMethod(iid, request, response -> Thing.Remote.of(concepts, thingGetter.apply(response)));
        }

        protected Stream<Type.Remote> typeStream(final ConceptProto.ThingMethod.Iter.Req request, final Function<ConceptProto.ThingMethod.Iter.Res, ConceptProto.Type> typeGetter) {
            return concepts.iterateThingMethod(iid, request, response -> Type.Remote.of(concepts, typeGetter.apply(response)));
        }

        protected ConceptProto.ThingMethod.Res runMethod(final ConceptProto.ThingMethod.Req method) {
            return concepts.runThingMethod(iid, method).getConceptMethodThingRes().getResponse();
        }
    }
}
