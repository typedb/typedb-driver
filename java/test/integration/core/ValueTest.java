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

import com.typedb.driver.TypeDB;
import com.typedb.driver.api.Driver;
import com.typedb.driver.api.Transaction;
import com.typedb.driver.api.answer.ConceptRow;
import com.typedb.driver.api.answer.QueryAnswer;
import com.typedb.driver.api.concept.Concept;
import com.typedb.driver.api.concept.instance.Attribute;
import com.typedb.driver.api.concept.type.AttributeType;
import com.typedb.driver.api.concept.value.Value;
import com.typedb.driver.api.database.Database;
import com.typedb.driver.common.Duration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
public class ValueTest {
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
    public void attributes() {
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
                assertEquals(type.tryGetValueType().orElseGet(() -> "none"), attributeValueTypes.get(type.getLabel()));
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
                        assertEquals(Long.parseLong(attributeValues.get(attributeName)), value.getLong());
                        checked.incrementAndGet();
                    } else if (value.isString()) {
                        assertEquals(attributeValues.get(attributeName).substring(1, attributeValues.get(attributeName).length() - 1), value.getString());
                        checked.incrementAndGet();
                    } else if (value.isBoolean()) {
                        assertEquals(Boolean.parseBoolean(attributeValues.get(attributeName)), value.getBoolean());
                        checked.incrementAndGet();
                    } else if (value.isDouble()) {
                        assertEquals(Double.parseDouble(attributeValues.get(attributeName)), value.getDouble(), 0.00000001);
                        checked.incrementAndGet();
                    } else if (value.isDecimal()) {
                        BigDecimal valueAsDecimal = value.getDecimal();
                        assertEquals(new BigDecimal(attributeValues.get(attributeName)).setScale(valueAsDecimal.scale(), RoundingMode.UNNECESSARY), valueAsDecimal);
                        checked.incrementAndGet();
                    } else if (value.isDate()) {
                        assertEquals(LocalDate.parse(attributeValues.get(attributeName)), value.getDate());
                        checked.incrementAndGet();
                    } else if (value.isDatetime()) {
                        assertEquals(LocalDateTime.parse(attributeValues.get(attributeName)), value.getDatetime());
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
                        assertEquals(expected, value.getDatetimeTZ());
                        checked.incrementAndGet();
                    } else if (value.isDuration()) {
                        assertEquals(Duration.parse(attributeValues.get(attributeName)), value.getDuration());
                        checked.incrementAndGet();
                    }
                    // TODO: Add structs
                });
                assertEquals(checked.get(), attributeValues.size()); // Make sure that every attribute is checked!
            }
        }, Transaction.Type.READ);
    }

    @Test
    public void duration() {
        // parse examples do not fail
        Duration.parse("P1Y10M7DT15H44M5.00394892S");
        Duration.parse("P55W");

        localhostTypeDBTX(tx -> {
            tx.query("define attribute d, value duration;").resolve();
            tx.commit();
        }, Transaction.Type.SCHEMA);

        localhostTypeDBTX(tx -> {
            QueryAnswer answer = tx.query("insert $d P1Y isa d;").resolve();
            Duration typedbDuration = answer.asConceptRows().next().get("d").asAttribute().getDuration();
            assertEquals("P12M PT0S", typedbDuration.toString()); // we just reuse the java's classes
            assertEquals(12, typedbDuration.getMonths());
            assertEquals(0, typedbDuration.getDays());
            assertEquals(0, typedbDuration.getSeconds());
            assertEquals(0, typedbDuration.getNano());
            assertEquals(Duration.parse("P1Y0M0DT0H0M0S"), typedbDuration);
            assertEquals(Duration.parse("P0Y12M0DT0H0M0S"), typedbDuration);
            assertNotEquals(Duration.parse("P0Y1M0DT0H0M0S"), typedbDuration);
        }, Transaction.Type.WRITE);

        localhostTypeDBTX(tx -> {
            QueryAnswer answer = tx.query("insert $d P1M isa d;").resolve();
            Duration typedbDuration = answer.asConceptRows().next().get("d").asAttribute().getDuration();
            assertEquals("P1M PT0S", typedbDuration.toString()); // we just reuse the java's classes
            assertEquals(1, typedbDuration.getMonths());
            assertEquals(0, typedbDuration.getDays());
            assertEquals(0, typedbDuration.getSeconds());
            assertEquals(0, typedbDuration.getNano());
            assertEquals(Duration.parse("P0Y1M0DT0H0M0S"), typedbDuration);
            assertNotEquals(Duration.parse("P0Y0M31DT0H0M0S"), typedbDuration);
            assertNotEquals(Duration.parse("P0Y0M30DT0H0M0S"), typedbDuration);
            assertNotEquals(Duration.parse("P0Y0M29DT0H0M0S"), typedbDuration);
            assertNotEquals(Duration.parse("P0Y0M28DT0H0M0S"), typedbDuration);
        }, Transaction.Type.WRITE);

        localhostTypeDBTX(tx -> {
            QueryAnswer answer = tx.query("insert $d P1D isa d;").resolve();
            Duration typedbDuration = answer.asConceptRows().next().get("d").asAttribute().getDuration();
            assertEquals("P1D PT0S", typedbDuration.toString()); // we just reuse the java's classes
            assertEquals(0, typedbDuration.getMonths());
            assertEquals(1, typedbDuration.getDays());
            assertEquals(0, typedbDuration.getSeconds());
            assertEquals(0, typedbDuration.getNano());
            assertEquals(Duration.parse("P0Y0M1DT0H0M0S"), typedbDuration);
        }, Transaction.Type.WRITE);

        localhostTypeDBTX(tx -> {
            QueryAnswer answer = tx.query("insert $d P0DT1H isa d;").resolve();
            Duration typedbDuration = answer.asConceptRows().next().get("d").asAttribute().getDuration();
            assertEquals("P0D PT1H", typedbDuration.toString()); // we just reuse the java's classes
            assertEquals(0, typedbDuration.getMonths());
            assertEquals(0, typedbDuration.getDays());
            assertEquals(3600, typedbDuration.getSeconds());
            assertEquals(0, typedbDuration.getNano());
            assertEquals(Duration.parse("P0Y0M0DT1H0M0S"), typedbDuration);
        }, Transaction.Type.WRITE);

        localhostTypeDBTX(tx -> {
            QueryAnswer answer = tx.query("insert $d P0DT1S isa d;").resolve();
            Duration typedbDuration = answer.asConceptRows().next().get("d").asAttribute().getDuration();
            assertEquals("P0D PT1S", typedbDuration.toString()); // we just reuse the java's classes
            assertEquals(0, typedbDuration.getMonths());
            assertEquals(0, typedbDuration.getDays());
            assertEquals(1, typedbDuration.getSeconds());
            assertEquals(0, typedbDuration.getNano());
            assertEquals(Duration.parse("P0Y0M0DT0H0M1S"), typedbDuration);
        }, Transaction.Type.WRITE);

        localhostTypeDBTX(tx -> {
            QueryAnswer answer = tx.query("insert $d P0DT0.000000001S isa d;").resolve();
            Duration typedbDuration = answer.asConceptRows().next().get("d").asAttribute().getDuration();
            assertEquals("P0D PT0.000000001S", typedbDuration.toString()); // we just reuse the java's classes
            assertEquals(0, typedbDuration.getMonths());
            assertEquals(0, typedbDuration.getDays());
            assertEquals(0, typedbDuration.getSeconds());
            assertEquals(1, typedbDuration.getNano());
            assertEquals(Duration.parse("P0DT0.000000001S"), typedbDuration);
        }, Transaction.Type.WRITE);

        localhostTypeDBTX(tx -> {
            QueryAnswer answer = tx.query("insert $d P0DT0.0000001S isa d;").resolve();
            Duration typedbDuration = answer.asConceptRows().next().get("d").asAttribute().getDuration();
            assertEquals("P0D PT0.0000001S", typedbDuration.toString()); // we just reuse the java's classes
            assertEquals(0, typedbDuration.getMonths());
            assertEquals(0, typedbDuration.getDays());
            assertEquals(0, typedbDuration.getSeconds());
            assertEquals(100, typedbDuration.getNano());
            assertEquals(Duration.parse("P0DT0.0000001S"), typedbDuration);
        }, Transaction.Type.WRITE);

        localhostTypeDBTX(tx -> {
            QueryAnswer answer = tx.query("insert $d P0DT0S isa d;").resolve();
            Duration typedbDuration = answer.asConceptRows().next().get("d").asAttribute().getDuration();
            assertEquals("P0D PT0S", typedbDuration.toString()); // we just reuse the java's classes
            assertEquals(0, typedbDuration.getMonths());
            assertEquals(0, typedbDuration.getDays());
            assertEquals(0, typedbDuration.getSeconds());
            assertEquals(0, typedbDuration.getNano());
            assertEquals(Duration.parse("P0DT0S"), typedbDuration);
            assertEquals(Duration.parse("P0W"), typedbDuration);
        }, Transaction.Type.WRITE);

        localhostTypeDBTX(tx -> {
            QueryAnswer answer = tx.query("insert $d P7W isa d;").resolve();
            Duration typedbDuration = answer.asConceptRows().next().get("d").asAttribute().getDuration();
            assertEquals("P49D PT0S", typedbDuration.toString()); // we just reuse the java's classes
            assertEquals(0, typedbDuration.getMonths());
            assertEquals(49, typedbDuration.getDays());
            assertEquals(0, typedbDuration.getSeconds());
            assertEquals(0, typedbDuration.getNano());
            assertEquals(Duration.parse("P7W"), typedbDuration);
            assertEquals(Duration.parse("P0Y0M49DT0H0M0S"), typedbDuration);
        }, Transaction.Type.WRITE);

        localhostTypeDBTX(tx -> {
            QueryAnswer answer = tx.query("insert $d P999Y12M31DT24H59M59.999999999S isa d;").resolve();
            Duration typedbDuration = answer.asConceptRows().next().get("d").asAttribute().getDuration();
            assertEquals("P12000M31D PT24H59M59.999999999S", typedbDuration.toString()); // we just reuse the java's classes
            assertEquals(12000, typedbDuration.getMonths());
            assertEquals(31, typedbDuration.getDays());
            assertEquals(89999, typedbDuration.getSeconds());
            assertEquals(999999999, typedbDuration.getNano());
            assertEquals(Duration.parse("P999Y12M31DT24H59M59.999999999S"), typedbDuration);
        }, Transaction.Type.WRITE);
    }

    private void localhostTypeDBTX(Consumer<Transaction> fn, Transaction.Type type/*, Options options*/) {
        try (Transaction transaction = typedbDriver.transaction(DB_NAME, type/*, options*/)) {
            fn.accept(transaction);
        }
    }
}
