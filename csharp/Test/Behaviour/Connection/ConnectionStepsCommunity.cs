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
using Xunit;
using Xunit.Gherkin.Quick;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
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
            Console.WriteLine("Creating community driver with address " + address);
            return TypeDB.Driver(
                address,
                new Credentials(DefaultUsername, DefaultPassword),
                new DriverOptions(false, null));
        }

        [Given(@"typedb starts")]
        [When(@"typedb starts")]
        public override void TypeDBStarts()
        {
            Console.WriteLine("Typedb starts");
        }

        [Given(@"connection opens with default authentication")]
        [When(@"connection opens with default authentication")]
        public override void ConnectionOpensWithDefaultAuthentication()
        {
            Console.WriteLine("Creating driver with default auth");
            Driver = CreateTypeDBDriver(TypeDB.DefaultAddress);
        }

        [Given(@"connection opens with authentication: {}, {}")]
        [When(@"connection opens with authentication: {}, {}")]
        public void ConnectionOpensWithAuthentication(string username, string password)
        {
            Console.WriteLine("Creating driver with username " + username + " and password " + password);
            Driver = TypeDB.Driver(
                TypeDB.DefaultAddress,
                new Credentials(username, password),
                new DriverOptions(false, null));
        }

        [Given(@"typedb has configuration")]
        public void TypeDBHasConfiguration(DataTable data)
        {
            throw new NotImplementedException("Community tests are not expected to use this method");
        }

        // Connection-level steps (from Base)

        [Given(@"connection has been opened")]
        public override void ConnectionHasBeenOpened()
        {
            base.ConnectionHasBeenOpened();
        }

        [When(@"connection closes")]
        [Then(@"connection closes")]
        public override void ConnectionCloses()
        {
            base.ConnectionCloses();
        }

        [Given(@"connection is open: (.*)")]
        [Then(@"connection is open: (.*)")]
        public void ConnectionIsOpen(string expectedState)
        {
            if (_requiredConfiguration) return;

            bool expected = bool.Parse(expectedState);
            if (expected)
            {
                Assert.NotNull(Driver);
                Assert.True(Driver.IsOpen());
            }
            else
            {
                Assert.True(Driver == null || !Driver.IsOpen());
            }
        }

        [Given(@"connection has (\d+) databases?")]
        [Then(@"connection has (\d+) databases?")]
        public void ConnectionHasDatabaseCount(int expectedCount)
        {
            if (_requiredConfiguration) return;

            Assert.NotNull(Driver);
            Assert.Equal(expectedCount, Driver.Databases.GetAll().Count);
        }

        // Connection: wrong host/port (from DriverSteps)

        [When(@"connection opens with a wrong port; fails")]
        [Then(@"connection opens with a wrong port; fails")]
        public void ConnectionOpensWithWrongPortFails()
        {
            Assert.ThrowsAny<Exception>(() =>
            {
                var wrongPortDriver = TypeDB.Driver(
                    "localhost:9999",
                    new Credentials("admin", "password"),
                    new DriverOptions(false, null));
            });
        }

        [When(@"connection opens with a wrong host; fails with a message containing: ""(.*)""")]
        [Then(@"connection opens with a wrong host; fails with a message containing: ""(.*)""")]
        public void ConnectionOpensWithWrongHostFailsWithMessage(string expectedMessage)
        {
            var exception = Assert.ThrowsAny<Exception>(() =>
            {
                var wrongHostDriver = TypeDB.Driver(
                    "nonexistent-host.invalid:1729",
                    new Credentials("admin", "password"),
                    new DriverOptions(false, null));
            });
            Assert.Contains(expectedMessage, exception.Message);
        }
    }
}
