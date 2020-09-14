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

import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.Rule;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import grakn.client.concept.type.impl.TypeImpl;
import grakn.client.rpc.RPCTransaction;
import grakn.protocol.ConceptProto;
import grakn.protocol.TransactionProto;
import graql.lang.common.GraqlToken;
import graql.lang.pattern.Pattern;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.client.common.ProtoBuilder.tracingData;
import static grakn.client.concept.proto.ConceptProtoBuilder.iid;
import static grakn.client.concept.proto.ConceptProtoBuilder.valueType;

public final class Concepts {

    private final RPCTransaction transaction;

    public Concepts(final RPCTransaction transaction) {
        this.transaction = transaction;
    }

    public ThingType getRootThingType() {
        return getType(GraqlToken.Type.THING.toString()).asThingType();
    }

    public EntityType getRootEntityType() {
        return getType(GraqlToken.Type.ENTITY.toString()).asEntityType();
    }

    public RelationType getRootRelationType() {
        return getType(GraqlToken.Type.RELATION.toString()).asRelationType();
    }

    public AttributeType getRootAttributeType() {
        return getType(GraqlToken.Type.ATTRIBUTE.toString()).asAttributeType();
    }

    public RoleType getRootRoleType() {
        return getType(GraqlToken.Type.ROLE.toString()).asRoleType();
    }

    public Rule getRootRule() {
        return getType(GraqlToken.Type.RULE.toString()).asRule();
    }

    public EntityType putEntityType(final String label) {
        final TransactionProto.Transaction.Req req = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setPutEntityTypeReq(TransactionProto.Transaction.PutEntityType.Req.newBuilder()
                                             .setLabel(label)).build();

        final TransactionProto.Transaction.Res res = transaction.transceiver().sendAndReceiveOrThrow(req);
        return TypeImpl.of(res.getPutEntityTypeRes().getEntityType()).asEntityType();
    }

    @Nullable
    public EntityType getEntityType(final String label) {
        final Type concept = getType(label);
        if (concept instanceof EntityType) return concept.asEntityType();
        else return null;
    }

    public RelationType putRelationType(final String label) {
        final TransactionProto.Transaction.Req req = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setPutRelationTypeReq(TransactionProto.Transaction.PutRelationType.Req.newBuilder()
                                               .setLabel(label)).build();
        final TransactionProto.Transaction.Res res = transaction.transceiver().sendAndReceiveOrThrow(req);
        return TypeImpl.of(res.getPutRelationTypeRes().getRelationType()).asRelationType();
    }

    @Nullable
    public RelationType getRelationType(final String label) {
        final Type concept = getType(label);
        if (concept instanceof RelationType) return concept.asRelationType();
        else return null;
    }

    public AttributeType putAttributeType(final String label, final AttributeType.ValueType valueType) {
        final TransactionProto.Transaction.Req req = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setPutAttributeTypeReq(TransactionProto.Transaction.PutAttributeType.Req.newBuilder()
                                                .setLabel(label)
                                                .setValueType(valueType(valueType))).build();
        final TransactionProto.Transaction.Res res = transaction.transceiver().sendAndReceiveOrThrow(req);
        return TypeImpl.of(res.getPutAttributeTypeRes().getAttributeType()).asAttributeType();
    }

    @Nullable
    public AttributeType getAttributeType(final String label) {
        final Type concept = getType(label);
        if (concept instanceof AttributeType) return concept.asAttributeType();
        else return null;
    }

    public Rule putRule(final String label, final Pattern when, final Pattern then) {
        // TODO
        thow new GraknClientException(new UnsupportedOperationException());
        /*final TransactionProto.Transaction.Req req = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setPutRuleReq(TransactionProto.Transaction.PutRule.Req.newBuilder()
                        .setLabel(label)
                        .setWhen(when.toString())
                        .setThen(then.toString())).build();

        final TransactionProto.Transaction.Res res = sendAndReceiveOrThrow(req);
        return Type.Remote.of(this, res.getPutRuleRes().getRule()).asRule();*/
    }

    @Nullable
    public Rule getRule(String label) {
        // TODO
        Type concept = getType(label);
        if (concept instancof Rule) return concept.asRule();
        else return null;
    }

    @Nullable
    public Type getType(final String label) {
        final TransactionProto.Transaction.Req req = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setGetTypeReq(TransactionProto.Transaction.GetType.Req.newBuilder().setLabel(label)).build();

        final TransactionProto.Transaction.Res response = transaction.transceiver().sendAndReceiveOrThrow(req);
        switch (response.getGetTypeRes().getResCase()) {
            case TYPE:
                return TypeImpl.of(response.getGetTypeRes().getType());
            default:
            case RES_NOT_SET:
                return null;
        }
    }

    @Nullable
    public Thing getThing(final String iid) {
        final TransactionProto.Transaction.Req req = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setGetThingReq(TransactionProto.Transaction.GetThing.Req.newBuilder().setIid(iid(iid))).build();

        final TransactionProto.Transaction.Res response = transaction.transceiver().sendAndReceiveOrThrow(req);
        switch (response.getGetThingRes().getResCase()) {
            case THING:
                return ThingImpl.of(response.getGetThingRes().getThing());
            default:
            case RES_NOT_SET:
                return null;
        }
    }

    public TransactionProto.Transaction.Res runThingMethod(final ConceptProto.ThingMethod.Req.Builder method) {
        final TransactionProto.Transaction.Req request = TransactionProto.Transaction.Req.newBuilder()
                .setThingMethodReq(method).build();

        return transaction.transceiver().sendAndReceiveOrThrow(request);
    }

    public TransactionProto.Transaction.Res runTypeMethod(final ConceptProto.TypeMethod.Req.Builder method) {
        final TransactionProto.Transaction.Req request = TransactionProto.Transaction.Req.newBuilder()
                .setTypeMethodReq(method).build();

        return transaction.transceiver().sendAndReceiveOrThrow(request);
    }

    public <T> Stream<T> iterateThingMethod(final ConceptProto.ThingMethod.Iter.Req.Builder method,
                                            final Function<ConceptProto.ThingMethod.Iter.Res, T> responseReader) {
        final TransactionProto.Transaction.Iter.Req request = TransactionProto.Transaction.Iter.Req.newBuilder()
                .setThingMethodIterReq(method).build();

        return transaction.transceiver().iterate(request, res -> responseReader.apply(res.getConceptMethodThingIterRes()));
    }

    public <T> Stream<T> iterateTypeMethod(final ConceptProto.TypeMethod.Iter.Req.Builder method,
                                           final Function<ConceptProto.TypeMethod.Iter.Res, T> responseReader) {
        final TransactionProto.Transaction.Iter.Req request = TransactionProto.Transaction.Iter.Req.newBuilder()
                .setTypeMethodIterReq(method).build();

        return transaction.transceiver().iterate(request, res -> responseReader.apply(res.getConceptMethodTypeIterRes()));
    }
}
