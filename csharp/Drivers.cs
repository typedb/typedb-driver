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

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Connection;

namespace TypeDB.Driver
{
    public static class Drivers
    {
        public const string DEFAULT_ADDRESS = "localhost:1729";

        /**
         * Open a TypeDB Driver to a TypeDB Core server available at the provided address.
         *
         * <h3>Examples</h3>
         * <pre>
         * Drivers.CoreDriver(address);
         * </pre>
         *
         * @param address The address of the TypeDB server
         */
        public static ITypeDBDriver CoreDriver(string address)
        {
            return new TypeDBDriver(address);
        }

        /**
         * Open a TypeDB Driver to a TypeDB Cloud server available at the provided address, using
         * the provided credential.
         *
         * <h3>Examples</h3>
         * <pre>
         * Drivers.CloudDriver(address, credential);
         * </pre>
         *
         * @param address The address of the TypeDB server
         * @param credential The credential to connect with
         */
        public static ITypeDBDriver CloudDriver(string address, TypeDBCredential credential)
        {
            return CloudDriver(new string[]{address}, credential);
        }

        /**
         * Open a TypeDB Driver to TypeDB Cloud server(s) available at the provided addresses, using
         * the provided credential.
         *
         * <h3>Examples</h3>
         * <pre>
         * Drivers.CloudDriver(addresses, credential);
         * </pre>
         *
         * @param addresses The address(es) of the TypeDB server(s) or translation map from addresses
         * received from the TypeDB server(s) to addresses to be used by the driver for connection
         * @param credential The credential to connect with
         */
        public static ITypeDBDriver CloudDriver(ICollection<string> addresses, TypeDBCredential credential)
        {
            return new TypeDBDriver(addresses, credential);
        }

        public static ITypeDBDriver CloudDriver(IDictionary<string, string> addressTranslation, TypeDBCredential credential)
        {
            return new TypeDBDriver(addressTranslation, credential);
        }
    }
}
