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
using System.Linq;

using Xunit;
using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Deployment
{
    public class NugetApplicationTest
    {
        [Fact]
        public static void Main()
        {
            Console.WriteLine("Starting test...");

            string dbName = "nuget-test-database";

            using (ITypeDBDriver driver = Drivers.CoreDriver("localhost:1729"))
            {
                try
                {
                    driver.Databases.Create(dbName);
                }
                catch (TypeDBDriverException e)
                {
                    if (!e.Message.Contains("already exists"))
                    {
                        throw e;
                    }

                    driver.Databases.Get(dbName).Delete();
                    driver.Databases.Create(dbName);
                }

                IDatabase database = driver.Databases.Get(dbName);

                try
                {
                    using (ITypeDBSession session = driver.Session(dbName, SessionType.Schema))
                    {
                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Write))
                        {
                            IEntityType root = transaction.Concepts.RootEntityType;
                            Assert.NotNull(root);
                            Assert.Single(root.GetSubtypes(transaction));

                            transaction.Query.Define("define person sub entity;").Resolve();
                            string longQuery = "define name sub attribute, value string; person owns name;";
                            transaction.Query.Define(longQuery).Resolve();
                            transaction.Commit();
                        }
                    }

                    using (ITypeDBSession session = driver.Session(dbName, SessionType.Data))
                    {
                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Write))
                        {
                            IEntityType root = transaction.Concepts.RootEntityType;
                            Assert.NotNull(root);
                            Assert.Equal(2, root.GetSubtypes(transaction).Count());

                            string query = "insert $p isa person, has name 'Alice';";
                            IEnumerable<IConceptMap> insertResults = transaction.Query.Insert(query);
                            Assert.NotNull(insertResults);
                            Assert.Single(insertResults);

                            transaction.Commit();
                        }

                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Read))
                        {
                            IConceptMap[] matchResults =
                                transaction.Query.Get("match $p isa person, has name $n; get $n;").ToArray();

                            var fetchResults =
                                transaction.Query.Fetch("match $f isa person, has name $n; fetch $n;").ToList();
                            Assert.NotNull(fetchResults);
                            Assert.Single(fetchResults);

                            foreach (var result in fetchResults)
                            {
                                Console.WriteLine($"JSON result: {result}");
                            }
                        }
                    }
                }
                finally
                {
                    database.Delete();
                }
            }

            Console.WriteLine("Success!");
        }
    }
}
