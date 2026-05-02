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
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Connection
{
    internal class ServerImpl : NativeObjectWrapper<Pinvoke.Server>, IServer
    {
        internal ServerImpl(Pinvoke.Server server)
            : base(server)
        {
        }

        public long Id => Pinvoke.typedb_driver.server_get_id(NativeObject);

        public string Address => Pinvoke.typedb_driver.server_get_address(NativeObject);

        public ReplicationRole? Role
        {
            get
            {
                if (Pinvoke.typedb_driver.server_has_role(NativeObject))
                {
                    return ReplicationRoleExtensions.Of(Pinvoke.typedb_driver.server_get_role(NativeObject));
                }
                return null;
            }
        }

        public bool IsPrimary => Pinvoke.typedb_driver.server_is_primary(NativeObject);

        public long? Term
        {
            get
            {
                if (Pinvoke.typedb_driver.server_has_term(NativeObject))
                {
                    return Pinvoke.typedb_driver.server_get_term(NativeObject);
                }
                return null;
            }
        }

        public override string ToString()
        {
            return Address;
        }
    }
}
