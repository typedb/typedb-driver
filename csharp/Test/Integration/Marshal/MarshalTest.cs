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

using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.Linq;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using static TypeDB.Driver.Api.IThingType;
using static TypeDB.Driver.Api.IThingType.Annotation;

namespace TypeDB.Driver.Test.Integration
{
    public static class Utils
    {
        public static ITypeDBDriver OpenConnection()
        {
            ITypeDBDriver driver = Drivers.CoreDriver(Drivers.DEFAULT_ADDRESS);
            Assert.IsNotNull(driver);
            Assert.True(driver.IsOpen());

            return driver;
        }

        public static void CloseConnection(ITypeDBDriver driver)
        {
            driver.Close();
            Assert.False(driver.IsOpen());
        }

        public static IDatabase CreateAndGetDatabase(
            IDatabaseManager dbManager, string expectedDbName, bool checkAbsence = true)
        {
            if (checkAbsence)
            {
                Assert.False(dbManager.Contains(expectedDbName));
            }

            dbManager.Create(expectedDbName);
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
    }

    [TestFixture]
    public class MarshalTestFixture
    {
        [Test]
        public void SendArraysToNativeSide()
        {
            string dbName = "mydb";
            ITypeDBDriver driver = Utils.OpenConnection();
            IDatabaseManager dbManager = driver.Databases;
            IDatabase db1 = Utils.CreateAndGetDatabase(dbManager, dbName);

            using (ITypeDBSession schemaSession = driver.Session(dbName, SessionType.Schema))
            {
                using (ITypeDBTransaction writeTransaction = schemaSession.Transaction(TransactionType.Write))
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

            using (ITypeDBSession dataSession = driver.Session(dbName, SessionType.Data))
            {
                using (ITypeDBTransaction writeTransaction = dataSession.Transaction(TransactionType.Write))
                {
                    string insertQuery = @"
                        insert
                         $x isa person, has ref 0, has email ""georgii@typedb.com"";
                         $y isa person, has ref 1;
                        ";

                    IConceptMap conceptMap = writeTransaction.Query.Insert(insertQuery).ToArray()[0];

                    var x = conceptMap.Get("x").AsThing();
                    Assert.NotNull(x);

                    var emptyAnnotations = new Annotation[0];
                    IAttribute[] xAttributesWithEmptyAnnotations =
                        x.GetHas(writeTransaction, emptyAnnotations).ToArray();

                    // The content of such results is checked in behaviour tests.
                    Assert.AreEqual(xAttributesWithEmptyAnnotations.Length, 2);

                    var annotations = new []{NewKey()};
                    IAttribute[] xAttributesWithAnnotations = x.GetHas(writeTransaction, annotations).ToArray();

                    Assert.AreEqual(xAttributesWithAnnotations.Length, 1);
                    Assert.That(xAttributesWithAnnotations[0].IsAttribute());
                    Assert.That(xAttributesWithAnnotations[0].Value.IsLong());

                    var refType = writeTransaction.Concepts.GetAttributeType("ref").Resolve();
                    var emailType = writeTransaction.Concepts.GetAttributeType("email").Resolve();

                    Assert.NotNull(refType);
                    Assert.NotNull(emailType);

                    IAttribute[] xAttributesWithAttributeTypes = x.GetHas(
                        writeTransaction, refType!, emailType!).ToArray();

                    Assert.AreEqual(xAttributesWithAttributeTypes.Length, 2);

                    IAttribute[] xAttributesWithAttributeTypes2 = x.GetHas(
                        writeTransaction, refType!).ToArray();

                    Assert.AreEqual(xAttributesWithAttributeTypes2.Length, 1);
                    Assert.That(xAttributesWithAttributeTypes2[0].IsAttribute());
                    Assert.That(xAttributesWithAttributeTypes2[0].Value.IsLong());

                    IAttribute[] xAttributesWithAttributeTypes3 = x.GetHas(
                        writeTransaction, emailType!).ToArray();

                    Assert.AreEqual(xAttributesWithAttributeTypes3.Length, 1);
                    Assert.That(xAttributesWithAttributeTypes3[0].IsAttribute());
                    Assert.That(xAttributesWithAttributeTypes3[0].Value.IsString());
                }
            }

            Utils.DeleteDatabase(dbManager, db1);
            Utils.CloseConnection(driver);
        }

        [Test]
        public void SendOptionalEnumToNativeSide()
        {
            string dbName = "mydb";
            ITypeDBDriver driver = Utils.OpenConnection();
            IDatabaseManager dbManager = driver.Databases;
            IDatabase db1 = Utils.CreateAndGetDatabase(dbManager, dbName);

            using (ITypeDBSession schemaSession = driver.Session(dbName, SessionType.Schema))
            {
                using (ITypeDBTransaction writeTransaction = schemaSession.Transaction(TransactionType.Write))
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

            using (ITypeDBSession dataSession = driver.Session(dbName, SessionType.Data))
            {
                using (ITypeDBTransaction writeTransaction = dataSession.Transaction(TransactionType.Write))
                {
                    string insertQuery = @"
                        insert
                         $x isa person, has ref 0;
                         $y isa person, has ref 1;
                        ";

                    IConceptMap conceptMap = writeTransaction.Query.Insert(insertQuery).ToArray()[0];

                    var x = conceptMap.Get("x").AsThing();
                    Assert.NotNull(x);

                    var emptyAnnotations = new Annotation[0];
                    IAttributeType[] xAttributeTypesLong =
                        x.Type.GetOwns(writeTransaction, IValue.ValueType.Long, emptyAnnotations).ToArray();

                    Assert.AreEqual(xAttributeTypesLong.Length, 2);

                    IAttributeType[] xAttributeTypesString =
                        x.Type.GetOwns(writeTransaction, IValue.ValueType.String, emptyAnnotations).ToArray();

                    Assert.AreEqual(xAttributeTypesString.Length, 2);

                    IAttributeType[] xAttributeTypesNull =
                        x.Type.GetOwns(writeTransaction, null, emptyAnnotations).ToArray();

                    Assert.AreEqual(xAttributeTypesNull.Length, 4);

                    IAttributeType[] xAttributeTypesDatetime =
                        x.Type.GetOwns(writeTransaction, IValue.ValueType.DateTime, emptyAnnotations).ToArray();

                    Assert.AreEqual(xAttributeTypesDatetime.Length, 0);
                }
            }

            Utils.DeleteDatabase(dbManager, db1);
            Utils.CloseConnection(driver);
        }
    }
}
