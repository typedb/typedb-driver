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

package com.vaticle.typedb.driver.api.concept.type;

import com.vaticle.typedb.driver.api.concept.Concept;
import com.vaticle.typedb.driver.common.Label;

import javax.annotation.CheckReturnValue;

public interface Type extends Concept {
    /**
     * Retrieves the unique label of the type.
     *
     * <h3>Examples</h3>
     * <pre>
     * type.getLabel();
     * </pre>
     */
    @CheckReturnValue
    Label getLabel();

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    default boolean isType() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    default Type asType() {
        return this;
    }
}
