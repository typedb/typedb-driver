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
using System.Linq;

using TypeDB.Driver.Api;
using TypeDB.Driver.Api.Answer;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Answer
{
    /// <summary>
    /// Contains a row of concepts with a header.
    /// </summary>
    public class ConceptRow : NativeObjectWrapper<Pinvoke.ConceptRow>, IConceptRow
    {
        private int _hash = 0;

        internal ConceptRow(Pinvoke.ConceptRow nativeConceptRow)
            : base(nativeConceptRow)
        {
        }

        /// <inheritdoc/>
        public IEnumerable<string> ColumnNames
        {
            get
            {
                try
                {
                    return new NativeEnumerable<string>(
                        Pinvoke.typedb_driver.concept_row_get_column_names(NativeObject));
                }
                catch (Pinvoke.Error e)
                {
                    throw new TypeDBDriverException(e);
                }
            }
        }

        /// <inheritdoc/>
        public QueryType QueryType
        {
            get
            {
                return QueryTypeExtensions.FromNative(
                    Pinvoke.typedb_driver.concept_row_get_query_type(NativeObject));
            }
        }

        /// <inheritdoc/>
        public IConcept? Get(string columnName)
        {
            try
            {
                Pinvoke.Concept? nativeConcept = Pinvoke.typedb_driver.concept_row_get(NativeObject, columnName);
                if (nativeConcept != null)
                {
                    return Concept.Concept.ConceptOf(nativeConcept);
                }
                return null;
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
        public IConcept? GetIndex(long columnIndex)
        {
            try
            {
                Pinvoke.Concept? nativeConcept = Pinvoke.typedb_driver.concept_row_get_index(NativeObject, (uint)columnIndex);
                if (nativeConcept != null)
                {
                    return Concept.Concept.ConceptOf(nativeConcept);
                }
                return null;
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
        public IEnumerable<IConcept> Concepts
        {
            get
            {
                try
                {
                    return new NativeEnumerable<Pinvoke.Concept>(
                        Pinvoke.typedb_driver.concept_row_get_concepts(NativeObject))
                        .Select(nativeConcept => Concept.Concept.ConceptOf(nativeConcept));
                }
                catch (Pinvoke.Error e)
                {
                    throw new TypeDBDriverException(e);
                }
            }
        }

        /// <inheritdoc/>
        public override string ToString()
        {
            return Pinvoke.typedb_driver.concept_row_to_string(NativeObject);
        }

        /// <inheritdoc/>
        public override bool Equals(object? obj)
        {
            if (ReferenceEquals(this, obj))
            {
                return true;
            }

            if (obj == null || GetType() != obj.GetType())
            {
                return false;
            }

            ConceptRow that = (ConceptRow)obj;
            return Pinvoke.typedb_driver.concept_row_equals(NativeObject, that.NativeObject);
        }

        /// <inheritdoc/>
        public override int GetHashCode()
        {
            if (_hash == 0)
            {
                _hash = ComputeHash();
            }
            return _hash;
        }

        private int ComputeHash()
        {
            return Concepts.GetHashCode();
        }
    }
}
