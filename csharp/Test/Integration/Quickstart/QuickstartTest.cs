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

using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Integration
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
            var allDbs = dbManager.GetAll();
            Assert.AreEqual(expectedDbNames.Count, allDbs.Count);

            foreach (var db in allDbs)
            {
                Assert.True(expectedDbNames.Contains(db.Name));
            }
        }
    }

    [TestFixture]
    public class ConnectionTestFixture
    {
        private void ProcessPersonInsertResult(
            IConceptMap[] results,
            string variableName,
            string expectedVariableTypeLabel,
            string expectedAttributeValue)
        {
            Assert.AreEqual(1, results.Length);

            var result = results[0];
            Assert.AreEqual(2, result.Variables.Count());
            Assert.AreEqual(2, result.Concepts.Count());

            var entity = result.Get(variableName);
            Assert.IsNotNull(entity);
            Assert.IsTrue(entity.IsEntity());

            var entityType = entity.AsEntity().Type;
            Assert.IsNotNull(entityType);
            Assert.IsTrue(entityType.IsType() && entityType.IsEntityType());
            Assert.AreEqual(expectedVariableTypeLabel, entityType.Label.ToString());

            var attribute = result.Get("_0");
            Assert.IsNotNull(attribute);
            Assert.IsTrue(attribute.IsAttribute());

            var attributeValue = attribute.AsAttribute().Value;
            Assert.IsNotNull(attributeValue);
            Assert.IsTrue(attributeValue.IsString());
            Assert.AreEqual(expectedAttributeValue, attributeValue.AsString());
        }

        private void ProcessPersonMatchResult(
            IConceptMap[] results,
            string variableName,
            string expectedVariableTypeLabel,
            string expectedAttributeValue)
        {
            Assert.AreEqual(1, results.Length); // Only one insert has been committed

            var result = results[0];

            var attribute = result.Get(variableName);
            Assert.IsNotNull(attribute);
            Assert.IsTrue(attribute.IsAttribute());

            var attributeValue = attribute.AsAttribute().Value;
            Assert.IsNotNull(attributeValue);
            Assert.IsTrue(attributeValue.IsString());
            Assert.AreEqual(expectedAttributeValue, attributeValue.AsString());

            var attributeType = attribute.AsAttribute().Type;
            Assert.IsNotNull(attributeType);
            Assert.IsTrue(attributeType.IsType() && attributeType.IsAttributeType());
            Assert.AreEqual(expectedVariableTypeLabel, attributeType.Label.ToString());
        }

        [Test]
        public void Usings()
        {
            string dbName = "access-management-db";
            string serverAddr = "127.0.0.1:1729";

            try
            {
                using (ITypeDBDriver driver = TypeDB.CoreDriver(serverAddr))
                {
                    driver.Databases.Create(dbName);
                    IDatabase database = driver.Databases.Get(dbName);

                    // Example of one transaction for one session
                    using (ITypeDBSession session = driver.Session(dbName, SessionType.SCHEMA))
                    {
                        // Example of multiple queries for one transaction
                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.WRITE))
                        {
                            transaction.Query.Define("define person sub entity;").Resolve();

                            string longQuery = "define name sub attribute, value string; person owns name;";
                            transaction.Query.Define(longQuery).Resolve();

                            transaction.Commit();
                        }
                    }

                    // Example of multiple transactions for one session
                    using (ITypeDBSession session = driver.Session(dbName, SessionType.DATA))
                    {
                        // Examples of one query for one transaction
                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.WRITE))
                        {
                            string query = "insert $p isa person, has name 'Alice';";
                            IConceptMap[] insertResults = transaction.Query.Insert(query).ToArray();
                            ProcessPersonInsertResult(insertResults, "p", "person", "Alice");

                            transaction.Commit();
                        }

                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.WRITE))
                        {
                            IConceptMap[] insertResults =
                                transaction.Query.Insert("insert $p isa person, has name 'Bob';").ToArray();
                            ProcessPersonInsertResult(insertResults, "p", "person", "Bob");

                            // Not committed
                        }

                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.READ))
                        {
                            IConceptMap[] matchResults =
                                transaction.Query.Get("match $p isa person, has name $n; get $n;").ToArray();

                            // Matches only Alice as Bob has not been committed
                            ProcessPersonMatchResult(matchResults, "n", "name", "Alice");
                        }
                    }

                    database.Delete();
                }
            }
            catch (TypeDBDriverException e)
            {
                Console.WriteLine($"Caught TypeDB Driver Exception: {e}");
                // ...
            }
        }
        
        [Test]
        public void Manual()
        {
            string dbName = "access-management-db";
            string serverAddr = "127.0.0.1:1729";

            try
            {
                ITypeDBDriver driver = TypeDB.CoreDriver(serverAddr);
                driver.Databases.Create(dbName);

                IDatabase database = driver.Databases.Get(dbName);

                ITypeDBSession schemaSession = driver.Session(dbName, SessionType.SCHEMA);
                ITypeDBTransaction schemaWriteTransaction = schemaSession.Transaction(TransactionType.WRITE);

                schemaWriteTransaction.Query.Define("define person sub entity;").Resolve();

                string longQuery = "define name sub attribute, value string; person owns name;";
                schemaWriteTransaction.Query.Define(longQuery).Resolve();

                schemaWriteTransaction.Commit(); // No need to close manually if committed
                schemaSession.Close();

                ITypeDBSession dataSession = driver.Session(dbName, SessionType.DATA);
                ITypeDBTransaction dataWriteTransaction = dataSession.Transaction(TransactionType.WRITE);

                string query = "insert $p isa person, has name 'Alice';";
                IConceptMap[] insertResults = dataWriteTransaction.Query.Insert(query).ToArray();
                ProcessPersonInsertResult(insertResults, "p", "person", "Alice");

                dataWriteTransaction.Commit();
                dataSession.Close();

                dataSession = driver.Session(dbName, SessionType.DATA);
                dataWriteTransaction = dataSession.Transaction(TransactionType.WRITE);

                insertResults = dataWriteTransaction.Query.Insert("insert $p isa person, has name 'Bob';").ToArray();
                ProcessPersonInsertResult(insertResults, "p", "person", "Bob");

    //            dataWriteTransaction.Commit(); // Not committed

                ITypeDBTransaction readTransaction = dataSession.Transaction(TransactionType.READ);
                IConceptMap[] matchResults =
                    readTransaction.Query.Get("match $p isa person, has name $n; get $n;").ToArray();

                ProcessPersonMatchResult(matchResults, "n", "name", "Alice");

                readTransaction.Close();
                dataSession.Close();

                database.Delete();
                driver.Close();
            }
            catch (TypeDBDriverException e)
            {
                Console.WriteLine($"Caught TypeDB Driver Exception: {e}");
                // ...
            }
        }

        [Test]
        public void DocExample()
        {
            string dbName = "access-management-db";
            string serverAddr = "127.0.0.1:1729";

            try
            {
                using (ITypeDBDriver driver = TypeDB.CoreDriver(serverAddr))
                {
                    driver.Databases.Create(dbName);
                    IDatabase database = driver.Databases.Get(dbName);

                    // Example of one transaction for one session
                    using (ITypeDBSession session = driver.Session(dbName, SessionType.SCHEMA))
                    {
                        // Example of multiple queries for one transaction
                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.WRITE))
                        {
                            transaction.Query.Define("define person sub entity;").Resolve();

                            string longQuery = "define name sub attribute, value string; person owns name;";
                            transaction.Query.Define(longQuery).Resolve();

                            transaction.Commit();
                        }
                    }

                    // Example of multiple transactions for one session
                    using (ITypeDBSession session = driver.Session(dbName, SessionType.DATA))
                    {
                        // Examples of one query for one transaction
                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.WRITE))
                        {
                            string query = "insert $p isa person, has name 'Alice';";
                            IEnumerable<IConceptMap> insertResults = transaction.Query.Insert(query);

                            Console.WriteLine($"Inserted with {insertResults.Count()} result(s)");

                            transaction.Commit();
                        }

                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.WRITE))
                        {
                            IEnumerable<IConceptMap> insertResults =
                                transaction.Query.Insert("insert $p isa person, has name 'Bob';");

                            foreach (IConceptMap insertResult in insertResults)
                            {
                                Console.WriteLine($"Inserted: {insertResult}");
                            }

                            // transaction.Commit(); // Not committed
                        }

                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.READ))
                        {
                            IConceptMap[] matchResults =
                                transaction.Query.Get("match $p isa person, has name $n; get $n;").ToArray();

                            // Matches only Alice as Bob has not been committed
                            Console.WriteLine($"Found the first name: {matchResults[0]}");
                            if (matchResults.Length > 1) // Will work only if the previous transaction is committed.
                            {
                                Console.WriteLine($"Found the second name: {matchResults[1]}");
                            }
                        }
                    }

                    database.Delete();
                }
            }
            catch (TypeDBDriverException e)
            {
                Console.WriteLine($"Caught TypeDB Driver Exception: {e}");
                // ...
            }
        }
    }
}
