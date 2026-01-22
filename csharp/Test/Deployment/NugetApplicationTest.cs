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
using System.Linq;

using Xunit;
using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Api.Answer;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Deployment
{
    public class NugetApplicationTest
    {
        [Fact]
        public static void Main()
        {
            Console.WriteLine("Starting NuGet deployment test...");

            string dbName = "nuget-test-database";

            using (var driver = TypeDB.Driver(TypeDB.DefaultAddress, new Credentials("admin", "password"), new DriverOptions(false, null)))
            {
                if (driver.Databases.Contains(dbName))
                {
                    driver.Databases.Get(dbName).Delete();
                }
                driver.Databases.Create(dbName);

                var database = driver.Databases.Get(dbName);
                Assert.NotNull(database);
                Assert.Equal(dbName, database.Name);

                try
                {
                    // Schema transaction - define types
                    using (var transaction = driver.Transaction(dbName, TransactionType.Schema))
                    {
                        var defineQuery = @"define
                            entity person, owns name, owns age;
                            attribute name, value string;
                            attribute age, value integer;";

                        var answer = transaction.Query(defineQuery);
                        Assert.True(answer.IsOk);
                        Assert.Equal(QueryType.Schema, answer.QueryType);

                        transaction.Commit();
                    }

                    // Write transaction - insert data
                    using (var transaction = driver.Transaction(dbName, TransactionType.Write))
                    {
                        var insertQuery = "insert $p isa person, has name \"Alice\", has age 30;";
                        var answer = transaction.Query(insertQuery);

                        Assert.True(answer.IsConceptRows);
                        Assert.Equal(QueryType.Write, answer.QueryType);

                        var rows = answer.AsConceptRows().ToList();
                        Assert.Single(rows);

                        var row = rows[0];
                        var p = row.Get("p");
                        Assert.NotNull(p);
                        Assert.True(p!.IsEntity());

                        transaction.Commit();
                    }

                    // Read transaction - match query
                    using (var transaction = driver.Transaction(dbName, TransactionType.Read))
                    {
                        var matchQuery = "match $p isa person, has name $n;";
                        var matchAnswer = transaction.Query(matchQuery);

                        Assert.True(matchAnswer.IsConceptRows);
                        Assert.Equal(QueryType.Read, matchAnswer.QueryType);

                        var matchRows = matchAnswer.AsConceptRows().ToList();
                        Assert.Single(matchRows);

                        var row = matchRows[0];
                        var n = row.Get("n");
                        Assert.NotNull(n);
                        Assert.True(n!.IsAttribute());
                    }

                    // Read transaction - fetch query
                    using (var transaction = driver.Transaction(dbName, TransactionType.Read))
                    {
                        var fetchQuery = @"match
                            $p isa person, has name $n;
                            fetch {
                                ""person_name"": $n
                            };";

                        var fetchAnswer = transaction.Query(fetchQuery);

                        Assert.True(fetchAnswer.IsConceptDocuments);
                        Assert.Equal(QueryType.Read, fetchAnswer.QueryType);

                        var documents = fetchAnswer.AsConceptDocuments().ToList();
                        Assert.Single(documents);

                        var doc = documents[0];
                        Console.WriteLine($"Fetched document: {doc}");
                    }
                }
                finally
                {
                    database.Delete();
                }
            }

            Console.WriteLine("NuGet deployment test completed successfully!");
        }
    }
}
