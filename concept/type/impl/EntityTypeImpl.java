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

package grakn.client.concept.type.impl;

import grakn.client.Grakn;
import grakn.client.concept.thing.impl.EntityImpl;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.EntityType;
import grakn.protocol.ConceptProto;

import java.util.stream.Stream;

public class EntityTypeImpl extends ThingTypeImpl implements EntityType {

    EntityTypeImpl(String label, boolean isRoot) {
        super(label, isRoot);
    }

    public static EntityTypeImpl of(ConceptProto.Type typeProto) {
        return new EntityTypeImpl(typeProto.getLabel(), typeProto.getRoot());
    }

    @Override
    public EntityTypeImpl.Remote asRemote(Grakn.Transaction transaction) {
        return new EntityTypeImpl.Remote(transaction, getLabel(), isRoot());
    }

    @Override
    public EntityTypeImpl asEntityType() {
        return this;
    }

    public static class Remote extends ThingTypeImpl.Remote implements EntityType.Remote {

        Remote(Grakn.Transaction transaction, String label, boolean isRoot) {
            super(transaction, label, isRoot);
        }

        @Override
        public final void setSupertype(EntityType superEntityType) {
            this.setSupertypeExecute(superEntityType);
        }

        @Override
        public EntityTypeImpl getSupertype() {
            final ThingTypeImpl supertype = super.getSupertype();
            return supertype != null ? supertype.asEntityType() : null;
        }

        @Override
        public final Stream<EntityImpl> getInstances() {
            return super.getInstances(ThingImpl::asEntity);
        }

        @Override
        public EntityTypeImpl.Remote asRemote(Grakn.Transaction transaction) {
            return new EntityTypeImpl.Remote(transaction, getLabel(), isRoot());
        }

        @Override
        public final Stream<EntityTypeImpl> getSupertypes() {
            return super.getSupertypes(TypeImpl::asEntityType);
        }

        @Override
        public final Stream<EntityTypeImpl> getSubtypes() {
            return super.getSubtypes(TypeImpl::asEntityType);
        }

        @Override
        public final EntityImpl create() {
            final ConceptProto.Type.Req.Builder method = ConceptProto.Type.Req.newBuilder()
                    .setEntityTypeCreateReq(ConceptProto.EntityType.Create.Req.getDefaultInstance());
            return EntityImpl.of(execute(method).getEntityTypeCreateRes().getEntity());
        }

        @Override
        public EntityTypeImpl.Remote asEntityType() {
            return this;
        }
    }
}
