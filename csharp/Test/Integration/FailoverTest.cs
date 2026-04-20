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
using System.Diagnostics;
using System.Linq;
using System.Threading;
using NUnit.Framework;
using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Integration
{
    [TestFixture]
    public class FailoverTest
    {
        private static readonly ISet<string> Addresses = new HashSet<string>
        {
            "127.0.0.1:11729", "127.0.0.1:21729", "127.0.0.1:31729"
        };
        private const string Username = "admin";
        private const string Password = "password";
        private const string DatabaseName = "test-failover";
        private const int FailoverIterations = 10;
        private const int PostKillWaitSecs = 5;
        private const int PrimaryPollRetries = 20;
        private const int PrimaryPollIntervalSecs = 2;
        private const string ClusterNodeScript = "tool/test/cluster-server.sh";

        private static void ClusterNode(string command, string nodeId)
        {
            var process = new Process();
            process.StartInfo.FileName = ClusterNodeScript;
            process.StartInfo.Arguments = $"{command} {nodeId}";
            process.StartInfo.UseShellExecute = false;
            process.StartInfo.RedirectStandardError = true;
            process.Start();
            var stderr = process.StandardError.ReadToEnd();
            process.WaitForExit();
            Assert.AreEqual(0, process.ExitCode,
                $"{ClusterNodeScript} {command} {nodeId} failed: {stderr}");
        }

        private static string NodeIdFromAddress(string address)
        {
            var port = address.Substring(address.LastIndexOf(':') + 1);
            return port.Substring(0, 1);
        }

        private static IDriver CreateDriver()
        {
            return TypeDB.Driver(
                Addresses,
                new Credentials(Username, Password),
                new DriverOptions(DriverTlsConfig.Disabled())
            );
        }

        private static IServer GetPrimaryServer(IDriver driver)
        {
            for (int attempt = 0; attempt < PrimaryPollRetries; attempt++)
            {
                var primary = driver.GetPrimaryServer();
                if (primary != null)
                {
                    return primary;
                }
                if (attempt < PrimaryPollRetries - 1)
                {
                    Console.WriteLine($"  No primary server found (attempt {attempt + 1}/{PrimaryPollRetries}). "
                        + $"Retrying in {PrimaryPollIntervalSecs}s...");
                    Thread.Sleep(PrimaryPollIntervalSecs * 1000);
                }
            }
            Assert.Fail("Retry limit exceeded while seeking a primary server.");
            return null;
        }

        [SetUp]
        public void SetUp()
        {
            try
            {
                using var driver = CreateDriver();
                if (driver.Databases.Contains(DatabaseName))
                {
                    driver.Databases.Get(DatabaseName).Delete();
                }
            }
            catch (Exception)
            {
                // Best effort cleanup
            }
        }

        [TearDown]
        public void TearDown()
        {
            try
            {
                using var driver = CreateDriver();
                if (driver.Databases.Contains(DatabaseName))
                {
                    driver.Databases.Get(DatabaseName).Delete();
                }
            }
            catch (Exception)
            {
                // Best effort cleanup
            }
        }

        [Test]
        public void PrimaryFailover()
        {
            Console.WriteLine("=== Cluster Failover Test ===");

            // Connect driver (cluster must already be running via start-cluster-servers.sh)
            Console.WriteLine("Connecting driver...");
            using var driver = CreateDriver();

            // Setup database with schema
            Console.WriteLine("Setting up database and schema...");
            driver.Databases.Create(DatabaseName);

            using (var tx = driver.Transaction(DatabaseName, TransactionType.Schema))
            {
                tx.Query("define entity person;").Resolve();
                tx.Commit();
            }

            VerifyReadQuery(driver);
            Console.WriteLine("Initial setup verified.");

            // Failover loop
            for (int iteration = 1; iteration <= FailoverIterations; iteration++)
            {
                Console.WriteLine($"\n--- Failover iteration {iteration}/{FailoverIterations} ---");

                var primary = GetPrimaryServer(driver);
                Assert.IsNotNull(primary);
                var primaryAddress = primary.Address;
                var nodeId = NodeIdFromAddress(primaryAddress);
                Console.WriteLine($"  Primary server: {primaryAddress} (node {nodeId})");

                Console.WriteLine($"  Killing node {nodeId}...");
                ClusterNode("kill", nodeId);

                Console.WriteLine($"  Waiting {PostKillWaitSecs}s for re-election...");
                Thread.Sleep(PostKillWaitSecs * 1000);

                Console.WriteLine("  Verifying read query on new primary...");
                VerifyReadQuery(driver);
                Console.WriteLine("  Read query succeeded.");

                Console.WriteLine($"  Restarting node {nodeId}...");
                ClusterNode("start", nodeId);
                ClusterNode("await", nodeId);
                Console.WriteLine($"  Node {nodeId} restarted.");
            }

            Console.WriteLine($"\n=== All {FailoverIterations} failover iterations passed! ===");
        }

        private void VerifyReadQuery(IDriver driver)
        {
            using var tx = driver.Transaction(DatabaseName, TransactionType.Read);
            var answer = tx.Query("match entity $t;").Resolve()!;
            var rows = answer.AsConceptRows();
            Assert.IsTrue(rows.Any(), "Expected at least one entity type in read query results");
        }
    }
}
