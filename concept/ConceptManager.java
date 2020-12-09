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

import grakn.client.concept.thing.Thing;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import grakn.client.concept.type.impl.AttributeTypeImpl;
import grakn.client.concept.type.impl.EntityTypeImpl;
import grakn.client.concept.type.impl.RelationTypeImpl;
import grakn.client.concept.type.impl.TypeImpl;
import grakn.client.rpc.RPCTransaction;
import grakn.protocol.ConceptProto;
import grakn.protocol.TransactionProto;
import graql.lang.common.GraqlToken;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

import static grakn.client.common.ProtoBuilder.tracingData;
import static grakn.client.concept.proto.ConceptProtoBuilder.iid;
import static grakn.client.concept.proto.ConceptProtoBuilder.valueType;

public final class ConceptManager {

    private final RPCTransaction rpcTransaction;

    public ConceptManager(RPCTransaction rpcTransaction) {
        this.rpcTransaction = rpcTransaction;
    }

    @CheckReturnValue
    public ThingType getRootThingType() {
        return getType(GraqlToken.Type.THING.toString()).asThingType();
    }

    @CheckReturnValue
    public EntityType getRootEntityType() {
        return getType(GraqlToken.Type.ENTITY.toString()).asEntityType();
    }

    @CheckReturnValue
    public RelationType getRootRelationType() {
        return getType(GraqlToken.Type.RELATION.toString()).asRelationType();
    }

    @CheckReturnValue
    public AttributeType getRootAttributeType() {
        return getType(GraqlToken.Type.ATTRIBUTE.toString()).asAttributeType();
    }

    public EntityType putEntityType(String label) {
        final ConceptProto.ConceptManager.Req req = ConceptProto.ConceptManager.Req.newBuilder()
                .setPutEntityTypeReq(ConceptProto.ConceptManager.PutEntityType.Req.newBuilder()
                        .setLabel(label)).build();
        final ConceptProto.ConceptManager.Res res = execute(req);
        return EntityTypeImpl.of(res.getPutEntityTypeRes().getEntityType());
    }

    @Nullable
    @CheckReturnValue
    public EntityType getEntityType(String label) {
        final Type type = getType(label);
        if (type instanceof EntityType) return type.asEntityType();
        else return null;
    }

    public RelationType putRelationType(String label) {
        final ConceptProto.ConceptManager.Req req = ConceptProto.ConceptManager.Req.newBuilder()
                .setPutRelationTypeReq(ConceptProto.ConceptManager.PutRelationType.Req.newBuilder()
                        .setLabel(label)).build();
        final ConceptProto.ConceptManager.Res res = execute(req);
        return RelationTypeImpl.of(res.getPutRelationTypeRes().getRelationType());
    }

    @Nullable
    @CheckReturnValue
    public RelationType getRelationType(String label) {
        final Type type = getType(label);
        if (type instanceof RelationType) return type.asRelationType();
        else return null;
    }

    public AttributeType putAttributeType(String label, AttributeType.ValueType valueType) {
        final ConceptProto.ConceptManager.Req req = ConceptProto.ConceptManager.Req.newBuilder()
                .setPutAttributeTypeReq(ConceptProto.ConceptManager.PutAttributeType.Req.newBuilder()
                        .setLabel(label)
                        .setValueType(valueType(valueType))).build();
        final ConceptProto.ConceptManager.Res res = execute(req);
        return AttributeTypeImpl.of(res.getPutAttributeTypeRes().getAttributeType());
    }

    @Nullable
    @CheckReturnValue
    public AttributeType getAttributeType(String label) {
        final Type type = getType(label);
        if (type instanceof AttributeType) return type.asAttributeType();
        else return null;
    }

    @Nullable
    @CheckReturnValue
    public Thing getThing(String iid) {
        final ConceptProto.ConceptManager.Req req = ConceptProto.ConceptManager.Req.newBuilder()
                .setGetThingReq(ConceptProto.ConceptManager.GetThing.Req.newBuilder().setIid(iid(iid))).build();

        final ConceptProto.ConceptManager.Res response = execute(req);
        if (response.getGetThingRes().getResCase() == ConceptProto.ConceptManager.GetThing.Res.ResCase.THING)
            return ThingImpl.of(response.getGetThingRes().getThing());
        else
            return null;
    }

    @Nullable
    @CheckReturnValue
    public Type getType(String label) {
        final ConceptProto.ConceptManager.Req req = ConceptProto.ConceptManager.Req.newBuilder()
                .setGetTypeReq(ConceptProto.ConceptManager.GetType.Req.newBuilder().setLabel(label)).build();

        final ConceptProto.ConceptManager.Res response = execute(req);
        if (response.getGetTypeRes().getResCase() == ConceptProto.ConceptManager.GetType.Res.ResCase.TYPE)
            return TypeImpl.of(response.getGetTypeRes().getType());
        else
            return null;
    }

    private ConceptProto.ConceptManager.Res execute(ConceptProto.ConceptManager.Req request) {
        final TransactionProto.Transaction.Req.Builder req = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setConceptManagerReq(request);
        return rpcTransaction.execute(req).getConceptManagerRes();
    }
}
