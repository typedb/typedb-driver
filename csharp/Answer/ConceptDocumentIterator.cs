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

using System.Collections;
using System.Collections.Generic;
using System.Linq;

using TypeDB.Driver.Api.Answer;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Answer
{
    /// <summary>
    /// Represents an iterator over concept documents (represented as JSON) returned as a server answer.
    /// </summary>
    public class ConceptDocumentIterator : QueryAnswer, IConceptDocumentIterator
    {
        private readonly NativeEnumerable<string> _nativeEnumerable;

        internal ConceptDocumentIterator(Pinvoke.QueryAnswer nativeAnswer)
            : base(nativeAnswer)
        {
            // query_answer_into_documents CONSUMES the native QueryAnswer via take_ownership in Rust.
            // We must release ownership on the C# wrapper BEFORE calling into_documents to prevent
            // the wrapper's finalizer from trying to free an already-freed pointer.
            var releasedAnswer = nativeAnswer.Released();
            _nativeEnumerable = new NativeEnumerable<string>(
                Pinvoke.typedb_driver.query_answer_into_documents(releasedAnswer));
        }

        /// <inheritdoc/>
        public IEnumerator<IJSON> GetEnumerator()
        {
            return _nativeEnumerable
                .Select(jsonString => (IJSON)JSON.Parse(jsonString))
                .GetEnumerator();
        }

        /// <inheritdoc/>
        IEnumerator IEnumerable.GetEnumerator()
        {
            return GetEnumerator();
        }
    }
}
