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
using com.vaticle.typedb.driver.Test.Behaviour.Connection;

namespace com.vaticle.typedb.driver.Test.Behaviour.Connection.Database
{
    [FeatureFile("external/vaticle_typedb_behaviour/connection/database.feature")]
    public class DatabaseSteps : ConnectionSteps
    {
        [Given(@"connection create database: {word}")]
        [When(@"connection create database: {word}")]
        public void ConnectionCreateDatabase(string name)
        {
            Driver.Databases().Create(name);
        }

        [Given(@"connection create databases:")]
        [When(@"connection create databases:")]
        public void ConnectionCreateDatabases(DataTable names)
        {
            foreach (var row in names.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    ConnectionCreateDatabase(cell.Value);
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
                foreach (var cell in row.Cells)
                {
                    collectedNames.Add(cell.Value);
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

        [When(@"connection delete database: {word}")]
        public void ConnectionDeleteDatabase(string name)
        {
            Driver.Databases().Get(name).Delete();
        }

        [When(@"connection delete databases:")]
        public void ConnectionDeleteDatabases(DataTable names)
        {
            foreach (var row in names.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    ConnectionDeleteDatabase(cell.Value);
                }
            }
        }

        [When(@"connection delete database; throws exception: {word}")]
        public void ConnectionDeleteDatabaseThrowsException(string name)
        {
            Console.WriteLine("ConnectionDeleteDatabaseThrowsException: " + name);
            // TODO: Not implemented!
            Assert.True(false);
//            connection_delete_databases_throws_exception(list(name));
        }

        [When(@"connection delete databases in parallel:")]
        public void ConnectionDeleteDatabasesInParallel(DataTable names)
        {
            var collectedNames = new List<string>();
            foreach (var row in names.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    collectedNames.Add(cell.Value);
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

        [Then(@"connection has database: {word}")]
        public void ConnectionHasDatabase(string name)
        {
            Assert.True(Driver.Databases().Contains(name));
        }

        [Then(@"connection has databases:")]
        public void ConnectionHasDatabases(DataTable names)
        {
            int expectedDatabasesSize = 0;

            foreach (var row in names.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    ConnectionHasDatabase(cell.Value);
                    expectedDatabasesSize++;
                }
            }

            // TODO: Could there be just == ? The description is more like >=!
            Assert.True(expectedDatabasesSize >= Driver.Databases().GetAll().Count);
        }

        [Then(@"connection does not have database: {word}")]
        public void ConnectionDoesNotHaveDatabase(string name)
        {
            Assert.False(Driver.Databases().Contains(name));
        }

        [Then(@"connection does not have databases:")]
        public void ConnectionDoesNotHaveDatabases(DataTable names)
        {
            foreach (var row in names.Rows)
            {
                foreach (var cell in row.Cells)
                {
                    ConnectionDoesNotHaveDatabase(cell.Value);
                }
            }
        }
    }
}
