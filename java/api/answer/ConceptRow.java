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

package com.typedb.driver.api.answer;

import com.typedb.driver.api.QueryType;
import com.typedb.driver.api.analyze.Pipeline;
import com.typedb.driver.api.concept.Concept;
import com.typedb.driver.common.exception.TypeDBDriverException;

import javax.annotation.CheckReturnValue;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Contains a row of concepts with a header.
 */
public interface ConceptRow {
    /**
     * Produces a stream over all column names (variables) in the header of this <code>ConceptRow</code>.
     * Shared between all the rows in a QueryAnswer.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptRow.columnNames();
     * </pre>
     */
    @CheckReturnValue
    Stream<String> columnNames();

    /**
     * Retrieves the executed query's type of this <code>ConceptRow</code>.
     * Shared between all the rows in a QueryAnswer.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptRow.getQueryType();
     * </pre>
     */
    @CheckReturnValue
    QueryType getQueryType();

    /**
     * Retrieves the executed query's structure of this <code>ConceptRow</code>.
     * Shared between all the rows in a QueryAnswer.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptRow.getQueryStructure();
     * </pre>
     */
    Optional<Pipeline> getQueryStructure();

    /**
     * Retrieves a concept for a given column name (variable).
     * Returns an empty <code>Optional</code> if the variable has an empty answer.
     * Throws an exception if the variable is not present.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptRow.get(columnName);
     * </pre>
     *
     * @param columnName the variable (column name from ``column_names``)
     */
    @CheckReturnValue
    Optional<Concept> get(String columnName) throws TypeDBDriverException;

    /**
     * Retrieves a concept for a given index of the header (<code>columnNames</code>).
     * Returns an empty <code>Optional</code> if the index points to an empty answer.
     * Throws an exception if the index is not in the row's range.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptRow.getIndex(columnIndex);
     * </pre>
     *
     * @param columnIndex the column index
     */
    @CheckReturnValue
    Optional<Concept> getIndex(long columnIndex) throws TypeDBDriverException;

    /**
     * Retrieve the <code>ConjunctionID</code>s of <code>Conjunction</code>s that answered this row.
     */
    Optional<Stream<com.typedb.driver.jni.ConjunctionID>> involvedConjunctions();

    /**
     * Produces a stream over all concepts in this `ConceptRow`, skipping empty results.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptRow.concepts();
     * </pre>
     */
    @CheckReturnValue
    Stream<? extends Concept> concepts();
}
