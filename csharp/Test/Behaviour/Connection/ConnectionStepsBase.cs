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
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Xunit;
using Xunit.Gherkin.Quick;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Behaviour
{
    public abstract class ConnectionStepsBase : Feature, IDisposable
    {
        public static IDriver? Driver;
        public static IDriver? BackgroundDriver;

        public static List<ITransaction> Transactions = new List<ITransaction>();
        public static List<ITransaction> BackgroundTransactions = new List<ITransaction>();
        public static List<Task<ITransaction>> TransactionsParallel = new List<Task<ITransaction>>();

        public static TransactionOptions? CurrentTransactionOptions;

        private static string? _tempDir;

        public static readonly string AdminUsername = "admin";
        public static readonly string AdminPassword = "password";
        public static readonly Credentials DefaultCredentials = new Credentials("admin", "password");

        protected static DriverOptions DriverOptions = new DriverOptions(DriverTlsConfig.Disabled());
        protected static ServerRouting? OperationServerRouting;

        private const int BeforeTimeoutMillis = 50;

        protected bool _requiredConfiguration = false;

        public static ITransaction Tx => Transactions[0];

        public static string TempDir
        {
            get
            {
                if (_tempDir == null || !Directory.Exists(_tempDir))
                {
                    _tempDir = Path.Combine(Path.GetTempPath(), "typedb-test-" + Guid.NewGuid().ToString("N"));
                    Directory.CreateDirectory(_tempDir);
                }
                return _tempDir;
            }
        }

        public static string FullPath(string fileName) => Path.Combine(TempDir, fileName);

        public static ITransaction TxPop()
        {
            var tx = Transactions[0];
            Transactions.RemoveAt(0);
            return tx;
        }

        public ConnectionStepsBase()
        {
            Thread.Sleep(BeforeTimeoutMillis);
            Cleanup();
            BackgroundDriver = CreateDefaultTypeDBDriver();
        }

        public virtual void Dispose()
        {
            CleanupTempDir();
            Cleanup();
        }

        public abstract IDriver CreateDefaultTypeDBDriver();

        public abstract IDriver CreateTypeDBDriver(string address);

        public abstract void TypeDBStarts();

        public abstract void ConnectionOpensWithDefaultAuthentication();

        public virtual void ConnectionHasBeenOpened()
        {
            if (_requiredConfiguration) return;
            Assert.NotNull(Driver);
            Assert.True(Driver!.IsOpen());
        }

        public virtual void ConnectionCloses()
        {
            CleanupTransactions();
            Driver?.Close();
            Driver = null;
        }

        public static ITransaction OpenTransaction(
            IDriver driver, string databaseName, TransactionType type, TransactionOptions? options = null)
        {
            return options != null
                ? driver.Transaction(databaseName, type, options)
                : driver.Transaction(databaseName, type);
        }

        protected ServerVersion GetServerVersion()
        {
            return Driver!.GetServerVersion(OperationServerRouting);
        }

        protected ISet<IServer> GetServers()
        {
            return Driver!.GetServers(OperationServerRouting);
        }

        protected IServer? GetPrimaryServer()
        {
            return Driver!.GetPrimaryServer(OperationServerRouting);
        }

        private void Cleanup()
        {
            CleanupTransactions();
            CleanupBackgroundTransactions();
            CurrentTransactionOptions = null;
            OperationServerRouting = null;

            if (Driver != null && Driver.IsOpen())
            {
                Driver.Close();
            }

            try
            {
                var cleanupDriver = CreateDefaultTypeDBDriver();
                try
                {
                    foreach (var user in cleanupDriver.Users.GetAll())
                    {
                        if (user.Name != AdminUsername)
                        {
                            try { cleanupDriver.Users.Get(user.Name)?.Delete(); } catch { }
                        }
                    }

                    cleanupDriver.Users.Get(AdminUsername)?.UpdatePassword(AdminPassword);

                    foreach (var db in cleanupDriver.Databases.GetAll())
                    {
                        try { cleanupDriver.Databases.Get(db.Name).Delete(); } catch { }
                    }
                }
                catch { }
                finally
                {
                    cleanupDriver.Close();
                }
            }
            catch { }

            BackgroundDriver?.Close();
            BackgroundDriver = null;
            Driver = null;

            DriverOptions = new DriverOptions(DriverTlsConfig.Disabled());
        }

        private static void CleanupTempDir()
        {
            if (_tempDir != null && Directory.Exists(_tempDir))
            {
                try { Directory.Delete(_tempDir, recursive: true); } catch { }
                _tempDir = null;
            }
        }

        public static void CleanupTransactions()
        {
            foreach (var tx in Transactions)
            {
                try { tx.Close(); } catch { }
            }
            Transactions.Clear();

            foreach (var futureTx in TransactionsParallel)
            {
                try { futureTx.Result.Close(); } catch { }
            }
            TransactionsParallel.Clear();
        }

        public static void CleanupBackgroundTransactions()
        {
            foreach (var tx in BackgroundTransactions)
            {
                try { tx.Close(); } catch { }
            }
            BackgroundTransactions.Clear();
        }
    }
}
