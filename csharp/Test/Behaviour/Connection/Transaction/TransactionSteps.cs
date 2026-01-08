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
using DocString = Gherkin.Ast.DocString;
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
        // For parallel transaction tests
        private List<Task<ITypeDBTransaction>> _parallelTransactions = new List<Task<ITypeDBTransaction>>();

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
                default:
                    throw new Exception($"The test value {value} passed to StringToTransactionType is invalid!");
            }
        }

        [Then(@"transaction has type: (\S+)")]
        public void TransactionHasType(string type)
        {
            Assert.True(Transactions.Count > 0, "No transaction open");
            var tx = Transactions[Transactions.Count - 1];
            Assert.Equal(StringToTransactionType(type), tx.Type);
        }

        [Then(@"transaction commits; fails")]
        public void TransactionCommitsFails()
        {
            Assert.True(Transactions.Count > 0, "No transaction to commit");
            var tx = Transactions[Transactions.Count - 1];
            Assert.Throws<TypeDBDriverException>(() => tx.Commit());
        }

        [Then(@"transaction commits; fails with a message containing: ""(.*)""")]
        public void TransactionCommitsFailsWithMessage(string expectedMessage)
        {
            Assert.True(Transactions.Count > 0, "No transaction to commit");
            var tx = Transactions[Transactions.Count - 1];
            var exception = Assert.Throws<TypeDBDriverException>(() => tx.Commit());
            Assert.Contains(expectedMessage, exception.Message);
        }

        [Given(@"transaction rollbacks")]
        [When(@"transaction rollbacks")]
        [Then(@"transaction rollbacks")]
        public void TransactionRollbacks()
        {
            Assert.True(Transactions.Count > 0, "No transaction to rollback");
            var tx = Transactions[Transactions.Count - 1];
            tx.Rollback();
        }

        [Then(@"transaction rollbacks; fails")]
        public void TransactionRollbacksFails()
        {
            Assert.True(Transactions.Count > 0, "No transaction to rollback");
            var tx = Transactions[Transactions.Count - 1];
            Assert.Throws<TypeDBDriverException>(() => tx.Rollback());
        }

        [When(@"connection open transactions for database: (\S+), of type:")]
        public void ConnectionOpenTransactionsForDatabase(string database, DataTable types)
        {
            foreach (var row in types.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    var type = StringToTransactionType(cell.Value);
                    var tx = Driver!.Transaction(database, type);
                    Transactions.Add(tx);
                }
            }
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

            Assert.Equal(expectedTypes.Count, Transactions.Count);
            for (int i = 0; i < Transactions.Count; i++)
            {
                Assert.Equal(expectedTypes[i], Transactions[i].Type);
            }
        }

        [When(@"connection open transactions in parallel for database: (\S+), of type:")]
        public void ConnectionOpenTransactionsInParallelForDatabase(string database, DataTable types)
        {
            _parallelTransactions.Clear();

            var collectedTypes = new List<TransactionType>();
            foreach (var row in types.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    collectedTypes.Add(StringToTransactionType(cell.Value));
                }
            }

            int workerThreads;
            int ioThreads;
            ThreadPool.GetAvailableThreads(out workerThreads, out ioThreads);
            Assert.True(workerThreads > collectedTypes.Count);

            foreach (var type in collectedTypes)
            {
                var txType = type;
                _parallelTransactions.Add(Task.Factory.StartNew(() =>
                    Driver!.Transaction(database, txType)));
            }
        }

        [Then(@"transactions in parallel are open: (.*)")]
        public void TransactionsInParallelAreOpen(string expectedState)
        {
            bool expected = bool.Parse(expectedState);
            var assertions = new List<Task>();

            foreach (var txTask in _parallelTransactions)
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

            Assert.Equal(expectedTypes.Count, _parallelTransactions.Count);

            var assertions = new List<Task>();
            for (int i = 0; i < _parallelTransactions.Count; i++)
            {
                var expectedType = expectedTypes[i];
                assertions.Add(_parallelTransactions[i].ContinueWith(
                    antecedent => Assert.Equal(expectedType, antecedent.Result.Type)));
            }

            Task.WaitAll(assertions.ToArray());
        }

        // Query-related steps moved to QuerySteps.cs
    }
}
