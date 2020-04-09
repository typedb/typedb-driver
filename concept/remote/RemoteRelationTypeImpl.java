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
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.Role;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.session.ConceptProto;

import java.util.stream.Stream;

/**
 * Client implementation of RelationType
 */
public class RemoteRelationTypeImpl extends RemoteTypeImpl<RelationType.Remote, Relation.Remote> implements RelationType.Remote {

    public RemoteRelationTypeImpl(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    @Override
    public final Relation.Remote create() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRelationTypeCreateReq(ConceptProto.RelationType.Create.Req.getDefaultInstance()).build();

        return Concept.Remote.of(runMethod(method).getRelationTypeCreateRes().getRelation(), tx());
    }

    @Override
    public RelationType.Remote sup(RelationType<?, ?> superRelationType) {
        return super.sup(superRelationType);
    }

    @Override
    public final Stream<Role.Remote> roles() {
        ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                .setRelationTypeRolesIterReq(ConceptProto.RelationType.Roles.Iter.Req.getDefaultInstance()).build();

        return conceptStream(method, res -> res.getRelationTypeRolesIterRes().getRole()).map(Concept.Remote::asRole);
    }

    @Override
    public final RelationType.Remote relates(Role<?> role) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRelationTypeRelatesReq(ConceptProto.RelationType.Relates.Req.newBuilder()
                                                   .setRole(RequestBuilder.ConceptMessage.from(role))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    @Override
    public final RelationType.Remote unrelate(Role<?> role) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setRelationTypeUnrelateReq(ConceptProto.RelationType.Unrelate.Req.newBuilder()
                                                    .setRole(RequestBuilder.ConceptMessage.from(role))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    @Override
    final RelationType.Remote asCurrentBaseType(Concept.Remote<?> other) {
        return other.asRelationType();
    }

    @Override
    final boolean equalsCurrentBaseType(Concept.Remote<?> other) {
        return other.isRelationType();
    }

    @Override
    protected final Relation.Remote asInstance(Concept.Remote<?> concept) {
        return concept.asRelation();
    }
}
