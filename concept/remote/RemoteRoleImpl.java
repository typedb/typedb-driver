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

package grakn.client.concept.remote;

import grakn.client.GraknClient;
import grakn.client.concept.ConceptId;
import grakn.client.concept.Role;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.Type;
import grakn.protocol.session.ConceptProto;

import java.util.stream.Stream;

/**
 * Client implementation of Role
 */
public class RemoteRoleImpl extends RemoteSchemaConceptImpl<Role.Remote> implements Role.Remote {

    public RemoteRoleImpl(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    @Override
    public Role.Remote sup(Role<?> superRole) {
        return super.sup(superRole);
    }

    @Override
    public final Stream<RelationType.Remote> relations() {
        ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                .setRoleRelationsIterReq(ConceptProto.Role.Relations.Iter.Req.getDefaultInstance()).build();
        return conceptStream(method, res -> res.getRoleRelationsIterRes().getRelationType()).map(grakn.client.concept.Concept.Remote::asRelationType);
    }

    @Override
    public final Stream<Type.Remote<?, ?>> players() {
        ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                .setRolePlayersIterReq(ConceptProto.Role.Players.Iter.Req.getDefaultInstance()).build();
        return conceptStream(method, res -> res.getRolePlayersIterRes().getType()).map(grakn.client.concept.Concept.Remote::asType);
    }

    @Override
    final Role.Remote asCurrentBaseType(grakn.client.concept.Concept.Remote other) {
        return other.asRole();
    }

    @Override
    final boolean equalsCurrentBaseType(grakn.client.concept.Concept.Remote other) {
        return other.isRole();
    }

}
