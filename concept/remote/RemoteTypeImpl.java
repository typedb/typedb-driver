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
import grakn.client.concept.AttributeType;
import grakn.client.concept.ConceptId;
import grakn.client.concept.Role;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.session.ConceptProto;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Client implementation of Type
 *
 * @param <SomeRemoteType>  The exact type of this class
 * @param <SomeRemoteThing> the exact type of instances of this class
 */
public abstract class RemoteTypeImpl<
        SomeRemoteType extends RemoteType<SomeRemoteType, SomeRemoteThing>,
        SomeRemoteThing extends RemoteThing<SomeRemoteThing, SomeRemoteType>>
        extends RemoteSchemaConceptImpl<SomeRemoteType>
        implements RemoteType<SomeRemoteType, SomeRemoteThing> {

    RemoteTypeImpl(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    final Stream<SomeRemoteThing> thingStream(int iteratorId,
                                              Function<ConceptProto.Method.Iter.Res, ConceptProto.Concept> conceptGetter) {
        return conceptStream(tx(), iteratorId, conceptGetter);
    }

    @Override
    public final Stream<SomeRemoteThing> instances() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeInstancesReq(ConceptProto.Type.Instances.Req.getDefaultInstance()).build();

        int iteratorId = runMethod(method).getTypeInstancesIter().getId();
        return thingStream(iteratorId, res -> res.getTypeInstancesIterRes().getThing()).map(this::asInstance);
    }

    @Override
    public final Boolean isAbstract() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeIsAbstractReq(ConceptProto.Type.IsAbstract.Req.getDefaultInstance()).build();

        return runMethod(method).getTypeIsAbstractRes().getAbstract();
    }

    @Override
    public final SomeRemoteType isAbstract(Boolean isAbstract) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeSetAbstractReq(ConceptProto.Type.SetAbstract.Req.newBuilder()
                                               .setAbstract(isAbstract)).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    @Override
    public final Stream<RemoteAttributeType<?>> keys() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeKeysReq(ConceptProto.Type.Keys.Req.getDefaultInstance()).build();

        int iteratorId = runMethod(method).getTypeKeysIter().getId();
        return conceptStream(iteratorId, res -> res.getTypeKeysIterRes().getAttributeType()).map(RemoteConcept::asAttributeType);
    }

    @Override
    public final Stream<RemoteAttributeType<?>> attributes() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeAttributesReq(ConceptProto.Type.Attributes.Req.getDefaultInstance()).build();

        int iteratorId = runMethod(method).getTypeAttributesIter().getId();
        return conceptStream(iteratorId, res -> res.getTypeAttributesIterRes().getAttributeType()).map(RemoteConcept::asAttributeType);
    }

    @Override
    public final Stream<RemoteRole> playing() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypePlayingReq(ConceptProto.Type.Playing.Req.getDefaultInstance()).build();

        int iteratorId = runMethod(method).getTypePlayingIter().getId();
        return conceptStream(iteratorId, res -> res.getTypePlayingIterRes().getRole()).map(RemoteConcept::asRole);
    }

    @Override
    public final SomeRemoteType key(AttributeType<?, ?, ?> attributeType) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeKeyReq(ConceptProto.Type.Key.Req.newBuilder()
                                       .setAttributeType(RequestBuilder.ConceptMessage.from(attributeType))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    @Override
    public final SomeRemoteType has(AttributeType<?, ?, ?> attributeType) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeHasReq(ConceptProto.Type.Has.Req.newBuilder()
                                       .setAttributeType(RequestBuilder.ConceptMessage.from(attributeType))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    @Override
    public final SomeRemoteType plays(Role<?> role) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypePlaysReq(ConceptProto.Type.Plays.Req.newBuilder()
                                         .setRole(RequestBuilder.ConceptMessage.from(role))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    @Override
    public final SomeRemoteType unkey(AttributeType<?, ?, ?> attributeType) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeUnkeyReq(ConceptProto.Type.Unkey.Req.newBuilder()
                                         .setAttributeType(RequestBuilder.ConceptMessage.from(attributeType))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    @Override
    public final SomeRemoteType unhas(AttributeType<?, ?, ?> attributeType) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeUnhasReq(ConceptProto.Type.Unhas.Req.newBuilder()
                                         .setAttributeType(RequestBuilder.ConceptMessage.from(attributeType))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    @Override
    public final SomeRemoteType unplay(Role<?> role) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeUnplayReq(ConceptProto.Type.Unplay.Req.newBuilder()
                                          .setRole(RequestBuilder.ConceptMessage.from(role))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    protected abstract SomeRemoteThing asInstance(RemoteConcept<?> concept);
}
