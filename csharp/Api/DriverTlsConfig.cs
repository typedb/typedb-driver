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
    public class DriverTlsConfig : NativeObjectWrapper<Pinvoke.DriverTlsConfig>
    {
        internal DriverTlsConfig(Pinvoke.DriverTlsConfig nativeObject)
            : base(nativeObject)
        {
        }

        public static DriverTlsConfig Disabled()
        {
            return new DriverTlsConfig(Pinvoke.typedb_driver.driver_tls_config_new_disabled());
        }

        public static DriverTlsConfig EnabledWithNativeRootCA()
        {
            return new DriverTlsConfig(Pinvoke.typedb_driver.driver_tls_config_new_enabled_with_native_root_ca());
        }

        public static DriverTlsConfig EnabledWithRootCA(string tlsRootCAPath)
        {
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

        public bool IsEnabled => Pinvoke.typedb_driver.driver_tls_config_is_enabled(NativeObject);

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
