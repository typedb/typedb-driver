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
using TypeDB.Driver.Common.Validation;

namespace TypeDB.Driver.Api
{
    /// <summary>
    /// TLS configuration for the TypeDB driver.
    ///
    /// <c>DriverTlsConfig</c> represents a fully constructed and validated TLS configuration.
    /// If TLS is enabled, the underlying TLS config is built eagerly at construction time,
    /// ensuring that no connection attempt can observe a partially-configured TLS state.
    ///
    /// The driver defaults to using TLS with native system trust roots.
    /// This matches typical system and container deployments while still allowing
    /// explicit opt-out or custom PKI configuration.
    /// </summary>
    public class DriverTlsConfig : NativeObjectWrapper<Pinvoke.DriverTlsConfig>
    {
        internal DriverTlsConfig(Pinvoke.DriverTlsConfig nativeObject)
            : base(nativeObject)
        {
        }

        /// <summary>
        /// Creates a TLS configuration with TLS disabled.
        /// WARNING: Disabling TLS causes credentials and data to be transmitted in plaintext.
        /// </summary>
        /// <example>
        /// <code>
        /// DriverTlsConfig tlsConfig = DriverTlsConfig.Disabled();
        /// </code>
        /// </example>
        public static DriverTlsConfig Disabled()
        {
            return new DriverTlsConfig(Pinvoke.typedb_driver.driver_tls_config_new_disabled());
        }

        /// <summary>
        /// Creates a TLS configuration enabled with system native trust roots.
        /// </summary>
        /// <example>
        /// <code>
        /// DriverTlsConfig tlsConfig = DriverTlsConfig.EnabledWithNativeRootCA();
        /// </code>
        /// </example>
        public static DriverTlsConfig EnabledWithNativeRootCA()
        {
            return new DriverTlsConfig(Pinvoke.typedb_driver.driver_tls_config_new_enabled_with_native_root_ca());
        }

        /// <summary>
        /// Creates a TLS configuration enabled with a custom root CA certificate bundle (PEM).
        /// </summary>
        /// <param name="tlsRootCAPath">Path to PEM-encoded root CA certificate bundle.</param>
        /// <example>
        /// <code>
        /// DriverTlsConfig tlsConfig = DriverTlsConfig.EnabledWithRootCA("path/to/ca-certificate.pem");
        /// </code>
        /// </example>
        public static DriverTlsConfig EnabledWithRootCA(string tlsRootCAPath)
        {
            Validator.RequireNonNull(tlsRootCAPath, nameof(tlsRootCAPath));
            try
            {
                return new DriverTlsConfig(
                    Pinvoke.typedb_driver.driver_tls_config_new_enabled_with_root_ca_path(tlsRootCAPath));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <summary>
        /// Returns whether TLS is enabled.
        /// </summary>
        /// <example>
        /// <code>
        /// tlsConfig.IsEnabled;
        /// </code>
        /// </example>
        public bool IsEnabled => Pinvoke.typedb_driver.driver_tls_config_is_enabled(NativeObject);

        /// <summary>
        /// Returns the configured custom root CA path, if present.
        /// If TLS is enabled with native roots (or disabled), this will be null.
        /// </summary>
        /// <example>
        /// <code>
        /// tlsConfig.RootCAPath;
        /// </code>
        /// </example>
        public string? RootCAPath
        {
            get
            {
                if (!Pinvoke.typedb_driver.driver_tls_config_has_root_ca_path(NativeObject))
                {
                    return null;
                }
                return Pinvoke.typedb_driver.driver_tls_config_get_root_ca_path(NativeObject);
            }
        }
    }
}
