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

package com.typedb.driver.test.integration;

import com.typedb.driver.TypeDB;
import com.typedb.driver.api.Driver;
import com.typedb.driver.api.Transaction;
import com.typedb.driver.api.answer.ConceptRow;
import com.typedb.driver.api.answer.QueryAnswer;
import com.typedb.driver.api.concept.Concept;
import com.typedb.driver.api.concept.instance.Attribute;
import com.typedb.driver.api.concept.instance.Entity;
import com.typedb.driver.api.concept.type.AttributeType;
import com.typedb.driver.api.concept.type.EntityType;
import com.typedb.driver.api.concept.value.Value;
import com.typedb.driver.api.database.Database;
import com.typedb.driver.common.Duration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
public class DriverQueryTest {
    private static final Logger LOG = LoggerFactory.getLogger(DriverQueryTest.class);
    private static final String DB_NAME = "typedb";
    private static final String ADDRESS = "0.0.0.0:1729";
    private static Driver typedbDriver;

    @BeforeClass
    public static void setUpClass() {
        typedbDriver = TypeDB.coreDriver(ADDRESS);
        if (typedbDriver.databases().contains(DB_NAME)) typedbDriver.databases().get(DB_NAME).delete();
        typedbDriver.databases().create(DB_NAME);
    }

    @AfterClass
    public static void close() {
        typedbDriver.close();
    }

    @Test
    public void basicTest() {
        if (typedbDriver.databases().contains(DB_NAME)) {
            typedbDriver.databases().get(DB_NAME).delete();
        }
        typedbDriver.databases().create(DB_NAME);
        Database database = typedbDriver.databases().get(DB_NAME);
        assertEquals(database.name(), DB_NAME);

        localhostTypeDBTX(tx -> {
            QueryAnswer answer = tx.query("define entity person, owns age; attribute age, value long;").resolve();
            assertTrue(answer.isOk());

            tx.commit();
        }, Transaction.Type.SCHEMA);

        localhostTypeDBTX(tx -> {
            QueryAnswer answer = tx.query("match entity $x;").resolve();
            assertTrue(answer.isConceptRows());

            List<ConceptRow> rows = answer.asConceptRows().stream().collect(Collectors.toList());
            assertEquals(rows.size(), 1);

            ConceptRow row = rows.get(0);
            List<String> header = row.columnNames().collect(Collectors.toList());
            assertEquals(header.size(), 1);

            String columnName = header.get(0);
            Concept conceptByName = row.get(columnName);
            Concept conceptByIndex = row.getIndex(0);
            assertEquals(conceptByName, conceptByIndex);

            assertTrue(conceptByName.isEntityType());
            assertFalse(conceptByName.isEntity());
            assertFalse(conceptByName.isAttributeType());
            assertTrue(conceptByName.isType());
            assertFalse(conceptByName.isInstance());
            assertEquals(conceptByName.asEntityType().getLabel(), "person");
            assertNotEquals(conceptByName.asEntityType().getLabel(), "not person");
            assertNotEquals(conceptByName.asEntityType().getLabel(), "age");
        }, Transaction.Type.READ);

        localhostTypeDBTX(tx -> {
            QueryAnswer answer = tx.query("match attribute $a;").resolve();
            assertTrue(answer.isConceptRows());

            List<ConceptRow> rows = answer.asConceptRows().stream().collect(Collectors.toList());
            assertEquals(rows.size(), 1);

            ConceptRow row = rows.get(0);
            List<String> header = row.columnNames().collect(Collectors.toList());
            assertEquals(header.size(), 1);

            String columnName = header.get(0);
            Concept conceptByName = row.get(columnName);
            Concept conceptByIndex = row.getIndex(0);
            assertEquals(conceptByName, conceptByIndex);

            assertTrue(conceptByName.isAttributeType());
            assertFalse(conceptByName.isAttribute());
            assertFalse(conceptByName.isEntityType());
            assertTrue(conceptByName.isType());
            assertFalse(conceptByName.isInstance());
            assertFalse(conceptByName.asAttributeType().isBoolean());
            assertFalse(conceptByName.asAttributeType().isStruct());
            assertFalse(conceptByName.asAttributeType().isString());
            assertFalse(conceptByName.asAttributeType().isDecimal());
            assertFalse(conceptByName.asAttributeType().isDouble());
            assertTrue(conceptByName.asAttributeType().isLong());
            assertEquals(conceptByName.asAttributeType().getLabel(), "age");
            assertNotEquals(conceptByName.asAttributeType().getLabel(), "person");
        }, Transaction.Type.READ);

        localhostTypeDBTX(tx -> {
            QueryAnswer answer = tx.query("insert $z isa person, has age 10; $x isa person, has age 20;").resolve();
            assertTrue(answer.isConceptRows());

            List<ConceptRow> rows = answer.asConceptRows().stream().collect(Collectors.toList());
            assertEquals(rows.size(), 1);

            ConceptRow row = rows.get(0);
            List<String> header = row.columnNames().collect(Collectors.toList());
            assertEquals(header.size(), 2);
            assertTrue(header.contains("x"));
            assertTrue(header.contains("z"));

            Concept x = row.getIndex(header.indexOf("x"));
            assertTrue(x.isEntity());
            assertFalse(x.isEntityType());
            assertFalse(x.isAttribute());
            assertFalse(x.isType());
            assertTrue(x.isInstance());
            assertEquals(x.asEntity().getType().asEntityType().getLabel(), "person");
            assertNotEquals(x.asEntity().getType().asEntityType().getLabel(), "not person");

            Concept z = row.get("z");
            assertTrue(z.isEntity());
            assertFalse(z.isEntityType());
            assertFalse(z.isAttribute());
            assertFalse(z.isType());
            assertTrue(z.isInstance());
            Entity zEntity = z.asEntity();
            assertEquals(zEntity.getType().asEntityType().getLabel(), "person");
            assertNotEquals(zEntity.getType().asEntityType().getLabel(), "not person");

            tx.commit();
        }, Transaction.Type.WRITE);

        localhostTypeDBTX(tx -> {
            String var = "x";
            QueryAnswer matchAnswer = tx.query(String.format("match $%s isa person;", var)).resolve();
            assertTrue(matchAnswer.isConceptRows());

            AtomicInteger matchCount = new AtomicInteger(0);
            matchAnswer.asConceptRows().stream().forEach(row -> {
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
            });
            assertEquals(matchCount.get(), 2);

            QueryAnswer fetchAnswer = tx.query("match" +
                    "    $x isa! person, has $a;" +
                    "    $a isa! $t;" +
                    "fetch {" +
                    "    \"single attribute type\": $t," +
                    "    \"single attribute\": $a," +
                    "    \"all attributes\": { $x.* }," +
                    "};").resolve();
            assertTrue(fetchAnswer.isConceptDocuments());

            System.out.println("Fetch results for manual testing:");
            AtomicInteger fetchCount = new AtomicInteger(0);
            fetchAnswer.asConceptDocuments().stream().forEach(document -> {
                assertNotNull(document);
                System.out.println(document);
                fetchCount.incrementAndGet();
            });
            assertEquals(fetchCount.get(), 2);
        }, Transaction.Type.READ);
    }

