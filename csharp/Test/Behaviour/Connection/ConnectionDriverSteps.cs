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

using DocString = Gherkin.Ast.DocString;
using System;
using Xunit;
using Xunit.Gherkin.Quick;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        // Connection: wrong host/port

        [When(@"connection opens with a wrong port; fails")]
        [Then(@"connection opens with a wrong port; fails")]
        public void ConnectionOpensWithWrongPortFails()
        {
            Assert.ThrowsAny<Exception>(() =>
            {
                var wrongPortDriver = TypeDB.Driver(
                    "localhost:9999",
                    new Credentials("admin", "password"),
                    new DriverOptions(false, null));
            });
        }

        [When(@"connection opens with a wrong host; fails with a message containing: ""(.*)""")]
        [Then(@"connection opens with a wrong host; fails with a message containing: ""(.*)""")]
        public void ConnectionOpensWithWrongHostFailsWithMessage(string expectedMessage)
        {
            var exception = Assert.ThrowsAny<Exception>(() =>
            {
                var wrongHostDriver = TypeDB.Driver(
                    "nonexistent-host.invalid:1729",
                    new Credentials("admin", "password"),
                    new DriverOptions(false, null));
            });
            Assert.Contains(expectedMessage, exception.Message);
        }

        // Database: schema retrieval

        [Then(@"connection get database\(([^)]+)\) has schema:")]
        public void ConnectionGetDatabaseHasSchema(string databaseName, DocString expectedSchema)
        {
            Assert.NotNull(Driver);
            var actualSchema = Driver!.Databases.Get(databaseName).GetSchema();

            var expectedText = expectedSchema.Content.Trim();
            if (string.IsNullOrWhiteSpace(expectedText))
            {
                Assert.Equal("", actualSchema);
                return;
            }

            // Normalize expected schema through a temp database for comparison
            // (handles reordering and formatting differences)
            // Note: Don't use RemoveTwoSpacesInTabulation here because the actual schema
            // was inserted via typeql schema query which preserves Gherkin indentation
            var expectedNormalized = ExecuteAndRetrieveSchemaForComparison(expectedText);
            Assert.Equal(expectedNormalized, actualSchema);
        }

        [Then(@"connection get database\(([^)]+)\) has type schema:")]
        public void ConnectionGetDatabaseHasTypeSchema(string databaseName, DocString expectedSchema)
        {
            Assert.NotNull(Driver);
            var actualSchema = Driver!.Databases.Get(databaseName).GetTypeSchema();

            var expectedText = expectedSchema.Content.Trim();
            if (string.IsNullOrWhiteSpace(expectedText))
            {
                Assert.Equal("", actualSchema);
                return;
            }

            // Normalize expected type schema through a temp database for comparison
            // Note: Don't use RemoveTwoSpacesInTabulation here because the actual schema
            // was inserted via typeql schema query which preserves Gherkin indentation
            var tempName = "temp-" + new System.Random().Next(10000);
            Driver!.Databases.Create(tempName);
            try
            {
                var tx = Driver.Transaction(tempName, TransactionType.Schema);
                tx.Query(expectedText);
                tx.Commit();
                var expectedNormalized = Driver.Databases.Get(tempName).GetTypeSchema();
                Assert.Equal(expectedNormalized, actualSchema);
            }
            finally
            {
                try { Driver.Databases.Get(tempName).Delete(); } catch { }
            }
        }

        // Database: create/delete with message variants

        [Then(@"connection create database with empty name; fails with a message containing: ""(.*)""")]
        public void ConnectionCreateDatabaseWithEmptyNameFailsWithMessage(string expectedMessage)
        {
            var exception = Assert.ThrowsAny<Exception>(
                () => Driver!.Databases.Create(""));
            Assert.Contains(expectedMessage, exception.Message);
        }

        [When(@"connection delete database: ([^;]+); fails with a message containing: ""(.*)""")]
        [Then(@"connection delete database: ([^;]+); fails with a message containing: ""(.*)""")]
        public void ConnectionDeleteDatabaseFailsWithMessage(string name, string expectedMessage)
        {
            var exception = Assert.ThrowsAny<Exception>(() =>
            {
                var db = Driver!.Databases.Get(name.Trim());
                db.Delete();
            });
            Assert.Contains(expectedMessage, exception.Message);
        }

        // Background driver operations

        private IDriver CreateBackgroundDriver()
        {
            return TypeDB.Driver(
                TypeDB.DefaultAddress,
                new Credentials("admin", "password"),
                new DriverOptions(false, null));
        }

        [When(@"in background, connection create database: (.+)")]
        [Then(@"in background, connection create database: (.+)")]
        public void InBackgroundConnectionCreateDatabase(string databaseName)
        {
            var bgDriver = CreateBackgroundDriver();
            bgDriver.Databases.Create(databaseName);
            bgDriver.Close();
        }

        [When(@"in background, connection delete database: (.+)")]
        [Then(@"in background, connection delete database: (.+)")]
        public void InBackgroundConnectionDeleteDatabase(string databaseName)
        {
            var bgDriver = CreateBackgroundDriver();
            bgDriver.Databases.Get(databaseName).Delete();
            bgDriver.Close();
        }

        [When(@"in background, connection open schema transaction for database: ([^;]+)$")]
        [Then(@"in background, connection open schema transaction for database: ([^;]+)$")]
        public void InBackgroundConnectionOpenSchemaTransaction(string databaseName)
        {
            BackgroundDriver ??= ConnectionStepsBase.CreateDefaultTypeDBDriver();
            var tx = ConnectionStepsBase.OpenTransaction(
                BackgroundDriver, databaseName.Trim(), TransactionType.Schema, CurrentTransactionOptions);
            BackgroundTransactions.Add(tx);
        }

        // Transaction: open fails with message

        [Then(@"connection open schema transaction for database: ([^;]+); fails with a message containing: ""(.*)""")]
        public void ConnectionOpenSchemaTransactionFailsWithMessage(
            string name, string expectedMessage)
        {
            var exception = Assert.ThrowsAny<Exception>(() =>
            {
                var tx = CurrentTransactionOptions != null
                    ? Driver!.Transaction(
                        name.Trim(), TransactionType.Schema, CurrentTransactionOptions)
                    : Driver!.Transaction(name.Trim(), TransactionType.Schema);
                Transactions.Add(tx);
            });
            Assert.Contains(expectedMessage, exception.Message);
        }

        [Then(@"connection open write transaction for database: ([^;]+); fails with a message containing: ""(.*)""")]
        public void ConnectionOpenWriteTransactionFailsWithMessage(
            string name, string expectedMessage)
        {
            var exception = Assert.ThrowsAny<Exception>(() =>
            {
                var tx = CurrentTransactionOptions != null
                    ? Driver!.Transaction(
                        name.Trim(), TransactionType.Write, CurrentTransactionOptions)
                    : Driver!.Transaction(name.Trim(), TransactionType.Write);
                Transactions.Add(tx);
            });
            Assert.Contains(expectedMessage, exception.Message);
        }

        [Then(@"connection open read transaction for database: ([^;]+); fails with a message containing: ""(.*)""")]
        public void ConnectionOpenReadTransactionFailsWithMessage(
            string name, string expectedMessage)
        {
            var exception = Assert.ThrowsAny<Exception>(() =>
            {
                var tx = CurrentTransactionOptions != null
                    ? Driver!.Transaction(
                        name.Trim(), TransactionType.Read, CurrentTransactionOptions)
                    : Driver!.Transaction(name.Trim(), TransactionType.Read);
                Transactions.Add(tx);
            });
            Assert.Contains(expectedMessage, exception.Message);
        }

        // Transaction: options

        [When(@"set transaction option transaction_timeout_millis to: (\d+)")]
        public void SetTransactionOptionTransactionTimeoutMillis(int value)
        {
            if (CurrentTransactionOptions == null)
                CurrentTransactionOptions = new TransactionOptions();
            CurrentTransactionOptions.TransactionTimeoutMillis = value;
        }

        [When(@"set transaction option schema_lock_acquire_timeout_millis to: (\d+)")]
        public void SetTransactionOptionSchemaLockAcquireTimeoutMillis(int value)
        {
            if (CurrentTransactionOptions == null)
                CurrentTransactionOptions = new TransactionOptions();
            CurrentTransactionOptions.SchemaLockAcquireTimeoutMillis = value;
        }

    }
}
