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

using System.Collections.Generic;
using System.Linq;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using TypeDB.Driver.Connection;
using TypeDB.Driver.User;

namespace TypeDB.Driver.Connection
{
    /// <summary>
    /// Implementation of the TypeDB driver connection.
    /// </summary>
    public class TypeDBDriver : NativeObjectWrapper<Pinvoke.TypeDBDriver>, IDriver
    {
        private readonly IDatabaseManager _databaseManager;
        private readonly UserManager _userManager;

        /// <summary>
        /// Creates a new TypeDB driver connection using the 3.0 unified API.
        /// </summary>
        /// <param name="address">The address (host:port) on which the TypeDB Server is running.</param>
        /// <param name="credentials">The credentials to connect with.</param>
        /// <param name="driverOptions">The driver options (TLS settings, etc.).</param>
        public TypeDBDriver(string address, Credentials credentials, DriverOptions driverOptions)
            : this(Open(address, credentials, driverOptions))
        {
        }

        private TypeDBDriver(Pinvoke.TypeDBDriver driver)
            : base(driver)
        {
            _databaseManager = new TypeDBDatabaseManager(NativeObject);
            _userManager = new UserManager(NativeObject);
        }

        private static Pinvoke.TypeDBDriver Open(string address, Credentials credentials, DriverOptions driverOptions)
        {
            try
            {
                return Pinvoke.typedb_driver.driver_open_with_description(
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

        /// <inheritdoc/>
        public bool IsOpen()
        {
            return Pinvoke.typedb_driver.driver_is_open(NativeObject);
        }

        /// <inheritdoc/>
        public IDatabaseManager Databases
        {
            get { return _databaseManager; }
        }

        /// <inheritdoc/>
        public IUserManager Users
        {
            get { return _userManager; }
        }

        /// <inheritdoc/>
        public ITypeDBTransaction Transaction(string database, TransactionType type)
        {
            return Transaction(database, type, new TransactionOptions());
        }

        /// <inheritdoc/>
        public ITypeDBTransaction Transaction(string database, TransactionType type, TransactionOptions options)
        {
            try
            {
                Pinvoke.Transaction nativeTransaction = Pinvoke.typedb_driver.transaction_new(
                    NativeObject,
                    database,
                    (Pinvoke.TransactionType)type,
                    options.NativeObject);
                return new TypeDBTransaction(nativeTransaction, type);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
        public void Close()
        {
            if (!IsOpen())
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

        /// <inheritdoc/>
        public void Dispose()
        {
            Close();
        }
    }
}
