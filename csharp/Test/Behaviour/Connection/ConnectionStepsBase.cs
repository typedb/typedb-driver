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

        // TODO: Add transaction tracking when transactions are implemented in Milestone 2
        public static List<ITypeDBTransaction> Transactions = new List<ITypeDBTransaction>();

        // TODO: implement configuration and remove skips when @ignore-typedb-driver is removed from .feature.
        protected bool _requiredConfiguration = false;

        public ConnectionStepsBase() // "Before"
        {
            CleanInCaseOfPreviousFail();
        }

        public virtual void Dispose() // "After"
        {
            foreach (var tx in Transactions)
            {
                if (tx.IsOpen())
                {
                    tx.Close();
                }
            }
            Transactions.Clear();

            // Clean up databases using current driver before closing
            if (Driver != null && Driver.IsOpen())
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

                Driver.Close();
            }
            Driver = null;
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
            Assert.NotNull(Driver);
            Assert.Equal(expected, Driver.IsOpen());
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

            if (Driver != null && Driver.IsOpen())
            {
                Driver.Close();
            }
            Driver = null;
        }

        [When(@"connection open schema transaction for database: (\S+)")]
        [Given(@"connection open schema transaction for database: (\S+)")]
        public void ConnectionOpenSchemaTransactionForDatabase(string name)
        {
            if (_requiredConfiguration) return;

            var tx = Driver!.Transaction(name, TransactionType.Schema);
            Transactions.Add(tx);
        }

        [When(@"connection open read transaction for database: (\S+)")]
        [Given(@"connection open read transaction for database: (\S+)")]
        public void ConnectionOpenReadTransactionForDatabase(string name)
        {
            if (_requiredConfiguration) return;

            var tx = Driver!.Transaction(name, TransactionType.Read);
            Transactions.Add(tx);
        }

        [When(@"connection open write transaction for database: (\S+)")]
        [Given(@"connection open write transaction for database: (\S+)")]
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
                }
                Transactions.Clear();

                // Close leftover driver
                if (Driver != null)
                {
                    try { if (Driver.IsOpen()) Driver.Close(); } catch { }
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
