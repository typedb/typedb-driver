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
    /// User connection settings (TLS encryption, etc.) for connecting to TypeDB Server.
    /// </summary>
    /// <example>
    /// <code>
    /// // No TLS
    /// DriverOptions options = new DriverOptions(false, null);
    ///
    /// // TLS with custom CA certificate
    /// DriverOptions options = new DriverOptions(true, "/path/to/ca-certificate.pem");
    /// </code>
    /// </example>
    public class DriverOptions : NativeObjectWrapper<Pinvoke.DriverOptions>
    {
        /// <summary>
        /// Creates a new <see cref="DriverOptions"/> for connecting to TypeDB Server.
        /// </summary>
        /// <param name="isTlsEnabled">Specify whether the connection to TypeDB Server must be done over TLS.</param>
        /// <param name="tlsRootCAPath">Path to the CA certificate to use for authenticating server certificates. Can be null if using system CA or TLS is disabled.</param>
        public DriverOptions(bool isTlsEnabled, string? tlsRootCAPath = null)
            : base(NewNative(isTlsEnabled, tlsRootCAPath))
        {
        }

        private static Pinvoke.DriverOptions NewNative(bool isTlsEnabled, string? tlsRootCAPath)
        {
            try
            {
                return Pinvoke.typedb_driver.driver_options_new(isTlsEnabled, tlsRootCAPath);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    }
}
