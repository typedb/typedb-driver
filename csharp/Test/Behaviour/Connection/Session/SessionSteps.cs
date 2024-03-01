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
using Vaticle.Typedb.Driver.Test.Behaviour;

namespace Vaticle.Typedb.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        private void ConnectionOpenSessionForDatabase(string name, SessionType sessionType)
        {
            ConnectionStepsBase.Sessions.Add(
                ConnectionStepsBase.Driver.Session(
                    name, sessionType, ConnectionStepsBase.SessionOptions));
        }

        [Given(@"connection open schema session for database: {word}")]
        [When(@"connection open schema session for database: {word}")]
        public void ConnectionOpenSchemaSessionForDatabase(string name)
        {
            ConnectionOpenSessionForDatabase(name, SessionType.SCHEMA);
        }

        [When(@"connection open schema session for database: {word}")]
        public void ConnectionOpenSchemaSessionForDatabases(DataTable names)
        {
            foreach (var row in names.Rows)
            {
                foreach (var name in row.Cells)
                {
                    ConnectionOpenSchemaSessionForDatabase(name.Value);
                }
            }
        }

        [Given(@"connection open[ data | ]+session[s]? for database[s]?: {word}")]
        [When(@"connection open[ data | ]+session[s]? for database[s]?: {word}")]
        public void ConnectionOpenDataSessionForDatabase(string name)
        {
            ConnectionOpenSessionForDatabase(name, SessionType.DATA);
        }

        [Given(@"connection open[ data | ]+session[s]? for database[s]?:")]
        [When(@"connection open[ data | ]+session[s]? for database[s]?:")]
        public void ConnectionOpenDataSessionForDatabases(DataTable names)
        {
            foreach (var row in names.Rows)
            {
                foreach (var name in row.Cells)
                {
                    ConnectionOpenDataSessionForDatabase(name.Value);
                }
            }
        }

        [When(@"connection open sessions in parallel for databases:")]
        public void ConnectionOpenSessionsInParallelForDatabases(DataTable names)
        {
            var collectedNames = new List<string>();
            foreach (var row in names.Rows)
            {
                foreach (var name in row.Cells)
                {
                    collectedNames.Add(name.Value);
                }
            }

            int workerThreads;
            int ioThreads;
            ThreadPool.GetAvailableThreads(out workerThreads, out ioThreads);
            Assert.True(workerThreads > collectedNames.Count);

            Assert.False(ConnectionStepsBase.ParallelSessions.Any());

            for (int i = 0; i < collectedNames.Count; i++)
            {
                var name = collectedNames[i];
                ConnectionStepsBase.ParallelSessions.Add(Task.Factory.StartNew<ITypeDBSession?>(() =>
                    {
                        return ConnectionStepsBase.Driver.Session(
                            name, SessionType.DATA, ConnectionStepsBase.SessionOptions);
                    }));
            }
        }

        [When(@"connection close all sessions")]
        public void ConnectionCloseAllSessions()
        {
            foreach (var session in ConnectionStepsBase.Sessions)
            {
                session.Close();
            }

            ConnectionStepsBase.Sessions.Clear();
        }

        [Then(@"session[s]? [is|are]+ null: {}")]
        public void SessionsAreNull(bool expectedNull)
        {
            foreach (var session in ConnectionStepsBase.Sessions)
            {
                Assert.Equal(expectedNull, session == null);
            }
        }

        [Then(@"sessions in parallel are null: {}")]
        public void SessionsInParallelAreNull(bool expectedNull)
        {
            foreach (var session in ConnectionStepsBase.ParallelSessions)
            {
                session.ContinueWith(antecedent => Assert.Equal(expectedNull, antecedent.Result == null));
            }
        }

        [Then(@"session[s]? [is|are]+ open: {}")]
        public void SessionsAreOpen(bool expectedOpen)
        {
            foreach (var session in ConnectionStepsBase.Sessions)
            {
                Assert.Equal(expectedOpen, session.IsOpen());
            }
        }

        [Then(@"sessions in parallel are open: {}")]
        public void SessionsInParallelAreOpen(bool expectedOpen)
        {
            foreach (var session in ConnectionStepsBase.ParallelSessions)
            {
                session.ContinueWith(antecedent => Assert.Equal(expectedOpen, antecedent.Result.IsOpen()));
            }
        }

        private void SessionsHaveDatabases(List<string> names)
        {
            Assert.Equal(names.Count, ConnectionStepsBase.Sessions.Count);
            IEnumerator<ITypeDBSession?> sessionsEnumerator = ConnectionStepsBase.Sessions.GetEnumerator();
            IEnumerator<string> namesEnumerator = names.GetEnumerator();

            while (sessionsEnumerator.MoveNext() && namesEnumerator.MoveNext())
            {
                Assert.Equal(namesEnumerator.Current, sessionsEnumerator.Current.DatabaseName);
            }
        }

        [Then(@"session[s]? has database: {word}")]
        public void SessionsHaveDatabase(string name)
        {
            SessionsHaveDatabases(new List<string>(){name});
        }

        [Then(@"session[s]? [have|has]+ database[s]?:")]
        public void SessionsHaveDatabases(DataTable names)
        {
            List<string> collectedNames = new List<string>();
            foreach (var row in names.Rows)
            {
                foreach (var name in row.Cells)
                {
                    collectedNames.Add(name.Value);
                }
            }

            SessionsHaveDatabases(collectedNames);
        }

        [Then(@"sessions in parallel have databases:")]
        public void SessionsInParallelHaveDatabases(DataTable names)
        {
            foreach (var row in names.Rows)
            {
                foreach (var name in row.Cells)
                {
                    foreach (var session in ConnectionStepsBase.ParallelSessions)
                    {
                        session.ContinueWith(
                            antecedent => Assert.Equal(name.Value, antecedent.Result.DatabaseName));
                    }
                }
            }
        }

        [Then(@"set session option {} to: {word}")]
        public void SetSessionOptionTo(string option, string value)
        {
            if (!OptionSetters.ContainsKey(option))
            {
                throw new Exception("Unrecognised option: " + option);
            }

            OptionSetters[option](SessionOptions, value.ToString());
        }
    }
}
