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

package com.typedb.driver;

import com.typedb.driver.api.concept.GivenRows;
import com.typedb.driver.api.concept.Concept;
import com.typedb.driver.api.concept.value.Value;
import com.typedb.driver.common.Duration;
import com.typedb.driver.common.exception.ErrorMessage;
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.concept.ConceptImpl;
import com.typedb.driver.concept.value.ValueImpl;
import com.typedb.driver.concept.GivenRowsImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.typedb.driver.jni.typedb_driver.concept_new_boolean;
import static com.typedb.driver.jni.typedb_driver.concept_new_date_from_seconds;
import static com.typedb.driver.jni.typedb_driver.concept_new_datetime;
import static com.typedb.driver.jni.typedb_driver.concept_new_datetime_tz_iana;
import static com.typedb.driver.jni.typedb_driver.concept_new_datetime_tz_offset;
import static com.typedb.driver.jni.typedb_driver.concept_new_decimal;
import static com.typedb.driver.jni.typedb_driver.concept_new_double;
import static com.typedb.driver.jni.typedb_driver.concept_new_duration;
import static com.typedb.driver.jni.typedb_driver.concept_new_integer;
import static com.typedb.driver.jni.typedb_driver.concept_new_string;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_new;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_finish;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_commit_row;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_start_new_row;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_set_index_to_concept;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_set_index_to_empty;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_set_variable_to_concept;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_set_variable_to_empty;
import static com.typedb.driver.jni.typedb_driver.given_rows_header_builder_new;
import static com.typedb.driver.jni.typedb_driver.given_rows_header_builder_push;
import static com.typedb.driver.jni.typedb_driver.given_rows_header_builder_finish;

/**
 * Factory class to instantiate new <code>Value</code> concepts from raw values.
 * These can then be used in the <code>GivenRows</code> of a query.
 */
public class ConceptFactory {

