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
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;
import grakn.protocol.ConceptProto;

import java.util.stream.Stream;

public class RoleTypeImpl {
    /**
     * Client implementation of Role
     */
    public static class Local extends TypeImpl.Local implements RoleType.Local {

        public Local(ConceptProto.Concept concept) {
            super(concept);
        }
    }

    /**
     * Client implementation of Role
     */
    public static class Remote extends TypeImpl.Remote implements RoleType.Remote {

        public Remote(Transaction tx, ConceptIID iid) {
            super(tx, iid);
        }

        @Override
        public final Stream<RoleType.Remote> getSupertypes() {
            return super.getSupertypes().map(this::asCurrentBaseType);
        }

        @Override
        public final Stream<RoleType.Remote> getSubtypes() {
            return super.getSubtypes().map(this::asCurrentBaseType);
        }

        @Override
        public final RoleType.Remote setLabel(Label label) {
            return (RoleType.Remote) super.setLabel(label);
        }

        @Override
        public RoleType.Remote setSupertype(RoleType superRole) {
            return (RoleType.Remote) super.setSupertype(superRole);
        }

        @Override
        public Label getScopedLabel() {
            return Label.of("unknown:" + getLabel().getValue()); // TODO fix
        }

        @Override
        public final Stream<RelationType.Remote> getRelations() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setRoleTypeGetRelationsIterReq(ConceptProto.RoleType.GetRelations.Iter.Req.getDefaultInstance()).build();
            return conceptStream(method, res -> res.getRoleTypeGetRelationsIterRes().getRelationType()).map(Concept.Remote::asRelationType);
        }

        @Override
        public final Stream<ThingType.Remote> getPlayers() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setRoleTypeGetPlayersIterReq(ConceptProto.RoleType.GetPlayers.Iter.Req.getDefaultInstance()).build();
            return conceptStream(method, res -> res.getRoleTypeGetPlayersIterRes().getThingType()).map(Concept.Remote::asType);
        }

        @Override
        protected final RoleType.Remote asCurrentBaseType(Concept.Remote other) {
            return other.asRoleType();
        }

        @Override
        protected final boolean equalsCurrentBaseType(Concept.Remote other) {
            return other.isRoleType();
        }

    }
}
