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

using TypeDB.Driver.Api;
using TypeDB.Driver.Connection;

namespace TypeDB.Driver
{
    public static class TypeDB
    {
        /// <summary>
        /// The default address of the TypeDB server.
        /// </summary>
        public const string DefaultAddress = "127.0.0.1:1729";

        /// <summary>
        /// Open a TypeDB Driver to a TypeDB server available at the provided address.
        /// </summary>
        /// <param name="address">The address of the TypeDB server.</param>
        /// <param name="credentials">The credentials to connect with.</param>
        /// <param name="driverOptions">The driver connection options to connect with.</param>
        /// <example>
        /// <code>
        /// TypeDB.Driver(address, credentials, driverOptions);
        /// </code>
        /// </example>
        public static IDriver Driver(string address, Credentials credentials, DriverOptions driverOptions)
        {
            return new TypeDBDriver(address, credentials, driverOptions);
        }

        /// <summary>
        /// Open a TypeDB Driver to a TypeDB cluster available at the provided addresses.
        /// </summary>
        /// <param name="addresses">The addresses of TypeDB cluster servers for connection.</param>
        /// <param name="credentials">The credentials to connect with.</param>
        /// <param name="driverOptions">The driver connection options to connect with.</param>
        /// <example>
        /// <code>
        /// TypeDB.Driver(addresses, credentials, driverOptions);
        /// </code>
        /// </example>
        public static IDriver Driver(ISet<string> addresses, Credentials credentials, DriverOptions driverOptions)
        {
            return new TypeDBDriver(addresses, credentials, driverOptions);
        }

        /// <summary>
        /// Open a TypeDB Driver to a TypeDB cluster, using the provided address translation.
        /// </summary>
        /// <param name="addressTranslation">The translation of public TypeDB cluster server addresses (keys) to server-side private addresses (values).</param>
        /// <param name="credentials">The credentials to connect with.</param>
        /// <param name="driverOptions">The driver connection options to connect with.</param>
        /// <example>
        /// <code>
        /// TypeDB.Driver(addressTranslation, credentials, driverOptions);
        /// </code>
        /// </example>
        public static IDriver Driver(IDictionary<string, string> addressTranslation, Credentials credentials, DriverOptions driverOptions)
        {
            return new TypeDBDriver(addressTranslation, credentials, driverOptions);
        }
    }
}
