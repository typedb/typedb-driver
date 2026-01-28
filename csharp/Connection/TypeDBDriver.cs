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
using System.Runtime.CompilerServices;

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
            : base(Open(address, credentials, driverOptions))
        {
            _databaseManager = new TypeDBDatabaseManager(NativeObject);
            _userManager = new UserManager(NativeObject);
        }

        private static Pinvoke.TypeDBDriver Open(string address, Credentials credentials, DriverOptions driverOptions)
        {
            try
            {
                var result = Pinvoke.typedb_driver.driver_open_with_description(
                    address,
                    credentials.NativeObject,
                    driverOptions.NativeObject,
                    IDriver.Language);

                // Prevent GC from collecting credentials/driverOptions during the native call
                // The Rust code borrows these pointers and the objects must remain alive
                GC.KeepAlive(credentials);
                GC.KeepAlive(driverOptions);

                return result;
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
        public bool IsOpen()
        {
            // Check if the SWIG object has been disposed (pointer is null)
            if (!NativeObject.IsOwned())
            {
                return false;
            }
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

                // Prevent GC from collecting options during the native call
                GC.KeepAlive(options);

                return new TypeDBTransaction(this, nativeTransaction, type, options);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
        public void Close()
        {
            // Check if already closed/disposed
            if (!NativeObject.IsOwned())
            {
                return;
            }

            try
            {
                // Signal the driver to shut down gracefully, like Java/Python do.
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
//            // Dispose all native objects to free native memory immediately
//            // instead of waiting for GC finalization (which can cause race conditions)
//            if (NativeObject is IDisposable nativeDisposable)
//            {
//                nativeDisposable.Dispose();
//            }
        }
    }
}
