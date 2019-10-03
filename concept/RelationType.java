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
import grakn.client.concept.api.ConceptId;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.session.ConceptProto;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * Client implementation of RelationType
 */
public class RelationType extends TypeImpl<RelationType, RelationImpl> {

    RelationType(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    public final RelationImpl create() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRelationTypeCreateReq(ConceptProto.RelationType.Create.Req.getDefaultInstance()).build();

        ConceptImpl concept = ConceptImpl.of(runMethod(method).getRelationTypeCreateRes().getRelation(), tx());

        return asInstance(concept);
    }

    public final Stream<Role> roles() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRelationTypeRolesReq(ConceptProto.RelationType.Roles.Req.getDefaultInstance()).build();

        int iteratorId = runMethod(method).getRelationTypeRolesIter().getId();
        return conceptStream(iteratorId, res -> res.getRelationTypeRolesIterRes().getRole()).map(ConceptImpl::asRole);
    }

    public final RelationType relates(Role role) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRelationTypeRelatesReq(ConceptProto.RelationType.Relates.Req.newBuilder()
                                                   .setRole(RequestBuilder.ConceptMessage.from(role))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    public final RelationType unrelate(Role role) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRelationTypeUnrelateReq(ConceptProto.RelationType.Unrelate.Req.newBuilder()
                                                    .setRole(RequestBuilder.ConceptMessage.from(role))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    @Override
    final RelationType asCurrentBaseType(ConceptImpl other) {
        return other.asRelationType();
    }

    @Override
    final boolean equalsCurrentBaseType(ConceptImpl other) {
        return other.isRelationType();
    }

    @Override
    protected final RelationImpl asInstance(ConceptImpl concept) {
        return concept.asRelation();
    }

    @Deprecated
    @CheckReturnValue
    @Override
    public RelationType asRelationType() {
        return this;
    }

    @Deprecated
    @CheckReturnValue
    @Override
    public boolean isRelationType() {
        return true;
    }
}
