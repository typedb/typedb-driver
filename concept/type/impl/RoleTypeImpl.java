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

import grakn.client.Grakn;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.stream.Stream;

public class RoleTypeImpl {

    public static class Local extends TypeImpl.Local implements RoleType.Local {

        private final String scopedLabel;

        public Local(ConceptProto.Type type) {
            super(type.getLabel(), type.getScope() != null && !type.getScope().isEmpty() ? type.getScope() : null, type.getRoot());
            scopedLabel = type.getScope();
        }

        @Override
        public final String getScope() {
            return scopedLabel;
        }

        @Override
        public RoleTypeImpl.Remote asRemote(Grakn.Transaction transaction) {
            return new RoleTypeImpl.Remote(transaction, getLabel(), getScope(), isRoot());
        }
    }

    public static class Remote extends TypeImpl.Remote implements RoleType.Remote {

        private final String scope;

        public Remote(final Grakn.Transaction transaction, final String label, final String scope, final boolean isRoot) {
            super(transaction, label, scope, isRoot);
            this.scope = scope;
        }

        public static RoleTypeImpl.Remote of(final Grakn.Transaction transaction, final ConceptProto.Type proto) {
            return new RoleTypeImpl.Remote(transaction, proto.getLabel(), proto.getScope(), proto.getRoot());
        }

        @Nullable
        @Override
        public Type.Remote getSupertype() {
            return getSupertypeInternal(Type.Remote::asRoleType);
        }

        @Override
        public final Stream<RoleType.Remote> getSupertypes() {
            return super.getSupertypes().map(Type.Remote::asRoleType);
        }

        @Override
        public final Stream<RoleType.Remote> getSubtypes() {
            return super.getSubtypes().map(Type.Remote::asRoleType);
        }

        @Override
        public void setSupertype(RoleType superRole) {
            setSupertypeInternal(superRole);
        }

        @Override
        public String getScope() {
            return scope;
        }

        @Override
        public final RelationType.Remote getRelation() {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setRoleTypeGetRelationReq(ConceptProto.RoleType.GetRelation.Req.getDefaultInstance()).build();

            final ConceptProto.RoleType.GetRelation.Res response = runMethod(method).getRoleTypeGetRelationRes();

            return TypeImpl.Remote.of(tx(), response.getRelationType()).asRelationType();
        }

        @Override
        public final Stream<RelationType.Remote> getRelations() {
            ConceptProto.TypeMethod.Iter.Req method = ConceptProto.TypeMethod.Iter.Req.newBuilder()
                    .setRoleTypeGetRelationsIterReq(ConceptProto.RoleType.GetRelations.Iter.Req.getDefaultInstance()).build();
            return typeStream(method, res -> res.getRoleTypeGetRelationsIterRes().getRelationType()).map(Type.Remote::asRelationType);
        }

        @Override
        public final Stream<ThingType.Remote> getPlayers() {
            ConceptProto.TypeMethod.Iter.Req method = ConceptProto.TypeMethod.Iter.Req.newBuilder()
                    .setRoleTypeGetPlayersIterReq(ConceptProto.RoleType.GetPlayers.Iter.Req.getDefaultInstance()).build();
            return typeStream(method, res -> res.getRoleTypeGetPlayersIterRes().getThingType()).map(Type.Remote::asThingType);
        }

        @Override
        protected Stream<Thing.Remote> thingStream(ConceptProto.TypeMethod.Iter.Req request, Function<ConceptProto.TypeMethod.Iter.Res, ConceptProto.Thing> thingGetter) {
            return tx().concepts().iterateTypeMethod(getLabel(), getScope(), request, response -> ThingImpl.Remote.of(tx(), thingGetter.apply(response)));
        }

        @Override
        protected Stream<Type.Remote> typeStream(ConceptProto.TypeMethod.Iter.Req request, Function<ConceptProto.TypeMethod.Iter.Res, ConceptProto.Type> typeGetter) {
            return tx().concepts().iterateTypeMethod(getLabel(), getScope(), request, response -> TypeImpl.Remote.of(tx(), typeGetter.apply(response)));
        }

        @Override
        protected ConceptProto.TypeMethod.Res runMethod(ConceptProto.TypeMethod.Req typeMethod) {
            return tx().concepts().runTypeMethod(getLabel(), getScope(), typeMethod).getConceptMethodTypeRes().getResponse();
        }
    }
}
