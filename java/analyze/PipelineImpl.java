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

import com.typedb.driver.api.analyze.ConjunctionID;
import com.typedb.driver.api.analyze.Pipeline;
import com.typedb.driver.api.analyze.Variable;
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.NativeObject;

import java.util.Optional;
import java.util.stream.Stream;

public class PipelineImpl extends NativeObject<com.typedb.driver.jni.Pipeline> implements Pipeline {

    public PipelineImpl(com.typedb.driver.jni.Pipeline nativeObject) {
        super(nativeObject);
    }

    public Stream<PipelineStageImpl> stages() {
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.pipeline_stages(nativeObject)).stream().map(PipelineStageImpl::of);
    }

    @Override
    public Optional<String> getVariableName(Variable variable) {
        return Optional.ofNullable(com.typedb.driver.jni.typedb_driver.variable_get_name(nativeObject, ((VariableImpl) variable).nativeObject));
    }

    @Override
    public Optional<ConjunctionImpl> conjunction(ConjunctionID conjunctionID) {
        com.typedb.driver.jni.Conjunction nativeConjunction = com.typedb.driver.jni.typedb_driver.pipeline_get_conjunction(nativeObject, ((ConjunctionIDImpl) conjunctionID).nativeObject);
        if (nativeConjunction == null) {
            return Optional.empty();
        } else {
            return Optional.of(new ConjunctionImpl(nativeConjunction));
        }
    }
}
