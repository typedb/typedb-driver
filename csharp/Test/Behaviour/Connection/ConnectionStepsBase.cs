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
        public static ITypeDBDriver? Driver;

        public static List<ITypeDBSession> Sessions = new List<ITypeDBSession>();
        public static List<Task<ITypeDBSession>> ParallelSessions = new List<Task<ITypeDBSession>>();

        public static Dictionary<ITypeDBSession, List<ITypeDBTransaction>> SessionsToTransactions =
            new Dictionary<ITypeDBSession, List<ITypeDBTransaction>>();
        public static Dictionary<ITypeDBSession, List<Task<ITypeDBTransaction>>> SessionsToParallelTransactions =
            new Dictionary<ITypeDBSession, List<Task<ITypeDBTransaction>>>();
        public static Dictionary<Task<ITypeDBSession>, List<Task<ITypeDBTransaction>>> ParallelSessionsToParallelTransactions =
            new Dictionary<Task<ITypeDBSession>, List<Task<ITypeDBTransaction>>>();

        public static TypeDBOptions SessionOptions = new TypeDBOptions();
        public static TypeDBOptions TransactionOptions = new TypeDBOptions();

        public static readonly Dictionary<string, Action<TypeDBOptions, string>> OptionSetters =
            new Dictionary<string, Action<TypeDBOptions, string>>(){
                {"session-idle-timeout-millis", (option, val) => option.SessionIdleTimeoutMillis(Int32.Parse(val))},
                {"transaction-timeout-millis", (option, val) => option.TransactionTimeoutMillis(Int32.Parse(val))}
        };

        // TODO: implement configuration and remove skips when @ignore-typedb-driver is removed from .feature.
        protected bool _requiredConfiguration = false;

        public ConnectionStepsBase() // "Before"
        {
            CleanInCaseOfPreviousFail();

            SessionOptions = SessionOptions.Infer(true);
            TransactionOptions = TransactionOptions.Infer(true);
        }

        public virtual void Dispose() // "After"
        {
            foreach (var (session, transactions) in SessionsToParallelTransactions)
            {
                Task.WaitAll(transactions.ToArray());
            }
            SessionsToParallelTransactions.Clear();

            foreach (var session in Sessions)
            {
                session.Close();
            }

            Sessions.Clear();
            SessionsToTransactions.Clear();

            Task.WaitAll(ParallelSessions.ToArray());
            ParallelSessions.Clear();

            foreach (var (session, transactions) in ParallelSessionsToParallelTransactions)
            {
                session.Wait();
                Task.WaitAll(transactions.ToArray());
            }
            
            ParallelSessionsToParallelTransactions.Clear();

            if (Driver != null)
            {
                foreach (var db in Driver!.Databases.GetAll())
                {
                    db.Delete();
                }

                if (Driver.IsOpen())
                {
                    Driver!.Close();
                }
            }
        }

        public static ITypeDBTransaction Tx
        {
            get { return SessionsToTransactions[Sessions[0]][0]; }
        }

        public abstract ITypeDBDriver CreateTypeDBDriver(string address);

        public abstract void TypeDBStarts();

        public abstract void ConnectionOpensWithDefaultAuthentication();

        [Given(@"connection has been opened")]
        public virtual void ConnectionHasBeenOpened()
        {
            if (_requiredConfiguration) return; // Skip tests with configuration

            Assert.NotNull(Driver);
            Assert.True(Driver.IsOpen());
        }

        [When(@"connection closes")]
        [Then(@"connection closes")]
        public virtual void ConnectionCloses()
        {
            if (_requiredConfiguration) return; // Skip tests with configuration

            Driver!.Close();
            Driver = null;
        }

        public static void ClearTransactions(ITypeDBSession session)
        {
            if (SessionsToTransactions.ContainsKey(session))
            {
                SessionsToTransactions[session].Clear();
            }
        }

        private void CleanInCaseOfPreviousFail() // Fails are exceptions which do not clean resources
        {
            TypeDBStarts();
            ConnectionOpensWithDefaultAuthentication();
            ConnectionHasBeenOpened();
            Dispose();
            ConnectionCloses();
        }
    }
}
