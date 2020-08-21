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
import grakn.client.common.exception.GraknClientException;
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

        protected Local(ConceptProto.Thing thing) {
            // TODO we (probably) do need the Type, but we should have a better way of retrieving it.
            // In 1.8 it was in a "pre-filled response" in ConceptProto.Concept, which was highly confusing as it was
            // not actually prefilled when using the Concept API.
            // We should probably create a dedicated Proto class for Graql and keep the code clean.
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

        private final Transaction tx;
        private final String iid;

        protected Remote(final Transaction tx, final String iid) {
            if (tx == null) {
                throw new GraknClientException(ILLEGAL_ARGUMENT_NULL.message("tx"));
            }
            this.tx = tx;
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

            return Type.Remote.of(tx(), runMethod(method).getThingGetTypeRes().getThingType()).asThingType();
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
            return tx().getThing(getIID()) == null;
        }

        @Override
        public String toString() {
            return this.getClass().getCanonicalName() + "{tx=" + tx + ", iid=" + iid + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ThingImpl.Remote that = (ThingImpl.Remote) o;

            return this.tx.equals(that.tx) &&
                    this.iid.equals(that.iid);
        }

        @Override
        public int hashCode() {
            int h = 1;
            h *= 1000003;
            h ^= tx.hashCode();
            h *= 1000003;
            h ^= iid.hashCode();
            return h;
        }

        protected Transaction tx() {
            return tx;
        }

        protected Stream<Thing.Remote> thingStream(ConceptProto.ThingMethod.Iter.Req request, Function<ConceptProto.ThingMethod.Iter.Res, ConceptProto.Thing> thingGetter) {
            return tx.iterateConceptMethod(iid, request, response -> Thing.Remote.of(tx, thingGetter.apply(response)));
        }

        protected Stream<Type.Remote> typeStream(ConceptProto.ThingMethod.Iter.Req request, Function<ConceptProto.ThingMethod.Iter.Res, ConceptProto.Type> typeGetter) {
            return tx.iterateConceptMethod(iid, request, response -> Type.Remote.of(tx, typeGetter.apply(response)));
        }

        protected ConceptProto.ThingMethod.Res runMethod(ConceptProto.ThingMethod.Req method) {
            return tx().runConceptMethod(iid, method).getConceptMethodThingRes().getResponse();
        }
    }
}
