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
    /// TypeDB driver options. <c>DriverOptions</c> are used to specify the driver's connection behavior.
    /// </summary>
    public class DriverOptions : NativeObjectWrapper<Pinvoke.DriverOptions>
    {
        /// <summary>
        /// Produces a new <c>DriverOptions</c> object for connecting to TypeDB Server using custom TLS settings.
        /// WARNING: Disabled TLS settings will make the driver sending passwords as plaintext.
        /// </summary>
        /// <param name="tlsConfig">The TLS configuration for the connection.</param>
        /// <example>
        /// <code>
        /// DriverOptions options = new DriverOptions(DriverTlsConfig.EnabledWithNativeRootCA())
        /// {
        ///     RequestTimeoutMillis = 30000
        /// };
        /// </code>
        /// </example>
        public DriverOptions(DriverTlsConfig tlsConfig)
            : base(Pinvoke.typedb_driver.driver_options_new(tlsConfig.NativeObject))
        {
        }

        /// <summary>
        /// Gets or sets the TLS configuration associated with this <c>DriverOptions</c>.
        /// Specifies the TLS configuration of the connection to TypeDB.
        /// WARNING: Disabled TLS settings will make the driver sending passwords as plaintext.
        /// </summary>
        /// <example>
        /// <code>
        /// options.TlsConfig = DriverTlsConfig.EnabledWithNativeRootCA();
        /// </code>
        /// </example>
        public DriverTlsConfig TlsConfig
        {
            get { return new DriverTlsConfig(Pinvoke.typedb_driver.driver_options_get_tls_config(NativeObject)); }
            set
            {
                Validator.RequireNonNull(value, nameof(TlsConfig));
                Pinvoke.typedb_driver.driver_options_set_tls_config(NativeObject, value.NativeObject);
            }
        }

        /// <summary>
        /// Gets or sets the maximum time (in milliseconds) to wait for a response to a unary RPC request.
        /// This applies to operations like database creation, user management, and initial
        /// transaction opening. It does NOT apply to operations within transactions (queries, commits).
        /// Defaults to 2 hours (7200000 milliseconds).
        /// </summary>
        /// <example>
        /// <code>
        /// options.RequestTimeoutMillis = 30000;
        /// </code>
        /// </example>
        public long RequestTimeoutMillis
        {
            get { return Pinvoke.typedb_driver.driver_options_get_request_timeout_millis(NativeObject); }
            set
            {
                Validator.RequireNonNegative(value, nameof(RequestTimeoutMillis));
                Pinvoke.typedb_driver.driver_options_set_request_timeout_millis(NativeObject, value);
            }
        }

        /// <summary>
        /// Gets or sets the limit on the number of attempts to redirect a request to another
        /// primary server in case of a failure due to the change of server roles. Defaults to 1.
        /// </summary>
        /// <example>
        /// <code>
        /// options.PrimaryFailoverRetries = 3;
        /// </code>
        /// </example>
        public int PrimaryFailoverRetries
        {
            get { return (int)Pinvoke.typedb_driver.driver_options_get_primary_failover_retries(NativeObject); }
            set
            {
                Validator.RequireNonNegative(value, nameof(PrimaryFailoverRetries));
                Pinvoke.typedb_driver.driver_options_set_primary_failover_retries(NativeObject, value);
            }
        }
    }
}
