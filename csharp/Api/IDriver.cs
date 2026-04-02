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

using System;
using System.Collections.Generic;

namespace TypeDB.Driver.Api
{
    public interface IDriver : IDisposable
    {
        public const string Language = "csharp";

        bool IsOpen();

        IDatabaseManager Databases { get; }

        IUserManager Users { get; }

        ITransaction Transaction(string database, TransactionType type);

        ITransaction Transaction(string database, TransactionType type, TransactionOptions options);

        ServerVersion GetServerVersion(ServerRouting? serverRouting = null);

        ISet<IServer> GetServers(ServerRouting? serverRouting = null);

        IServer? GetPrimaryServer(ServerRouting? serverRouting = null);

        void RegisterServer(long serverId, string address);

        void DeregisterServer(long serverId);

        void UpdateAddressTranslation(IDictionary<string, string> addressTranslation);

        void Close();
    }
}
