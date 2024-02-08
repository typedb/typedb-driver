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

using com.vaticle.typedb.driver;
using com.vaticle.typedb.driver.Api;
using com.vaticle.typedb.driver.Test.Behaviour;

namespace com.vaticle.typedb.driver.Test.Behaviour
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

        [Given(@"connection open data session for database: {word}")]
        [When(@"connection open [data ]?session for database: {word}")]
        public void ConnectionOpenDataSessionForDatabase(string name)
        {
            ConnectionOpenSessionForDatabase(name, SessionType.DATA);
        }

        [When(@"connection open [data ]?sessions for databases:")]
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
            throw new NotImplementedException("Not yet for parallel!"); // TODO
            foreach (var row in names.Rows)
            {
                foreach (var name in row.Cells)
                {
                    //ConnectionOpenDataSessionForDatabase(name.Value);
                }
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
            throw new NotImplementedException("Not yet for parallel null!"); // TODO
            foreach (var session in ConnectionStepsBase.Sessions)
            {
//                Assert.Equal(expectedNull, session == null);
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
            throw new NotImplementedException("Not yet for parallel open!"); // TODO
            foreach (var session in ConnectionStepsBase.Sessions)
            {
//                Assert.Equal(expectedOpen, session.IsOpen());
            }
        }

        [Then(@"session[s]? has database: {word}")]
        public void SessionsHaveDatabase(string name)
        {
            throw new NotImplementedException("Not yet for ITERATOR!"); // TODO
        }

        [Then(@"session[s]? [have|has]+ database[s]?:")]
        public void SessionsHaveDatabases(DataTable names)
        {
            List<string> expectedNames = new List<string>();
            foreach (var row in names.Rows)
            {
                foreach (var name in row.Cells)
                {
                    expectedNames.Add(name.Value);
                }
            }

            Assert.Equal(expectedNames.Count, ConnectionStepsBase.Sessions.Count);
            throw new NotImplementedException("Not yet for ITERATOR!"); // TODO
        }

        [Then(@"sessions in parallel have databases:")]
        public void SessionsInParallelHaveDatabases(DataTable names)
        {
            throw new NotImplementedException("Not yet for parallel and ITERATOR!"); // TODO
            foreach (var row in names.Rows)
            {
                foreach (var name in row.Cells)
                {

                }
            }
        }

        [Given(@"set session option {word} to: {word}")]
        public void SetSessionOptionTo(string option, string value)
        {
            throw new NotImplementedException("Not yet for SetSessionOption"); // TODO

//            if (!optionSetters.containsKey(option))
//            {
//                throw new Exception("Unrecognised option: " + option);
//            }
//            optionSetters.get(option).accept(sessionOptions, value);
        }
    }
}
