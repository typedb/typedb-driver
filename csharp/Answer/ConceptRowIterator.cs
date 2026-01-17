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

using TypeDB.Driver.Api.Answer;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Answer
{
    /// <summary>
    /// Represents an iterator over <see cref="IConceptRow"/>s returned as a server answer.
    /// </summary>
    public class ConceptRowIterator : QueryAnswer, IConceptRowIterator
    {
        private readonly Pinvoke.ConceptRowIterator _nativeIterator;

        internal ConceptRowIterator(Pinvoke.QueryAnswer nativeAnswer)
            : base(nativeAnswer)
        {
            // IntoRows() handles ownership transfer automatically via SWIG typemap
            _nativeIterator = nativeAnswer.IntoRows();
        }

        /// <inheritdoc/>
        public IEnumerator<IConceptRow> GetEnumerator()
        {
            return new ConceptRowEnumerator(_nativeIterator);
        }

        /// <inheritdoc/>
        IEnumerator IEnumerable.GetEnumerator()
        {
            return GetEnumerator();
        }

        private class ConceptRowEnumerator : IEnumerator<IConceptRow>
        {
            private readonly Pinvoke.ConceptRowIterator _iterator;
            private IConceptRow? _current;

            public ConceptRowEnumerator(Pinvoke.ConceptRowIterator iterator)
            {
                _iterator = iterator;
                _current = null;
            }

            public IConceptRow Current => _current!;

            object IEnumerator.Current => Current;

            public bool MoveNext()
            {
                try
                {
                    Pinvoke.ConceptRow? nativeRow = Pinvoke.typedb_driver.concept_row_iterator_next(_iterator);
                    if (nativeRow != null)
                    {
                        _current = new ConceptRow(nativeRow);
                        return true;
                    }
                    _current = null;
                    return false;
                }
                catch (Pinvoke.Error e)
                {
                    throw new TypeDBDriverException(e);
                }
            }

            public void Reset()
            {
                throw new System.NotSupportedException("Cannot reset a native iterator");
            }

            public void Dispose()
            {
                // Native iterator is managed by the parent ConceptRowIterator
            }
        }
    }
}
