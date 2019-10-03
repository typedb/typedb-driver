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

package grakn.client.concept;

import grakn.client.GraknClient;
import grakn.client.concept.api.Concept;
import grakn.client.concept.api.ConceptId;
import grakn.client.concept.api.RelationType;
import grakn.client.concept.api.Role;
import grakn.client.concept.api.Type;
import grakn.protocol.session.ConceptProto;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * Client implementation of Role
 */
public class RoleImpl extends SchemaConceptImpl<Role> implements Role {

    RoleImpl(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    @Override
    public final Stream<RelationType> relations() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRoleRelationsReq(ConceptProto.Role.Relations.Req.getDefaultInstance()).build();
        int iteratorId = runMethod(method).getRoleRelationsIter().getId();
        return conceptStream(iteratorId, res -> res.getRoleRelationsIterRes().getRelationType()).map(ConceptImpl::asRelationType);
    }

    @Override
    public final Stream<Type> players() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRolePlayersReq(ConceptProto.Role.Players.Req.getDefaultInstance()).build();
        int iteratorId = runMethod(method).getRolePlayersIter().getId();
        return conceptStream(iteratorId, res -> res.getRolePlayersIterRes().getType()).map(ConceptImpl::asType);
    }

    @Override
    final Role asCurrentBaseType(Concept other) {
        return other.asRole();
    }

    @Override
    final boolean equalsCurrentBaseType(Concept other) {
        return other.isRole();
    }

}
