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

using TypeDB.Driver.Api;
using TypeDB.Driver.Connection;

namespace TypeDB.Driver
{
    /// <summary>
    /// Entry point for creating TypeDB driver connections.
    /// </summary>
    public static class TypeDB
    {
        /// <summary>
        /// The default address for TypeDB Server.
        /// </summary>
        public const string DefaultAddress = "localhost:1730";

        /// <summary>
        /// Open a TypeDB Driver to a TypeDB server available at the provided address.
        /// </summary>
        /// <param name="address">The address (host:port) on which the TypeDB Server is running.</param>
        /// <param name="credentials">The <see cref="Credentials"/> to connect with.</param>
        /// <param name="driverOptions">The <see cref="DriverOptions"/> to connect with.</param>
        /// <returns>An open <see cref="IDriver"/> connection to the TypeDB server.</returns>
        /// <example>
        /// <code>
        /// using var driver = TypeDB.Driver(
        ///     "localhost:1729",
        ///     new Credentials("admin", "password"),
        ///     new DriverOptions(false, null));
        /// </code>
        /// </example>
        public static IDriver Driver(string address, Credentials credentials, DriverOptions driverOptions)
        {
            return new TypeDBDriver(address, credentials, driverOptions);
        }

        /// <summary>
        /// Open a TypeDB Driver to a TypeDB server available at the default address (localhost:1729).
        /// </summary>
        /// <param name="credentials">The <see cref="Credentials"/> to connect with.</param>
        /// <param name="driverOptions">The <see cref="DriverOptions"/> to connect with.</param>
        /// <returns>An open <see cref="IDriver"/> connection to the TypeDB server.</returns>
        public static IDriver Driver(Credentials credentials, DriverOptions driverOptions)
        {
            return Driver(DefaultAddress, credentials, driverOptions);
        }

        /// <summary>
        /// Open a TypeDB Driver to a TypeDB server available at the provided address with no TLS.
        /// </summary>
        /// <param name="address">The address (host:port) on which the TypeDB Server is running.</param>
        /// <param name="credentials">The <see cref="Credentials"/> to connect with.</param>
        /// <returns>An open <see cref="IDriver"/> connection to the TypeDB server.</returns>
        public static IDriver Driver(string address, Credentials credentials)
        {
            return Driver(address, credentials, new DriverOptions(false, null));
        }

        /// <summary>
        /// Open a TypeDB Driver to a TypeDB server available at the default address with no TLS.
        /// </summary>
        /// <param name="credentials">The <see cref="Credentials"/> to connect with.</param>
        /// <returns>An open <see cref="IDriver"/> connection to the TypeDB server.</returns>
        public static IDriver Driver(Credentials credentials)
        {
            return Driver(DefaultAddress, credentials, new DriverOptions(false, null));
        }
    }
}
