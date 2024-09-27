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
import com.typedb.driver.api.concept.Concept;

import javax.annotation.CheckReturnValue;
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
     * Retrieves a concept for a given column name (variable).
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptRow.get(columnName);
     * </pre>
     *
     * @param columnName the variable (column name from ``column_names``)
     */
    @CheckReturnValue
    Concept get(String columnName);
    
    /**
     * Retrieves a concept for a given index of the header (<code>columnNames</code>).
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptRow.getIndex(columnIndex);
     * </pre>
     *
     * @param columnIndex the column index
     */
    @CheckReturnValue
    Concept getIndex(long columnIndex);

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
