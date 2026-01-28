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

using DataTable = Gherkin.Ast.DataTable;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Xunit;
using Xunit.Gherkin.Quick;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using TypeDB.Driver.Test.Behaviour;

namespace TypeDB.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        private TransactionType StringToTransactionType(string value)
        {
            switch (value.ToLower())
            {
                case "read":
                    return TransactionType.Read;
                case "write":
                    return TransactionType.Write;
                case "schema":
                    return TransactionType.Schema;
                case "<type>":
                    throw new BehaviourTestException(
                        "KNOWN LIMITATION: Xunit.Gherkin.Quick doesn't substitute <type> placeholder in DataTables.");
                default:
                    throw new Exception($"The test value {value} passed to StringToTransactionType is invalid!");
            }
        }

        [Then(@"transaction has type: (\S+)")]
        public void TransactionHasType(string type)
        {
            Assert.Equal(StringToTransactionType(type), ConnectionStepsBase.Tx.Type);
        }

        [Then(@"transactions are open: (.*)")]
        public void TransactionsAreOpen(string expectedState)
        {
            bool expected = bool.Parse(expectedState);
            foreach (var tx in Transactions)
            {
                Assert.Equal(expected, tx.IsOpen());
            }
        }

        // Transaction commits/closes/rollbacks ; fails variants

        [Then(@"transaction commits; fails")]
        public void TransactionCommitsFails()
        {
            Assert.ThrowsAny<Exception>(() => TxPop().Commit());
        }

        [Then(@"transaction commits; fails with a message containing: ""(.*)""")]
        public void TransactionCommitsFailsWithMessage(string expectedMessage)
        {
            var exception = Assert.ThrowsAny<Exception>(() => TxPop().Commit());
            Assert.Contains(expectedMessage, exception.Message);
        }

        [Then(@"transaction closes; fails")]
        public void TransactionClosesFails()
        {
            Assert.ThrowsAny<Exception>(() => TxPop().Close());
        }

        [Then(@"transaction closes; fails with a message containing: ""(.*)""")]
        public void TransactionClosesFailsWithMessage(string expectedMessage)
        {
            var exception = Assert.ThrowsAny<Exception>(() => TxPop().Close());
            Assert.Contains(expectedMessage, exception.Message);
        }

        [Then(@"transaction rollbacks; fails")]
        public void TransactionRollbacksFails()
        {
            Assert.ThrowsAny<Exception>(() => ConnectionStepsBase.Tx.Rollback());
        }

        [Then(@"transaction rollbacks; fails with a message containing: ""(.*)""")]
        public void TransactionRollbacksFailsWithMessage(string expectedMessage)
        {
            var exception = Assert.ThrowsAny<Exception>(() => ConnectionStepsBase.Tx.Rollback());
            Assert.Contains(expectedMessage, exception.Message);
        }

        // Plural transaction open steps

        [When(@"connection open transactions for database: (\S+), of type:")]
        public void ConnectionOpenTransactionsForDatabase(string database, DataTable types)
        {
            foreach (var row in types.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    var type = StringToTransactionType(cell.Value);
                    var tx = ConnectionStepsBase.OpenTransaction(
                        Driver!, database, type, CurrentTransactionOptions);
                    Transactions.Add(tx);
                }
            }
        }

        [Then(@"transactions have type:")]
        public void TransactionsHaveType(DataTable types)
        {
            var expectedTypes = new List<TransactionType>();
            foreach (var row in types.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    expectedTypes.Add(StringToTransactionType(cell.Value));
                }
            }

            var typeIterator = expectedTypes.GetEnumerator();
            foreach (var tx in Transactions)
            {
                Assert.True(typeIterator.MoveNext(), "types list is shorter than saved transactions");
                Assert.Equal(typeIterator.Current, tx.Type);
            }
            Assert.False(typeIterator.MoveNext(), "types list is longer than saved transactions");
        }

        // Parallel transaction steps

        [When(@"connection open transactions in parallel for database: (\S+), of type:")]
        public void ConnectionOpenTransactionsInParallelForDatabase(string database, DataTable types)
        {
            TransactionsParallel.Clear();

            var collectedTypes = new List<TransactionType>();
            foreach (var row in types.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    collectedTypes.Add(StringToTransactionType(cell.Value));
                }
            }

            foreach (var type in collectedTypes)
            {
                var txType = type;
                TransactionsParallel.Add(Task.Run(() =>
                    Driver!.Transaction(database, txType)));
            }
        }

        [Then(@"transactions in parallel are open: (.*)")]
        public void TransactionsInParallelAreOpen(string expectedState)
        {
            bool expected = bool.Parse(expectedState);
            var assertions = new List<Task>();

            foreach (var txTask in TransactionsParallel)
            {
                assertions.Add(txTask.ContinueWith(
                    antecedent => Assert.Equal(expected, antecedent.Result.IsOpen())));
            }

            Task.WaitAll(assertions.ToArray());
        }

        [Then(@"transactions in parallel have type:")]
        public void TransactionsInParallelHaveType(DataTable types)
        {
            var expectedTypes = new List<TransactionType>();
            foreach (var row in types.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    expectedTypes.Add(StringToTransactionType(cell.Value));
                }
            }

            var assertions = new List<Task>();
            int index = 0;
            foreach (var txTask in TransactionsParallel)
            {
                Assert.True(index < expectedTypes.Count, "types list is shorter than saved transactions");
                var expectedType = expectedTypes[index++];
                assertions.Add(txTask.ContinueWith(
                    antecedent => Assert.Equal(expectedType, antecedent.Result.Type)));
            }

            Task.WaitAll(assertions.ToArray());
            Assert.Equal(expectedTypes.Count, index);
        }

        // Background transaction steps - any transaction type with may_error

        [When(@"in background, connection open schema transaction for database: ([^;]+); fails with a message containing: ""(.*)""")]
        [Then(@"in background, connection open schema transaction for database: ([^;]+); fails with a message containing: ""(.*)""")]
        public void InBackgroundConnectionOpenSchemaTransactionFailsWithMessage(string databaseName, string expectedMessage)
        {
            var exception = Assert.ThrowsAny<Exception>(() =>
            {
                BackgroundDriver ??= ConnectionStepsBase.CreateDefaultTypeDBDriver();
                var tx = ConnectionStepsBase.OpenTransaction(
                    BackgroundDriver, databaseName.Trim(), TransactionType.Schema, CurrentTransactionOptions);
                BackgroundTransactions.Add(tx);
            });
            Assert.Contains(expectedMessage, exception.Message);
        }

        [When(@"in background, connection open write transaction for database: ([^;]+)")]
        [Then(@"in background, connection open write transaction for database: ([^;]+)")]
        public void InBackgroundConnectionOpenWriteTransaction(string databaseName)
        {
            BackgroundDriver ??= ConnectionStepsBase.CreateDefaultTypeDBDriver();
            var tx = ConnectionStepsBase.OpenTransaction(
                BackgroundDriver, databaseName.Trim(), TransactionType.Write, CurrentTransactionOptions);
            BackgroundTransactions.Add(tx);
        }

        [When(@"in background, connection open read transaction for database: ([^;]+)")]
        [Then(@"in background, connection open read transaction for database: ([^;]+)")]
        public void InBackgroundConnectionOpenReadTransaction(string databaseName)
        {
            BackgroundDriver ??= ConnectionStepsBase.CreateDefaultTypeDBDriver();
            var tx = ConnectionStepsBase.OpenTransaction(
                BackgroundDriver, databaseName.Trim(), TransactionType.Read, CurrentTransactionOptions);
            BackgroundTransactions.Add(tx);
        }
    }
}
