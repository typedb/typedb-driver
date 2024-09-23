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

package com.vaticle.typedb.driver.api.answer;

import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;

import javax.annotation.CheckReturnValue;

import static com.vaticle.typedb.common.util.Objects.className;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Concept.INVALID_QUERY_ANSWER_CASTING;

/**
 * TODO: docs
 */
public interface QueryAnswer {
    /**
     * Checks if the query answer is an <code>Ok</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isOk();
     * </pre>
     */
    @CheckReturnValue
    default boolean isOk() {
        return false;
    }

    /**
     * Checks if the query answer is a <code>ConceptRowsStream</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isConceptRowsStream();
     * </pre>
     */
    @CheckReturnValue
    default boolean isConceptRowsStream() {
        return false;
    }

    /**
     * Checks if the query answer is a <code>ConceptTreesStream</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isConceptTreesStream();
     * </pre>
     */
    @CheckReturnValue
    default boolean isConceptTreesStream() {
        return false;
    }

    /**
     * Casts the query answer to <code>OkQueryAnswer</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asOk();
     * </pre>
     */
    default OkQueryAnswer asOk() {
        throw new TypeDBDriverException(INVALID_QUERY_ANSWER_CASTING, className(this.getClass()), className(OkQueryAnswer.class));
    }

    /**
     * Casts the query answer to <code>ConceptRowsStreamQueryAnswer</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asConceptRowsStream();
     * </pre>
     */
    default ConceptRowsStreamQueryAnswer asConceptRowsStream() {
        throw new TypeDBDriverException(INVALID_QUERY_ANSWER_CASTING, className(this.getClass()), className(ConceptRowsStreamQueryAnswer.class));
    }

    /**
     * Casts the query answer to <code>ConceptTreesStreamQueryAnswer</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asConceptTreesStream();
     * </pre>
     */
    default ConceptTreesStreamQueryAnswer asConceptTreesStream() {
        throw new TypeDBDriverException(INVALID_QUERY_ANSWER_CASTING, className(this.getClass()), className(ConceptTreesStreamQueryAnswer.class));
    }
}
