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

using System;

using TypeDB.Driver.Api;
using TypeDB.Driver.Api.Analyze;
using TypeDB.Driver.Api.Answer;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Api
{
    /// <summary>
    /// A transaction with a TypeDB database.
    /// </summary>
    public interface ITypeDBTransaction : IDisposable
    {
        /// <summary>
        /// The transaction's type (Read, Write, or Schema).
        /// </summary>
        TransactionType Type { get; }

        /// <summary>
        /// Checks whether this transaction is open.
        /// </summary>
        /// <returns><c>true</c> if the transaction is open, <c>false</c> otherwise.</returns>
        bool IsOpen();

        /// <summary>
        /// Registers a callback function which will be executed when this transaction is closed.
        /// </summary>
        /// <param name="function">The callback function.</param>
        void OnClose(Action<Exception?> function);

        /// <summary>
        /// Commits the changes made via this transaction to the TypeDB database.
        /// Whether or not the transaction is committed successfully, it gets closed after the commit call.
        /// </summary>
        void Commit();

        /// <summary>
        /// Rolls back the uncommitted changes made via this transaction.
        /// </summary>
        void Rollback();

        /// <summary>
        /// Closes the transaction.
        /// </summary>
        void Close();

        /// <summary>
        /// Executes a TypeQL query in this transaction.
        /// </summary>
        /// <param name="query">The TypeQL query string to execute.</param>
        /// <returns>The query answer containing the results.</returns>
        IQueryAnswer Query(string query);

        /// <summary>
        /// Executes a TypeQL query in this transaction with custom options.
        /// </summary>
        /// <param name="query">The TypeQL query string to execute.</param>
        /// <param name="options">Query options.</param>
        /// <returns>The query answer containing the results.</returns>
        IQueryAnswer Query(string query, QueryOptions options);

        /// <summary>
        /// Analyzes a TypeQL query and returns information about its structure and type inference results.
        /// </summary>
        /// <param name="query">The TypeQL query string to analyze.</param>
        /// <returns>The analyzed query containing structure and type information.</returns>
        IAnalyzedQuery Analyze(string query);
    }

    /// <summary>
    /// Used to specify the type of transaction.
    /// </summary>
    /// <example>
    /// <code>
    /// driver.Transaction(database, TransactionType.Read);
    /// </code>
    /// </example>
    public enum TransactionType
    {
        Read = 0,
        Write = 1,
        Schema = 2,
    }
}
