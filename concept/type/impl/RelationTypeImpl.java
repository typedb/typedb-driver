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

import grakn.client.Grakn.Transaction;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.Label;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.RoleType;
import grakn.protocol.ConceptProto;

import java.util.stream.Stream;

public class RelationTypeImpl {
    /**
     * Client implementation of RelationType
     */
    public static class Local extends ThingTypeImpl.Local implements RelationType.Local {

        public Local(ConceptProto.Concept concept) {
            super(concept);
        }
    }

    /**
     * Client implementation of RelationType
     */
    public static class Remote extends ThingTypeImpl.Remote implements RelationType.Remote {

        public Remote(Transaction tx, ConceptIID iid) {
            super(tx, iid);
        }

        @Override
        public final RelationType.Remote setOwns(AttributeType attributeType) {
            return (RelationType.Remote) super.setOwns(attributeType);
        }

        @Override
        public final RelationType.Remote setOwns(AttributeType attributeType, boolean isKey) {
            return (RelationType.Remote) super.setOwns(attributeType, isKey);
        }

        @Override
        public final RelationType.Remote setOwns(AttributeType attributeType, AttributeType overriddenType) {
            return (RelationType.Remote) super.setOwns(attributeType, overriddenType);
        }

        @Override
        public final RelationType.Remote setOwns(AttributeType attributeType, AttributeType overriddenType, boolean isKey) {
            return (RelationType.Remote) super.setOwns(attributeType, overriddenType, isKey);
        }

        @Override
        public Stream<? extends AttributeType.Remote> getOwns(boolean keysOnly) {
            return super.getOwns(keysOnly);
        }

        @Override
        public final RelationType.Remote setPlays(RoleType role) {
            return (RelationType.Remote) super.setPlays(role);
        }

        @Override
        public final RelationType.Remote unsetOwns(AttributeType attributeType) {
            return (RelationType.Remote) super.unsetOwns(attributeType);
        }

        @Override
        public final RelationType.Remote unsetPlays(RoleType role) {
            return (RelationType.Remote) super.unsetPlays(role);
        }

        @Override
        public final RelationType.Remote isAbstract(Boolean isAbstract) {
            return (RelationType.Remote) super.isAbstract(isAbstract);
        }

        @Override
        public final Stream<Relation.Remote> getInstances() {
            return super.getInstances().map(this::asInstance);
        }

        @Override
        public final Stream<RelationType.Remote> getSupertypes() {
            return super.getSupertypes().map(this::asCurrentBaseType);
        }

        @Override
        public final Stream<RelationType.Remote> getSubtypes() {
            return super.getSubtypes().map(this::asCurrentBaseType);
        }

        @Override
        public final RelationType.Remote setLabel(Label label) {
            return (RelationType.Remote) super.setLabel(label);
        }

        @Override
        public final RelationType.Remote setSupertype(RelationType type) {
            return (RelationType.Remote) super.setSupertype(type);
        }

        @Override
        public final Relation.Remote create() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setRelationTypeCreateReq(ConceptProto.RelationType.Create.Req.getDefaultInstance()).build();

            return Concept.Remote.of(tx(), runMethod(method).getRelationTypeCreateRes().getRelation());
        }

        @Override
        public final RoleType.Remote getRelates(Label role) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setRelationTypeGetRelatesForRoleLabelReq(ConceptProto.RelationType.GetRelatesForRoleLabel.Req.newBuilder().setLabel(role.getValue())).build();

            return Concept.Remote.of(tx(), runMethod(method).getRelationTypeGetRelatesForRoleLabelRes().getRole());
        }

        @Override
        public final Stream<RoleType.Remote> getRelates() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setRelationTypeGetRelatesIterReq(ConceptProto.RelationType.GetRelates.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getRelationTypeGetRelatesIterRes().getRole()).map(Concept.Remote::asRoleType);
        }

        @Override
        public final RoleType.Remote setRelates(Label role) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setRelationTypeSetRelatesReq(ConceptProto.RelationType.SetRelates.Req.newBuilder()
                                                       .setLabel(role.getValue())).build();

            runMethod(method);
            return Concept.Remote.of(tx(), runMethod(method).getRelationTypeSetRelatesRes().getRole());
        }

        @Override
        protected final Relation.Remote asInstance(Concept.Remote concept) {
            return concept.asRelation();
        }

        @Override
        protected final RelationType.Remote asCurrentBaseType(Concept.Remote other) {
            return other.asRelationType();
        }

        @Override
        protected final boolean equalsCurrentBaseType(Concept.Remote other) {
            return other.isRelationType();
        }

    }
}
