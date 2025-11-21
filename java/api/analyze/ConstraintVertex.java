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

package com.typedb.driver.api.analyze;

import com.typedb.driver.api.concept.type.Type;
import com.typedb.driver.api.concept.value.Value;

/**
 * The answer to a TypeDB query is a set of concepts which satisfy the constraints in the query.
 * A ConstraintVertex is either a variable, or some identifier of the concept.
 * A <code>Variable</code> is a vertex the query must match and return.
 * A <code>Label</code> uniquely identifies a type
 * A <code>Value</code> represents a primitive value literal in TypeDB.
 * A <code>NamedRole</code> vertex is used in links & relates constraints, as multiple relations may have roles with the same name.
 */
public interface ConstraintVertex {
    /**
     * Checks if this vertex is a variable.
     *
     * @return true if this vertex is a variable
     */
    boolean isVariable();

    /**
     * Checks if this vertex is a label.
     *
     * @return true if this vertex is a label
     */
    boolean isLabel();

    /**
     * Checks if this vertex is a value.
     *
     * @return true if this vertex is a value
     */
    boolean isValue();

    /**
     * Checks if this vertex is a named role.
     *
     * @return true if this vertex is a named role
     */
    boolean isNamedRole();

    /**
     * Down-casts this variable to a vertex.
     *
     * @return the variable
     */
    com.typedb.driver.jni.Variable asVariable();

    /**
     * Down-casts this vertex to a type label.
     *
     * @return the type representation of this vertex
     * @throws IllegalStateException if this vertex is not a label
     */
    Type asLabel();

    /**
     * Down-casts this vertex to a value.
     *
     * @return the value representation of this vertex
     */
    Value asValue();


    /**
     * Down-casts this vertex to a NamedRole.
     * This is an internal variable injected to handle ambiguity in unscoped role-names.
     *
     * @return This vertex down-casted to NamedRole.
     */
    NamedRole asNamedRole();
}
