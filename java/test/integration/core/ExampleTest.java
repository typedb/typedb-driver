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

package com.typedb.driver.test.integration.core;

// EXAMPLE START MARKER
import com.typedb.driver.TypeDB;
import com.typedb.driver.api.Driver;
import com.typedb.driver.api.QueryType;
import com.typedb.driver.api.Transaction;
import com.typedb.driver.api.answer.ConceptRow;
import com.typedb.driver.api.answer.ConceptRowIterator;
import com.typedb.driver.api.answer.QueryAnswer;
import com.typedb.driver.api.concept.Concept;
import com.typedb.driver.api.concept.type.AttributeType;
import com.typedb.driver.api.concept.type.EntityType;
import com.typedb.driver.api.database.Database;
import com.typedb.driver.common.Promise;
import com.typedb.driver.common.exception.TypeDBDriverException;
// EXAMPLE END MARKER

import org.junit.BeforeClass;
import org.junit.Test;

// EXAMPLE START MARKER
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

// EXAMPLE END MARKER

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("Duplicates")
// EXAMPLE START MARKER
public class ExampleTest {
    // EXAMPLE END MARKER
    @BeforeClass
    public static void setUpClass() {
        Driver typedbDriver = TypeDB.coreDriver(TypeDB.DEFAULT_ADDRESS);
        if (typedbDriver.databases().contains("typedb")) {
            typedbDriver.databases().get("typedb").delete();
        }
        typedbDriver.close();
    }

