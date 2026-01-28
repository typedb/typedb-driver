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

namespace TypeDB.Driver.Api
{
    /// <summary>
    /// A connection to a TypeDB server which serves as the starting point for all interaction.
    /// </summary>
    public interface IDriver : IDisposable
    {
        /// <summary>
        /// The language identifier for this driver.
        /// </summary>
        public const string Language = "csharp";

        /// <summary>
        /// Checks whether this connection is presently open.
        /// </summary>
        /// <returns><c>true</c> if the connection is open, <c>false</c> otherwise.</returns>
        /// <example>
        /// <code>
        /// driver.IsOpen();
        /// </code>
        /// </example>
        bool IsOpen();

        /// <summary>
        /// The <see cref="IDatabaseManager"/> for this connection, providing access to database management methods.
        /// </summary>
        /// <example>
        /// <code>
        /// driver.Databases
        /// </code>
        /// </example>
        IDatabaseManager Databases { get; }

        /// <summary>
        /// The <see cref="IUserManager"/> instance for this connection, providing access to user management methods.
        /// </summary>
        /// <example>
        /// <code>
        /// driver.Users
        /// </code>
        /// </example>
        IUserManager Users { get; }

        /// <summary>
        /// Opens a transaction to perform read or write queries on the database.
        /// </summary>
        /// <param name="database">The name of the database to open a transaction for.</param>
        /// <param name="type">The type of transaction to be created (Read, Write, or Schema).</param>
        /// <returns>A new transaction.</returns>
        /// <example>
        /// <code>
        /// driver.Transaction(database, TransactionType.Read);
        /// </code>
        /// </example>
        ITypeDBTransaction Transaction(string database, TransactionType type);

        /// <summary>
        /// Opens a transaction to perform read or write queries on the database with custom options.
        /// </summary>
        /// <param name="database">The name of the database to open a transaction for.</param>
        /// <param name="type">The type of transaction to be created (Read, Write, or Schema).</param>
        /// <param name="options">Transaction options to configure the opened transaction.</param>
        /// <returns>A new transaction.</returns>
        /// <example>
        /// <code>
        /// driver.Transaction(database, TransactionType.Read, options);
        /// </code>
        /// </example>
        ITypeDBTransaction Transaction(string database, TransactionType type, TransactionOptions options);

        /// <summary>
        /// Closes the driver. Before instantiating a new driver, the driver that's currently open should first be closed.
        /// </summary>
        /// <example>
        /// <code>
        /// driver.Close();
        /// </code>
        /// </example>
        void Close();
    }
}
