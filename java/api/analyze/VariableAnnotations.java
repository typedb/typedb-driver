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

import java.util.stream.Stream;

public interface VariableAnnotations {
    /**
     * The variant indicates whether this is a thing variable, type variable, or value variable.
     */
    com.typedb.driver.jni.VariableAnnotationsVariant variant();

    /**
     * @return true if this variable is an Instance variable
     */
    boolean isThing();

    /**
     * @return true if this variable is a Type variable
     */
    boolean isType();

    /**
     * @return true if this variable is a Value variable
     */
    boolean isValue();

    /*
     * @return the possible <code>Type</code>s of instances this variable can hold.
     */
    Stream<? extends Type> asThing();

    /*
     * @return the possible <code>Type</code>s of types this variable can hold.
     */
    Stream<? extends Type> asType();

    /*
     * @return the possible <code>ValueType</code>s of values this variable can hold.
     */
    Stream<String> asValue();
}
