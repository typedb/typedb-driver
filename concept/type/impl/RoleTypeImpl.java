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
import grakn.client.concept.type.RoleType;
import grakn.protocol.ConceptProto;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.common.util.Objects.className;

public class RoleTypeImpl extends TypeImpl implements RoleType {

    private final String scope;
    private final int hash;

    public RoleTypeImpl(final String label, final String scope, final boolean root) {
        super(label, root);
        this.scope = scope;
        this.hash = Objects.hash(this.scope, label);
    }

    public static RoleTypeImpl of(final ConceptProto.Type typeProto) {
        return new RoleTypeImpl(typeProto.getLabel(), typeProto.getScope(), typeProto.getRoot());
    }

    @Override
    public final String getScope() {
        return scope;
    }

    @Override
    public RoleTypeImpl.Remote asRemote(Grakn.Transaction transaction) {
        return new RoleTypeImpl.Remote(transaction, getLabel(), getScope(), isRoot());
    }

    @Override
    public RoleTypeImpl asRoleType() {
        return this;
    }

    @Override
    public String toString() {
        return className(this.getClass()) + "[label: " + (scope != null ? scope + ":" : "") + getLabel() + "]";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final RoleTypeImpl that = (RoleTypeImpl) o;
        return (this.getLabel().equals(that.getLabel()) && Objects.equals(this.scope, that.scope));
    }

    @Override
    public int hashCode() {
        return hash;
    }

    public static class Remote extends TypeImpl.Remote implements RoleType.Remote {

        private final String scope;
        private final int hash;

        public Remote(final Grakn.Transaction transaction, final String label,
                      final String scope, final boolean isRoot) {
            super(transaction, label, isRoot);
            this.scope = scope;
            this.hash = Objects.hash(transaction, label, scope);
        }

        public static RoleTypeImpl.Remote of(final Grakn.Transaction transaction, final ConceptProto.Type proto) {
            return new RoleTypeImpl.Remote(transaction, proto.getLabel(), proto.getScope(), proto.getRoot());
        }

        @Nullable
        @Override
        public RoleTypeImpl getSupertype() {
            return getSupertypeExecute(TypeImpl::asRoleType);
        }

        @Override
        public final Stream<RoleTypeImpl> getSupertypes() {
            return super.getSupertypes(TypeImpl::asRoleType);
        }

        @Override
        public final Stream<RoleTypeImpl> getSubtypes() {
            return super.getSubtypes(TypeImpl::asRoleType);
        }

        @Override
        public final String getScope() {
            return scope;
        }

        @Override
        public RoleType.Remote asRemote(final Grakn.Transaction transaction) {
            return new RoleTypeImpl.Remote(transaction, getLabel(), getScope(), isRoot());
        }

        @Override
        public final RelationTypeImpl getRelation() {
            final ConceptProto.Type.Req.Builder method = ConceptProto.Type.Req.newBuilder()
                    .setRoleTypeGetRelationTypeReq(ConceptProto.RoleType.GetRelationType.Req.getDefaultInstance());
            final ConceptProto.RoleType.GetRelationType.Res response = execute(method).getRoleTypeGetRelationTypeRes();
            return TypeImpl.of(response.getRelationType()).asRelationType();
        }

        @Override
        public final Stream<RelationTypeImpl> getRelations() {
            return stream(
                    ConceptProto.Type.Req.newBuilder().setRoleTypeGetRelationTypesReq(
                            ConceptProto.RoleType.GetRelationTypes.Req.getDefaultInstance()),
                    res -> res.getRoleTypeGetRelationTypesRes().getRelationType()
            ).map(TypeImpl::asRelationType);
        }

        @Override
        public final Stream<ThingTypeImpl> getPlayers() {
            return stream(
                    ConceptProto.Type.Req.newBuilder().setRoleTypeGetPlayersReq(
                            ConceptProto.RoleType.GetPlayers.Req.getDefaultInstance()),
                    res -> res.getRoleTypeGetPlayersRes().getThingType()
            ).map(TypeImpl::asThingType);
        }

        @Override
        public RoleTypeImpl.Remote asRoleType() {
            return this;
        }

        @Override
        Stream<TypeImpl> stream(final ConceptProto.Type.Req.Builder method, final Function<ConceptProto.Type.Res, ConceptProto.Type> typeGetter) {
            return super.stream(method.setScope(scope), typeGetter);
        }

        @Override
        ConceptProto.Type.Res execute(final ConceptProto.Type.Req.Builder method) {
            return super.execute(method.setScope(scope));
        }

        @Override
        public String toString() {
            return className(this.getClass()) + "[label: " + scope + ":" + getLabel() + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final RoleTypeImpl.Remote that = (RoleTypeImpl.Remote) o;
            return (this.tx().equals(that.tx()) &&
                    this.getLabel().equals(that.getLabel()) &&
                    Objects.equals(this.getScope(), that.getScope()));
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
