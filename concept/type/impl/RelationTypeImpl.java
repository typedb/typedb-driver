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
import grakn.client.concept.thing.Relation;
import grakn.client.concept.thing.impl.RelationImpl;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;
import grakn.protocol.ConceptProto.RelationType.GetRelates;
import grakn.protocol.ConceptProto.RelationType.GetRelatesForRoleLabel;
import grakn.protocol.ConceptProto.RelationType.SetRelates;
import grakn.protocol.ConceptProto.RelationType.UnsetRelates;
import grakn.protocol.ConceptProto.TypeMethod;

import java.util.stream.Stream;

public class RelationTypeImpl {

    public static class Local extends ThingTypeImpl.Local implements RelationType.Local {

        public Local(final String label, final boolean isRoot) {
            super(label, isRoot);
        }

        public static RelationTypeImpl.Local of(ConceptProto.Type typeProto) {
            return new RelationTypeImpl.Local(typeProto.getLabel(), typeProto.getRoot());
        }

        @Override
        public RelationTypeImpl.Remote asRemote(final Grakn.Transaction transaction) {
            return new RelationTypeImpl.Remote(transaction, getLabel(), isRoot());
        }
    }

    public static class Remote extends ThingTypeImpl.Remote implements RelationType.Remote {

        public Remote(final Grakn.Transaction transaction, final String label, final boolean isRoot) {
            super(transaction, label, isRoot);
        }

        public static RelationTypeImpl.Remote of(final Grakn.Transaction transaction, final ConceptProto.Type proto) {
            return new RelationTypeImpl.Remote(transaction, proto.getLabel(), proto.getRoot());
        }

        @Override
        public final Stream<Relation.Local> getInstances() {
            return super.getInstances(RelationImpl.Local::of);
        }

        @Override
        public RelationType.Remote asRemote(Grakn.Transaction transaction) {
            return new RelationTypeImpl.Remote(transaction, label, isRoot);
        }

        @Override
        public RelationType.Local getSupertype() {
            return super.getSupertypeExecute(Type.Local::asRelationType);
        }

        @Override
        public final Stream<RelationType.Local> getSupertypes() {
            return super.getSupertypes(Type.Local::asRelationType);
        }

        @Override
        public final Stream<RelationType.Local> getSubtypes() {
            return super.getSubtypes(Type.Local::asRelationType);
        }

        @Override
        public final void setSupertype(final RelationType type) {
            this.setSupertypeExecute(type);
        }

        @Override
        public final Relation.Local create() {
            TypeMethod.Req method = TypeMethod.Req.newBuilder().setRelationTypeCreateReq(
                    ConceptProto.RelationType.Create.Req.getDefaultInstance()).build();
            return ThingImpl.Local.of(execute(method).getRelationTypeCreateRes().getRelation()).asRelation();
        }

        @Override
        public final RoleType.Local getRelates(final String roleLabel) {
            final TypeMethod.Req method = TypeMethod.Req.newBuilder().setRelationTypeGetRelatesForRoleLabelReq(
                    GetRelatesForRoleLabel.Req.newBuilder().setLabel(roleLabel)).build();
            final GetRelatesForRoleLabel.Res res = execute(method).getRelationTypeGetRelatesForRoleLabelRes();
            if (res.hasRoleType()) return TypeImpl.Local.of(res.getRoleType()).asRoleType();
            else return null;
        }

        @Override
        public final Stream<RoleType.Local> getRelates() {
            return stream(
                    TypeMethod.Iter.Req.newBuilder().setRelationTypeGetRelatesIterReq(
                            GetRelates.Iter.Req.getDefaultInstance()).build(),
                    res -> res.getRelationTypeGetRelatesIterRes().getRole()
            ).map(Type.Local::asRoleType);
        }

        @Override
        public final void setRelates(final String roleLabel) {
            execute(TypeMethod.Req.newBuilder().setRelationTypeSetRelatesReq(
                    SetRelates.Req.newBuilder().setLabel(roleLabel)
            ).build());
        }

        @Override
        public final void setRelates(final String roleLabel, final String overriddenLabel) {
            execute(TypeMethod.Req.newBuilder().setRelationTypeSetRelatesReq(
                    SetRelates.Req.newBuilder().setLabel(roleLabel).setOverriddenLabel(overriddenLabel)
            ).build());
        }

        @Override
        public final void unsetRelates(String roleLabel) {
            execute(TypeMethod.Req.newBuilder().setRelationTypeUnsetRelatesReq(
                    UnsetRelates.Req.newBuilder().setLabel(roleLabel)
            ).build());
        }
    }
}
