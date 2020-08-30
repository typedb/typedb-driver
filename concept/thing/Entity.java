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

import grakn.client.Grakn;
import grakn.client.concept.type.EntityType;

public interface Entity extends Thing {

    @Override
    Entity.Remote asRemote(Grakn.Transaction transaction);

    interface Local extends Thing.Local, Entity {

        @Override
        default Entity.Local asEntity() {
            return this;
        }

        @Override
        Entity.Remote asRemote(final Grakn.Transaction transaction);
    }

    interface Remote extends Thing.Remote, Entity {

        @Override
        EntityType.Local getType();

        @Override
        default Entity.Remote asRemote(Grakn.Transaction transaction) {
            return this;
        }

        @Override
        default Entity.Remote asEntity() {
            return this;
        }
    }
}
