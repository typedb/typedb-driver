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

namespace TypeDB.Driver.Api
{
    /// <summary>
    /// A TypeDB database.
    /// </summary>
    public interface IDatabase
    {
        /// <summary>
        /// The database name as a string.
        /// </summary>
        /// <example>
        /// <code>
        /// database.Name;
        /// </code>
        /// </example>
        public string Name { get; }

        /// <summary>
        /// Returns the full schema text as a valid TypeQL define query string.
        /// </summary>
        /// <returns>The schema string.</returns>
        /// <example>
        /// <code>
        /// database.GetSchema();
        /// </code>
        /// </example>
        public string GetSchema();

        /// <summary>
        /// Returns the types in the schema as a valid TypeQL define query string.
        /// </summary>
        /// <returns>The type schema string.</returns>
        /// <example>
        /// <code>
        /// database.GetTypeSchema();
        /// </code>
        /// </example>
        public string GetTypeSchema();

        /// <summary>
        /// Deletes this database.
        /// </summary>
        /// <example>
        /// <code>
        /// database.Delete();
        /// </code>
        /// </example>
        public void Delete();

        /// <summary>
        /// Exports this database to a schema definition file and a data file on disk.
        /// This is a blocking operation and may take a significant amount of time
        /// depending on the database size.
        /// </summary>
        /// <param name="schemaFile">The path to the schema definition file to be created.</param>
        /// <param name="dataFile">The path to the data file to be created.</param>
        /// <example>
        /// <code>
        /// database.ExportToFile("schema.tql", "data.typedb");
        /// </code>
        /// </example>
        public void ExportToFile(string schemaFile, string dataFile);
    }
}
