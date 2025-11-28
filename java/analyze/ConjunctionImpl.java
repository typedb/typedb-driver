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

package com.typedb.driver.analyze;

import com.typedb.driver.api.analyze.Conjunction;
import com.typedb.driver.api.analyze.Variable;
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.NativeObject;

import java.util.stream.Stream;

public class ConjunctionImpl extends NativeObject<com.typedb.driver.jni.Conjunction> implements Conjunction {
    protected ConjunctionImpl(com.typedb.driver.jni.Conjunction nativeObject) {
        super(nativeObject);
    }

    public Stream<ConstraintImpl> constraints() {
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.conjunction_get_constraints(nativeObject)).stream().map(ConstraintImpl::of);
    }

    public Stream<VariableImpl> annotated_variables() {
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.conjunction_get_annotated_variables(nativeObject)).stream().map(VariableImpl::new);
    }

    public VariableAnnotationsImpl variable_annotations(Variable variable) {
        return new VariableAnnotationsImpl(com.typedb.driver.jni.typedb_driver.conjunction_get_variable_annotations(nativeObject, ((VariableImpl) variable).nativeObject));
    }
}
