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
    [TestFixture]
    public class CoreExamplesTestFixture
    {
        [Test]
        public void UsingsExample()
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
                    using (ITypeDBSession session = driver.Session(dbName, SessionType.Schema))
                    {
                        // Example of multiple queries for one transaction
                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Write))
                        {
                            transaction.Query.Define("define person sub entity;").Resolve();

                            string longQuery = "define name sub attribute, value string; person owns name;";
                            transaction.Query.Define(longQuery).Resolve();

                            transaction.Commit();
                        }
                    }

                    // Example of multiple transactions for one session
                    using (ITypeDBSession session = driver.Session(dbName, SessionType.Data))
                    {
                        // Examples of one query for one transaction
                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Write))
                        {
                            string query = "insert $p isa person, has name 'Alice';";
                            IConceptMap[] insertResults = transaction.Query.Insert(query).ToArray();
                            ProcessPersonInsertResult(insertResults, "p", "person", "Alice");

                            transaction.Commit();
                        }

                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Write))
                        {
                            IConceptMap[] insertResults =
                                transaction.Query.Insert("insert $p isa person, has name 'Bob';").ToArray();
                            ProcessPersonInsertResult(insertResults, "p", "person", "Bob");

                            // Not committed
                        }

                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Read))
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
        public void ManualExample()
        {
            string dbName = "access-management-db";
            string serverAddr = "127.0.0.1:1729";

            try
            {
                ITypeDBDriver driver = TypeDB.CoreDriver(serverAddr);
                driver.Databases.Create(dbName);

                IDatabase database = driver.Databases.Get(dbName);

                ITypeDBSession schemaSession = driver.Session(dbName, SessionType.Schema);
                ITypeDBTransaction schemaWriteTransaction = schemaSession.Transaction(TransactionType.Write);

                schemaWriteTransaction.Query.Define("define person sub entity;").Resolve();

                string longQuery = "define name sub attribute, value string; person owns name;";
                schemaWriteTransaction.Query.Define(longQuery).Resolve();

                schemaWriteTransaction.Commit(); // No need to close manually if committed
                schemaSession.Close();

                ITypeDBSession dataSession = driver.Session(dbName, SessionType.Data);
                ITypeDBTransaction dataWriteTransaction = dataSession.Transaction(TransactionType.Write);

                string query = "insert $p isa person, has name 'Alice';";
                IConceptMap[] insertResults = dataWriteTransaction.Query.Insert(query).ToArray();
                ProcessPersonInsertResult(insertResults, "p", "person", "Alice");

                dataWriteTransaction.Commit();
                dataSession.Close();

                dataSession = driver.Session(dbName, SessionType.Data);
                dataWriteTransaction = dataSession.Transaction(TransactionType.Write);

                insertResults = dataWriteTransaction.Query.Insert("insert $p isa person, has name 'Bob';").ToArray();
                ProcessPersonInsertResult(insertResults, "p", "person", "Bob");

    //            dataWriteTransaction.Commit(); // Not committed

                ITypeDBTransaction readTransaction = dataSession.Transaction(TransactionType.Read);
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
                    using (ITypeDBSession session = driver.Session(dbName, SessionType.Schema))
                    {
                        // Example of multiple queries for one transaction
                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Write))
                        {
                            transaction.Query.Define("define person sub entity;").Resolve();

                            string longQuery = "define name sub attribute, value string; person owns name;";
                            transaction.Query.Define(longQuery).Resolve();

                            transaction.Commit();
                        }
                    }

                    // Example of multiple transactions for one session
                    using (ITypeDBSession session = driver.Session(dbName, SessionType.Data))
                    {
                        // Examples of one query for one transaction
                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Write))
                        {
                            string query = "insert $p isa person, has name 'Alice';";
                            IEnumerable<IConceptMap> insertResults = transaction.Query.Insert(query);

                            Console.WriteLine($"Inserted with {insertResults.Count()} result(s)");

                            transaction.Commit();
                        }

                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Write))
                        {
                            IEnumerable<IConceptMap> insertResults =
                                transaction.Query.Insert("insert $p isa person, has name 'Bob';");

                            foreach (IConceptMap insertResult in insertResults)
                            {
                                Console.WriteLine($"Inserted: {insertResult}");
                            }

                            // transaction.Commit(); // Not committed
                        }

                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Read))
                        {
                            IConceptMap[] matchResults =
                                transaction.Query.Get("match $p isa person, has name $n; get $n;").ToArray();

                            // Matches only Alice as Bob has not been committed
                            var resultName = matchResults[0].Get("n");
                            Console.WriteLine($"Found the first name: {resultName.AsAttribute().Value.AsString()}");

                            if (matchResults.Length > 1) // Will work only if the previous transaction is committed
                            {
                                Console.WriteLine($"Found the second name as concept: {matchResults[1]}");
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

        private void ProcessPersonInsertResult(
            IConceptMap[] results,
            string variableName,
            string expectedVariableTypeLabel,
            string expectedAttributeValue)
        {
            Assert.AreEqual(1, results.Length);

            var result = results[0];
            Assert.AreEqual(2, result.GetVariables().Count());
            Assert.AreEqual(2, result.GetConcepts().Count());

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
    }
}