    @Test
    // EXAMPLE START MARKER
    public void example() {
        // Open a driver connection. Try-with-resources can be used for automatic driver connection management
        try (Driver driver = TypeDB.coreDriver(TypeDB.DEFAULT_ADDRESS)) {
            // Create a database
            driver.databases().create("typedb");
            Database database = driver.databases().get("typedb");
            assertEquals(database.name(), "typedb");

            // Open transactions of 3 types
            Transaction tx = driver.transaction(database.name(), Transaction.Type.READ);

            // Use "try" blocks to catch driver exceptions
            try {
                // Execute any TypeDB query using TypeQL. Wrong queries are rejected with an explicit exception
                Promise<? extends QueryAnswer> promise = tx.query("define entity i-cannot-be-defined-in-read-transactions;");

                System.out.println("The result is still promised, so it needs resolving even in case of errors!");
                promise.resolve();
            } catch (TypeDBDriverException expectedException) {
                System.out.println("Once the query's promise is resolved, the exception is revealed: " + expectedException);
            } finally {
                // Don't forget to close the transaction!
                tx.close();
            }

            // Open a schema transaction to make schema changes
            // Use try-with-resources blocks to forget about "close" operations (similarly to connections)
            try (Transaction transaction = driver.transaction(database.name(), Transaction.Type.SCHEMA)) {
                String defineQuery = "define " +
                        "entity person, owns name, owns age; " +
                        "attribute name, value string;\n" +
                        "attribute age, value long;";

                QueryAnswer answer = transaction.query(defineQuery).resolve();
                assertTrue(answer.isOk());
                assertEquals(QueryType.SCHEMA, answer.getQueryType());

                // Commit automatically closes the transaction. It can still be safely called inside "try" blocks
                transaction.commit();
            }


            // Open a read transaction to safely read anything without database modifications
            try (Transaction transaction = driver.transaction(database.name(), Transaction.Type.READ)) {
                QueryAnswer entityAnswer = transaction.query("match entity $x;").resolve();
                assertTrue(entityAnswer.isConceptRows());
                assertFalse(entityAnswer.isConceptDocuments());
                assertEquals(QueryType.READ, entityAnswer.getQueryType());

                // Collect concept rows that represent the answer as a table
                List<ConceptRow> entityRows = entityAnswer.asConceptRows().stream().collect(Collectors.toList());
                assertEquals(entityRows.size(), 1);
                ConceptRow entityRow = entityRows.get(0);

                // Collect column names to get concepts by index if the variable names are lost
                List<String> entityHeader = entityRow.columnNames().collect(Collectors.toList());
                assertEquals(entityHeader.size(), 1);

                String columnName = entityHeader.get(0);
                assertEquals(columnName, "x");

                // Get concept by the variable name (column name)
                Concept conceptByName = entityRow.get(columnName);

                // Get concept by the header's index
                Concept conceptByIndex = entityRow.getIndex(0);
                assertEquals(conceptByName, conceptByIndex);

                // Check if it's an entity type before the conversion
                if (conceptByName.isEntityType()) {
                    System.out.printf("Getting concepts by variable names and indexes is equally correct. " +
                                    "Both represent the defined entity type: '%s' (in case of a doubt: '%s')%n",
                            conceptByName.asEntityType().getLabel(),
                            conceptByIndex.asEntityType().getLabel());
                }
                assertTrue(conceptByName.isEntityType());
                assertTrue(conceptByName.isType());
                assertEquals(conceptByName.asEntityType().getLabel(), "person");
                assertNotEquals(conceptByName.asEntityType().getLabel(), "not person");
                assertNotEquals(conceptByName.asEntityType().getLabel(), "age");

                // Continue querying in the same transaction if needed
                QueryAnswer attributeAnswer = transaction.query("match attribute $a;").resolve();
                assertTrue(attributeAnswer.isConceptRows());
                assertEquals(QueryType.READ, attributeAnswer.getQueryType());

                // ConceptRowIterator can be used as any other Iterator
                ConceptRowIterator attributeRowIterator = attributeAnswer.asConceptRows();

                while (attributeRowIterator.hasNext()) {
                    ConceptRow attributeRow = attributeRowIterator.next();

                    // Column names are a stream, so they can be used in a similar way
                    Iterator<String> columnNameIterator = attributeRow.columnNames().iterator();
                    columnName = columnNameIterator.next();
                    assertFalse(columnNameIterator.hasNext());

                    conceptByName = attributeRow.get(columnName);

                    // Check if it's an attribute type before the conversion
                    if (conceptByName.isAttributeType()) {
                        AttributeType attributeType = conceptByName.asAttributeType();
                        System.out.printf("Defined attribute type's label: '%s', value type: '%s'%n", attributeType.getLabel(), attributeType.getValueType());
                        assertTrue(attributeType.isLong() || attributeType.isString());
                        assertTrue(Objects.equals(attributeType.getValueType(), "long") || Objects.equals(attributeType.getValueType(), "string"));
                        assertTrue(Objects.equals(attributeType.getLabel(), "age") || Objects.equals(attributeType.getLabel(), "name"));
                        assertNotEquals(attributeType.getLabel(), "person");
                        assertNotEquals(attributeType.getLabel(), "person:age");

                        System.out.printf("It is also possible to just print the concept itself: '%s'%n", conceptByName);
                        assertTrue(conceptByName.isAttributeType());
                        assertTrue(conceptByName.isType());
                    }
                }
            }

            // Open a write transaction to insert data
            try (Transaction transaction = driver.transaction(database.name(), Transaction.Type.WRITE)) {
                String insertQuery = "insert $z isa person, has age 10; $x isa person, has age 20, has name \"John\";";
                QueryAnswer answer = transaction.query(insertQuery).resolve();
                assertTrue(answer.isConceptRows());
                assertEquals(QueryType.WRITE, answer.getQueryType());

                // Insert queries also return concept rows
                List<ConceptRow> rows = answer.asConceptRows().stream().collect(Collectors.toList());
                assertEquals(rows.size(), 1);
                ConceptRow row = rows.get(0);
                row.columnNames().iterator().forEachRemaining(columnName -> {
                    Concept insertedConcept = row.get(columnName);
                    System.out.printf("Successfully inserted $%s: %s%n", columnName, insertedConcept);
                    if (insertedConcept.isEntity()) {
                        System.out.println("This time, it's an entity, not a type!");
                    }
                });

                // It is possible to ask for the column names again
                List<String> header = row.columnNames().collect(Collectors.toList());
                assertEquals(header.size(), 2);
                assertTrue(header.contains("x"));
                assertTrue(header.contains("z"));

                Concept x = row.getIndex(header.indexOf("x"));
                if (x.isEntity()) {
                    System.out.println("Each entity receives a unique IID. It can be retrieved directly: " + x.asEntity().getIID());
                }

                // Do not forget to commit if the changes should be persisted
                transaction.commit();
            }

            // Open another write transaction to try inserting even more data
            try (Transaction transaction = driver.transaction(database.name(), Transaction.Type.WRITE)) {
                // When loading a large dataset, it's often better not to resolve every query's promise immediately.
                // Instead, collect promises and handle them later. Alternatively, if a commit is expected in the end,
                // just call `commit`, which will wait for all ongoing operations to finish before executing.
                List<String> queries = List.of("insert $a isa person, has name \"Alice\";", "insert $b isa person, has name \"Bob\";");
                for (String query : queries) {
                    transaction.query(query);
                }
                transaction.commit();
            }

            try (Transaction transaction = driver.transaction(database.name(), Transaction.Type.WRITE)) {
                // Commit will still fail if at least one of the queries produce an error.
                List<String> queries = List.of("insert $c isa not-person, has name \"Chris\";", "insert $d isa person, has name \"David\";");
                List<Promise<? extends QueryAnswer>> promises = new ArrayList<>();
                for (String query : queries) {
                    promises.add(transaction.query(query));
                }

                try {
                    transaction.commit();
                    fail("TypeDBDriverException is expected");
                } catch (TypeDBDriverException expectedException) {
                    System.out.println("Commit result will contain the unresolved query's error: " + expectedException);
                }
            }

            // Open a read transaction to verify that the inserted data is saved
            try (Transaction transaction = driver.transaction(database.name(), Transaction.Type.READ)) {
                // A match query can be used for concept row outputs
                String var = "x";
                QueryAnswer matchAnswer = transaction.query(String.format("match $%s isa person;", var)).resolve();
                assertTrue(matchAnswer.isConceptRows());
                assertEquals(QueryType.READ, matchAnswer.getQueryType());

                // Simple match queries always return concept rows
                AtomicInteger matchCount = new AtomicInteger(0);
                matchAnswer.asConceptRows().stream().forEach(row -> {
                    assertEquals(QueryType.READ, row.getQueryType());
                    Concept x = row.get(var);
                    assertTrue(x.isEntity());
                    assertFalse(x.isEntityType());
                    assertFalse(x.isAttribute());
                    assertFalse(x.isType());
                    assertTrue(x.isInstance());
                    EntityType xType = x.asEntity().getType().asEntityType();
                    assertEquals(xType.getLabel(), "person");
                    assertNotEquals(xType.getLabel(), "not person");
                    matchCount.incrementAndGet();
                    System.out.printf("Found a person %s of type %s%n", x, xType);
                });
                assertEquals(matchCount.get(), 4);
                System.out.println("Total persons found: " + matchCount.get());

                // A fetch query can be used for concept document outputs with flexible structure
                QueryAnswer fetchAnswer = transaction.query("match" +
                        "  $x isa! person, has $a;" +
                        "  $a isa! $t;" +
                        "fetch {" +
                        "  \"single attribute type\": $t," +
                        "  \"single attribute\": $a," +
                        "  \"all attributes\": { $x.* }," +
                        "};").resolve();
                assertTrue(fetchAnswer.isConceptDocuments());
                assertEquals(QueryType.READ, fetchAnswer.getQueryType());

                // Fetch queries always return concept documents
                AtomicInteger fetchCount = new AtomicInteger(0);
                fetchAnswer.asConceptDocuments().stream().forEach(document -> {
                    assertNotNull(document);
                    System.out.println("Fetched a document: " + document);
                    System.out.print("This document contains an attribute of type: ");
                    System.out.println(document.asObject().get("single attribute type").asObject().get("label"));

                    fetchCount.incrementAndGet();
                });
                assertEquals(fetchCount.get(), 5);
                System.out.println("Total documents fetched: " + fetchCount.get());
            }
        }

        System.out.println("More examples can be found in the API reference and the documentation.\nWelcome to TypeDB!");
    }
}
// EXAMPLE END MARKER
