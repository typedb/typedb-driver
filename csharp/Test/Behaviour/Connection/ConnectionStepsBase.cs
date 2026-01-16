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
    ///
    /// KNOWN ISSUE: There is a native memory corruption issue in the C# SWIG bindings that
    /// causes tests to crash (SIGABRT or SIGSEGV) when creating a new driver after tests
    /// that involve transactions. The crash occurs during the call to driver_open_with_description
    /// and appears to be related to C# GC finalization of SWIG-generated wrappers interacting
    /// with global state in the native library.
    ///
    /// The issue does NOT occur in Java tests with the same Rust code, suggesting it's
    /// specific to the SWIG C# binding layer. Investigation found:
    /// - The Rust BackgroundRuntime properly joins worker threads on Drop
    /// - Explicit GC.Collect() causes the crash to happen sooner (confirms finalization issue)
    /// - Even 1+ second delays between tests do not prevent the crash
    /// - The crash is not a simple race condition but appears to be memory corruption
    ///
    /// Tests that pass: 7-8 database tests (no transactions), 2 transaction tests
    /// Tests that crash: Any test that runs after GC has finalized previous driver/transaction objects
    ///
    /// TODO: Investigate the SWIG C# director callback handling and static callback maps
    /// that may be causing the corruption (see ThreadSafeTransactionCallbacks in typedb_driver.i)
    /// </summary>
    public abstract class ConnectionStepsBase : Feature, IDisposable
    {
        public static IDriver? Driver;

        // TODO: Add transaction tracking when transactions are implemented in Milestone 2
        public static List<ITypeDBTransaction> Transactions = new List<ITypeDBTransaction>();

        // TODO: implement configuration and remove skips when @ignore-typedb-driver is removed from .feature.
        protected bool _requiredConfiguration = false;

        // Sleep between scenarios to let the driver close completely
        // (`close` is not synced and can cause lock failures in CI)
        // This mirrors the Java driver's workaround for the same issue.
        private const int BeforeTimeoutMillis = 50;  // Small delay for async cleanup

        public ConnectionStepsBase() // "Before"
        {
            // Sleep between scenarios to let async driver cleanup complete
            Thread.Sleep(BeforeTimeoutMillis);

            // Force GC to run BEFORE creating a new driver.
            // This ensures all finalizers from the previous test complete
            // before we start creating native objects for this test.
            GC.Collect();
            GC.WaitForPendingFinalizers();

            CleanInCaseOfPreviousFail();
        }

        public virtual void Dispose() // "After"
        {
            foreach (var tx in Transactions)
            {
                try
                {
                    if (tx.IsOpen())
                    {
                        tx.Close();
                    }
                    // Explicitly dispose to release native resources immediately
                    if (tx is IDisposable disposable)
                    {
                        disposable.Dispose();
                    }
                }
                catch
                {
                    // Ignore cleanup errors
                }
            }
            Transactions.Clear();

            // Clean up databases using current driver before closing
            if (Driver != null)
            {
                if (Driver.IsOpen())
                {
                    try
                    {
                        foreach (var db in Driver.Databases.GetAll())
                        {
                            try
                            {
                                db.Delete();
                            }
                            catch
                            {
                                // Ignore individual database deletion errors
                            }
                        }
                    }
                    catch
                    {
                        // Ignore errors
                    }
                }

                // Dispose the driver to free native resources immediately
                // instead of waiting for GC finalization (which can cause race conditions)
                if (Driver is IDisposable disposable)
                {
                    disposable.Dispose();
                }
            }
            Driver = null;

            // Force garbage collection to run any remaining finalizers synchronously
            // This ensures all native resources are freed before starting the next test
            GC.Collect();
            GC.WaitForPendingFinalizers();

            // Sleep to let async driver cleanup complete
            Thread.Sleep(BeforeTimeoutMillis);
        }

        private void CleanupAllDatabases()
        {
            // Note: This method is not used - cleanup is done in Dispose() using the current driver
            // Keeping this for potential future use with a separate cleanup driver
            try
            {
                var cleanupDriver = TypeDB.Driver(
                    TypeDB.DefaultAddress,
                    new Credentials("admin", "password"),
                    new DriverOptions(false, null));

                foreach (var db in cleanupDriver.Databases.GetAll())
                {
                    try
                    {
                        db.Delete();
                    }
                    catch
                    {
                        // Ignore individual database deletion errors
                    }
                }

                cleanupDriver.Close();
            }
            catch
            {
                // Ignore cleanup errors
            }
        }

        public abstract IDriver CreateTypeDBDriver(string address);

        public abstract void TypeDBStarts();

        public abstract void ConnectionOpensWithDefaultAuthentication();

        [Given(@"connection has been opened")]
        public virtual void ConnectionHasBeenOpened()
        {
            if (_requiredConfiguration) return; // Skip tests with configuration

            Assert.NotNull(Driver);
            Assert.True(Driver.IsOpen());
        }

        [Given(@"connection is open: (.*)")]
        [Then(@"connection is open: (.*)")]
        public void ConnectionIsOpen(string expectedState)
        {
            if (_requiredConfiguration) return; // Skip tests with configuration

            bool expected = bool.Parse(expectedState);
            if (expected)
            {
                Assert.NotNull(Driver);
                Assert.True(Driver.IsOpen());
            }
            else
            {
                // If expected to not be open, either driver is null or not open
                Assert.True(Driver == null || !Driver.IsOpen());
            }
        }

        [Given(@"connection has (\d+) databases")]
        [Then(@"connection has (\d+) databases")]
        public void ConnectionHasDatabaseCount(int expectedCount)
        {
            if (_requiredConfiguration) return; // Skip tests with configuration

            Assert.NotNull(Driver);
            Assert.Equal(expectedCount, Driver.Databases.GetAll().Count);
        }

        [When(@"connection closes")]
        [Then(@"connection closes")]
        public virtual void ConnectionCloses()
        {
            if (_requiredConfiguration) return; // Skip tests with configuration

            if (Driver != null)
            {
                // Dispose the driver to free native resources immediately
                if (Driver is IDisposable disposable)
                {
                    disposable.Dispose();
                }
            }
            Driver = null;
        }

        [When(@"connection open schema transaction for database: (\S+)")]
        [Given(@"connection open schema transaction for database: (\S+)")]
        [Then(@"connection open schema transaction for database: (\S+)")]
        public void ConnectionOpenSchemaTransactionForDatabase(string name)
        {
            if (_requiredConfiguration) return;

            var tx = Driver!.Transaction(name, TransactionType.Schema);
            Transactions.Add(tx);
        }

        [When(@"connection open read transaction for database: (\S+)")]
        [Given(@"connection open read transaction for database: (\S+)")]
        [Then(@"connection open read transaction for database: (\S+)")]
        public void ConnectionOpenReadTransactionForDatabase(string name)
        {
            if (_requiredConfiguration) return;

            var tx = Driver!.Transaction(name, TransactionType.Read);
            Transactions.Add(tx);
        }

        [When(@"connection open write transaction for database: (\S+)")]
        [Given(@"connection open write transaction for database: (\S+)")]
        [Then(@"connection open write transaction for database: (\S+)")]
        public void ConnectionOpenWriteTransactionForDatabase(string name)
        {
            if (_requiredConfiguration) return;

            var tx = Driver!.Transaction(name, TransactionType.Write);
            Transactions.Add(tx);
        }

        [Then(@"transaction is open: (.*)")]
        public void TransactionIsOpen(string expectedState)
        {
            if (_requiredConfiguration) return;

            bool expected = bool.Parse(expectedState);
            Assert.True(Transactions.Count > 0, "No transaction is open");
            var tx = Transactions[Transactions.Count - 1];
            Assert.Equal(expected, tx.IsOpen());
        }

        [Given(@"transaction commits")]
        [When(@"transaction commits")]
        [Then(@"transaction commits")]
        public void TransactionCommits()
        {
            if (_requiredConfiguration) return;

            Assert.True(Transactions.Count > 0, "No transaction to commit");
            var tx = Transactions[Transactions.Count - 1];
            tx.Commit();
            // Don't remove from list - tests may want to check IsOpen() afterward
        }

        [Given(@"transaction closes")]
        [When(@"transaction closes")]
        [Then(@"transaction closes")]
        public void TransactionCloses()
        {
            if (_requiredConfiguration) return;

            Assert.True(Transactions.Count > 0, "No transaction to close");
            var tx = Transactions[Transactions.Count - 1];
            tx.Close();
            // Don't remove from list - tests may want to check IsOpen() afterward
        }

        private void CleanInCaseOfPreviousFail() // Fails are exceptions which do not clean resources
        {
            try
            {
                // Close any leftover transactions from previous failed tests
                foreach (var tx in Transactions)
                {
                    try { if (tx.IsOpen()) tx.Close(); } catch { }
                    try { if (tx is IDisposable d) d.Dispose(); } catch { }
                }
                Transactions.Clear();

                // Clean up leftover databases from previous failed tests
                // Create a fresh driver to do cleanup since the old one might be in a bad state
                try
                {
                    var cleanupDriver = TypeDB.Driver(
                        TypeDB.DefaultAddress,
                        new Credentials("admin", "password"),
                        new DriverOptions(false, null));

                    if (cleanupDriver.IsOpen())
                    {
                        foreach (var db in cleanupDriver.Databases.GetAll())
                        {
                            try { db.Delete(); } catch { }
                        }
                    }

                    if (cleanupDriver is IDisposable cd) cd.Dispose();
                }
                catch
                {
                    // Ignore - server might not be running yet
                }

                // Dispose leftover driver to free native resources
                if (Driver != null)
                {
                    try { if (Driver is IDisposable d) d.Dispose(); } catch { }
                    Driver = null;
                }
            }
            catch
            {
                // Ignore cleanup errors from previous failed tests
            }
        }
    }
}
