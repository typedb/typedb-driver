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
 * Facilitates construction of ontological elements.
 * Allows you to create schema or ontological elements.
 * These differ from normal graph constructs in two ways:
 * 1. They have a unique Label which identifies them
 * 2. You can link them together into a hierarchical structure
 */
public interface SchemaConcept<SchemaConceptType extends SchemaConcept<SchemaConceptType>> extends Concept<SchemaConceptType> {
    //------------------------------------- Accessors ---------------------------------

    /**
     * Returns the unique label of this Type.
     *
     * @return The unique label of this type
     */
    @CheckReturnValue
    Label label();

    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default SchemaConcept<SchemaConceptType> asSchemaConcept() {
        return this;
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isSchemaConcept() {
        return true;
    }
}
