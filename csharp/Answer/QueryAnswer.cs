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

using TypeDB.Driver.Api;
using TypeDB.Driver.Api.Answer;
using TypeDB.Driver.Common;

using DriverError = TypeDB.Driver.Common.Error.Driver;

namespace TypeDB.Driver.Answer
{
    /// <summary>
    /// Base implementation for query answers. Contains factory method for creating appropriate answer types.
    /// </summary>
    public abstract class QueryAnswer : IQueryAnswer
    {
        /// <summary>
        /// The query type of this answer.
        /// </summary>
        public QueryType QueryType { get; }

        protected QueryAnswer(Pinvoke.QueryAnswer nativeAnswer)
        {
            QueryType = QueryTypeExtensions.FromNative(
                Pinvoke.typedb_driver.query_answer_get_query_type(nativeAnswer));
        }

        /// <summary>
        /// Factory method to create the appropriate IQueryAnswer implementation from a native QueryAnswer.
        /// </summary>
        /// <param name="nativeAnswer">The native QueryAnswer object.</param>
        /// <returns>The appropriate IQueryAnswer implementation.</returns>
        /// <exception cref="TypeDBDriverException">If the answer type is not recognized.</exception>
        public static IQueryAnswer Of(Pinvoke.QueryAnswer nativeAnswer)
        {
            if (Pinvoke.typedb_driver.query_answer_is_ok(nativeAnswer))
            {
                return new OkQueryAnswer(nativeAnswer);
            }
            else if (Pinvoke.typedb_driver.query_answer_is_concept_row_stream(nativeAnswer))
            {
                return new ConceptRowIterator(nativeAnswer);
            }
            else if (Pinvoke.typedb_driver.query_answer_is_concept_document_stream(nativeAnswer))
            {
                return new ConceptDocumentIterator(nativeAnswer);
            }

            throw new TypeDBDriverException(DriverError.UNEXPECTED_NATIVE_VALUE);
        }
    }
}
