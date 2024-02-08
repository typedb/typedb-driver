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

using NUnit.Framework;
using System;
using System.Collections.Generic;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Api.Database;
using Vaticle.Typedb.Driver.Common;

namespace Vaticle.Typedb.Driver.Test.Integration
{
    public static class Utils
    {
        public static ITypeDBDriver OpenConnection()
        {
            ITypeDBDriver driver = TypeDB.CoreDriver(TypeDB.DEFAULT_ADDRESS);
            Assert.IsNotNull(driver);
            Assert.True(driver.IsOpen());

            return driver;
        }

        public static void CloseConnection(ITypeDBDriver driver)
        {
            driver.Close();
            Assert.False(driver.IsOpen());
        }

        public static void CreateDatabaseNoChecks(IDatabaseManager dbManager, string expectedDbName)
        {
            dbManager.Create(expectedDbName);
        }

        public static IDatabase CreateAndGetDatabase(
            IDatabaseManager dbManager, string expectedDbName, bool checkAbsence = true)
        {
            if (checkAbsence)
            {
                Assert.False(dbManager.Contains(expectedDbName));
            }

            CreateDatabaseNoChecks(dbManager, expectedDbName);
            Assert.True(dbManager.Contains(expectedDbName));

            IDatabase db = dbManager.Get(expectedDbName);
            Assert.IsNotNull(db);

            string realDbName = db.Name();
            Assert.AreEqual(expectedDbName, realDbName);

            return db;
        }

        public static void DeleteDatabase(IDatabaseManager dbManager, IDatabase db)
        {
            string dbName = db.Name();
            db.Delete();
            Assert.False(dbManager.Contains(dbName));
        }

        public static void CheckAllDatabases(IDatabaseManager dbManager, ICollection<string> expectedDbNames)
        {
            var allDbs = dbManager.GetAll();
            Assert.AreEqual(expectedDbNames.Count, allDbs.Count);

            foreach (var db in allDbs)
            {
                Assert.True(expectedDbNames.Contains(db.Name()));
            }
        }
    }

    [TestFixture]
    public class ConnectionTestFixture
    {
        [Test]
        public void OpenAndCloseConnection()
        {
            ITypeDBDriver driver = Utils.OpenConnection();
            Utils.CloseConnection(driver);
        }

        [Test]
        public void CreateAndDeleteAndRecreateDatabase()
        {
            string expectedDbName = "hello_from_csharp";

            ITypeDBDriver driver = Utils.OpenConnection();
            IDatabaseManager dbManager = driver.Databases();

            Assert.False(dbManager.Contains(""));
            Utils.CheckAllDatabases(dbManager, new HashSet<string>());

            IDatabase db1 = Utils.CreateAndGetDatabase(dbManager, expectedDbName);

            Utils.CheckAllDatabases(dbManager, new HashSet<string>(){expectedDbName});
            Assert.False(dbManager.Contains(expectedDbName + "1"));
            Assert.False(dbManager.Contains(expectedDbName.Substring(1)));
            Assert.False(dbManager.Contains(expectedDbName.Remove(expectedDbName.Length - 1)));
            Assert.False(dbManager.Contains(""));

            Utils.DeleteDatabase(dbManager, db1);

            IDatabase db2 = Utils.CreateAndGetDatabase(dbManager, expectedDbName);

            Utils.CheckAllDatabases(dbManager, new HashSet<string>(){expectedDbName});
            Utils.DeleteDatabase(dbManager, db2);
            Utils.CloseConnection(driver);
        }

        [Test]
        public void FailCreateTwoDatabasesWithSameName()
        {
            string expectedDbName = "hello_from_csharp";

            ITypeDBDriver driver = Utils.OpenConnection();
            IDatabaseManager dbManager = driver.Databases();

            IDatabase db1 = Utils.CreateAndGetDatabase(dbManager, expectedDbName);

            // TODO: This has to start failing after we implement exceptions!
            Utils.CreateDatabaseNoChecks(dbManager, expectedDbName);

            Utils.DeleteDatabase(dbManager, db1);
            Utils.CloseConnection(driver);
        }
    }
}
