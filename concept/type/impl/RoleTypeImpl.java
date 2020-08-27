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

import grakn.client.GraknClient;
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
    public static class Local extends TypeImpl.Local<RoleType> implements RoleType.Local {

        public Local(ConceptProto.Concept concept) {
            super(concept);
        }
    }

    /**
     * Client implementation of Role
     */
    public static class Remote extends TypeImpl.Remote<RoleType> implements RoleType.Remote {

        public Remote(GraknClient.Transaction tx, ConceptIID iid) {
            super(tx, iid);
        }

        @Override
        public final Stream<RoleType.Remote> sups() {
            return super.sups().map(this::asCurrentBaseType);
        }

        @Override
        public final Stream<RoleType.Remote> subs() {
            return super.subs().map(this::asCurrentBaseType);
        }

        @Override
        public final RoleType.Remote label(Label label) {
            return (RoleType.Remote) super.label(label);
        }

        @Override
        public RoleType.Remote sup(RoleType superRole) {
            return (RoleType.Remote) super.sup(superRole);
        }

        @Override
        public Label scopedLabel() {
            return Label.of("unknown:" + label().getValue()); // TODO fix
        }

        @Override
        public final Stream<RelationType.Remote> relations() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setRoleRelationsIterReq(ConceptProto.RoleType.Relations.Iter.Req.getDefaultInstance()).build();
            return conceptStream(method, res -> res.getRoleRelationsIterRes().getRelationType()).map(Concept.Remote::asRelationType);
        }

        @Override
        public final Stream<ThingType.Remote<?, ?>> players() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setRolePlayersIterReq(ConceptProto.RoleType.Players.Iter.Req.getDefaultInstance()).build();
            return conceptStream(method, res -> res.getRolePlayersIterRes().getThingType()).map(Concept.Remote::asType);
        }

        @Override
        protected final RoleType.Remote asCurrentBaseType(Concept.Remote<?> other) {
            return other.asRoleType();
        }

        @Override
        protected final boolean equalsCurrentBaseType(Concept.Remote<?> other) {
            return other.isRoleType();
        }

    }
}
