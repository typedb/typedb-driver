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

import grakn.client.GraknClient;
import grakn.client.concept.remote.RemoteEntityType;

import javax.annotation.CheckReturnValue;

/**
 * SchemaConcept used to represent categories.
 * An ontological element which represents categories instances can fall within.
 * Any instance of a Entity Type is called an Entity.
 */
public interface EntityType extends UserType<EntityType, Entity> {

    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default EntityType asEntityType() {
        return this;
    }

    @CheckReturnValue
    @Override
    default RemoteEntityType asRemote(GraknClient.Transaction tx) {
        return RemoteEntityType.of(tx, id());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isEntityType() {
        return true;
    }
}
