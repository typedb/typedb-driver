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
    public abstract class ServerRouting
    {
        public abstract Pinvoke.ServerRouting NativeValue();

        public static Pinvoke.ServerRouting? GetNativeValue(ServerRouting? serverRouting)
        {
            return serverRouting?.NativeValue();
        }

        public sealed class Auto : ServerRouting
        {
            public override Pinvoke.ServerRouting NativeValue()
            {
                return Pinvoke.typedb_driver.server_routing_auto();
            }

            public override string ToString()
            {
                return "Auto";
            }
        }

        public sealed class Direct : ServerRouting
        {
            public string Address { get; }

            public Direct(string address)
            {
                Address = address;
            }

            public override Pinvoke.ServerRouting NativeValue()
            {
                return Pinvoke.typedb_driver.server_routing_direct(Address);
            }

            public override string ToString()
            {
                return "Direct(" + Address + ")";
            }
        }
    }
}
