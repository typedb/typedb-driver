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

import grakn.client.common.exception.GraknException;
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

    public ThingType.Local getRootThingType() {
        return getType(GraqlToken.Type.THING.toString()).asThingType();
    }

    public EntityType.Local getRootEntityType() {
        return getType(GraqlToken.Type.ENTITY.toString()).asEntityType();
    }

    public RelationType.Local getRootRelationType() {
        return getType(GraqlToken.Type.RELATION.toString()).asRelationType();
    }

    public AttributeType.Local getRootAttributeType() {
        return getType(GraqlToken.Type.ATTRIBUTE.toString()).asAttributeType();
    }

    public RoleType.Local getRootRoleType() {
        return getType(GraqlToken.Type.ROLE.toString()).asRoleType();
    }

    public Rule.Local getRootRule() {
        return getType(GraqlToken.Type.RULE.toString()).asRule();
    }

    public EntityType.Remote putEntityType(final String label) {
        final TransactionProto.Transaction.Req req = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setPutEntityTypeReq(TransactionProto.Transaction.PutEntityType.Req.newBuilder()
                                             .setLabel(label)).build();

        final TransactionProto.Transaction.Res res = transaction.transceiver().sendAndReceiveOrThrow(req);
        return TypeImpl.Remote.of(transaction, res.getPutEntityTypeRes().getEntityType()).asEntityType();
    }

    @Nullable
    public EntityType.Local getEntityType(final String label) {
        final Type.Local concept = getType(label);
        if (concept instanceof EntityType) return concept.asEntityType();
        else return null;
    }

    public RelationType.Remote putRelationType(final String label) {
        final TransactionProto.Transaction.Req req = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setPutRelationTypeReq(TransactionProto.Transaction.PutRelationType.Req.newBuilder()
                                               .setLabel(label)).build();
        final TransactionProto.Transaction.Res res = transaction.transceiver().sendAndReceiveOrThrow(req);
        return TypeImpl.Remote.of(transaction, res.getPutRelationTypeRes().getRelationType()).asRelationType();
    }

    @Nullable
    public RelationType.Local getRelationType(final String label) {
        final Type.Local concept = getType(label);
        if (concept instanceof RelationType) return concept.asRelationType();
        else return null;
    }

    public AttributeType.Local putAttributeType(final String label, final AttributeType.ValueType valueType) {
        final TransactionProto.Transaction.Req req = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setPutAttributeTypeReq(TransactionProto.Transaction.PutAttributeType.Req.newBuilder()
                                                .setLabel(label)
                                                .setValueType(valueType(valueType))).build();
        final TransactionProto.Transaction.Res res = transaction.transceiver().sendAndReceiveOrThrow(req);
        return TypeImpl.Local.of(res.getPutAttributeTypeRes().getAttributeType()).asAttributeType();
    }

    @Nullable
    public AttributeType.Local getAttributeType(final String label) {
        final Type.Local concept = getType(label);
        if (concept instanceof AttributeType) return concept.asAttributeType();
        else return null;
    }

    public Rule.Remote putRule(final String label, final Pattern when, final Pattern then) {
        throw new GraknException(new UnsupportedOperationException());
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
    public Rule.Local getRule(String label) {
        Type.Local concept = getType(label);
        if (concept instanceof Rule) return concept.asRule();
        else return null;
    }

    @Nullable
    public Type.Local getType(final String label) {
        final TransactionProto.Transaction.Req req = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setGetTypeReq(TransactionProto.Transaction.GetType.Req.newBuilder().setLabel(label)).build();

        final TransactionProto.Transaction.Res response = transaction.transceiver().sendAndReceiveOrThrow(req);
        switch (response.getGetTypeRes().getResCase()) {
            case TYPE:
                return TypeImpl.Local.of(response.getGetTypeRes().getType());
            default:
            case RES_NOT_SET:
                return null;
        }
    }

    @Nullable
    public Thing.Local getThing(final String iid) {
        final TransactionProto.Transaction.Req req = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setGetThingReq(TransactionProto.Transaction.GetThing.Req.newBuilder().setIid(iid(iid))).build();

        final TransactionProto.Transaction.Res response = transaction.transceiver().sendAndReceiveOrThrow(req);
        switch (response.getGetThingRes().getResCase()) {
            case THING:
                return ThingImpl.Local.of(response.getGetThingRes().getThing());
            default:
            case RES_NOT_SET:
                return null;
        }
    }

    public TransactionProto.Transaction.Res runThingMethod(final String iid, final ConceptProto.ThingMethod.Req thingMethod) {
        final TransactionProto.Transaction.Req request = TransactionProto.Transaction.Req.newBuilder()
                .setConceptMethodThingReq(TransactionProto.Transaction.ConceptMethod.Thing.Req.newBuilder()
                                                  .setIid(iid(iid))
                                                  .setMethod(thingMethod)).build();

        return transaction.transceiver().sendAndReceiveOrThrow(request);
    }

    public TransactionProto.Transaction.Res runTypeMethod(final String label, @Nullable final String scope, final ConceptProto.TypeMethod.Req method) {
        final TransactionProto.Transaction.ConceptMethod.Type.Req.Builder typeMethod =
                TransactionProto.Transaction.ConceptMethod.Type.Req.newBuilder().setLabel(label).setMethod(method);
        if (scope != null && !scope.isEmpty()) typeMethod.setScope(scope);

        final TransactionProto.Transaction.Req request = TransactionProto.Transaction.Req.newBuilder()
                .setConceptMethodTypeReq(typeMethod.build()).build();

        return transaction.transceiver().sendAndReceiveOrThrow(request);
    }

    public <T> Stream<T> iterateThingMethod(final String iid, final ConceptProto.ThingMethod.Iter.Req method,
                                            final Function<ConceptProto.ThingMethod.Iter.Res, T> responseReader) {
        final TransactionProto.Transaction.Iter.Req request = TransactionProto.Transaction.Iter.Req.newBuilder()
                .setConceptMethodThingIterReq(TransactionProto.Transaction.ConceptMethod.Thing.Iter.Req.newBuilder()
                                                      .setIid(iid(iid))
                                                      .setMethod(method)).build();

        return transaction.transceiver().iterate(request, res -> responseReader.apply(res.getConceptMethodThingIterRes().getResponse()));
    }

    public <T> Stream<T> iterateTypeMethod(final String label, @Nullable final String scope, final ConceptProto.TypeMethod.Iter.Req method,
                                           final Function<ConceptProto.TypeMethod.Iter.Res, T> responseReader) {
        final TransactionProto.Transaction.ConceptMethod.Type.Iter.Req.Builder typeMethod =
                TransactionProto.Transaction.ConceptMethod.Type.Iter.Req.newBuilder().setLabel(label).setMethod(method);
        if (scope != null && !scope.isEmpty()) typeMethod.setScope(scope);

        final TransactionProto.Transaction.Iter.Req request = TransactionProto.Transaction.Iter.Req.newBuilder()
                .setConceptMethodTypeIterReq(typeMethod.build()).build();

        return transaction.transceiver().iterate(request, res -> responseReader.apply(res.getConceptMethodTypeIterRes().getResponse()));
    }
}
