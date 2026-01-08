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

using TypeDB.Driver.Common;

using ConceptError = TypeDB.Driver.Common.Error.Concept;

namespace TypeDB.Driver.Api.Answer
{
    /// <summary>
    /// General answer on a query returned by a server. Can be a simple Ok response or a collection of concepts.
    /// </summary>
    public interface IQueryAnswer
    {
        /// <summary>
        /// Retrieves the executed query's type of this <see cref="IQueryAnswer"/>.
        /// </summary>
        /// <example>
        /// <code>
        /// queryAnswer.QueryType
        /// </code>
        /// </example>
        QueryType QueryType { get; }

        /// <summary>
        /// Checks if the query answer is an <see cref="IOkQueryAnswer"/>.
        /// </summary>
        /// <example>
        /// <code>
        /// queryAnswer.IsOk
        /// </code>
        /// </example>
        bool IsOk => false;

        /// <summary>
        /// Checks if the query answer is a <see cref="IConceptRowIterator"/>.
        /// </summary>
        /// <example>
        /// <code>
        /// queryAnswer.IsConceptRows
        /// </code>
        /// </example>
        bool IsConceptRows => false;

        /// <summary>
        /// Checks if the query answer is a <see cref="IConceptDocumentIterator"/>.
        /// </summary>
        /// <example>
        /// <code>
        /// queryAnswer.IsConceptDocuments
        /// </code>
        /// </example>
        bool IsConceptDocuments => false;

        /// <summary>
        /// Casts the query answer to <see cref="IOkQueryAnswer"/>.
        /// </summary>
        /// <returns>The <see cref="IOkQueryAnswer"/> if this is an Ok response.</returns>
        /// <exception cref="Common.TypeDBDriverException">If the answer is not an Ok response.</exception>
        /// <example>
        /// <code>
        /// queryAnswer.AsOk()
        /// </code>
        /// </example>
        IOkQueryAnswer AsOk()
        {
            throw new TypeDBDriverException(ConceptError.INVALID_QUERY_ANSWER_CASTING,
                GetType().Name, nameof(IOkQueryAnswer));
        }

        /// <summary>
        /// Casts the query answer to <see cref="IConceptRowIterator"/>.
        /// </summary>
        /// <returns>The <see cref="IConceptRowIterator"/> if this is a concept rows response.</returns>
        /// <exception cref="TypeDBDriverException">If the answer is not a concept rows response.</exception>
        /// <example>
        /// <code>
        /// queryAnswer.AsConceptRows()
        /// </code>
        /// </example>
        IConceptRowIterator AsConceptRows()
        {
            throw new TypeDBDriverException(ConceptError.INVALID_QUERY_ANSWER_CASTING,
                GetType().Name, nameof(IConceptRowIterator));
        }

        /// <summary>
        /// Casts the query answer to <see cref="IConceptDocumentIterator"/>.
        /// </summary>
        /// <returns>The <see cref="IConceptDocumentIterator"/> if this is a concept documents response.</returns>
        /// <exception cref="TypeDBDriverException">If the answer is not a concept documents response.</exception>
        /// <example>
        /// <code>
        /// queryAnswer.AsConceptDocuments()
        /// </code>
        /// </example>
        IConceptDocumentIterator AsConceptDocuments()
        {
            throw new TypeDBDriverException(ConceptError.INVALID_QUERY_ANSWER_CASTING,
                GetType().Name, nameof(IConceptDocumentIterator));
        }
    }
}
