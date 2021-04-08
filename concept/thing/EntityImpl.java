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

package grakn.client.concept.thing;

import grakn.client.api.GraknTransaction;
import grakn.client.api.concept.thing.Entity;
import grakn.client.concept.type.EntityTypeImpl;
import grakn.common.collection.Bytes;
import grakn.protocol.ConceptProto;

public class EntityImpl extends ThingImpl implements Entity {

    private final EntityTypeImpl type;

    EntityImpl(String iid, boolean isInferred, EntityTypeImpl type) {
        super(iid, isInferred);
        this.type = type;
    }

    public static EntityImpl of(ConceptProto.Thing protoThing) {
        return new EntityImpl(Bytes.bytesToHexString(protoThing.getIid().toByteArray()), protoThing.getInferred(), EntityTypeImpl.of(protoThing.getType()));
    }

    @Override
    public EntityTypeImpl getType() {
        return type;
    }

    @Override
    public EntityImpl.Remote asRemote(GraknTransaction transaction) {
        return new EntityImpl.Remote(transaction, getIID(), isInferred(), type);
    }

    @Override
    public final EntityImpl asEntity() {
        return this;
    }

    public static class Remote extends ThingImpl.Remote implements Entity.Remote {

        private final EntityTypeImpl type;

        public Remote(GraknTransaction transaction, String iid, boolean isInferred, EntityTypeImpl type) {
            super(transaction, iid, isInferred);
            this.type = type;
        }

        @Override
        public EntityImpl.Remote asRemote(GraknTransaction transaction) {
            return new EntityImpl.Remote(transaction, getIID(), isInferred(), type);
        }

        @Override
        public EntityTypeImpl getType() {
            return type;
        }

        @Override
        public final EntityImpl.Remote asEntity() {
            return this;
        }
    }
}
