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
using System.Collections.Generic;

namespace TypeDB.Driver.Api
{
    public interface IDriver : IDisposable
    {
        /// <summary>
        /// The language identifier for this driver.
        /// </summary>
        public const string Language = "csharp";

        /// <summary>
        /// Checks whether this connection is presently open.
        /// </summary>
        /// <example>
        /// <code>
        /// driver.IsOpen();
        /// </code>
        /// </example>
        bool IsOpen();

        /// <summary>
        /// The <c>IDatabaseManager</c> for this connection, providing access to database management methods.
        /// </summary>
        /// <example>
        /// <code>
        /// driver.Databases;
        /// </code>
        /// </example>
        IDatabaseManager Databases { get; }

        /// <summary>
        /// The <c>IUserManager</c> for this connection, providing access to user management methods.
        /// </summary>
        /// <example>
        /// <code>
        /// driver.Users;
        /// </code>
        /// </example>
        IUserManager Users { get; }

        /// <summary>
        /// Opens a communication tunnel (transaction) to the given database on the running TypeDB server.
        /// </summary>
        /// <param name="database">The name of the database with which the transaction connects.</param>
        /// <param name="type">The type of transaction to be created (READ, WRITE, or SCHEMA).</param>
        /// <example>
        /// <code>
        /// driver.Transaction(database, TransactionType.Read);
        /// </code>
        /// </example>
        ITransaction Transaction(string database, TransactionType type);

        /// <summary>
        /// Opens a communication tunnel (transaction) to the given database on the running TypeDB server.
        /// </summary>
        /// <param name="database">The name of the database with which the transaction connects.</param>
        /// <param name="type">The type of transaction to be created (READ, WRITE, or SCHEMA).</param>
        /// <param name="options"><c>TransactionOptions</c> to configure the opened transaction.</param>
        /// <example>
        /// <code>
        /// driver.Transaction(database, TransactionType.Read, options);
        /// </code>
        /// </example>
        ITransaction Transaction(string database, TransactionType type, TransactionOptions options);

        /// <summary>
        /// Retrieves the server's version.
        /// </summary>
        /// <param name="serverRouting">The server routing to use for the operation.</param>
        /// <example>
        /// <code>
        /// driver.GetServerVersion();
        /// </code>
        /// </example>
        ServerVersion GetServerVersion(ServerRouting? serverRouting = null);

        /// <summary>
        /// Set of servers for this driver connection.
        /// </summary>
        /// <param name="serverRouting">The server routing to use for the operation.</param>
        /// <example>
        /// <code>
        /// driver.GetServers(new ServerRouting.Auto());
        /// </code>
        /// </example>
        ISet<IServer> GetServers(ServerRouting? serverRouting = null);

        /// <summary>
        /// Returns the primary server for this driver connection.
        /// </summary>
        /// <param name="serverRouting">The server routing to use for the operation.</param>
        /// <example>
        /// <code>
        /// driver.GetPrimaryServer(new ServerRouting.Auto());
        /// </code>
        /// </example>
        IServer? GetPrimaryServer(ServerRouting? serverRouting = null);

        /// <summary>
        /// Registers a new server in the cluster the driver is currently connected to. The registered
        /// server will become available eventually, depending on the behavior of the whole cluster.
        /// To register a server, its clustering address should be passed, not the connection address.
        /// </summary>
        /// <param name="serverId">The numeric identifier of the new server.</param>
        /// <param name="address">The address(es) of the TypeDB server as a string.</param>
        /// <example>
        /// <code>
        /// driver.RegisterServer(2, "127.0.0.1:11729");
        /// </code>
        /// </example>
        void RegisterServer(long serverId, string address);

        /// <summary>
        /// Deregisters a server from the cluster the driver is currently connected to. This server
        /// will no longer play a raft role in this cluster.
        /// </summary>
        /// <param name="serverId">The numeric identifier of the deregistered server.</param>
        /// <example>
        /// <code>
        /// driver.DeregisterServer(2);
        /// </code>
        /// </example>
        void DeregisterServer(long serverId);

        /// <summary>
        /// Updates address translation of the driver. This lets you actualize new translation
        /// information without recreating the driver from scratch. Useful after registering new
        /// servers requiring address translation.
        /// This operation will update existing connections using the provided addresses.
        /// </summary>
        /// <param name="addressTranslation">The translation of public TypeDB cluster server addresses (keys) to server-side private addresses (values).</param>
        /// <example>
        /// <code>
        /// driver.UpdateAddressTranslation(addressTranslation);
        /// </code>
        /// </example>
        void UpdateAddressTranslation(IDictionary<string, string> addressTranslation);

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