    @Test
    public void attributesTest() {
        Database db = typedbDriver.databases().get(DB_NAME);
        db.delete();
        typedbDriver.databases().create(DB_NAME);

        Map<String, String> attributeValueTypes = Map.ofEntries(
                Map.entry("root", "none"),
                Map.entry("age", "long"),
                Map.entry("name", "string"),
                Map.entry("is-new", "boolean"),
                Map.entry("success", "double"),
                Map.entry("balance", "decimal"),
                Map.entry("birth-date", "date"),
                Map.entry("birth-time", "datetime"),
                Map.entry("current-time", "datetime-tz"),
                Map.entry("current-time-off", "datetime-tz"),
                Map.entry("expiration", "duration")
        );

        Map<String, String> attributeValues = Map.ofEntries(
                Map.entry("age", "25"),
                Map.entry("name", "\"John\""),
                Map.entry("is-new", "true"),
                Map.entry("success", "66.6"),
                Map.entry("balance", "1234567890.0001234567890"),
                Map.entry("birth-date", "2024-09-20"),
                Map.entry("birth-time", "1999-02-26T12:15:05"),
                Map.entry("current-time", "2024-09-20T16:40:05 Europe/London"),
                Map.entry("current-time-off", "2024-09-20T16:40:05.028129323+0545"),
                Map.entry("expiration", "P1Y10M7DT15H44M5.00394892S")
        );

        localhostTypeDBTX(tx -> {
            for (Map.Entry<String, String> entry : attributeValueTypes.entrySet()) {
                String query;
                if (Objects.equals(entry.getValue(), "none")) {
                    query = String.format("define attribute %s @abstract;", entry.getKey());
                } else {
                    query = String.format("define attribute %s, value %s; entity person owns %s;", entry.getKey(), entry.getValue(), entry.getKey());
                }
                assertTrue(tx.query(query).resolve().isOk());
            }
            tx.commit();
        }, Transaction.Type.SCHEMA);

        localhostTypeDBTX(tx -> {
            QueryAnswer answer = tx.query("match attribute $a;").resolve();
            assertTrue(answer.isConceptRows());

            AtomicInteger count = new AtomicInteger(0);
            answer.asConceptRows().stream().forEach(row -> {
                Concept a = row.get("a");
                assertTrue(a.isAttributeType());
                AttributeType type = a.asAttributeType();
                assertEquals(type.getValueType(), attributeValueTypes.get(type.getLabel()));
                count.incrementAndGet();
            });
            assertEquals(count.get(), attributeValueTypes.size());
        }, Transaction.Type.READ);

        localhostTypeDBTX(tx -> {
            for (Map.Entry<String, String> entry : attributeValues.entrySet()) {
                QueryAnswer answer = tx.query(String.format("insert $a isa person, has %s %s;", entry.getKey(), entry.getValue())).resolve();
                assertTrue(answer.isConceptRows());
                List<ConceptRow> rows = answer.asConceptRows().stream().collect(Collectors.toList());
                assertEquals(rows.size(), 1);

                ConceptRow row = rows.get(0);
                List<String> header = row.columnNames().collect(Collectors.toList());
                assertEquals(header.size(), 1);
                assertTrue(row.getIndex(header.indexOf("a")).isEntity());
            }
            tx.commit();
        }, Transaction.Type.WRITE);

        localhostTypeDBTX(tx -> {
            {
                QueryAnswer answer = tx.query("match attribute $t; $a isa! $t;").resolve();
                assertTrue(answer.isConceptRows());
                List<ConceptRow> rows = answer.asConceptRows().stream().collect(Collectors.toList());
                assertEquals(rows.size(), attributeValues.size());
                AtomicInteger checked = new AtomicInteger(0);
                rows.forEach(row -> {
                    Attribute attribute = row.get("a").asAttribute();
                    String attributeName = attribute.getType().getLabel();
                    Value value = attribute.getValue();
                    assertEquals(value.getType(), attributeValueTypes.get(attributeName));
                    if (value.isLong()) {
                        assertEquals(Long.parseLong(attributeValues.get(attributeName)), value.asLong());
                        checked.incrementAndGet();
                    } else if (value.isString()) {
                        assertEquals(attributeValues.get(attributeName).substring(1, attributeValues.get(attributeName).length() - 1), value.asString());
                        checked.incrementAndGet();
                    } else if (value.isBoolean()) {
                        assertEquals(Boolean.parseBoolean(attributeValues.get(attributeName)), value.asBoolean());
                        checked.incrementAndGet();
                    } else if (value.isDouble()) {
                        assertEquals(Double.parseDouble(attributeValues.get(attributeName)), value.asDouble(), 0.00000001);
                        checked.incrementAndGet();
                    } else if (value.isDecimal()) {
                        BigDecimal valueAsDecimal = value.asDecimal();
                        assertEquals(new BigDecimal(attributeValues.get(attributeName)).setScale(valueAsDecimal.scale(), RoundingMode.UNNECESSARY), valueAsDecimal);
                        checked.incrementAndGet();
                    } else if (value.isDate()) {
                        assertEquals(LocalDate.parse(attributeValues.get(attributeName)), value.asDate());
                        checked.incrementAndGet();
                    } else if (value.isDatetime()) {
                        assertEquals(LocalDateTime.parse(attributeValues.get(attributeName)), value.asDatetime());
                        checked.incrementAndGet();
                    } else if (value.isDatetimeTZ()) {
                        ZonedDateTime expected;
                        if (attributeName.contains("-off")) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSZ");
                            expected = OffsetDateTime.parse(attributeValues.get(attributeName), formatter).toZonedDateTime();
                        } else {
                            String[] expectedValue = attributeValues.get(attributeName).split(" ");
                            expected = LocalDateTime.parse(expectedValue[0]).atZone(ZoneId.of(expectedValue[1]));
                        }
                        assertEquals(expected, value.asDatetimeTZ());
                        checked.incrementAndGet();
                    } else if (value.isDuration()) {
                        String[] expectedValue = attributeValues.get(attributeName).split("T");
                        assertEquals(new Duration(java.time.Period.parse(expectedValue[0]), java.time.Duration.parse("PT" + expectedValue[1])), value.asDuration());
                        checked.incrementAndGet();
                    }
                    // TODO: Add structs
                });
                assertEquals(checked.get(), attributeValues.size()); // Make sure that every attribute is checked!
            }
        }, Transaction.Type.READ);
    }

    private void localhostTypeDBTX(Consumer<Transaction> fn, Transaction.Type type/*, Options options*/) {
        try (Transaction transaction = typedbDriver.transaction(DB_NAME, type/*, options*/)) {
            fn.accept(transaction);
        }
    }
}
