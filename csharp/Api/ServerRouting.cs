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

namespace TypeDB.Driver.Api
{
    /// <summary>
    /// Server routing directive for operations against a distributed server. All driver methods have
    /// default recommended values, however, some operations can be configured in order to
    /// target a specific server in the cluster. This setting does not affect clusters with a single node.
    /// </summary>
    public abstract class ServerRouting
    {
        /// <summary>
        /// Returns the native server routing value.
        /// </summary>
        public abstract Pinvoke.ServerRouting NativeValue();

        /// <summary>
        /// Returns the native server routing value, or <c>null</c> if routing is not specified.
        /// </summary>
        /// <param name="serverRouting">The server routing, or <c>null</c>.</param>
        public static Pinvoke.ServerRouting? GetNativeValue(ServerRouting? serverRouting)
        {
            return serverRouting?.NativeValue();
        }

        /// <summary>
        /// Automatic server routing. Driver automatically selects the server (primary in clusters).
        /// </summary>
        public sealed class Auto : ServerRouting
        {
            /// <inheritdoc />
            public override Pinvoke.ServerRouting NativeValue()
            {
                return Pinvoke.typedb_driver.server_routing_auto();
            }

            /// <inheritdoc />
            public override string ToString()
            {
                return "Auto";
            }
        }

        /// <summary>
        /// Route to a specific known server at the given address. Mostly used for debugging purposes.
        /// </summary>
        public sealed class Direct : ServerRouting
        {
            /// <summary>
            /// Retrieves the address of the server this routing targets.
            /// </summary>
            public string Address { get; }

            /// <summary>
            /// Creates a Direct routing to the specified server address.
            /// </summary>
            /// <param name="address">The address of the server to route to.</param>
            public Direct(string address)
            {
                Address = address;
            }

            /// <inheritdoc />
            public override Pinvoke.ServerRouting NativeValue()
            {
                return Pinvoke.typedb_driver.server_routing_direct(Address);
            }

            /// <inheritdoc />
            public override string ToString()
            {
                return "Direct(" + Address + ")";
            }
        }
    }
}
