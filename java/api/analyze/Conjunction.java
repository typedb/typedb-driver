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

import java.util.stream.Stream;

public interface Conjunction {
    /**
     * The <code>Constraint</code>s in the conjunction.
     *
     * @return stream of constraints
     */
    Stream<? extends Constraint> constraints();

    /**
     * The variables that have annotations in this conjunction.
     *
     * @return stream of annotated variables
     */
    Stream<com.typedb.driver.jni.Variable> annotated_variables();

    /**
     * Gets the annotations for a specific variable in this conjunction.
     *
     * @param variable the variable to get annotations for
     * @return the annotations
     */
    VariableAnnotations variable_annotations(com.typedb.driver.jni.Variable variable);
}
