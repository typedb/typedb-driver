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

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A representation of a query pipeline.
 */
public interface Pipeline {
    /**
     * @return A stream of the stages making up the pipeline
     */
    Stream<? extends PipelineStage> stages();

    /**
     * Gets the name of the specified variable, if it has one.
     *
     * @return the name, if any
     */
    Optional<String> getVariableName(Variable variable);

    /**
     * @param conjunctionID The ConjunctionID of the conjunction to retrieve
     * @return The corresponding <code>Conjunction</code> instance
     */
    Optional<? extends Conjunction> conjunction(ConjunctionID conjunctionID);
}
