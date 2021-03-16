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

import grakn.client.api.Transaction;
import grakn.client.api.concept.ConceptManager;
import grakn.client.api.concept.thing.Thing;
import grakn.client.api.concept.type.AttributeType;
import grakn.client.api.concept.type.EntityType;
import grakn.client.api.concept.type.RelationType;
import grakn.client.api.concept.type.ThingType;
import grakn.client.concept.thing.ThingImpl;
import grakn.client.concept.type.AttributeTypeImpl;
import grakn.client.concept.type.EntityTypeImpl;
import grakn.client.concept.type.RelationTypeImpl;
import grakn.client.concept.type.ThingTypeImpl;
import grakn.protocol.ConceptProto;
import grakn.protocol.TransactionProto;
import graql.lang.common.GraqlToken;

import javax.annotation.Nullable;

import static grakn.client.common.RequestBuilder.ConceptManager.getThingReq;
import static grakn.client.common.RequestBuilder.ConceptManager.getThingTypeReq;
import static grakn.client.common.RequestBuilder.ConceptManager.putAttributeTypeReq;
import static grakn.client.common.RequestBuilder.ConceptManager.putEntityTypeReq;
import static grakn.client.common.RequestBuilder.ConceptManager.putRelationTypeReq;

public final class ConceptManagerImpl implements ConceptManager {

    private final Transaction.Extended transactionExt;

    public ConceptManagerImpl(Transaction.Extended transactionExt) {
        this.transactionExt = transactionExt;
    }

    @Override
    public ThingType getRootThingType() {
        return getThingType(GraqlToken.Type.THING.toString());
    }

    @Override
    public EntityType getRootEntityType() {
        return getEntityType(GraqlToken.Type.ENTITY.toString());
    }

    @Override
    public RelationType getRootRelationType() {
        return getRelationType(GraqlToken.Type.RELATION.toString());
    }

    @Override
    public AttributeType getRootAttributeType() {
        return getAttributeType(GraqlToken.Type.ATTRIBUTE.toString());
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
