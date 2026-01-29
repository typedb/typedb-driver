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
using System.Threading;
using System.Threading.Tasks;
using Xunit;
using Xunit.Gherkin.Quick;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Behaviour
{
    /// <summary>
    /// Base class for connection-related behavior tests.
    /// </summary>
    public abstract class ConnectionStepsBase : Feature, IDisposable
    {
        public static IDriver? Driver;

        public static List<ITypeDBTransaction> Transactions = new List<ITypeDBTransaction>();

        // Background driver and transactions for "in background" steps
        public static IDriver? BackgroundDriver;
        public static List<ITypeDBTransaction> BackgroundTransactions = new List<ITypeDBTransaction>();

        // Parallel transactions for "in parallel" steps
        public static List<Task<ITypeDBTransaction>> TransactionsParallel = new List<Task<ITypeDBTransaction>>();

        // Transaction options set via "set transaction option" steps
        public static TransactionOptions? CurrentTransactionOptions;

        // Temporary directory for migration file operations (export/import)
        private static string? _tempDir;

        /// <summary>
        /// Gets or creates a temporary directory for the current test run.
        /// Used by file-related step definitions (export, import, file existence checks).
        /// </summary>
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

        /// <summary>
        /// Resolves a file name to a full path in the temp directory.
        /// </summary>
        public static string FullPath(string fileName)
        {
            return Path.Combine(TempDir, fileName);
        }

        // TODO: implement configuration and remove skips when @ignore-typedb-driver is removed from .feature.
        protected bool _requiredConfiguration = false;

        public static ITypeDBTransaction Tx => Transactions[0];

        public static ITypeDBTransaction TxPop()
        {
            var tx = Transactions[0];
            Transactions.RemoveAt(0);
            return tx;
        }

        // Sleep between scenarios to let the driver close completely
        // (`close` is not synced and can cause lock failures in CI)
        // This mirrors the Java driver's workaround for the same issue.
        private const int BeforeTimeoutMillis = 50;  // Small delay for async cleanup

        public static readonly string AdminUsername = "admin";
        public static readonly string AdminPassword = "password";

        public ConnectionStepsBase() // "Before"
        {
            Thread.Sleep(BeforeTimeoutMillis);
            CleanInCaseOfPreviousFail();
            BackgroundDriver = CreateDefaultTypeDBDriver();
        }

        public static IDriver CreateDefaultTypeDBDriver()
        {
            return TypeDB.Driver(
                TypeDB.DefaultAddress,
                new Credentials(AdminUsername, AdminPassword),
                new DriverOptions(false, null));
        }

        public static IDriver CreateBackgroundDriver()
        {
            return TypeDB.Driver(
                TypeDB.DefaultAddress,
                new Credentials("admin", "password"),
                new DriverOptions(false, null));
        }

        public virtual void Dispose() // "After"
        {
            // Clean up temp directory
            if (_tempDir != null && Directory.Exists(_tempDir))
            {
                try { Directory.Delete(_tempDir, recursive: true); } catch { }
                _tempDir = null;
            }

            // Clean up transactions (matches Java's after() order)
            CleanupTransactions();
            CleanupBackgroundTransactions();
            CurrentTransactionOptions = null;

            // Create a fresh driver to clean up state (like Java's after())
            try
            {
                var cleanupDriver = CreateDefaultTypeDBDriver();

                // Delete non-admin users
                try
                {
                    foreach (var user in cleanupDriver.Users.GetAll())
                    {
                        if (user.Username != AdminUsername)
                        {
                            try { cleanupDriver.Users.Get(user.Username).Delete(); } catch { }
                        }
                    }
                }
                catch { }

                // Reset admin password
                try
                {
                    cleanupDriver.Users.Get(AdminUsername).UpdatePassword(AdminPassword);
                }
                catch { }

                // Delete all databases
                try
                {
                    foreach (var db in cleanupDriver.Databases.GetAll())
                    {
                        try { cleanupDriver.Databases.Get(db.Name).Delete(); } catch { }
                    }
                }
                catch { }

                cleanupDriver.Close();
            }
            catch { }

            // Close the background driver
            if (BackgroundDriver != null)
            {
                try { BackgroundDriver.Close(); } catch { }
                BackgroundDriver = null;
            }

            // Close the main driver
            if (Driver != null)
            {
                try { Driver.Close(); } catch { }
                Driver = null;
            }
        }

        public abstract IDriver CreateTypeDBDriver(string address);

        public abstract void TypeDBStarts();

        public abstract void ConnectionOpensWithDefaultAuthentication();

        public virtual void ConnectionHasBeenOpened()
        {
            if (_requiredConfiguration) return; // Skip tests with configuration

            Assert.NotNull(Driver);
            Assert.True(Driver.IsOpen());
        }

        public virtual void ConnectionCloses()
        {
            if (_requiredConfiguration) return; // Skip tests with configuration

            CleanupTransactions();
            if (Driver != null)
            {
                Driver.Close();
            }
            Driver = null;
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

        public static ITypeDBTransaction OpenTransaction(
            IDriver driver, string databaseName, TransactionType type, TransactionOptions? options = null)
        {
            if (options != null)
                return driver.Transaction(databaseName, type, options);
            return driver.Transaction(databaseName, type);
        }

        private void CleanInCaseOfPreviousFail()
        {
            try
            {
                CleanupTransactions();
                CleanupBackgroundTransactions();
                if (BackgroundDriver != null)
                {
                    try { BackgroundDriver.Close(); } catch { }
                    BackgroundDriver = null;
                }
                CurrentTransactionOptions = null;

                try
                {
                    var cleanupDriver = CreateDefaultTypeDBDriver();

                    foreach (var user in cleanupDriver.Users.GetAll())
                    {
                        if (user.Username != AdminUsername)
                        {
                            try { cleanupDriver.Users.Get(user.Username).Delete(); } catch { }
                        }
                    }

                    try { cleanupDriver.Users.Get(AdminUsername).UpdatePassword(AdminPassword); } catch { }

                    foreach (var db in cleanupDriver.Databases.GetAll())
                    {
                        try { cleanupDriver.Databases.Get(db.Name).Delete(); } catch { }
                    }

                    cleanupDriver.Close();
                }
                catch { }

                if (Driver != null)
                {
                    try { Driver.Close(); } catch { }
                    Driver = null;
                }
            }
            catch { }
        }
    }
}
