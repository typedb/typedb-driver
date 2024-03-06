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
using System.Linq;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Common;

namespace Vaticle.Typedb.Driver.Test.Integration
{
    public static class Utils // TODO: Move to a separate library for all tests
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

            string realDbName = db.Name;
            Assert.AreEqual(expectedDbName, realDbName);

            return db;
        }

        public static void DeleteDatabase(IDatabaseManager dbManager, IDatabase db)
        {
            string dbName = db.Name;
            db.Delete();
            Assert.False(dbManager.Contains(dbName));
        }

        public static void CheckAllDatabases(IDatabaseManager dbManager, ICollection<string> expectedDbNames)
        {
            var allDbs = dbManager.All;
            Assert.AreEqual(expectedDbNames.Count, allDbs.Count);

            foreach (var db in allDbs)
            {
                Assert.True(expectedDbNames.Contains(db.Name));
            }
        }
    }

    [TestFixture]
    public class MemoryTestFixture
    {
        // TODO: Add cleanup in case of errors.

        [Test]
        public void SendArraysToNativeSide()
        {
            string dbName = "mydb";
            ITypeDBDriver driver = Utils.OpenConnection();
            IDatabaseManager dbManager = driver.Databases;
            IDatabase db1 = Utils.CreateAndGetDatabase(dbManager, dbName);

            using (ITypeDBSession schemaSession = driver.Session(dbName, SessionType.SCHEMA))
            {
                using (ITypeDBTransaction writeTransaction = schemaSession.Transaction(TransactionType.WRITE))
                {
                    string defineQuery = @"
                        define

                        person sub entity,
                         plays employment:employee,
                         plays friendship:friend,
                         owns name,
                         owns age,
                         owns ref @key,
                         owns email @unique;

                        company sub entity,
                         plays employment:employer,
                         owns name,
                         owns ref @key;

                        employment sub relation,
                         relates employee,
                         relates employer,
                         owns ref @key;

                        friendship sub relation,
                         relates friend,
                         owns ref @key;

                        name sub attribute,
                         value string;

                        age sub attribute,
                         value long;

                        ref sub attribute,
                         value long;

                        email sub attribute,
                         value string;
                        ";

                    writeTransaction.Query.Define(defineQuery).Resolve();
                    writeTransaction.Commit();
                }
            }

            using (ITypeDBSession dataSession = driver.Session(dbName, SessionType.DATA))
            {
                using (ITypeDBTransaction writeTransaction = dataSession.Transaction(TransactionType.WRITE))
                {
                    string insertQuery = @"
                        insert
                         $x isa person, has ref 0, has email ""my@dog.com"";
                         $y isa person, has ref 1;
                        ";

                    IConceptMap conceptMap = writeTransaction.Query.Insert(insertQuery).ToArray()[0];

                    var xConcept = conceptMap.Get("x");
                    System.Console.WriteLine(conceptMap);
                    System.Console.WriteLine(xConcept);

                    var annotation = new HashSet<IThingType.Annotation>(){IThingType.Annotation.NewKey()};
                    IAttribute[] annotationAttributes = xConcept.AsThing().GetHas(writeTransaction, annotation).ToArray();
                    System.Console.WriteLine("GetHas:");
                    foreach (var attr in annotationAttributes) System.Console.WriteLine(attr);

                    var refType = writeTransaction.Concepts.GetAttributeType("ref").Resolve();
                    var emailType = writeTransaction.Concepts.GetAttributeType("email").Resolve();
                    IAttribute[] annotationAttributes2 = xConcept.AsThing().GetHas(
                        writeTransaction,
//                        refType,
                        emailType).ToArray();

                    System.Console.WriteLine("GetHas:");
                    foreach (var attr in annotationAttributes2) System.Console.WriteLine(attr);
                }
            }

            Utils.DeleteDatabase(dbManager, db1);
            Utils.CloseConnection(driver);
        }

    }
}
