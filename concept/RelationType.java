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
 * An ontological element which categorises how Things may relate to each other.
 * A RelationType defines how Type may relate to one another.
 * They are used to model and categorise n-ary Relations.
 */
public interface RelationType<
        SomeType extends RelationType<SomeType, SomeThing>,
        SomeThing extends Relation<SomeThing, SomeType>>
        extends Type<SomeType, SomeThing> {
    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default RelationType asRelationType() {
        return this;
    }

    @Override
    default RemoteRelationType asRemote(GraknClient.Transaction tx) {
        return RemoteRelationType.of(tx, id());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isRelationType() {
        return true;
    }
}
