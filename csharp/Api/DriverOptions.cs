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
    public class DriverOptions : NativeObjectWrapper<Pinvoke.DriverOptions>
    {
        public DriverOptions(DriverTlsConfig tlsConfig)
            : base(Pinvoke.typedb_driver.driver_options_new(tlsConfig.NativeObject))
        {
        }

        public DriverTlsConfig TlsConfig
        {
            get { return new DriverTlsConfig(Pinvoke.typedb_driver.driver_options_get_tls_config(NativeObject)); }
        }

        public DriverOptions SetTlsConfig(DriverTlsConfig tlsConfig)
        {
            Pinvoke.typedb_driver.driver_options_set_tls_config(NativeObject, tlsConfig.NativeObject);
            return this;
        }

        public long RequestTimeoutMillis
        {
            get { return Pinvoke.typedb_driver.driver_options_get_request_timeout_millis(NativeObject); }
        }

        public DriverOptions SetRequestTimeoutMillis(long requestTimeoutMillis)
        {
            Pinvoke.typedb_driver.driver_options_set_request_timeout_millis(NativeObject, requestTimeoutMillis);
            return this;
        }

        public int PrimaryFailoverRetries
        {
            get { return (int)Pinvoke.typedb_driver.driver_options_get_primary_failover_retries(NativeObject); }
        }

        public DriverOptions SetPrimaryFailoverRetries(int primaryFailoverRetries)
        {
            Pinvoke.typedb_driver.driver_options_set_primary_failover_retries(NativeObject, primaryFailoverRetries);
            return this;
        }
    }
}
