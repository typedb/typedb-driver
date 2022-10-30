/*
 * Copyright (C) 2022 Vaticle
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

package com.vaticle.typedb.client.concept.type;

import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.type.EntityType;
import com.vaticle.typedb.client.common.Label;
import com.vaticle.typedb.client.concept.thing.EntityImpl;
import com.vaticle.typedb.client.concept.thing.ThingImpl;
import com.vaticle.typedb.protocol.ConceptProto;

import java.util.stream.Stream;

import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.EntityType.createReq;

public class EntityTypeImpl extends ThingTypeImpl implements EntityType {

    EntityTypeImpl(Label label, boolean isRoot, boolean isAbstract) {
        super(label, isRoot, isAbstract);
    }

    public static EntityTypeImpl of(ConceptProto.Type proto) {
        return new EntityTypeImpl(Label.of(proto.getLabel()), proto.getIsRoot(), proto.getIsAbstract());
    }

    @Override
    public EntityTypeImpl.Remote asRemote(TypeDBTransaction transaction) {
        return new EntityTypeImpl.Remote(transaction, getLabel(), isRoot(), isAbstract());
    }

    @Override
    public EntityTypeImpl asEntityType() {
        return this;
    }

    public static class Remote extends ThingTypeImpl.Remote implements EntityType.Remote {

        Remote(TypeDBTransaction transaction, Label label, boolean isRoot, boolean isAbstract) {
            super(transaction, label, isRoot, isAbstract);
        }

        @Override
        public EntityTypeImpl.Remote asRemote(TypeDBTransaction transaction) {
            return new EntityTypeImpl.Remote(transaction, getLabel(), isRoot(), isAbstract());
        }

        @Override
        public final EntityImpl create() {
            return EntityImpl.of(execute(createReq(getLabel())).getEntityTypeCreateRes().getEntity());
        }

        @Override
        public final void setSupertype(EntityType entityType) {
            super.setSupertype(entityType);
        }

        @Override
        public final Stream<EntityTypeImpl> getSubtypes() {
            return super.getSubtypes().map(ThingTypeImpl::asEntityType);
        }

        @Override
        public final Stream<EntityTypeImpl> getSubtypesExplicit() {
            return super.getSubtypesExplicit().map(ThingTypeImpl::asEntityType);
        }

        @Override
        public final Stream<EntityImpl> getInstances() {
            return super.getInstances().map(ThingImpl::asEntity);
        }

        @Override
        public final Stream<EntityImpl> getInstancesExplicit() {
            return super.getInstancesExplicit().map(ThingImpl::asEntity);
        }

        @Override
        public EntityTypeImpl.Remote asEntityType() {
            return this;
        }
    }
}
