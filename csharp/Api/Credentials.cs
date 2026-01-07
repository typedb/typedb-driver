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
    /// User credentials for connecting to TypeDB Server.
    /// </summary>
    /// <example>
    /// <code>
    /// Credentials credentials = new Credentials("admin", "password");
    /// </code>
    /// </example>
    public class Credentials : NativeObjectWrapper<Pinvoke.Credentials>
    {
        /// <summary>
        /// Creates a new <see cref="Credentials"/> for connecting to TypeDB Server.
        /// </summary>
        /// <param name="username">The name of the user to connect as.</param>
        /// <param name="password">The password for the user.</param>
        public Credentials(string username, string password)
            : base(NewNative(username, password))
        {
        }

        private static Pinvoke.Credentials NewNative(string username, string password)
        {
            try
            {
                return Pinvoke.typedb_driver.credentials_new(username, password);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    }
}
