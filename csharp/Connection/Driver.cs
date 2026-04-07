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
using System.Linq;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using TypeDB.Driver.Common.Validation;
using TypeDB.Driver.Connection;
using TypeDB.Driver.User;

namespace TypeDB.Driver.Connection
{
    public class TypeDBDriver : NativeObjectWrapper<Pinvoke.TypeDBDriver>, IDriver
    {
        private readonly IDatabaseManager _databaseManager;
        private readonly UserManager _userManager;

        public TypeDBDriver(string address, Credentials credentials, DriverOptions driverOptions)
            : base(Open(address, credentials, driverOptions))
        {
            _databaseManager = new DatabaseManager(NativeObject);
            _userManager = new UserManager(NativeObject);
        }

        public TypeDBDriver(ISet<string> addresses, Credentials credentials, DriverOptions driverOptions)
            : base(Open(addresses, credentials, driverOptions))
        {
            _databaseManager = new DatabaseManager(NativeObject);
            _userManager = new UserManager(NativeObject);
        }

        public TypeDBDriver(IDictionary<string, string> addressTranslation, Credentials credentials, DriverOptions driverOptions)
            : base(Open(addressTranslation, credentials, driverOptions))
        {
            _databaseManager = new DatabaseManager(NativeObject);
            _userManager = new UserManager(NativeObject);
        }

        private static Pinvoke.TypeDBDriver Open(string address, Credentials credentials, DriverOptions driverOptions)
        {
            Validator.RequireNonNull(address, nameof(address));
            Validator.RequireNonNull(credentials, nameof(credentials));
            Validator.RequireNonNull(driverOptions, nameof(driverOptions));
            try
            {
                return Pinvoke.typedb_driver.driver_new_with_description(
                    address,
                    credentials.NativeObject,
                    driverOptions.NativeObject,
                    IDriver.Language);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        private static Pinvoke.TypeDBDriver Open(ISet<string> addresses, Credentials credentials, DriverOptions driverOptions)
        {
            Validator.RequireNonNull(addresses, nameof(addresses));
            Validator.RequireNonNull(credentials, nameof(credentials));
            Validator.RequireNonNull(driverOptions, nameof(driverOptions));
            try
            {
                return Pinvoke.typedb_driver.driver_new_with_addresses_with_description(
                    addresses.ToArray(),
                    credentials.NativeObject,
                    driverOptions.NativeObject,
                    IDriver.Language);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        private static Pinvoke.TypeDBDriver Open(IDictionary<string, string> addressTranslation, Credentials credentials, DriverOptions driverOptions)
        {
            Validator.RequireNonNull(addressTranslation, nameof(addressTranslation));
            Validator.RequireNonNull(credentials, nameof(credentials));
            Validator.RequireNonNull(driverOptions, nameof(driverOptions));
            try
            {
                string[] publicAddresses = addressTranslation.Keys.ToArray();
                string[] privateAddresses = addressTranslation.Values.ToArray();
                return Pinvoke.typedb_driver.driver_new_with_address_translation_with_description(
                    publicAddresses,
                    privateAddresses,
                    credentials.NativeObject,
                    driverOptions.NativeObject,
                    IDriver.Language);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public bool IsOpen()
        {
            if (!NativeObject.IsOwned())
            {
                return false;
            }
            return Pinvoke.typedb_driver.driver_is_open(NativeObject);
        }

        public IDatabaseManager Databases
        {
            get { return _databaseManager; }
        }

        public IUserManager Users
        {
            get { return _userManager; }
        }

        public ITransaction Transaction(string database, TransactionType type)
        {
            return Transaction(database, type, new TransactionOptions());
        }

        public ITransaction Transaction(string database, TransactionType type, TransactionOptions options)
        {
            Validator.RequireNonNull(database, nameof(database));
            Validator.RequireNonNull(options, nameof(options));
            try
            {
                Pinvoke.Transaction nativeTransaction = Pinvoke.typedb_driver.transaction_new(
                    NativeObject,
                    database,
                    (Pinvoke.TransactionType)type,
                    options.NativeObject);

                return new Transaction(this, nativeTransaction, type, options);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public ServerVersion GetServerVersion(ServerRouting? serverRouting = null)
        {
            try
            {
                return new ServerVersion(
                    Pinvoke.typedb_driver.driver_server_version(
                        NativeObject,
                        ServerRouting.GetNativeValue(serverRouting)));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public ISet<IServer> GetServers(ServerRouting? serverRouting = null)
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Server>(
                    Pinvoke.typedb_driver.driver_servers(
                        NativeObject,
                        ServerRouting.GetNativeValue(serverRouting)))
                    .Select(s => (IServer)new ServerImpl(s))
                    .ToHashSet();
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public IServer? GetPrimaryServer(ServerRouting? serverRouting = null)
        {
            try
            {
                Pinvoke.Server? nativeServer = Pinvoke.typedb_driver.driver_primary_server(
                    NativeObject,
                    ServerRouting.GetNativeValue(serverRouting));
                if (nativeServer != null)
                {
                    return new ServerImpl(nativeServer);
                }
                return null;
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void RegisterServer(long serverId, string address)
        {
            try
            {
                Pinvoke.typedb_driver.driver_register_server(NativeObject, serverId, address);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void DeregisterServer(long serverId)
        {
            try
            {
                Pinvoke.typedb_driver.driver_deregister_server(NativeObject, serverId);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void UpdateAddressTranslation(IDictionary<string, string> addressTranslation)
        {
            Validator.RequireNonNull(addressTranslation, nameof(addressTranslation));
            try
            {
                string[] publicAddresses = addressTranslation.Keys.ToArray();
                string[] privateAddresses = addressTranslation.Values.ToArray();
                Pinvoke.typedb_driver.driver_update_address_translation(
                    NativeObject, publicAddresses, privateAddresses);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void Close()
        {
            if (!NativeObject.IsOwned())
            {
                return;
            }

            try
            {
                Pinvoke.typedb_driver.driver_force_close(NativeObject);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void Dispose()
        {
            Close();
        }
    }
}
