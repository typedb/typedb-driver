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
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.ConceptId;
import grakn.client.concept.Role;
import grakn.client.concept.type.Type;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.session.ConceptProto;

import java.util.stream.Stream;

/**
 * Client implementation of Type
 *
 * @param <SomeRemoteType>  The exact type of this class
 * @param <SomeRemoteThing> the exact type of instances of this class
 */
public abstract class RemoteTypeImpl<
        SomeRemoteType extends Type.Remote,
        SomeRemoteThing extends Thing.Remote<SomeRemoteThing, SomeRemoteType>>
        extends RemoteSchemaConceptImpl<SomeRemoteType>
        implements Type.Remote {

    protected RemoteTypeImpl(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    @Override
    public final Stream<SomeRemoteThing> instances() {
        ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                .setTypeInstancesIterReq(ConceptProto.Type.Instances.Iter.Req.getDefaultInstance()).build();

        return conceptStream(method, res -> res.getTypeInstancesIterRes().getThing()).map(this::asInstance);
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
    public final Stream<AttributeType.Remote<?>> keys() {
        ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                .setTypeKeysIterReq(ConceptProto.Type.Keys.Iter.Req.getDefaultInstance()).build();

        return conceptStream(method, res -> res.getTypeKeysIterRes().getAttributeType()).map(Concept.Remote::asAttributeType);
    }

    @Override
    public final Stream<AttributeType.Remote<?>> attributes() {
        ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                .setTypeAttributesIterReq(ConceptProto.Type.Attributes.Iter.Req.getDefaultInstance()).build();

        return conceptStream(method, res -> res.getTypeAttributesIterRes().getAttributeType()).map(Concept.Remote::asAttributeType);
    }

    @Override
    public final Stream<Role.Remote> playing() {
        ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                .setTypePlayingIterReq(ConceptProto.Type.Playing.Iter.Req.getDefaultInstance()).build();

        return conceptStream(method, res -> res.getTypePlayingIterRes().getRole()).map(Concept.Remote::asRole);
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

    protected abstract SomeRemoteThing asInstance(Concept.Remote<?> concept);
}
