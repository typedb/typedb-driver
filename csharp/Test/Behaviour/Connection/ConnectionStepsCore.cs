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
using Xunit.Gherkin.Quick;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Test.Behaviour;

namespace TypeDB.Driver.Test.Behaviour
{
    public partial class BehaviourSteps : ConnectionStepsBase
    {
        // Default test credentials
        private const string DefaultUsername = "admin";
        private const string DefaultPassword = "password";

        public BehaviourSteps()
            : base()
        {
            // Reset query-level state between scenarios.
            // These static fields persist across scenarios and must be cleared.
            _queryOptions = null;
            _queryAnswer = null;
            _collectedRows = null;
            _collectedDocuments = null;
            _concurrentAnswers = null;
            _concurrentRowStreams = null;
        }

        public override IDriver CreateTypeDBDriver(string address)
        {
            return TypeDB.Driver(
                address,
                new Credentials(DefaultUsername, DefaultPassword),
                new DriverOptions(false, null));
        }

        [Given(@"typedb starts")]
        [When(@"typedb starts")]
        public override void TypeDBStarts()
        {
        }

        [Given(@"connection opens with default authentication")]
        [When(@"connection opens with default authentication")]
        public override void ConnectionOpensWithDefaultAuthentication()
        {
            Driver = CreateTypeDBDriver(TypeDB.DefaultAddress);
        }

        [Given(@"connection opens with authentication: {}, {}")]
        [When(@"connection opens with authentication: {}, {}")]
        public void ConnectionOpensWithAuthentication(string username, string password)
        {
            Driver = TypeDB.Driver(
                TypeDB.DefaultAddress,
                new Credentials(username, password),
                new DriverOptions(false, null));
        }

        [Given(@"typedb has configuration")]
        public void TypeDBHasConfiguration(DataTable data)
        {
            throw new NotImplementedException("Core tests are not expected to use this method");
        }
    }
}
