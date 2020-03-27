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

import javax.annotation.CheckReturnValue;

/**
 * A Type represents any ontological element in the graph.
 * Types are used to model the behaviour of Thing and how they relate to each other.
 * They also aid in categorising Thing to different types.
 */
public interface Type<
        SomeType extends Type<SomeType, SomeThing>,
        SomeThing extends Thing<SomeThing, SomeType>>
        extends SchemaConcept<SomeType> {

    @Deprecated
    @CheckReturnValue
    @Override
    default Type<SomeType, SomeThing> asType() {
        return this;
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isType() {
        return true;
    }
}