    /**
     * Constructs a <code>GivenRows</code> instance for use as inputs to queries.
     *
     * @param variables The variables describing the content of the givenRows.
     * @param rows Input rows for the query; each inner iterable is one row, {@code Optional.empty()} entries represent empty variables.
     */
    public static GivenRows buildGivenRowsFrom(List<String> variables, List<? extends List<? extends Concept>> rows) throws TypeDBDriverException {
        try{
            com.typedb.driver.jni.GivenRowsHeaderBuilder headerBuilder = given_rows_header_builder_new(variables.size());
            for (String variable : variables) {
                given_rows_header_builder_push(headerBuilder, variable);
            }
            com.typedb.driver.jni.GivenRowsHeader header = given_rows_header_builder_finish(headerBuilder.released());

            com.typedb.driver.jni.GivenRowsBuilder rowsBuilder = given_rows_builder_new(header, rows.size());
            for (List<? extends Concept> row : rows) {
                given_rows_builder_start_new_row(rowsBuilder);
                int colIndex = 0;
                for (Concept concept : row) {
                    if (concept == null) {
                        given_rows_builder_set_index_to_empty(rowsBuilder, colIndex);
                    } else {
                        given_rows_builder_set_index_to_concept(rowsBuilder, colIndex, ((ConceptImpl) concept).nativeObject);
                    }
                    colIndex += 1;
                }
                given_rows_builder_commit_row(rowsBuilder);
            }
            return new GivenRowsImpl(given_rows_builder_finish(rowsBuilder.released()));
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    /**
     * Constructs a <code>GivenRows</code> instance from the dictionary for use as inputs to queries.
     *
     * @param givenRows A list of input rows for the query. Each row is a dictionary mapping a variable to its value.
     */
    public static GivenRows buildGivenRowsFrom(List<? extends Map<String, ? extends Concept>> givenRows) throws TypeDBDriverException {
        try{
            Set<String> variables= new HashSet<>();
            givenRows.forEach(row -> variables.addAll(row.keySet()));
            com.typedb.driver.jni.GivenRowsHeaderBuilder headerBuilder = given_rows_header_builder_new(variables.size());
            for (String variable : variables) {
                given_rows_header_builder_push(headerBuilder, variable);
            }
            com.typedb.driver.jni.GivenRowsHeader header = given_rows_header_builder_finish(headerBuilder.released());
            com.typedb.driver.jni.GivenRowsBuilder rowsBuilder = given_rows_builder_new(header, givenRows.size());
            for (Map<String, ? extends Concept> row : givenRows) {
                given_rows_builder_start_new_row(rowsBuilder);
                for (Map.Entry<String, ? extends Concept> variableAndEntry : row.entrySet()) {
                    if (variableAndEntry.getValue() == null) {
                        given_rows_builder_set_variable_to_empty(rowsBuilder, variableAndEntry.getKey());
                    } else {
                        Concept concept = variableAndEntry.getValue();
                        given_rows_builder_set_variable_to_concept(rowsBuilder, variableAndEntry.getKey(), ((ConceptImpl) concept).nativeObject);
                    }
                }
                given_rows_builder_commit_row(rowsBuilder);
            }
            return new GivenRowsImpl(given_rows_builder_finish(rowsBuilder.released()));
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    /**
     * Constructs a <code>GivenRows</code> instance from a list of rows given as plain Java objects.
     * Each map entry value may be a {@link Concept} (used directly) or a raw host-language value
     * which is converted via {@link #tryConvertToValue}.
     *
     * @param givenRows A list of input rows; each row maps variable names to concepts or raw values.
     */
    public static GivenRows buildGivenRowsFromObjects(List<? extends Map<String, Object>> givenRows) throws TypeDBDriverException {
        List<Map<String, Concept>> converted = givenRows.stream()
                .map(row -> {
                    Map<String, Concept> convertedRow = new HashMap<>();
                    row.forEach((variable, value) -> {
                        if (value == null) {
                            convertedRow.put(variable, null);
                        } else if (value instanceof Concept) {
                            convertedRow.put(variable, (Concept) value);
                        } else {
                            convertedRow.put(variable, tryConvertToValue(value));
                        }
                    });
                    return convertedRow;
                })
                .collect(Collectors.toList());
        return buildGivenRowsFrom(converted);
    }

    /**
     * Converts a raw host-language object to a {@link Value} concept.
     * Accepted types: {@code Boolean}, {@code Long}, {@code Integer}, {@code Double}, {@code Float},
     * {@code BigDecimal}, {@code String}, {@code LocalDate}, {@code LocalDateTime}, {@code ZonedDateTime}, {@code Duration}.
     *
     * @throws TypeDBDriverException if the object's type is not supported
     */
    public static Value tryConvertToValue(Object value) {
        if (value instanceof Boolean) return newBoolean((Boolean) value);
        if (value instanceof Long) return newInteger((Long) value);
        if (value instanceof Integer) return newInteger((Integer) value);
        if (value instanceof Double) return newDouble((Double) value);
        if (value instanceof Float) return newDouble(((Float) value).doubleValue());
        if (value instanceof BigDecimal) return newDecimal((BigDecimal) value);
        if (value instanceof String) return newString((String) value);
        if (value instanceof LocalDate) return newDate((LocalDate) value);
        if (value instanceof LocalDateTime) return newDatetime((LocalDateTime) value);
        if (value instanceof ZonedDateTime) return newDatetimeTz((ZonedDateTime) value);
        if (value instanceof Duration) return newDuration((Duration) value);
        throw new TypeDBDriverException(ErrorMessage.Concept.UNSUPPORTED_VALUE_CONVERSION, value.getClass().getName());
    }

    /** Creates a new {@code Value} wrapping the specified {@code boolean} value. */
    public static Value newBoolean(boolean value) {
        return new ValueImpl(concept_new_boolean(value));
    }

    /** Creates a new {@code Value} wrapping the specified {@code long} value. */
    public static Value newInteger(long value) {
        return new ValueImpl(concept_new_integer(value));
    }

    /** Creates a new {@code Value} wrapping the specified {@code double} value. */
    public static Value newDouble(double value) {
        return new ValueImpl(concept_new_double(value));
    }

    /** Creates a new {@code Value} wrapping the specified {@code BigDecimal} value. */
    public static Value newDecimal(BigDecimal value) {
        long integerPart = value.setScale(0, RoundingMode.FLOOR).longValue();
        BigDecimal fractional = value.subtract(new BigDecimal(integerPart)).setScale(Concept.DECIMAL_SCALE, RoundingMode.UNNECESSARY);
        BigInteger fractionalPart = fractional.movePointRight(Concept.DECIMAL_SCALE).toBigInteger();
        return new ValueImpl(concept_new_decimal(integerPart, fractionalPart));
    }

    /** Creates a new {@code Value} wrapping the specified {@code String} value. */
    public static Value newString(String value) {
        return new ValueImpl(concept_new_string(value));
    }

    /** Creates a new {@code Value} wrapping the specified {@code LocalDate} value. */
    public static Value newDate(LocalDate value) {
        long epochSeconds = value.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        return new ValueImpl(concept_new_date_from_seconds(epochSeconds));
    }

    /** Creates a new {@code Value} wrapping the specified {@code LocalDateTime} value. */
    public static Value newDatetime(LocalDateTime value) {
        long seconds = value.toEpochSecond(ZoneOffset.UTC);
        int nanos = value.getNano();
        return new ValueImpl(concept_new_datetime(seconds, nanos));
    }

    /** Creates a new {@code Value} wrapping the specified {@code ZonedDateTime} value. */
    public static Value newDatetimeTz(ZonedDateTime value) {
        long seconds = value.toInstant().getEpochSecond();
        int nanos = value.getNano();
        if (value.getZone() instanceof ZoneOffset) {
            int offsetSeconds = ((ZoneOffset) value.getZone()).getTotalSeconds();
            return new ValueImpl(concept_new_datetime_tz_offset(seconds, nanos, offsetSeconds));
        } else {
            String zoneName = value.getZone().getId();
            return new ValueImpl(concept_new_datetime_tz_iana(seconds, nanos, zoneName));
        }
    }

    /** Creates a new {@code Value} wrapping the specified {@code Duration} value. */
    public static Value newDuration(Duration value) {
        int months = value.getMonths();
        int days = value.getDays();
        long nanos = value.getTimePart().toNanos();
        return new ValueImpl(concept_new_duration(months, days, BigInteger.valueOf(nanos)));
    }
}
