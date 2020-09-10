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
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;
import grakn.protocol.ConceptProto.RoleType.GetPlayers;
import grakn.protocol.ConceptProto.RoleType.GetRelation;
import grakn.protocol.ConceptProto.RoleType.GetRelations;
import grakn.protocol.ConceptProto.TypeMethod;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class RoleTypeImpl {

    public static class Local extends TypeImpl.Local implements RoleType.Local {

        public Local(String label, String scope, boolean root) {
            super(label, scope, root);
        }

        public static RoleTypeImpl.Local of(ConceptProto.Type typeProto) {
            return new RoleTypeImpl.Local(typeProto.getLabel(), typeProto.getScope(), typeProto.getRoot());
        }

        @Override
        public final String getScope() {
            return scope;
        }

        @Override
        public RoleTypeImpl.Remote asRemote(Grakn.Transaction transaction) {
            return new RoleTypeImpl.Remote(transaction, getLabel(), getScope(), isRoot());
        }
    }

    public static class Remote extends TypeImpl.Remote implements RoleType.Remote {

        private final String scope;

        public Remote(final Grakn.Transaction transaction, final String label,
                      final String scope, final boolean isRoot) {
            super(transaction, label, scope, isRoot);
            this.scope = scope;
        }

        public static RoleTypeImpl.Remote of(final Grakn.Transaction transaction, final ConceptProto.Type proto) {
            return new RoleTypeImpl.Remote(transaction, proto.getLabel(), proto.getScope(), proto.getRoot());
        }

        @Nullable
        @Override
        public RoleType.Local getSupertype() {
            return getSupertypeExecute(Type.Local::asRoleType);
        }

        @Override
        public final Stream<RoleType.Local> getSupertypes() {
            return super.getSupertypes(Type.Local::asRoleType);
        }

        @Override
        public final Stream<RoleType.Local> getSubtypes() {
            return super.getSubtypes(Type.Local::asRoleType);
        }

        @Override
        public String getScope() {
            return scope;
        }

        @Override
        public RoleType.Remote asRemote(Grakn.Transaction transaction) {
            return new RoleTypeImpl.Remote(transaction, label, scope, isRoot);
        }

        @Override
        public final RelationType.Local getRelation() {
            final TypeMethod.Req method = TypeMethod.Req.newBuilder()
                    .setRoleTypeGetRelationReq(GetRelation.Req.getDefaultInstance()).build();
            final GetRelation.Res response = execute(method).getRoleTypeGetRelationRes();
            return TypeImpl.Local.of(response.getRelationType()).asRelationType();
        }

        @Override
        public final Stream<RelationType.Local> getRelations() {
            return stream(
                    TypeMethod.Iter.Req.newBuilder().setRoleTypeGetRelationsIterReq(
                            GetRelations.Iter.Req.getDefaultInstance()).build(),
                    res -> res.getRoleTypeGetRelationsIterRes().getRelationType()
            ).map(Type.Local::asRelationType);
        }

        @Override
        public final Stream<ThingType.Local> getPlayers() {
            return stream(
                    TypeMethod.Iter.Req.newBuilder().setRoleTypeGetPlayersIterReq(
                            GetPlayers.Iter.Req.getDefaultInstance()).build(),
                    res -> res.getRoleTypeGetPlayersIterRes().getThingType()
            ).map(Type.Local::asThingType);
        }
    }
}
