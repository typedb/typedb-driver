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

import grakn.client.concept.remote.RemoteThing;
import grakn.client.concept.remote.RemoteType;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * A data instance in the graph belonging to a specific Type
 * Instances represent data in the graph.
 * Every instance belongs to a Type which serves as a way of categorising them.
 * Instances can relate to one another via Relation
 */
public interface Thing<
        SomeThing extends Thing<SomeThing, SomeType, SomeRemoteThing, SomeRemoteType>,
        SomeType extends Type<SomeType, SomeThing, SomeRemoteType, SomeRemoteThing>,
        SomeRemoteThing extends RemoteThing<SomeRemoteThing, SomeRemoteType, SomeThing, SomeType>,
        SomeRemoteType extends RemoteType<SomeRemoteThing, SomeRemoteType, SomeThing, SomeType>>
        extends Concept<SomeThing, SomeRemoteThing> {
    //------------------------------------- Accessors ----------------------------------

    /**
     * Return the Type of the Concept.
     *
     * @return A Type which is the type of this concept. This concept is an instance of that type.
     */
    @CheckReturnValue
    Type<SomeType, SomeThing> type();

    /**
     * Used to indicate if this Thing has been created as the result of a Rule inference.
     *
     * @return true if this Thing exists due to a rule
     * @see Rule
     */
    boolean isInferred();

    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default Thing<SomeThing, SomeType> asThing() {
        return this;
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isThing() {
        return true;
    }
}
