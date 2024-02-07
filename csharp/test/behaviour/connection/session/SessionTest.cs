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

using com.vaticle.typedb.driver.Test.Behaviour.Connection;
using com.vaticle.typedb.driver.Test.Behaviour.Connection.Database;
using com.vaticle.typedb.driver.Test.Behaviour.Connection.Session;

namespace com.vaticle.typedb.driver.Test.Behaviour.Connection.Session
{
    [FeatureFile("external/vaticle_typedb_behaviour/connection/session.feature")]
    public class SessionTest
        : Feature
        , IDisposable
        , IClassFixture<SessionSteps>
        , IClassFixture<DatabaseSteps>
        , IClassFixture<ConnectionSteps>
    {
        public SessionTest()
            : base()
        {
            _sessionSteps = new SessionSteps();
            _connectionSteps = new ConnectionSteps();
            _databaseSteps = new DatabaseSteps();
        }

        public void Dispose()
        {
            _connectionSteps.Dispose();
        }

        [Given(@"typedb starts")]
        [When(@"typedb starts")]
        public void TypeDBStarts()
            => _connectionSteps.TypeDBStarts();

        [Given(@"connection opens with default authentication")]
        [When(@"connection opens with default authentication")]
        public void ConnectionOpensWithDefaultAuthentication()
            => _connectionSteps.ConnectionOpensWithDefaultAuthentication();

        [When(@"connection opens with authentication: {word}, {word}")]
        public void ConnectionOpensWithAuthentication(string username, string password)
            => _connectionSteps.ConnectionOpensWithAuthentication(username, password);

        [Given(@"connection has been opened")]
        public void ConnectionHasBeenOpened()
            => _connectionSteps.ConnectionHasBeenOpened();

        [When(@"connection closes")]
        public void ConnectionCloses()
            => _connectionSteps.ConnectionCloses();

        [Given(@"connection create database: {word}")]
        [When(@"connection create database: {word}")]
        public void ConnectionCreateDatabase(string name)
            => _databaseSteps.ConnectionCreateDatabase(name);

        [Given(@"connection create databases:")]
        [When(@"connection create databases:")]
        public void ConnectionCreateDatabases(DataTable names)
            => _databaseSteps.ConnectionCreateDatabases(names);

        [Given(@"connection create databases in parallel:")]
        [When(@"connection create databases in parallel:")]
        public void ConnectionCreateDatabasesInParallel(DataTable names)
            => _databaseSteps.ConnectionCreateDatabasesInParallel(names);

        [When(@"connection delete database: {word}")]
        public void ConnectionDeleteDatabase(string name)
            => _databaseSteps.ConnectionDeleteDatabase(name);

        [When(@"connection delete databases:")]
        public void ConnectionDeleteDatabases(DataTable names)
            => _databaseSteps.ConnectionDeleteDatabases(names);

        [When(@"connection delete database; throws exception: {word}")]
        public void ConnectionDeleteDatabaseThrowsException(string databaseName)
            => _databaseSteps.ConnectionDeleteDatabaseThrowsException(databaseName);

        [When(@"connection delete databases in parallel:")]
        public void ConnectionDeleteDatabasesInParallel(DataTable names)
            => _databaseSteps.ConnectionDeleteDatabasesInParallel(names);

        [Then(@"connection has database: {word}")]
        public void ConnectionHasDatabase(string name)
            => _databaseSteps.ConnectionHasDatabase(name);

        [Then(@"connection has databases:")]
        public void ConnectionHasDatabases(DataTable names)
            => _databaseSteps.ConnectionHasDatabases(names);

        [Then(@"connection does not have database: {word}")]
        public void ConnectionDoesNotHaveDatabase(string name)
            => _databaseSteps.ConnectionDoesNotHaveDatabase(name);

        [Then(@"connection does not have databases:")]
        public void ConnectionDoesNotHaveDatabases(DataTable names)
            => _databaseSteps.ConnectionDoesNotHaveDatabases(names);

        [Given(@"connection does not have any database")]
        [Then(@"connection does not have any database")]
        public void ConnectionDoesNotHaveAnyDatabase()
            => _databaseSteps.ConnectionDoesNotHaveAnyDatabase();

        [When(@"connection open schema session for database: {word}")]
        public void ConnectionOpenSchemaSessionForDatabase(string name)
            => _sessionSteps.ConnectionOpenSchemaSessionForDatabase(name);

        [When(@"connection open (data )session for database: {word}")]
        public void ConnectionOpenSchemaSessionForDatabases(DataTable names)
            => _sessionSteps.ConnectionOpenSchemaSessionForDatabases(names);

        [When(@"connection open schema session(s) for database(s):")]
        public void ConnectionOpenDataSessionForDatabase(string name)
            => _sessionSteps.ConnectionOpenDataSessionForDatabase(name);

        [When(@"connection open (data )session(s) for database(s):")]
        public void ConnectionOpenDataSessionForDatabases(DataTable names)
            => _sessionSteps.ConnectionOpenDataSessionForDatabases(names);

        [When(@"connection open (data )sessions in parallel for databases:")]
        public void ConnectionOpenDataSessionsInParallelForDatabases(DataTable names)
            => _sessionSteps.ConnectionOpenDataSessionsInParallelForDatabases(names);

        [When(@"connection close all sessions")]
        public void ConnectionCloseAllSessions()
            => _sessionSteps.ConnectionCloseAllSessions();

        [Then(@"session(s) is/are null: {bool}")]
        public void SessionsAreNull(bool expectedNull)
            => _sessionSteps.SessionsAreNull(expectedNull);

        [Then(@"sessions in parallel are null: {bool}")]
        public void SessionsInParallelAreNull(bool expectedNull)
            => _sessionSteps.SessionsInParallelAreNull(expectedNull);

        [Then(@"session(s) is/are open: {bool}")]
        public void SessionsAreOpen(bool expectedOpen)
            => _sessionSteps.SessionsAreOpen(expectedOpen);

        [Then(@"sessions in parallel are open: {bool}")]
        public void SessionsInParallelAreOpen(bool expectedNull)
            => _sessionSteps.SessionsInParallelAreOpen(expectedNull);

        [Then(@"session(s) has/have database: {word}")]
        public void SessionsHaveDatabase(string name)
            => _sessionSteps.SessionsHaveDatabase(name);

        [Then(@"session(s) has/have database(s):")]
        public void SessionsHaveDatabases(DataTable names)
            => _sessionSteps.SessionsHaveDatabases(names);

        [Then(@"sessions in parallel have databases:")]
        public void SessionsInParallelHaveDatabases(DataTable names)
            => _sessionSteps.SessionsInParallelHaveDatabases(names);

        [Given(@"set session option {word} to: {word}")]
        public void SetSessionOptionTo(string option, string value)
            => _sessionSteps.SetSessionOptionTo(option, value);

        private readonly SessionSteps _sessionSteps;
        private readonly DatabaseSteps _databaseSteps;
        private readonly ConnectionSteps _connectionSteps;
    }
}
