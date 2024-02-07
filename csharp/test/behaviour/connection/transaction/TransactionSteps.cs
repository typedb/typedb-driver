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

#nullable enable

using DataTable = Gherkin.Ast.DataTable;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Xunit;

using com.vaticle.typedb.driver;
using com.vaticle.typedb.driver.Api;
using com.vaticle.typedb.driver.Test.Behaviour.Connection;

namespace com.vaticle.typedb.driver.Test.Behaviour.Connection.Transaction
{
    public class TransactionSteps
    {
        private TransactionType StringToTransactionType(string value)
        {
            switch (value)
            {
                case "read":
                    return TransactionType.Read;
                case "write":
                    return TransactionType.Write;
                default:
                    throw new Exception($"The test value {value} passed to StringToTransactionType is invalid!");
            }
        }

        public void ForEachSessionOpenTransactionsOfType(string type)
        {
            TransactionType transactionType = StringToTransactionType(type);
            
            foreach (ITypeDBSession session in ConnectionStepsBase.Sessions)
            {
                ITypeDBTransaction transaction =
                    session.Transaction(transactionType, ConnectionStepsBase.TransactionOptions);

                ConnectionStepsBase.SaveTransaction(transaction, session);
            }
        }

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

        public void ForEachSessionOpenTransactionsOfTypeThrowsException(string type)
        {
            TransactionType transactionType = StringToTransactionType(type);

            foreach (ITypeDBSession session in ConnectionStepsBase.Sessions)
            {
                Assert.Throws<Common.Exception.TypeDBDriverException>(
                    () => session.Transaction(transactionType));
            }
        }

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

        public void ForEachSessionTransactionsAreNull(bool expectedNull)
        {
            foreach (ITypeDBSession session in ConnectionStepsBase.Sessions)
            {
                foreach (ITypeDBTransaction transaction in ConnectionStepsBase.SessionsToTransactions[session])
                {
                    Assert.Equal(expectedNull, transaction == null);
                }
            }
        }

        public void ForEachSessionTransactionsAreOpen(bool expectedOpen)
        {
            foreach (ITypeDBSession session in ConnectionStepsBase.Sessions)
            {
                foreach (ITypeDBTransaction transaction in ConnectionStepsBase.SessionsToTransactions[session])
                {
                    Assert.Equal(expectedOpen, transaction.IsOpen());
                }
            }
        }

        public void TransactionCommits()
        {
            ConnectionStepsBase.SessionsToTransactions[ConnectionStepsBase.Sessions[0]][0].Commit();
        }

        public void TransactionCommitsThrowsException()
        {
            Assert.Throws<Common.Exception.TypeDBDriverException>(
                () => TransactionCommits());
        }

        public void TransactionCommitsThrowsException(string expectedMessage)
        {
            var exception = Assert.Throws<Common.Exception.TypeDBDriverException>(
                () => TransactionCommits());

            Assert.Equal(expectedMessage, exception.Message);
        }

        public void ForEachSessionTransactionsCommit()
        {
            foreach (ITypeDBSession session in ConnectionStepsBase.Sessions)
            {
                foreach (ITypeDBTransaction transaction in ConnectionStepsBase.SessionsToTransactions[session])
                {
                    transaction.Commit();
                }
            }
        }

        public void ForEachSessionTransactionsCommitThrowsException()
        {
            foreach (ITypeDBSession session in ConnectionStepsBase.Sessions)
            {
                foreach (ITypeDBTransaction transaction in ConnectionStepsBase.SessionsToTransactions[session])
                {
                    Assert.Throws<Common.Exception.TypeDBDriverException>(
                        () => transaction.Commit());
                }
            }
        }

        public void ForEachSessionTransactionCloses()
        {
            foreach (ITypeDBSession session in ConnectionStepsBase.Sessions)
            {
                foreach (ITypeDBTransaction transaction in ConnectionStepsBase.SessionsToTransactions[session])
                {
                    transaction.Close();
                }
            }
        }

        private void ForEachSessionTransactionsHaveType(List<string> types)
        {
            foreach (ITypeDBSession session in ConnectionStepsBase.Sessions)
            {
                List<ITypeDBTransaction?> transactions = ConnectionStepsBase.SessionsToTransactions[session];
                Assert.Equal(types.Count, transactions.Count);

                IEnumerator<string> typesEnumerator = types.GetEnumerator();
                IEnumerator<ITypeDBTransaction?> transactionsEnumerator = transactions.GetEnumerator();

                while (typesEnumerator.MoveNext())
                {
                    Assert.True(transactionsEnumerator.MoveNext());
                    Assert.Equal(
                        StringToTransactionType(typesEnumerator.Current),
                        transactionsEnumerator.Current.Type());
                }
            }
        }

        public void ForEachSessionTransactionsHaveType(string type)
        {
            ForEachSessionTransactionsHaveType(new List<string>(){type});
        }

        public void ForEachSessionTransactionsHaveType(DataTable types)
        {
            List<string> collectedTypes = new List<string>();
            foreach (var row in types.Rows)
            {
                foreach (var type in row.Cells)
                {
                    collectedTypes.Add(type.Value);
                }
            }

            ForEachSessionTransactionsHaveType(collectedTypes);
        }

        public void ForEachSessionOpenTransactionsInParallelOfType(DataTable types)
        {
            foreach (ITypeDBSession session in ConnectionStepsBase.Sessions)
            {
                throw new System.Exception("Parallel test is not ready yet!"); // TODO
            }
        }

        public void ForEachSessionTransactionsInParallelAreNull(bool expectedNull)
        {
            foreach (ITypeDBSession session in ConnectionStepsBase.Sessions)
            {
                throw new System.Exception("Parallel Null test is not ready yet!"); // TODO
            }
        }

        public void ForEachSessionTransactionsInParallelAreOpen(bool expectedOpen)
        {
            foreach (ITypeDBSession session in ConnectionStepsBase.Sessions)
            {
                throw new System.Exception("Parallel Open test is not ready yet!"); // TODO
            }
        }

        public void ForEachSessionTransactionsInParallelHaveType(DataTable types)
        {
            foreach (ITypeDBSession session in ConnectionStepsBase.Sessions)
            {
                throw new System.Exception("Parallel HaveType test is not ready yet!"); // TODO
            }
        }

        public void ForEachSessionInParallelTransactionsInParallelAreNull(bool expectedNull)
        {
            throw new System.Exception("Parallel Parallel Null test is not ready yet!"); // TODO
        }

        public void ForEachSessionInParallelTransactionsInParallelAreOpen(bool expectedOpen)
        {
            throw new System.Exception("Parallel Parallel Open test is not ready yet!"); // TODO
        }

        public void SetTransactionOptionTo(string option, string value)
        {
            throw new System.Exception($"Options Setters are not ready yet! {option} {value}"); // TODO
        }

        public void ForEachTransactionExecuteDefineThrowsException(string expectedMessage, string defineQueryStatements)
        {
            foreach (ITypeDBSession session in ConnectionStepsBase.Sessions)
            {
                foreach (ITypeDBTransaction transaction in ConnectionStepsBase.SessionsToTransactions[session])
                {
                    throw new Exception("Not ready for this test as well!"); // TODO
//                    try
//                    {
//                        transaction.Query().Define(TypeQL.parseQuery(defineQueryStatements).asDefine()).resolve();
//                        fail();
//                    }
//                    catch (System.Exception e)
//                    {
//                        assertThat(e.getMessage(), Matchers.containsString(expectedException));
//                    }
                }
            }
        }
    }
}
