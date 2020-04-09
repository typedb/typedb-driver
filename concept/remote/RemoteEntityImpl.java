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
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Entity;
import grakn.client.concept.thing.ThingImpl;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;

/**
 * Client implementation of Entity
 */
public class RemoteEntityImpl extends ThingImpl.RemoteThingImpl<Entity, EntityType> implements Entity.Remote {

    public RemoteEntityImpl(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    @Override
    public final EntityType.Remote type() {
        return (EntityType.Remote) super.type();
    }

    @Override
    public Entity.Remote has(Attribute<?> attribute) {
        return (Entity.Remote) super.has(attribute);
    }

    @Override
    public Entity.Remote unhas(Attribute<?> attribute) {
        return (Entity.Remote) super.unhas(attribute);
    }

    @Override
    protected final Entity.Remote asCurrentBaseType(Concept.Remote<?> other) {
        return other.asEntity();
    }
}
