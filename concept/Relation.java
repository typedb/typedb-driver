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
import grakn.client.concept.remote.RemoteRelation;
import grakn.client.concept.remote.RemoteRelationType;

import javax.annotation.CheckReturnValue;

/**
 * Encapsulates relations between Thing
 * A relation which is an instance of a RelationType defines how instances may relate to one another.
 * It represents how different entities relate to one another.
 * Relation are used to model n-ary relations between instances.
 */
public interface Relation extends Thing<Relation, RelationType, RemoteRelation, RemoteRelationType> {
    //------------------------------------- Accessors ----------------------------------

    /**
     * Retrieve the associated RelationType for this Relation.
     *
     * @return The associated RelationType for this Relation.
     * @see RelationType
     */
    @Override
    RelationType type();

    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default Relation asRelation() {
        return this;
    }

    @Override
    default RemoteRelation asRemote(GraknClient.Transaction tx) {
        return RemoteRelation.of(tx, id());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isRelation() {
        return true;
    }
}
