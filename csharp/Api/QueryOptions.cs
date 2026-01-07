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

namespace TypeDB.Driver.Api
{
    /// <summary>
    /// Options for query execution.
    /// </summary>
    public class QueryOptions : NativeObjectWrapper<Pinvoke.QueryOptions>
    {
        /// <summary>
        /// Creates a new QueryOptions with default settings.
        /// </summary>
        public QueryOptions()
            : base(Pinvoke.typedb_driver.query_options_new())
        {
        }

        /// <summary>
        /// Gets or sets whether to include instance types in query results.
        /// </summary>
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
        /// </summary>
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
                    Pinvoke.typedb_driver.query_options_set_prefetch_size(NativeObject, value.Value);
                }
            }
        }
    }
}
