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

package grakn.client.concept.thing.impl;

import grakn.client.concept.Concepts;
import grakn.client.concept.thing.Entity;
import grakn.client.concept.type.EntityType;
import grakn.protocol.ConceptProto;

public abstract class EntityImpl {
    /**
     * Client implementation of Entity
     */
    public static class Local extends ThingImpl.Local implements Entity.Local {

        public Local(final ConceptProto.Thing thing) {
            super(thing);
        }

        public EntityType.Local getType() {
            return super.getType().asEntityType();
        }
    }

    /**
     * Client implementation of Entity
     */
    public static class Remote extends ThingImpl.Remote implements Entity.Remote {

        public Remote(final Concepts concepts, final String iid) {
            super(concepts, iid);
        }

        @Override
        public final EntityType.Remote getType() {
            return (EntityType.Remote) super.getType();
        }
    }
}
