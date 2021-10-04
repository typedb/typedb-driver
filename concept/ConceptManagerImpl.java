/*
 * Copyright (C) 2021 Vaticle
 *
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

package com.vaticle.typedb.client.concept;

import com.vaticle.typedb.client.api.concept.ConceptManager;
import com.vaticle.typedb.client.api.concept.thing.Thing;
import com.vaticle.typedb.client.api.concept.type.AttributeType;
import com.vaticle.typedb.client.api.concept.type.EntityType;
import com.vaticle.typedb.client.api.concept.type.RelationType;
import com.vaticle.typedb.client.api.concept.type.ThingType;
import com.vaticle.typedb.client.api.connection.TypeDBTransaction;
import com.vaticle.typedb.client.concept.thing.ThingImpl;
import com.vaticle.typedb.client.concept.type.AttributeTypeImpl;
import com.vaticle.typedb.client.concept.type.EntityTypeImpl;
import com.vaticle.typedb.client.concept.type.RelationTypeImpl;
import com.vaticle.typedb.client.concept.type.ThingTypeImpl;
import com.vaticle.typedb.protocol.ConceptProto;
import com.vaticle.typedb.protocol.TransactionProto;
import com.vaticle.typeql.lang.common.TypeQLToken;

import javax.annotation.Nullable;

import static com.vaticle.typedb.client.common.rpc.RequestBuilder.ConceptManager.getThingReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.ConceptManager.getThingTypeReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.ConceptManager.putAttributeTypeReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.ConceptManager.putEntityTypeReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.ConceptManager.putRelationTypeReq;

public final class ConceptManagerImpl implements ConceptManager {

    private final TypeDBTransaction.Extended transactionExt;

    public ConceptManagerImpl(TypeDBTransaction.Extended transactionExt) {
        this.transactionExt = transactionExt;
    }

    @Override
    public ThingType getRootThingType() {
        return getThingType(TypeQLToken.Type.THING.toString());
    }

    @Override
    public EntityType getRootEntityType() {
        return getEntityType(TypeQLToken.Type.ENTITY.toString());
    }

    @Override
    public RelationType getRootRelationType() {
        return getRelationType(TypeQLToken.Type.RELATION.toString());
    }

    @Override
    public AttributeType getRootAttributeType() {
        return getAttributeType(TypeQLToken.Type.ATTRIBUTE.toString());
    }

    @Override
    public EntityType putEntityType(String label) {
        return EntityTypeImpl.of(execute(putEntityTypeReq(label)).getPutEntityTypeRes().getEntityType());
    }

    @Override
    @Nullable
    public EntityType getEntityType(String label) {
        ThingType thingType = getThingType(label);
        if (thingType != null && thingType.isEntityType()) return thingType.asEntityType();
        else return null;
    }

    @Override
    public RelationType putRelationType(String label) {
        return RelationTypeImpl.of(execute(putRelationTypeReq(label)).getPutRelationTypeRes().getRelationType());
    }

    @Override
    @Nullable
    public RelationType getRelationType(String label) {
        ThingType thingType = getThingType(label);
        if (thingType != null && thingType.isRelationType()) return thingType.asRelationType();
        else return null;
    }

    @Override
    public AttributeType putAttributeType(String label, AttributeType.ValueType valueType) {
        ConceptProto.ConceptManager.Res res = execute(putAttributeTypeReq(label, valueType.proto()));
        return AttributeTypeImpl.of(res.getPutAttributeTypeRes().getAttributeType());
    }

    @Override
    @Nullable
    public AttributeType getAttributeType(String label) {
        ThingType thingType = getThingType(label);
        if (thingType != null && thingType.isAttributeType()) return thingType.asAttributeType();
        else return null;
    }

    @Override
    @Nullable
    public ThingType getThingType(String label) {
        ConceptProto.ConceptManager.GetThingType.Res res = execute(getThingTypeReq(label)).getGetThingTypeRes();
        switch (res.getResCase()) {
            case THING_TYPE:
                return ThingTypeImpl.of(res.getThingType());
            default:
            case RES_NOT_SET:
                return null;
        }
    }

    @Override
    @Nullable
    public Thing getThing(String iid) {
        ConceptProto.ConceptManager.GetThing.Res res = execute(getThingReq(iid)).getGetThingRes();
        switch (res.getResCase()) {
            case THING:
                return ThingImpl.of(res.getThing());
            default:
            case RES_NOT_SET:
                return null;
        }
    }

    private ConceptProto.ConceptManager.Res execute(TransactionProto.Transaction.Req.Builder req) {
        return transactionExt.execute(req).getConceptManagerRes();
    }
}
