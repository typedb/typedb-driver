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

// EXAMPLE START MARKER
using System;
using System.Collections.Generic;
using System.Linq;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Api.Answer;
using TypeDB.Driver.Common;

// EXAMPLE END MARKER
using NUnit.Framework;
// EXAMPLE START MARKER

namespace TypeDB.Driver.Test.Integration
{
    [TestFixture]
    public class ExampleTest
    {
        // EXAMPLE END MARKER
        [SetUp]
        public void SetUp()
        {
            // Clean up any existing test database before each test
            using var driver = TypeDB.Driver(TypeDB.DefaultAddress, new Credentials("admin", "password"), new DriverOptions(DriverTlsConfig.Disabled()));
            if (driver.Databases.Contains("typedb"))
            {
                driver.Databases.Get("typedb").Delete();
            }
        }

        [Test]
        // EXAMPLE START MARKER
        public void Example()
        {
            // Open a driver connection. Using statements can be used for automatic driver connection management
            using (var driver = TypeDB.Driver(TypeDB.DefaultAddress, new Credentials("admin", "password"), new DriverOptions(DriverTlsConfig.Disabled())))
            {
                // Create a database
                driver.Databases.Create("typedb");
                var database = driver.Databases.Get("typedb");
                Assert.AreEqual("typedb", database.Name);

                // Open transactions of 3 types
                var tx = driver.Transaction(database.Name, TransactionType.Read);

                // Use try blocks to catch driver exceptions
                try
                {
                    // Execute any TypeDB query using TypeQL. Wrong queries are rejected with an explicit exception
                    var answer = tx.Query("define entity i-cannot-be-defined-in-read-transactions;").Resolve()!;

                    Console.WriteLine("The query has been sent, iterating or committing will surface errors!");
                    // Iterating the answer would throw an exception
                }
                catch (TypeDBDriverException expectedException)
                {
                    Console.WriteLine("Query execution revealed the exception: " + expectedException);
                }
                finally
                {
                    // Don't forget to close the transaction!
                    tx.Close();
                }

                // Open a schema transaction to make schema changes
                // Transactions can be opened with configurable options. This option limits its lifetime
                var transactionOptions = new TransactionOptions { TransactionTimeoutMillis = 10_000 };

                // Use using blocks to forget about close operations (similarly to connections)
                using (var transaction = driver.Transaction(database.Name, TransactionType.Schema, transactionOptions))
                {
                    var defineQuery = @"define
                        entity person, owns name, owns age;
                        attribute name, value string;
                        attribute age, value integer;";

                    var answer = transaction.Query(defineQuery).Resolve()!;
                    Assert.IsTrue(answer.IsOk);
                    Assert.AreEqual(QueryType.Schema, answer.QueryType);

                    // Commit automatically closes the transaction. It can still be safely called inside using blocks
                    transaction.Commit();
                }

                // Open a read transaction to safely read anything without database modifications
                using (var transaction = driver.Transaction(database.Name, TransactionType.Read))
                {
                    var entityAnswer = transaction.Query("match entity $x;").Resolve()!;
                    Assert.IsTrue(entityAnswer.IsConceptRows);
                    Assert.IsFalse(entityAnswer.IsConceptDocuments);
                    Assert.AreEqual(QueryType.Read, entityAnswer.QueryType);

                    // Collect concept rows that represent the answer as a table
                    var entityRows = entityAnswer.AsConceptRows().ToList();
                    Assert.AreEqual(1, entityRows.Count);
                    var entityRow = entityRows[0];

                    // Collect column names to get concepts by index if the variable names are lost
                    var entityHeader = entityRow.ColumnNames.ToList();
                    Assert.AreEqual(1, entityHeader.Count);

                    var columnName = entityHeader[0];
                    Assert.AreEqual("x", columnName);

                    // Get concept by the variable name (column name)
                    var conceptByName = entityRow.Get(columnName);

                    // Get concept by the header's index
                    var conceptByIndex = entityRow.GetIndex(0);
                    Assert.AreEqual(conceptByName, conceptByIndex);

                    Console.WriteLine($"Getting concepts by variable names ({conceptByName?.TryGetLabel()}) and indexes ({conceptByIndex?.TryGetLabel()}) is equally correct. ");

                    // Check if it's an entity type before the conversion
                    if (conceptByName != null && conceptByName.IsEntityType())
                    {
                        Console.WriteLine($"Both represent the defined entity type: '{conceptByName.AsEntityType().GetLabel()}' (in case of a doubt: '{conceptByIndex?.AsEntityType().GetLabel()}')");
                    }
                    Assert.IsNotNull(conceptByName);
                    Assert.IsTrue(conceptByName!.IsEntityType());
                    Assert.IsTrue(conceptByName.IsType());
                    Assert.AreEqual("person", conceptByName.GetLabel());
                    Assert.AreEqual("person", conceptByName.AsEntityType().GetLabel());
                    Assert.AreNotEqual("not person", conceptByName.AsEntityType().GetLabel());
                    Assert.AreNotEqual("age", conceptByName.AsEntityType().GetLabel());

                    // Continue querying in the same transaction if needed
                    var attributeAnswer = transaction.Query("match attribute $a;").Resolve()!;
                    Assert.IsTrue(attributeAnswer.IsConceptRows);
                    Assert.AreEqual(QueryType.Read, attributeAnswer.QueryType);

                    // IConceptRowIterator can be used as any other IEnumerable
                    foreach (var attributeRow in attributeAnswer.AsConceptRows())
                    {
                        // Column names are an IEnumerable, so they can be used in a similar way
                        var columnNames = attributeRow.ColumnNames.ToList();
                        columnName = columnNames[0];
                        Assert.AreEqual(1, columnNames.Count);

                        conceptByName = attributeRow.Get(columnName);

                        // Check if it's an attribute type before the conversion
                        if (conceptByName != null && conceptByName.IsAttributeType())
                        {
                            var attributeType = conceptByName.AsAttributeType();
                            Console.WriteLine($"Defined attribute type's label: '{attributeType.GetLabel()}', value type: '{attributeType.TryGetValueType()}'");
                            Assert.IsTrue(attributeType.IsInteger() || attributeType.IsString());
                            Assert.IsTrue(attributeType.TryGetValueType() == "integer" || attributeType.TryGetValueType() == "string");
                            Assert.IsTrue(attributeType.GetLabel() == "age" || attributeType.GetLabel() == "name");
                            Assert.AreNotEqual("person", attributeType.GetLabel());
                            Assert.AreNotEqual("person:age", attributeType.GetLabel());

                            Console.WriteLine($"It is also possible to just print the concept itself: '{conceptByName}'");
                            Assert.IsTrue(conceptByName.IsAttributeType());
                            Assert.IsTrue(conceptByName.IsType());
                        }
                    }
                }

                // Open a write transaction to insert data
                using (var transaction = driver.Transaction(database.Name, TransactionType.Write))
                {
                    var insertQuery = "insert $z isa person, has age 10; $x isa person, has age 20, has name \"John\";";
                    var answer = transaction.Query(insertQuery).Resolve()!;
                    Assert.IsTrue(answer.IsConceptRows);
                    Assert.AreEqual(QueryType.Write, answer.QueryType);

                    // Insert queries also return concept rows
                    var rows = answer.AsConceptRows().ToList();
                    Assert.AreEqual(1, rows.Count);
                    var row = rows[0];
                    foreach (var colName in row.ColumnNames)
                    {
                        var insertedConcept = row.Get(colName);
                        Console.WriteLine($"Successfully inserted ${colName}: {insertedConcept}");
                        if (insertedConcept != null && insertedConcept.IsEntity())
                        {
                            Console.WriteLine("This time, it's an entity, not a type!");
                        }
                    }

                    // It is possible to ask for the column names again
                    var header = row.ColumnNames.ToList();
                    Assert.AreEqual(2, header.Count);
                    Assert.That(header, Does.Contain("x"));
                    Assert.That(header, Does.Contain("z"));

                    var x = row.GetIndex(header.IndexOf("x"));
                    Console.WriteLine($"As we expect an entity instance, we can try to get its IID (unique identification): {x?.TryGetIID()}. ");
                    if (x != null && x.IsEntity())
                    {
                        Console.WriteLine("It can also be retrieved directly and safely after a cast: " + x.AsEntity().TryGetIID());
                    }

                    // Do not forget to commit if the changes should be persisted
                    Console.WriteLine("CAUTION: Committing or closing (including leaving the using block) a transaction will invalidate all its uncollected answer iterators");
                    transaction.Commit();
                }

                // Open another write transaction to try inserting even more data
                using (var transaction = driver.Transaction(database.Name, TransactionType.Write))
                {
                    // When loading a large dataset, it's often better to batch queries.
                    // Just call commit, which will wait for all ongoing operations to finish before executing.
                    var queries = new[] { "insert $a isa person, has name \"Alice\";", "insert $b isa person, has name \"Bob\";" };
                    foreach (var query in queries)
                    {
                        transaction.Query(query).Resolve();
                    }
                    transaction.Commit();
                }

                using (var transaction = driver.Transaction(database.Name, TransactionType.Write))
                {
                    // In C#, query errors surface during query execution
                    try
                    {
                        var invalidQuery = "insert $c isa not-person, has name \"Chris\";";
                        transaction.Query(invalidQuery).Resolve();
                        Assert.Fail("TypeDBDriverException is expected");
                    }
                    catch (TypeDBDriverException expectedException)
                    {
                        Console.WriteLine("Query execution revealed the error: " + expectedException);
                    }
                    // Transaction is still usable after catching the exception
                }

                // Open a read transaction to verify that the inserted data is saved
                using (var transaction = driver.Transaction(database.Name, TransactionType.Read))
                {
                    // Queries can also be executed with configurable options. This option forces the database
                    // to include types of instance concepts in ConceptRows answers
                    var queryOptions = new QueryOptions { IncludeInstanceTypes = true };
                    // A match query can be used for concept row outputs
                    var varName = "x";
                    var matchAnswer = transaction.Query($"match ${varName} isa person;", queryOptions).Resolve()!;
                    Assert.IsTrue(matchAnswer.IsConceptRows);
                    Assert.AreEqual(QueryType.Read, matchAnswer.QueryType);

                    // Simple match queries always return concept rows
                    var matchCount = 0;
                    foreach (var row in matchAnswer.AsConceptRows())
                    {
                        Assert.AreEqual(QueryType.Read, row.QueryType);
                        var x = row.Get(varName);
                        Assert.IsNotNull(x);
                        Assert.IsTrue(x!.IsEntity());
                        Assert.IsFalse(x.IsEntityType());
                        Assert.IsFalse(x.IsAttribute());
                        Assert.IsFalse(x.IsType());
                        Assert.IsTrue(x.IsInstance());
                        var xType = x.AsEntity().Type.AsEntityType();
                        Assert.AreEqual("person", xType.GetLabel());
                        Assert.AreNotEqual("not person", xType.GetLabel());
                        matchCount++;
                        Console.WriteLine($"Found a person {x} of type {xType}");
                    }
                    Assert.AreEqual(4, matchCount);
                    Console.WriteLine("Total persons found: " + matchCount);

                    // A fetch query can be used for concept document outputs with flexible structure
                    var fetchAnswer = transaction.Query(@"match
                        $x isa! person, has $a;
                        $a isa! $t;
                        fetch {
                            ""single attribute type"": $t,
                            ""single attribute"": $a,
                            ""all attributes"": { $x.* },
                        };").Resolve()!;
                    Assert.IsTrue(fetchAnswer.IsConceptDocuments);
                    Assert.AreEqual(QueryType.Read, fetchAnswer.QueryType);

                    // Fetch queries always return concept documents
                    var fetchCount = 0;
                    foreach (var document in fetchAnswer.AsConceptDocuments())
                    {
                        Assert.IsNotNull(document);
                        Console.WriteLine("Fetched a document: " + document);
                        Console.Write("This document contains an attribute of type: ");
                        Console.WriteLine(document.AsObject()["single attribute type"].AsObject()["label"]);

                        fetchCount++;
                    }
                    Assert.AreEqual(5, fetchCount);
                    Console.WriteLine("Total documents fetched: " + fetchCount);
                }
            }

            Console.WriteLine("More examples can be found in the API reference and the documentation.\nWelcome to TypeDB!");
        }
    }
}
// EXAMPLE END MARKER
