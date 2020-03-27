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
import grakn.client.concept.Attribute;
import grakn.client.concept.AttributeType;
import grakn.client.concept.ConceptId;
import grakn.client.exception.GraknClientException;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.session.ConceptProto;

import javax.annotation.Nullable;

/**
 * Client implementation of AttributeType
 *
 * @param <D> The data type of this attribute type
 */
class RemoteAttributeTypeImpl<D>
        extends RemoteTypeImpl<RemoteAttributeType<D>, RemoteAttribute<D>, AttributeType<D>, Attribute<D>>
        implements RemoteAttributeType<D> {

    RemoteAttributeTypeImpl(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    @Override
    public final RemoteAttribute<D> create(D value) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setAttributeTypeCreateReq(ConceptProto.AttributeType.Create.Req.newBuilder()
                                                   .setValue(RequestBuilder.ConceptMessage.attributeValue(value))).build();

        return RemoteConcept.of(runMethod(method).getAttributeTypeCreateRes().getAttribute(), tx());
    }

    @Override
    @Nullable
    public final RemoteAttribute<D> attribute(D value) {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setAttributeTypeAttributeReq(ConceptProto.AttributeType.Attribute.Req.newBuilder()
                                                      .setValue(RequestBuilder.ConceptMessage.attributeValue(value))).build();

        ConceptProto.AttributeType.Attribute.Res response = runMethod(method).getAttributeTypeAttributeRes();
        switch (response.getResCase()) {
            case NULL:
                return null;
            case ATTRIBUTE:
                return RemoteConcept.of(response.getAttribute(), tx()).asAttribute();
            default:
                throw GraknClientException.unreachableStatement("Unexpected response " + response);
        }
    }

    @Override
    @Nullable
    public final RemoteAttributeType.DataType<D> dataType() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setAttributeTypeDataTypeReq(ConceptProto.AttributeType.DataType.Req.getDefaultInstance()).build();

        ConceptProto.AttributeType.DataType.Res response = runMethod(method).getAttributeTypeDataTypeRes();
        switch (response.getResCase()) {
            case NULL:
                return null;
            case DATATYPE:
                return RequestBuilder.ConceptMessage.dataType(response.getDataType());
            default:
                throw GraknClientException.unreachableStatement("Unexpected response " + response);
        }
    }

    @Override
    @Nullable
    public final String regex() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setAttributeTypeGetRegexReq(ConceptProto.AttributeType.GetRegex.Req.getDefaultInstance()).build();

        String regex = runMethod(method).getAttributeTypeGetRegexRes().getRegex();
        return regex.isEmpty() ? null : regex;
    }

    @Override
    public final RemoteAttributeType<D> regex(String regex) {
        if (regex == null) regex = "";
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setAttributeTypeSetRegexReq(ConceptProto.AttributeType.SetRegex.Req.newBuilder()
                                                     .setRegex(regex)).build();

        runMethod(method);
        return asCurrentBaseType(this);
    }

    @Override
    final RemoteAttributeType<D> asCurrentBaseType(RemoteConcept<RemoteAttributeType<D>, AttributeType<D>> other) {
        return other.asAttributeType();
    }

    @Override
    final boolean equalsCurrentBaseType(RemoteConcept<RemoteAttributeType<D>, AttributeType<D>> other) {
        return other.isAttributeType();
    }

    @Override
    protected final RemoteAttribute<D> asInstance(RemoteConcept<RemoteAttribute<D>, Attribute<D>> concept) {
        return concept.asAttribute();
    }

}
