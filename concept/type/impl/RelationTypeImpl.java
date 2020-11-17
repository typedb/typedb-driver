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
import grakn.client.concept.thing.impl.RelationImpl;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.RelationType;
import grakn.protocol.ConceptProto;
import grakn.protocol.ConceptProto.RelationType.GetRelates;
import grakn.protocol.ConceptProto.RelationType.GetRelatesForRoleLabel;
import grakn.protocol.ConceptProto.RelationType.SetRelates;
import grakn.protocol.ConceptProto.RelationType.UnsetRelates;

import java.util.stream.Stream;

public class RelationTypeImpl extends ThingTypeImpl implements RelationType {

    public RelationTypeImpl(final String label, final boolean isRoot) {
        super(label, isRoot);
    }

    public static RelationTypeImpl of(final ConceptProto.Type typeProto) {
        return new RelationTypeImpl(typeProto.getLabel(), typeProto.getRoot());
    }

    @Override
    public RelationTypeImpl.Remote asRemote(final Grakn.Transaction transaction) {
        return new RelationTypeImpl.Remote(transaction, getLabel(), isRoot());
    }

    @Override
    public RelationTypeImpl asRelationType() {
        return this;
    }

    public static class Remote extends ThingTypeImpl.Remote implements RelationType.Remote {

        public Remote(final Grakn.Transaction transaction, final String label, final boolean isRoot) {
            super(transaction, label, isRoot);
        }

        public static RelationTypeImpl.Remote of(final Grakn.Transaction transaction, final ConceptProto.Type proto) {
            return new RelationTypeImpl.Remote(transaction, proto.getLabel(), proto.getRoot());
        }

        @Override
        public final Stream<RelationImpl> getInstances() {
            return super.getInstances(ThingImpl::asRelation);
        }

        @Override
        public RelationTypeImpl.Remote asRemote(Grakn.Transaction transaction) {
            return new RelationTypeImpl.Remote(transaction, getLabel(), isRoot());
        }

        @Override
        public RelationTypeImpl getSupertype() {
            return super.getSupertypeExecute(TypeImpl::asRelationType);
        }

        @Override
        public final Stream<RelationTypeImpl> getSupertypes() {
            return super.getSupertypes(TypeImpl::asRelationType);
        }

        @Override
        public final Stream<RelationTypeImpl> getSubtypes() {
            return super.getSubtypes(TypeImpl::asRelationType);
        }

        @Override
        public final void setSupertype(final RelationType type) {
            this.setSupertypeExecute(type);
        }

        @Override
        public final RelationImpl create() {
            final ConceptProto.Type.Req.Builder method = ConceptProto.Type.Req.newBuilder().setRelationTypeCreateReq(
                    ConceptProto.RelationType.Create.Req.getDefaultInstance());
            return ThingImpl.of(execute(method).getRelationTypeCreateRes().getRelation()).asRelation();
        }

        @Override
        public final RoleTypeImpl getRelates(final String roleLabel) {
            final ConceptProto.Type.Req.Builder method = ConceptProto.Type.Req.newBuilder().setRelationTypeGetRelatesForRoleLabelReq(
                    GetRelatesForRoleLabel.Req.newBuilder().setLabel(roleLabel));
            final GetRelatesForRoleLabel.Res res = execute(method).getRelationTypeGetRelatesForRoleLabelRes();
            if (res.hasRoleType()) return TypeImpl.of(res.getRoleType()).asRoleType();
            else return null;
        }

        @Override
        public final Stream<RoleTypeImpl> getRelates() {
            return stream(
                    ConceptProto.Type.Req.newBuilder().setRelationTypeGetRelatesReq(
                            GetRelates.Req.getDefaultInstance()),
                    res -> res.getRelationTypeGetRelatesRes().getRole()
            ).map(TypeImpl::asRoleType);
        }

        @Override
        public final void setRelates(final String roleLabel) {
            execute(ConceptProto.Type.Req.newBuilder().setRelationTypeSetRelatesReq(
                    SetRelates.Req.newBuilder().setLabel(roleLabel)));
        }

        @Override
        public final void setRelates(final String roleLabel, final String overriddenLabel) {
            execute(ConceptProto.Type.Req.newBuilder().setRelationTypeSetRelatesReq(
                    SetRelates.Req.newBuilder().setLabel(roleLabel).setOverriddenLabel(overriddenLabel)));
        }

        @Override
        public final void unsetRelates(String roleLabel) {
            execute(ConceptProto.Type.Req.newBuilder().setRelationTypeUnsetRelatesReq(
                    UnsetRelates.Req.newBuilder().setLabel(roleLabel)));
        }

        @Override
        public RelationTypeImpl.Remote asRelationType() {
            return this;
        }
    }
}
