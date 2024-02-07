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
using Xunit;
using Xunit.Gherkin.Quick;

using com.vaticle.typedb.driver.Test.Behaviour.Connection;
using com.vaticle.typedb.driver.Test.Behaviour.Connection.Database;
using com.vaticle.typedb.driver.Test.Behaviour.Connection.Session;
using com.vaticle.typedb.driver.Test.Behaviour.Connection.Transaction;

namespace com.vaticle.typedb.driver.Test.Behaviour.Connection.Transaction
{
    [FeatureFile("external/vaticle_typedb_behaviour/connection/transaction.feature")]
    public class TransactionTest
        : Feature
        , IDisposable
        , IClassFixture<TransactionSteps>
        , IClassFixture<SessionSteps>
        , IClassFixture<DatabaseSteps>
        , IClassFixture<ConnectionSteps>
    {
        public TransactionTest()
            : base()
        {
            _transactionSteps = new TransactionSteps();
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

        [Given(@"connection open schema session for database: {word}")]
        [When(@"connection open schema session for database: {word}")]
        public void ConnectionOpenSchemaSessionForDatabase(string name)
            => _sessionSteps.ConnectionOpenSchemaSessionForDatabase(name);

        [When(@"connection open schema session for database: {word}")]
        public void ConnectionOpenSchemaSessionForDatabases(DataTable names)
            => _sessionSteps.ConnectionOpenSchemaSessionForDatabases(names);

        [Given(@"connection open data session for database: {word}")]
        [When(@"connection open session for database: {word}")]
        [When(@"connection open data session for database: {word}")]
        public void ConnectionOpenDataSessionForDatabase(string name)
            => _sessionSteps.ConnectionOpenDataSessionForDatabase(name);

        [When(@"connection open sessions for databases:")]
        [When(@"connection open data sessions for databases:")]
        public void ConnectionOpenDataSessionForDatabases(DataTable names)
            => _sessionSteps.ConnectionOpenDataSessionForDatabases(names);

        [When(@"connection open sessions in parallel for databases:")]
        public void ConnectionOpenSessionsInParallelForDatabases(DataTable names)
            => _sessionSteps.ConnectionOpenSessionsInParallelForDatabases(names);

        [When(@"connection close all sessions")]
        public void ConnectionCloseAllSessions()
            => _sessionSteps.ConnectionCloseAllSessions();

        [Then(@"session is null: {}")]
        [Then(@"sessions are null: {}")]
        public void SessionsAreNull(bool expectedNull)
            => _sessionSteps.SessionsAreNull(expectedNull);

        [Then(@"sessions in parallel are null: {}")]
        public void SessionsInParallelAreNull(bool expectedNull)
            => _sessionSteps.SessionsInParallelAreNull(expectedNull);

        [Then(@"session is open: {}")]
        [Then(@"sessions are open: {}")]
        public void SessionsAreOpen(bool expectedOpen)
            => _sessionSteps.SessionsAreOpen(expectedOpen);

        [Then(@"sessions in parallel are open: {}")]
        public void SessionsInParallelAreOpen(bool expectedNull)
            => _sessionSteps.SessionsInParallelAreOpen(expectedNull);

        [Then(@"session has database: {word}")]
        [Then(@"sessions have database: {word}")]
        public void SessionsHaveDatabase(string name)
            => _sessionSteps.SessionsHaveDatabase(name);

        [Then(@"session has database:")]
        [Then(@"sessions have databases:")]
        public void SessionsHaveDatabases(DataTable names)
            => _sessionSteps.SessionsHaveDatabases(names);

        [Then(@"sessions in parallel have databases:")]
        public void SessionsInParallelHaveDatabases(DataTable names)
            => _sessionSteps.SessionsInParallelHaveDatabases(names);

        [Given(@"set session option {word} to: {word}")]
        public void SetSessionOptionTo(string option, string value)
            => _sessionSteps.SetSessionOptionTo(option, value);

        [When(@"(for each )session(,) open(s) transaction(s) of type: {word}")]
        public void ForEachSessionOpenTransactionsOfType(string type)
            => _transactionSteps.ForEachSessionOpenTransactionsOfType(type);

        [When(@"(for each )session(,) open transaction(s) of type:")]
        public void ForEachSessionOpenTransactionsOfType(DataTable types)
            => _transactionSteps.ForEachSessionOpenTransactionsOfType(types);

        [When(@"(for each )session(,) open transaction(s) of type; throws exception: {word}")]
        public void ForEachSessionOpenTransactionsOfTypeThrowsException(string type)
            => _transactionSteps.ForEachSessionOpenTransactionsOfTypeThrowsException(type);

        [Then(@"(for each )session(,) open transaction(s) of type; throws exception")]
        public void ForEachSessionOpenTransactionsOfTypeThrowsException(DataTable types)
            => _transactionSteps.ForEachSessionOpenTransactionsOfTypeThrowsException(types);

        [Then(@"(for each )session(,) transaction(s) is/are null: {}")]
        public void ForEachSessionTransactionsAreNull(bool expectedNull)
            => _transactionSteps.ForEachSessionTransactionsAreNull(expectedNull);

        [Then(@"(for each )session(,) transaction(s) is/are open: {}")]
        public void ForEachSessionTransactionsAreOpen(bool expectedOpen)
            => _transactionSteps.ForEachSessionTransactionsAreOpen(expectedOpen);

        [Then(@"transaction commits")]
        public void TransactionCommits()
            => _transactionSteps.TransactionCommits();

        [Then(@"transaction commits; throws exception")]
        public void TransactionCommitsThrowsException()
            => _transactionSteps.TransactionCommitsThrowsException();

        [Then(@"transaction commits; throws exception containing {string}")]
        public void TransactionCommitsThrowsException(string expectedMessage)
            => _transactionSteps.TransactionCommitsThrowsException(expectedMessage);

        [Then(@"(for each )session(,) transaction(s) commit(s)")]
        public void ForEachSessionTransactionsCommit()
            => _transactionSteps.ForEachSessionTransactionsCommit();

        [Then(@"(for each )session(,) transaction(s) commit(s); throws exception")]
        public void ForEachSessionTransactionsCommitThrowsException()
            => _transactionSteps.ForEachSessionTransactionsCommitThrowsException();

        [Then(@"(for each )session(,) transaction close(s)")]
        public void ForEachSessionTransactionCloses()
            => _transactionSteps.ForEachSessionTransactionCloses();

        [Then(@"(for each )session(,) transaction(s) has/have type: {word}")]
        public void ForEachSessionTransactionsHaveType(string type)
            => _transactionSteps.ForEachSessionTransactionsHaveType(type);

        [Then(@"(for each )session(,) transaction(s) has/have type:")]
        public void ForEachSessionTransactionsHaveType(DataTable types)
            => _transactionSteps.ForEachSessionTransactionsHaveType(types);

        [When(@"for each session, open transaction(s) in parallel of type:")]
        public void ForEachSessionOpenTransactionsInParallelOfType(DataTable types)
            => _transactionSteps.ForEachSessionOpenTransactionsInParallelOfType(types);

        [Then(@"for each session, transactions in parallel are null: {}")]
        public void ForEachSessionTransactionsInParallelAreNull(bool expectedNull)
            => _transactionSteps.ForEachSessionTransactionsInParallelAreNull(expectedNull);

        [Then(@"for each session, transactions in parallel are open: {}")]
        public void ForEachSessionTransactionsInParallelAreOpen(bool expectedOpen)
            => _transactionSteps.ForEachSessionTransactionsInParallelAreOpen(expectedOpen);

        [Then(@"for each session, transactions in parallel have type:")]
        public void ForEachSessionTransactionsInParallelHaveType(DataTable types)
            => _transactionSteps.ForEachSessionTransactionsInParallelHaveType(types);

        [Then(@"for each session in parallel, transactions in parallel are null: {}")]
        public void ForEachSessionInParallelTransactionsInParallelAreNull(bool expectedNull)
            => _transactionSteps.ForEachSessionInParallelTransactionsInParallelAreNull(expectedNull);

        [Then(@"for each session in parallel, transactions in parallel are open: {}")]
        public void ForEachSessionInParallelTransactionsInParallelAreOpen(bool expectedOpen)
            => _transactionSteps.ForEachSessionInParallelTransactionsInParallelAreOpen(expectedOpen);

        [Given(@"set transaction option {word} to: {word}")]
        public void SetTransactionOptionTo(string option, string value)
            => _transactionSteps.SetTransactionOptionTo(option, value);

        [Then(@"for each transaction, define query; throws exception containing {string}")]
        public void ForEachTransactionExecuteDefineThrowsException(string expectedMessage, string defineQueryStatements)
            => _transactionSteps.ForEachTransactionExecuteDefineThrowsException(expectedMessage, defineQueryStatements);

        private readonly TransactionSteps _transactionSteps;
        private readonly SessionSteps _sessionSteps;
        private readonly DatabaseSteps _databaseSteps;
        private readonly ConnectionSteps _connectionSteps;
    }
}
