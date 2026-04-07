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
using System.Collections.Generic;
using System.Linq;
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
        private static readonly HashSet<string> DEFAULT_CLUSTER_ADDRESSES = new HashSet<string>
        {
            "127.0.0.1:11729",
            "127.0.0.1:21729",
            "127.0.0.1:31729",
        };

        private static readonly string[] DEFAULT_CLUSTER_CLUSTERING_ADDRESSES =
        {
            "127.0.0.1:11730",
            "127.0.0.1:21730",
            "127.0.0.1:31730",
        };

        private static bool _isBeforeAllRan = false;

        public BehaviourSteps()
            : base()
        {
            if (!_isBeforeAllRan)
            {
                SetupCluster();
                _isBeforeAllRan = true;
            }

            _queryOptions = null;
            _queryAnswer = null;
            _collectedRows = null;
            _collectedDocuments = null;
            _concurrentAnswers = null;
            _concurrentRowStreams = null;
        }

        protected override void InitializeDriverOptions()
        {
            string? rootCA = Environment.GetEnvironmentVariable("ROOT_CA");
            if (rootCA != null)
            {
                DriverOptions = new DriverOptions(DriverTlsConfig.EnabledWithRootCA(rootCA));
            }
            else
            {
                base.InitializeDriverOptions();
            }
        }

        private void SetupCluster()
        {
            using (var driver = CreateDefaultTypeDBDriver())
            {
                if (driver.GetServers().Count < DEFAULT_CLUSTER_CLUSTERING_ADDRESSES.Length)
                {
                    for (int i = 0; i < DEFAULT_CLUSTER_CLUSTERING_ADDRESSES.Length; i++)
                    {
                        long serverId = i + 1;
                        if (serverId != 1)
                        {
                            driver.RegisterServer(serverId, DEFAULT_CLUSTER_CLUSTERING_ADDRESSES[i]);
                        }
                    }
                }
            }
        }

        public override IDriver CreateDefaultTypeDBDriver()
        {
            return TypeDB.Driver(DEFAULT_CLUSTER_ADDRESSES, DefaultCredentials, DriverOptions);
        }

        private IDriver CreateTypeDBDriver(
            ISet<string>? addresses = null,
            string? username = null,
            string? password = null)
        {
            return TypeDB.Driver(
                addresses ?? DEFAULT_CLUSTER_ADDRESSES,
                new Credentials(
                    username ?? AdminUsername,
                    password ?? AdminPassword),
                DriverOptions);
        }

        public override IDriver CreateTypeDBDriver(string address)
        {
            return TypeDB.Driver(
                new HashSet<string> { address },
                DefaultCredentials,
                DriverOptions);
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
            Driver = CreateDefaultTypeDBDriver();
        }

        [When(@"connection opens to single server with default authentication")]
        public void ConnectionOpensToSingleServerWithDefaultAuthentication()
        {
            Driver = CreateTypeDBDriver(DEFAULT_CLUSTER_ADDRESSES.First());
        }

        [Given(@"connection opens with authentication: {}, {}")]
        [When(@"connection opens with authentication: {}, {}")]
        public void ConnectionOpensWithAuthentication(string username, string password)
        {
            if (Driver != null)
            {
                Driver.Close();
                Driver = null;
            }

            Driver = CreateTypeDBDriver(username: username, password: password);
        }

        [When(@"connection opens with authentication: {}, {}; throws exception")]
        public void ConnectionOpensWithAuthenticationThrowsException(string username, string password)
        {
            Assert.Throws<TypeDBDriverException>(
                () => ConnectionOpensWithAuthentication(username, password));
        }

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

        [Then(@"connection contains distribution")]
        public void ConnectionContainsDistribution()
        {
            if (_requiredConfiguration) return;
            Assert.False(string.IsNullOrEmpty(GetServerVersion().Distribution));
        }

        [Then(@"connection contains version")]
        public void ConnectionContainsVersion()
        {
            if (_requiredConfiguration) return;
            Assert.False(string.IsNullOrEmpty(GetServerVersion().Version));
        }

        [Then(@"connection has (\d+) servers?")]
        public void ConnectionHasServerCount(int count)
        {
            if (_requiredConfiguration) return;
            Assert.Equal(count, GetServers().Count);
        }

        [Then(@"connection primary server exists")]
        public void ConnectionPrimaryServerExists()
        {
            if (_requiredConfiguration) return;
            Assert.NotNull(GetPrimaryServer());
        }

        [Then(@"connection get server\((\S+)\) (exists|does not exist)")]
        public void ConnectionGetServerExists(string address, string existsOrDoesnt)
        {
            if (_requiredConfiguration) return;
            bool exists = GetServers().Any(s => s.Address == address);
            Assert.Equal(existsOrDoesnt == "exists", exists);
        }

        [Then(@"connection get server\((\S+)\) has term")]
        public void ConnectionGetServerHasTerm(string address)
        {
            if (_requiredConfiguration) return;
            var server = GetServers().FirstOrDefault(s => s.Address == address);
            Assert.NotNull(server);
        }

        [Then(@"connection servers have roles:")]
        public void ConnectionServersHaveRoles(DataTable data)
        {
            if (_requiredConfiguration) return;

            int expectedPrimaryCount = 0;
            int expectedSecondaryCount = 0;
            int expectedCandidateCount = 0;

            foreach (var row in data.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    switch (cell.Value.ToLower())
                    {
                        case "primary": expectedPrimaryCount++; break;
                        case "secondary": expectedSecondaryCount++; break;
                        case "candidate": expectedCandidateCount++; break;
                        default: throw new ArgumentException($"Unknown server replication role: {cell.Value}");
                    }
                }
            }

            var servers = GetServers();
            int actualPrimaryCount = servers.Count(s => s.Role?.IsPrimary() ?? false);
            int actualSecondaryCount = servers.Count(s => s.Role?.IsSecondary() ?? false);
            int actualCandidateCount = servers.Count(s => s.Role?.IsCandidate() ?? false);

            Assert.Equal(expectedPrimaryCount, actualPrimaryCount);
            Assert.Equal(expectedSecondaryCount, actualSecondaryCount);
            Assert.Equal(expectedCandidateCount, actualCandidateCount);
        }

        [Given(@"connection has (\d+) databases?")]
        [Then(@"connection has (\d+) databases?")]
        public void ConnectionHasDatabaseCount(int expectedCount)
        {
            if (_requiredConfiguration) return;
            Assert.NotNull(Driver);
            Assert.Equal(expectedCount, Driver.Databases.GetAll().Count);
        }

        [When(@"set operation server routing to: (.+)")]
        public void SetOperationServerRouting(string routing)
        {
            if (routing.Equals("auto", StringComparison.OrdinalIgnoreCase))
            {
                OperationServerRouting = new ServerRouting.Auto();
            }
            else if (routing.ToLower().StartsWith("direct(") && routing.EndsWith(")"))
            {
                string address = routing.Substring("direct(".Length, routing.Length - "direct(".Length - 1);
                OperationServerRouting = new ServerRouting.Direct(address);
            }
            else
            {
                throw new ArgumentException($"Unknown server routing: {routing}");
            }
        }

        [When(@"set driver option primary_failover_retries to: (\d+)")]
        public void SetDriverOptionPrimaryFailoverRetriesTo(int value)
        {
            DriverOptions.PrimaryFailoverRetries = value;
        }

        [When(@"connection opens with a wrong port; fails")]
        [Then(@"connection opens with a wrong port; fails")]
        public void ConnectionOpensWithWrongPortFails()
        {
            Assert.ThrowsAny<Exception>(() =>
            {
                var wrongPortDriver = TypeDB.Driver(
                    new HashSet<string> { "127.0.0.1:9999" },
                    DefaultCredentials,
                    DriverOptions);
            });
        }

        [When(@"connection opens with a wrong host; fails with a message containing: ""(.*)""")]
        [Then(@"connection opens with a wrong host; fails with a message containing: ""(.*)""")]
        public void ConnectionOpensWithWrongHostFailsWithMessage(string expectedMessage)
        {
            var exception = Assert.ThrowsAny<Exception>(() =>
            {
                var wrongHostDriver = TypeDB.Driver(
                    new HashSet<string> { "surely-not-localhost:11729" },
                    DefaultCredentials,
                    DriverOptions);
            });
            Assert.Contains(expectedMessage, exception.Message);
        }

        [Given(@"typedb has configuration")]
        public void TypeDBHasConfiguration(DataTable data)
        {
        }
    }
}
