/*
 * Copyright (C) 2022 Vaticle
 *
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

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Test.Behaviour;

namespace Vaticle.Typedb.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        private TransactionType StringToTransactionType(string value)
        {
            switch (value)
            {
                case "read":
                    return TransactionType.READ;
                case "write":
                    return TransactionType.WRITE;
                default:
                    throw new Exception($"The test value {value} passed to StringToTransactionType is invalid!");
            }
        }

        [Given(@"[for each ]*session[,]? open[s]? transaction[s]? of type: {word}")]
        [When(@"[for each ]*session[,]? open[s]? transaction[s]? of type: {word}")]
        [Then(@"[for each ]*session[,]? open[s]? transaction[s]? of type: {word}")]
        public void ForEachSessionOpenTransactionsOfType(string type)
        {
            TransactionType transactionType = StringToTransactionType(type);
            
            foreach (ITypeDBSession session in Sessions)
            {
                ClearTransactions(session);
                ITypeDBTransaction transaction =
                    session.Transaction(transactionType, TransactionOptions);

                SaveTransaction(transaction, session);
            }
        }

        [When(@"[for each ]*session[,]? open transaction[s]? of type:")]
        public void ForEachSessionOpenTransactionsOfType(DataTable types)
        {
            foreach (var row in types.Rows)
            {
                foreach (var type in row.Cells)
                {
                    ForEachSessionOpenTransactionsOfType(type.Value);
                }
            }
        }

        [When(@"[for each ]*session[,]? open transaction[s]? of type; throws exception: {word}")]
        public void ForEachSessionOpenTransactionsOfTypeThrowsException(string type)
        {
            TransactionType transactionType = StringToTransactionType(type);

            foreach (ITypeDBSession session in Sessions)
            {
                Assert.Throws<TypeDBDriverException>(
                    () => session.Transaction(transactionType));
            }
        }

        [Then(@"[for each ]*session[,]? open transaction[s]? of type; throws exception")]
        public void ForEachSessionOpenTransactionsOfTypeThrowsException(DataTable types)
        {
            foreach (var row in types.Rows)
            {
                foreach (var type in row.Cells)
                {
                    ForEachSessionOpenTransactionsOfTypeThrowsException(type.Value);
                }
            }
        }

        [Then(@"[for each ]*session[,]? transaction[s]? [is|are]+ null: {}")]
        public void ForEachSessionTransactionsAreNull(bool expectedNull)
        {
            foreach (ITypeDBSession session in Sessions)
            {
                foreach (ITypeDBTransaction transaction in SessionsToTransactions[session])
                {
                    Assert.Equal(expectedNull, transaction == null);
                }
            }
        }

        [Then(@"[for each ]*session[,]? transaction[s]? [is|are]+ open: {}")]
        public void ForEachSessionTransactionsAreOpen(bool expectedOpen)
        {
            foreach (ITypeDBSession session in Sessions)
            {
                foreach (ITypeDBTransaction transaction in SessionsToTransactions[session])
                {
                    Assert.Equal(expectedOpen, transaction.IsOpen());
                }
            }
        }

        [Given(@"transaction commits")]
        [When(@"transaction commits")]
        [Then(@"transaction commits")]
        public void TransactionCommits()
        {
            Tx.Commit();
        }

        [Then(@"transaction commits; throws exception")]
        public void TransactionCommitsThrowsException()
        {
            Assert.Throws<TypeDBDriverException>(() => TransactionCommits());
        }

        [Then(@"transaction commits; throws exception containing {string}")]
        public void TransactionCommitsThrowsException(string expectedMessage)
        {
            var exception = Assert.Throws<TypeDBDriverException>(
                () => TransactionCommits());

            Assert.Equal(expectedMessage, exception.Message);
        }

        [Then(@"[for each ]*session[,]? transaction[s]? commit[s]?")]
        public void ForEachSessionTransactionsCommit()
        {
            foreach (ITypeDBSession session in Sessions)
            {
                foreach (ITypeDBTransaction transaction in SessionsToTransactions[session])
                {
                    transaction.Commit();
                }
            }
        }

        [Then(@"[for each ]*session[,]? transaction[s]? commit[s]?; throws exception")]
        public void ForEachSessionTransactionsCommitThrowsException()
        {
            foreach (ITypeDBSession session in Sessions)
            {
                foreach (ITypeDBTransaction transaction in SessionsToTransactions[session])
                {
                    Assert.Throws<TypeDBDriverException>(() => transaction.Commit());
                }
            }
        }

        [Given(@"[for each ]*session[,]? transaction close[s]?")]
        [Then(@"[for each ]*session[,]? transaction close[s]?")]
        public void ForEachSessionTransactionCloses()
        {
            foreach (ITypeDBSession session in Sessions)
            {
                foreach (ITypeDBTransaction transaction in SessionsToTransactions[session])
                {
                    transaction.Close();
                }
            }
        }

        private void ForEachSessionTransactionsHaveType(List<string> types)
        {
            foreach (ITypeDBSession session in Sessions)
            {
                List<ITypeDBTransaction?> transactions = SessionsToTransactions[session];
                Assert.Equal(types.Count, transactions.Count);

                IEnumerator<string> typesEnumerator = types.GetEnumerator();
                IEnumerator<ITypeDBTransaction?> transactionsEnumerator = transactions.GetEnumerator();

                while (typesEnumerator.MoveNext())
                {
                    Assert.True(transactionsEnumerator.MoveNext());
                    Assert.Equal(
                        StringToTransactionType(typesEnumerator.Current),
                        transactionsEnumerator.Current.Type);
                }
            }
        }

        [Then(@"[for each ]*session[,]? transaction[s]? [has|have]+ type: {word}")]
        public void ForEachSessionTransactionsHaveType(string type)
        {
            ForEachSessionTransactionsHaveType(new List<string>(){type});
        }

        [Then(@"[for each ]*session[,]? transaction[s]? [has|have]+ type:")]
        public void ForEachSessionTransactionsHaveType(DataTable types)
        {
            List<string> collectedTypes = Util.ParseDataTableToTypeList(types, val => val.ToString());
            ForEachSessionTransactionsHaveType(collectedTypes);
        }

        [When(@"[for each ]*session, open transaction[s]? in parallel of type:")]
        public void ForEachSessionOpenTransactionsInParallelOfType(DataTable types)
        {
            List<TransactionType> collectedTypes =
                Util.ParseDataTableToTypeList<TransactionType>(types, StringToTransactionType);

            int workerThreads;
            int ioThreads;
            ThreadPool.GetAvailableThreads(out workerThreads, out ioThreads);
            Assert.True(workerThreads > collectedTypes.Count);

            foreach (ITypeDBSession session in Sessions)
            {
                List<Task<ITypeDBTransaction?>> parallelTransactions = new List<Task<ITypeDBTransaction?>>();
                for (int i = 0; i < collectedTypes.Count; i++)
                {
                    TransactionType type = collectedTypes[i];

                    parallelTransactions.Add(Task.Factory.StartNew<ITypeDBTransaction?>(() =>
                        {
                            return session.Transaction(type);
                        }));
                }

                SessionsToParallelTransactions[session] = parallelTransactions;
            }
        }

        [Then(@"[for each ]*session, transactions in parallel are null: {}")]
        public void ForEachSessionTransactionsInParallelAreNull(bool expectedNull)
        {
            List<Task> assertions = new List<Task>();

            foreach (ITypeDBSession session in Sessions)
            {
                foreach (var transaction in SessionsToParallelTransactions[session])
                {
                    assertions.Add(transaction.ContinueWith(
                        antecedent => Assert.Equal(expectedNull, antecedent.Result == null)));
                }
            }

            Task.WaitAll(assertions.ToArray());
        }

        [Then(@"[for each ]*session, transactions in parallel are open: {}")]
        public void ForEachSessionTransactionsInParallelAreOpen(bool expectedOpen)
        {
            List<Task> assertions = new List<Task>();

            foreach (ITypeDBSession session in Sessions)
            {
                foreach (var transaction in SessionsToParallelTransactions[session])
                {
                    assertions.Add(transaction.ContinueWith(
                        antecedent => Assert.Equal(expectedOpen, antecedent.Result.IsOpen())));
                }
            }

            Task.WaitAll(assertions.ToArray());
        }

        [Then(@"[for each ]*session, transactions in parallel have type:")]
        public void ForEachSessionTransactionsInParallelHaveType(DataTable types)
        {
            List<TransactionType> collectedTypes =
                Util.ParseDataTableToTypeList<TransactionType>(types, StringToTransactionType);

            List<Task> assertions = new List<Task>();

            foreach (ITypeDBSession session in Sessions)
            {
                var transactions = SessionsToParallelTransactions[session];

                Assert.Equal(transactions.Count, collectedTypes.Count);

                IEnumerator<TransactionType> typesEnumerator = collectedTypes.GetEnumerator();
                IEnumerator<Task<ITypeDBTransaction?>> transactionsEnumerator = transactions.GetEnumerator();

                while (typesEnumerator.MoveNext())
                {
                    Assert.True(transactionsEnumerator.MoveNext());
                    var expectedType = typesEnumerator.Current;
                    assertions.Add(transactionsEnumerator.Current.ContinueWith(
                        antecedent => Assert.Equal(antecedent.Result.Type, expectedType)));
                }
            }

            Task.WaitAll(assertions.ToArray());
        }

        [Given(@"set transaction option {} to: {word}")]
        public void SetTransactionOptionTo(string option, string value)
        {
            if (!OptionSetters.ContainsKey(option))
            {
                throw new Exception("Unrecognised option: " + option);
            }

            OptionSetters[option](TransactionOptions, value.ToString());
        }
    }
}
