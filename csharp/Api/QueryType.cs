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

using DriverError = TypeDB.Driver.Common.Error.Driver;

namespace TypeDB.Driver.Api
{
    /// <summary>
    /// Used to specify the type of the executed query.
    /// </summary>
    /// <example>
    /// <code>
    /// conceptRow.QueryType
    /// </code>
    /// </example>
    public enum QueryType
    {
        /// <summary>
        /// A read-only query that retrieves data.
        /// </summary>
        Read = 0,

        /// <summary>
        /// A write query that modifies data.
        /// </summary>
        Write = 1,

        /// <summary>
        /// A schema query that modifies the schema.
        /// </summary>
        Schema = 2
    }

    /// <summary>
    /// Extension methods for QueryType.
    /// </summary>
    public static class QueryTypeExtensions
    {
        /// <summary>
        /// Creates a QueryType from a native QueryType.
        /// </summary>
        public static QueryType FromNative(Pinvoke.QueryType nativeType)
        {
            return nativeType switch
            {
                Pinvoke.QueryType.ReadQuery => QueryType.Read,
                Pinvoke.QueryType.WriteQuery => QueryType.Write,
                Pinvoke.QueryType.SchemaQuery => QueryType.Schema,
                _ => throw new TypeDBDriverException(DriverError.UNEXPECTED_NATIVE_VALUE)
            };
        }

        /// <summary>
        /// Checks if this is a read query.
        /// </summary>
        public static bool IsRead(this QueryType type) => type == QueryType.Read;

        /// <summary>
        /// Checks if this is a write query.
        /// </summary>
        public static bool IsWrite(this QueryType type) => type == QueryType.Write;

        /// <summary>
        /// Checks if this is a schema query.
        /// </summary>
        public static bool IsSchema(this QueryType type) => type == QueryType.Schema;
    }
}
