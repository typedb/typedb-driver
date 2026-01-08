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
using DocString = Gherkin.Ast.DocString;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Xunit;
using Xunit.Gherkin.Quick;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using TypeDB.Driver.Test.Behaviour;

namespace TypeDB.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        [Given(@"connection create database: (.+)")]
        [When(@"connection create database: (.+)")]
        public void ConnectionCreateDatabase(string name)
        {
            Driver!.Databases.Create(name);
        }

        [Given(@"connection create databases:")]
        [When(@"connection create databases:")]
        public void ConnectionCreateDatabases(DataTable names)
        {
            foreach (var row in names.Rows)
            {
                foreach (var name in row.Cells)
                {
                    ConnectionCreateDatabase(name.Value);
                }
            }
        }

        [Given(@"connection create databases in parallel:")]
        [When(@"connection create databases in parallel:")]
        public void ConnectionCreateDatabasesInParallel(DataTable names)
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

            Task[] taskArray = new Task[collectedNames.Count];
            for (int i = 0; i < taskArray.Length; i++)
            {
                var name = collectedNames[i];
                taskArray[i] = Task.Factory.StartNew(() =>
                    {
                        ConnectionCreateDatabase(name);
                    });
            }

            Task.WaitAll(taskArray);
        }

        [When(@"connection delete database: (\S+)")]
        public void ConnectionDeleteDatabase(string name)
        {
            Driver!.Databases.Get(name).Delete();
        }

        [When(@"connection delete databases:")]
        public void ConnectionDeleteDatabases(DataTable names)
        {
            foreach (var row in names.Rows)
            {
                foreach (var name in row.Cells)
                {
                    ConnectionDeleteDatabase(name.Value);
                }
            }
        }

        [When(@"connection delete database; throws exception: (\S+)")]
        public void ConnectionDeleteDatabaseThrowsException(string databaseName)
        {
            Assert.Throws<TypeDBDriverException>(
                () => Driver!.Databases.Get(databaseName).Delete());
        }

        [When(@"connection delete databases in parallel:")]
        public void ConnectionDeleteDatabasesInParallel(DataTable names)
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

            Task[] taskArray = new Task[collectedNames.Count];
            for (int i = 0; i < collectedNames.Count; ++i)
            {
                var name = collectedNames[i];
                taskArray[i] = Task.Factory.StartNew(() =>
                    {
                        ConnectionDeleteDatabase(name);
                    });
            }

            Task.WaitAll(taskArray);
        }

        [Given(@"connection has database: (.+)")]
        [Then(@"connection has database: (.+)")]
        public void ConnectionHasDatabase(string name)
        {
            Assert.True(Driver!.Databases.Contains(name));
        }

        [Given(@"connection has databases:")]
        [Then(@"connection has databases:")]
        public void ConnectionHasDatabases(DataTable names)
        {
            int expectedDatabasesSize = 0;

            foreach (var row in names.Rows)
            {
                foreach (var name in row.Cells)
                {
                    ConnectionHasDatabase(name.Value);
                    expectedDatabasesSize++;
                }
            }

            Assert.True(expectedDatabasesSize >= Driver!.Databases.GetAll().Count);
        }

        [Then(@"connection does not have database: (.+)")]
        public void ConnectionDoesNotHaveDatabase(string name)
        {
            Assert.False(Driver!.Databases.Contains(name));
        }

        [Then(@"connection does not have databases:")]
        public void ConnectionDoesNotHaveDatabases(DataTable names)
        {
            foreach (var row in names.Rows)
            {
                foreach (var name in row.Cells)
                {
                    ConnectionDoesNotHaveDatabase(name.Value);
                }
            }
        }

        [Given(@"connection does not have any database")]
        [Then(@"connection does not have any database")]
        public void ConnectionDoesNotHaveAnyDatabase()
        {
            Assert.NotNull(Driver);
            Assert.True(Driver.IsOpen());
            Assert.Equal(0, Driver!.Databases.GetAll().Count);
        }

        [Then(@"connection create database with empty name; fails")]
        public void ConnectionCreateDatabaseWithEmptyNameFails()
        {
            Assert.Throws<TypeDBDriverException>(() => Driver!.Databases.Create(""));
        }

        [Then(@"connection create database: (.*); fails")]
        public void ConnectionCreateDatabaseFails(string name)
        {
            Assert.Throws<TypeDBDriverException>(() => Driver!.Databases.Create(name));
        }

        [When(@"connection delete database: (.*); fails")]
        [Then(@"connection delete database: (.*); fails")]
        public void ConnectionDeleteDatabaseFails(string name)
        {
            Assert.Throws<TypeDBDriverException>(() => Driver!.Databases.Get(name).Delete());
        }

        [Given(@"connection delete database: (\S+)")]
        [Then(@"connection delete database: (\S+)")]
        public void ConnectionDeleteDatabaseGivenThen(string name)
        {
            ConnectionDeleteDatabase(name);
        }

        [Given(@"connection reset database: (.+)")]
        [When(@"connection reset database: (.+)")]
        public void ConnectionResetDatabase(string name)
        {
            // First close any open transactions for this database
            foreach (var tx in Transactions.ToList())
            {
                if (tx.IsOpen())
                {
                    tx.Close();
                }
            }
            Transactions.Clear();

            // Delete the database if it exists
            if (Driver!.Databases.Contains(name))
            {
                Driver.Databases.Get(name).Delete();
            }

            // Recreate the database
            Driver.Databases.Create(name);
        }

        // Query-related steps moved to QuerySteps.cs
    }
}
