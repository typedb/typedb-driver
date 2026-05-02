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
using TypeDB.Driver.Common.Validation;

namespace TypeDB.Driver.Api
{
    /// <summary>
    /// TypeDB query options. <c>QueryOptions</c> can be used to override
    /// the default server behaviour for executed queries.
    /// </summary>
    public class QueryOptions : NativeObjectWrapper<Pinvoke.QueryOptions>
    {
        /// <summary>
        /// Produces a new <c>QueryOptions</c> object.
        /// </summary>
        /// <example>
        /// <code>
        /// QueryOptions options = new QueryOptions
        /// {
        ///     PrefetchSize = 50,
        ///     IncludeQueryStructure = true
        /// };
        /// </code>
        /// </example>
        public QueryOptions()
            : base(Pinvoke.typedb_driver.query_options_new())
        {
        }

        /// <summary>
        /// Gets or sets whether to include instance types in ConceptRow answers.
        /// If set, specifies if types should be included in instance structs returned in ConceptRow answers.
        /// This option allows reducing the amount of unnecessary data transmitted.
        /// </summary>
        /// <example>
        /// <code>
        /// options.IncludeInstanceTypes = true;
        /// </code>
        /// </example>
        public bool? IncludeInstanceTypes
        {
            get
            {
                if (Pinvoke.typedb_driver.query_options_has_include_instance_types(NativeObject))
                {
                    return Pinvoke.typedb_driver.query_options_get_include_instance_types(NativeObject);
                }
                return null;
            }
            set
            {
                if (value.HasValue)
                {
                    Pinvoke.typedb_driver.query_options_set_include_instance_types(NativeObject, value.Value);
                }
            }
        }

        /// <summary>
        /// Gets or sets the prefetch size for query results.
        /// If set, specifies the number of extra query responses sent before the client side
        /// has to re-request more responses. Increasing this may increase performance for queries
        /// with a huge number of answers, as it can reduce the number of network round-trips
        /// at the cost of more resources on the server side. Minimal value: 1.
        /// </summary>
        /// <example>
        /// <code>
        /// options.PrefetchSize = 50;
        /// </code>
        /// </example>
        public long? PrefetchSize
        {
            get
            {
                if (Pinvoke.typedb_driver.query_options_has_prefetch_size(NativeObject))
                {
                    return Pinvoke.typedb_driver.query_options_get_prefetch_size(NativeObject);
                }
                return null;
            }
            set
            {
                if (value.HasValue)
                {
                    Validator.RequireNonNegative(value.Value, nameof(PrefetchSize));
                    Pinvoke.typedb_driver.query_options_set_prefetch_size(NativeObject, value.Value);
                }
            }
        }

        /// <summary>
        /// Gets or sets whether to include the query structure in the ConceptRow header.
        /// If set, requests the server to return the structure of the query in the ConceptRow header.
        /// </summary>
        /// <example>
        /// <code>
        /// options.IncludeQueryStructure = true;
        /// </code>
        /// </example>
        public bool? IncludeQueryStructure
        {
            get
            {
                if (Pinvoke.typedb_driver.query_options_has_include_query_structure(NativeObject))
                {
                    return Pinvoke.typedb_driver.query_options_get_include_query_structure(NativeObject);
                }
                return null;
            }
            set
            {
                if (value.HasValue)
                {
                    Pinvoke.typedb_driver.query_options_set_include_query_structure(NativeObject, value.Value);
                }
            }
        }
    }
}
