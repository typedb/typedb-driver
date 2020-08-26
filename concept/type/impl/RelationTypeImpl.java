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

import grakn.client.concept.Concepts;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;

import java.util.stream.Stream;

public class RelationTypeImpl {
    /**
     * Client implementation of RelationType
     */
    public static class Local extends ThingTypeImpl.Local implements RelationType.Local {

        public Local(final ConceptProto.Type type) {
            super(type);
        }
    }

    /**
     * Client implementation of RelationType
     */
    public static class Remote extends ThingTypeImpl.Remote implements RelationType.Remote {

        public Remote(final Concepts concepts, final String label, final boolean isRoot) {
            super(concepts, label, isRoot);
        }

        @Override
        public final Stream<Relation.Remote> getInstances() {
            return super.getInstances().map(Thing.Remote::asRelation);
        }

        @Override
        public RelationType.Remote getSupertype() {
            return getSupertypeInternal(Type.Remote::asRelationType);
        }

        @Override
        public final Stream<RelationType.Remote> getSupertypes() {
            return super.getSupertypes().map(ThingType.Remote::asRelationType);
        }

        @Override
        public final Stream<RelationType.Remote> getSubtypes() {
            return super.getSubtypes().map(ThingType.Remote::asRelationType);
        }

        @Override
        public final void setSupertype(final RelationType type) {
            setSupertypeInternal(type);
        }

        @Override
        public final Relation.Remote create() {
            ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setRelationTypeCreateReq(ConceptProto.RelationType.Create.Req.getDefaultInstance()).build();

            return Thing.Remote.of(concepts(), runMethod(method).getRelationTypeCreateRes().getRelation()).asRelation();
        }

        @Override
        public final RoleType.Remote getRelates(final String roleLabel) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setRelationTypeGetRelatesForRoleLabelReq(ConceptProto.RelationType.GetRelatesForRoleLabel.Req.newBuilder().setLabel(roleLabel)).build();

            final ConceptProto.RelationType.GetRelatesForRoleLabel.Res res = runMethod(method).getRelationTypeGetRelatesForRoleLabelRes();
            if (res.hasRoleType()) {
                return Type.Remote.of(concepts(), res.getRoleType()).asRoleType();
            } else {
                return null;
            }
        }

        @Override
        public final Stream<RoleType.Remote> getRelates() {
            final ConceptProto.TypeMethod.Iter.Req method = ConceptProto.TypeMethod.Iter.Req.newBuilder()
                    .setRelationTypeGetRelatesIterReq(ConceptProto.RelationType.GetRelates.Iter.Req.getDefaultInstance()).build();

            return typeStream(method, res -> res.getRelationTypeGetRelatesIterRes().getRole()).map(Type.Remote::asRoleType);
        }

        @Override
        public final void setRelates(final String roleLabel) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setRelationTypeSetRelatesReq(ConceptProto.RelationType.SetRelates.Req.newBuilder()
                            .setLabel(roleLabel)).build();
            runMethod(method);
        }

        @Override
        public final void setRelates(final String roleLabel, final String overriddenLabel) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setRelationTypeSetRelatesReq(ConceptProto.RelationType.SetRelates.Req.newBuilder()
                            .setLabel(roleLabel)
                            .setOverriddenLabel(overriddenLabel)).build();
            runMethod(method);
        }

        @Override
        public final void unsetRelates(String roleLabel) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setRelationTypeUnsetRelatesReq(ConceptProto.RelationType.UnsetRelates.Req.newBuilder()
                            .setLabel(roleLabel)).build();
            runMethod(method);
        }
    }
}
