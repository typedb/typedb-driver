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
    public class CloudExamplesTestFixture
    {
        [Test]
        public void DocExample()
        {
            string dbName = "access-management-db";

            // You can also specify all node addresses like: {"localhost:11729", "localhost:21729", "localhost:31729"}
            string[] serverAddrs = new string[]{"localhost:11729"};

            try
            {
                TypeDBCredential connectCredential = new TypeDBCredential(
                    "admin",
                    "password",
                    Environment.GetEnvironmentVariable("ROOT_CA")!);

                using (ITypeDBDriver driver = Drivers.CloudDriver(serverAddrs, connectCredential))
                {
                    driver.Databases.Create(dbName);
                    IDatabase database = driver.Databases.Get(dbName);

                    TypeDBOptions options = new TypeDBOptions();

                    // Example of one transaction for one session with options
                    using (ITypeDBSession session = driver.Session(dbName, SessionType.Schema, options))
                    {
                        // Example of multiple queries for one transaction with options
                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Write, options))
                        {
                            transaction.Query.Define("define person sub entity;").Resolve();

                            string longQuery = "define name sub attribute, value string; person owns name;";
                            transaction.Query.Define(longQuery).Resolve();

                            transaction.Commit();
                        }
                    }

                    // Example of multiple transactions for one session without options
                    using (ITypeDBSession session = driver.Session(dbName, SessionType.Data))
                    {
                        // Examples of one query for one transaction without options
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
                Assert.Fail("You can handle exceptions here. However, we do not expect exceptions in CI, so we fail.");
            }
        }
    }
}
