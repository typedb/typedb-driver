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

using System.Collections.Generic;

using TypeDB.Driver.Api;
using TypeDB.Driver.Api.Analyze;

namespace TypeDB.Driver.Api.Answer
{
    /// <summary>
    /// Contains a row of concepts with a header.
    /// </summary>
    public interface IConceptRow
    {
        /// <summary>
        /// Produces a collection of all column names (variables) in the header of this <see cref="IConceptRow"/>.
        /// Shared between all the rows in a QueryAnswer.
        /// </summary>
        /// <example>
        /// <code>
        /// conceptRow.ColumnNames
        /// </code>
        /// </example>
        IEnumerable<string> ColumnNames { get; }

        /// <summary>
        /// Retrieves the executed query's type of this <see cref="IConceptRow"/>.
        /// Shared between all the rows in a QueryAnswer.
        /// </summary>
        /// <example>
        /// <code>
        /// conceptRow.QueryType
        /// </code>
        /// </example>
        QueryType QueryType { get; }

        /// <summary>
        /// Retrieves a concept for a given column name (variable).
        /// Returns null if the variable has an empty answer.
        /// Throws an exception if the variable is not present.
        /// </summary>
        /// <param name="columnName">The variable (column name from <see cref="ColumnNames"/>)</param>
        /// <returns>The concept for the given column name, or null if empty.</returns>
        /// <exception cref="Common.TypeDBDriverException">If the variable is not present.</exception>
        /// <example>
        /// <code>
        /// conceptRow.Get(columnName)
        /// </code>
        /// </example>
        IConcept? Get(string columnName);

        /// <summary>
        /// Retrieves a concept for a given index of the header (<see cref="ColumnNames"/>).
        /// Returns null if the index points to an empty answer.
        /// Throws an exception if the index is not in the row's range.
        /// </summary>
        /// <param name="columnIndex">The column index</param>
        /// <returns>The concept at the given index, or null if empty.</returns>
        /// <exception cref="Common.TypeDBDriverException">If the index is out of range.</exception>
        /// <example>
        /// <code>
        /// conceptRow.GetIndex(columnIndex)
        /// </code>
        /// </example>
        IConcept? GetIndex(long columnIndex);

        /// <summary>
        /// Produces a collection over all concepts in this <see cref="IConceptRow"/>, skipping empty results.
        /// </summary>
        /// <example>
        /// <code>
        /// conceptRow.Concepts
        /// </code>
        /// </example>
        IEnumerable<IConcept> Concepts { get; }

        /// <summary>
        /// Retrieves the query structure pipeline if available.
        /// Only available if the query was executed with IncludeQueryStructure option enabled.
        /// </summary>
        /// <example>
        /// <code>
        /// conceptRow.QueryStructure
        /// </code>
        /// </example>
        IPipeline? QueryStructure { get; }
    }
}
