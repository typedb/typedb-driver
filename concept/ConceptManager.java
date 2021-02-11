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
import grakn.client.concept.type.impl.AttributeTypeImpl;
import grakn.client.concept.type.impl.EntityTypeImpl;
import grakn.client.concept.type.impl.RelationTypeImpl;
import grakn.client.concept.type.impl.ThingTypeImpl;
import grakn.client.rpc.RPCTransaction;
import grakn.protocol.ConceptProto;
import grakn.protocol.TransactionProto;
import graql.lang.common.GraqlToken;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

import static grakn.client.common.tracing.TracingProtoBuilder.tracingData;
import static grakn.client.concept.proto.ConceptProtoBuilder.iid;
import static grakn.client.concept.proto.ConceptProtoBuilder.valueType;

public final class ConceptManager {

    private final RPCTransaction rpcTransaction;

    public ConceptManager(RPCTransaction rpcTransaction) {
        this.rpcTransaction = rpcTransaction;
    }

    @CheckReturnValue
    public ThingType getRootThingType() {
        return getThingType(GraqlToken.Type.THING.toString());
    }

    @CheckReturnValue
    public EntityType getRootEntityType() {
        return getEntityType(GraqlToken.Type.ENTITY.toString());
    }

    @CheckReturnValue
    public RelationType getRootRelationType() {
        return getRelationType(GraqlToken.Type.RELATION.toString());
    }

    @CheckReturnValue
    public AttributeType getRootAttributeType() {
        return getAttributeType(GraqlToken.Type.ATTRIBUTE.toString());
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
        final ThingType thingType = getThingType(label);
        if (thingType != null && thingType.isEntityType()) return thingType.asEntityType();
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
        final ThingType thingType = getThingType(label);
        if (thingType != null && thingType.isRelationType()) return thingType.asRelationType();
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
        final ThingType thingType = getThingType(label);
        if (thingType != null && thingType.isAttributeType()) return thingType.asAttributeType();
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
    public ThingType getThingType(String label) {
        final ConceptProto.ConceptManager.Req req = ConceptProto.ConceptManager.Req.newBuilder()
                .setGetThingTypeReq(ConceptProto.ConceptManager.GetThingType.Req.newBuilder().setLabel(label)).build();

        final ConceptProto.ConceptManager.Res response = execute(req);
        if (response.getGetThingTypeRes().getResCase() == ConceptProto.ConceptManager.GetThingType.Res.ResCase.THING_TYPE)
            return ThingTypeImpl.of(response.getGetThingTypeRes().getThingType());
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
