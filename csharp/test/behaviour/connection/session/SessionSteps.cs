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

using com.vaticle.typedb.driver;
using com.vaticle.typedb.driver.Api;
using com.vaticle.typedb.driver.Test.Behaviour.Connection;

namespace com.vaticle.typedb.driver.Test.Behaviour.Connection.Session
{
    public class SessionSteps
    {
        private void ConnectionOpenSessionForDatabase(string name, SessionType sessionType)
        {
            ConnectionStepsBase.Sessions.Add(
                ConnectionStepsBase.Driver.Session(
                    name, sessionType, ConnectionStepsBase.SessionOptions));
        }

        public void ConnectionOpenSchemaSessionForDatabase(string name)
        {
            ConnectionOpenSessionForDatabase(name, SessionType.Schema);
        }

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

        public void ConnectionOpenDataSessionForDatabase(string name)
        {
            ConnectionOpenSessionForDatabase(name, SessionType.Data);
        }

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

        public void ConnectionOpenDataSessionsInParallelForDatabases(DataTable names)
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

        public void ConnectionCloseAllSessions()
        {
            foreach (var session in ConnectionStepsBase.Sessions)
            {
                session.Close();
            }

            ConnectionStepsBase.Sessions.Clear();
        }

        public void SessionsAreNull(bool expectedNull)
        {
            foreach (var session in ConnectionStepsBase.Sessions)
            {
                Assert.Equal(expectedNull, session == null);
            }
        }

        public void SessionsInParallelAreNull(bool expectedNull)
        {
            throw new NotImplementedException("Not yet for parallel null!"); // TODO
            foreach (var session in ConnectionStepsBase.Sessions)
            {
//                Assert.Equal(expectedNull, session == null);
            }
        }

        public void SessionsAreOpen(bool expectedOpen)
        {
            foreach (var session in ConnectionStepsBase.Sessions)
            {
                Assert.Equal(expectedOpen, session.IsOpen());
            }
        }

        public void SessionsInParallelAreOpen(bool expectedOpen)
        {
            throw new NotImplementedException("Not yet for parallel open!"); // TODO
            foreach (var session in ConnectionStepsBase.Sessions)
            {
//                Assert.Equal(expectedOpen, session.IsOpen());
            }
        }

        public void SessionsHaveDatabase(string name)
        {
            throw new NotImplementedException("Not yet for ITERATOR!"); // TODO
        }

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
