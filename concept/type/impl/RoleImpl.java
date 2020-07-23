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
import grakn.client.concept.ConceptId;
import grakn.client.concept.Label;
import grakn.client.concept.impl.SchemaConceptImpl;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.Role;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;

import java.util.stream.Stream;

public class RoleImpl {
    /**
     * Client implementation of Role
     */
    public static class Local extends SchemaConceptImpl.Local<Role> implements Role.Local {

        public Local(ConceptProto.Concept concept) {
            super(concept);
        }
    }

    /**
     * Client implementation of Role
     */
    public static class Remote extends SchemaConceptImpl.Remote<Role> implements Role.Remote {

        public Remote(GraknClient.Transaction tx, ConceptId id) {
            super(tx, id);
        }

        @Override
        public final Stream<Role.Remote> sups() {
            return super.sups().map(this::asCurrentBaseType);
        }

        @Override
        public final Stream<Role.Remote> subs() {
            return super.subs().map(this::asCurrentBaseType);
        }

        @Override
        public final Role.Remote label(Label label) {
            return (Role.Remote) super.label(label);
        }

        @Override
        public Role.Remote sup(Role superRole) {
            return (Role.Remote) super.sup(superRole);
        }

        @Override
        public final Stream<RelationType.Remote> relations() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setRoleRelationsIterReq(ConceptProto.Role.Relations.Iter.Req.getDefaultInstance()).build();
            return conceptStream(method, res -> res.getRoleRelationsIterRes().getRelationType()).map(Concept.Remote::asRelationType);
        }

        @Override
        public final Stream<Type.Remote<?, ?>> players() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setRolePlayersIterReq(ConceptProto.Role.Players.Iter.Req.getDefaultInstance()).build();
            return conceptStream(method, res -> res.getRolePlayersIterRes().getType()).map(Concept.Remote::asType);
        }

        @Override
        protected final Role.Remote asCurrentBaseType(Concept.Remote<?> other) {
            return other.asRole();
        }

        @Override
        protected final boolean equalsCurrentBaseType(Concept.Remote<?> other) {
            return other.isRole();
        }

    }
}
