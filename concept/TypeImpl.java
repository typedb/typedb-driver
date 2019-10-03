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
 * Client implementation of Type
 *
 * @param <SomeType>  The exact type of this class
 * @param <SomeThing> the exact type of instances of this class
 */
public abstract class TypeImpl<SomeType extends TypeImpl, SomeThing extends Thing> extends SchemaConcept<SomeType> {

    TypeImpl(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    public final Stream<SomeThing> instances() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeInstancesReq(ConceptProto.Type.Instances.Req.getDefaultInstance()).build();

        int iteratorId = runMethod(method).getTypeInstancesIter().getId();
        return conceptStream(iteratorId, res -> res.getTypeInstancesIterRes().getThing()).map(this::asInstance);
    }

    public final Boolean isAbstract() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeIsAbstractReq(ConceptProto.Type.IsAbstract.Req.getDefaultInstance()).build();

        return runMethod(method).getTypeIsAbstractRes().getAbstract();
    }

    public final SomeType isAbstract(Boolean isAbstract) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeSetAbstractReq(ConceptProto.Type.SetAbstract.Req.newBuilder()
                                               .setAbstract(isAbstract)).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    public final Stream<AttributeTypeImpl> keys() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeKeysReq(ConceptProto.Type.Keys.Req.getDefaultInstance()).build();

        int iteratorId = runMethod(method).getTypeKeysIter().getId();
        return conceptStream(iteratorId, res -> res.getTypeKeysIterRes().getAttributeType()).map(ConceptImpl::asAttributeType);
    }

    public final Stream<AttributeTypeImpl> attributes() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeAttributesReq(ConceptProto.Type.Attributes.Req.getDefaultInstance()).build();

        int iteratorId = runMethod(method).getTypeAttributesIter().getId();
        return conceptStream(iteratorId, res -> res.getTypeAttributesIterRes().getAttributeType()).map(ConceptImpl::asAttributeType);
    }

    public final Stream<Role> playing() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypePlayingReq(ConceptProto.Type.Playing.Req.getDefaultInstance()).build();

        int iteratorId = runMethod(method).getTypePlayingIter().getId();
        return conceptStream(iteratorId, res -> res.getTypePlayingIterRes().getRole()).map(ConceptImpl::asRole);
    }

    public final SomeType key(AttributeTypeImpl attributeTypeImpl) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeKeyReq(ConceptProto.Type.Key.Req.newBuilder()
                                       .setAttributeType(RequestBuilder.ConceptMessage.from(attributeTypeImpl))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    public final SomeType has(AttributeTypeImpl attributeTypeImpl) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeHasReq(ConceptProto.Type.Has.Req.newBuilder()
                                       .setAttributeType(RequestBuilder.ConceptMessage.from(attributeTypeImpl))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    public final SomeType plays(Role role) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypePlaysReq(ConceptProto.Type.Plays.Req.newBuilder()
                                         .setRole(RequestBuilder.ConceptMessage.from(role))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    public final SomeType unkey(AttributeTypeImpl attributeTypeImpl) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeUnkeyReq(ConceptProto.Type.Unkey.Req.newBuilder()
                                         .setAttributeType(RequestBuilder.ConceptMessage.from(attributeTypeImpl))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    public final SomeType unhas(AttributeTypeImpl attributeTypeImpl) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeUnhasReq(ConceptProto.Type.Unhas.Req.newBuilder()
                                         .setAttributeType(RequestBuilder.ConceptMessage.from(attributeTypeImpl))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    public final SomeType unplay(Role role) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setTypeUnplayReq(ConceptProto.Type.Unplay.Req.newBuilder()
                                          .setRole(RequestBuilder.ConceptMessage.from(role))).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    protected abstract SomeThing asInstance(ConceptImpl concept);

    @Deprecated
    @CheckReturnValue
    @Override
    public TypeImpl asType() {
        return this;
    }

    @Deprecated
    @CheckReturnValue
    @Override
    public boolean isType() {
        return true;
    }
}
