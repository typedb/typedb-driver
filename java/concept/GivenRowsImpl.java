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

package com.typedb.driver.concept;

import com.typedb.driver.api.concept.Concept;
import com.typedb.driver.api.concept.GivenRows;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.concept.value.ValueImpl;


import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.typedb.driver.jni.typedb_driver.given_rows_builder_new;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_set_index_to_concept;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_set_index_to_empty;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_set_variable_to_concept;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_set_variable_to_empty;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_commit_row;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_start_new_row;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_finish;
import static com.typedb.driver.jni.typedb_driver.given_rows_header_builder_new;
import static com.typedb.driver.jni.typedb_driver.given_rows_header_builder_finish;
import static com.typedb.driver.jni.typedb_driver.given_rows_header_builder_push;



public class GivenRowsImpl extends NativeObject<com.typedb.driver.jni.GivenRows> implements GivenRows {
    public GivenRowsImpl(com.typedb.driver.jni.GivenRows nativeObject) {
        super(nativeObject);
    }

    public static GivenRows of(List<String> variables, List<? extends List<? extends com.typedb.driver.api.concept.Concept>> rows) throws TypeDBDriverException {
        try{
            com.typedb.driver.jni.GivenRowsHeaderBuilder headerBuilder = given_rows_header_builder_new(variables.size());
            for (String variable : variables) {
                given_rows_header_builder_push(headerBuilder, variable);
            }
            com.typedb.driver.jni.GivenRowsHeader header = given_rows_header_builder_finish(headerBuilder.released());

            com.typedb.driver.jni.GivenRowsBuilder rowsBuilder = given_rows_builder_new(header, rows.size());
            for (List<? extends com.typedb.driver.api.concept.Concept> row : rows) {
                given_rows_builder_start_new_row(rowsBuilder);
                int colIndex = 0;
                for (com.typedb.driver.api.concept.Concept concept : row) {
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

    public static GivenRows of(List<? extends Map<String, ? extends Concept>> givenRows) throws TypeDBDriverException {
        try{
            Set<String> variables= new HashSet<>();
            givenRows.forEach(row -> variables.addAll(row.keySet()));
            com.typedb.driver.jni.GivenRowsHeaderBuilder headerBuilder = given_rows_header_builder_new(variables.size());
            for (String variable : variables) {
                given_rows_header_builder_push(headerBuilder, variable);
            }
            com.typedb.driver.jni.GivenRowsHeader header = given_rows_header_builder_finish(headerBuilder.released());
            com.typedb.driver.jni.GivenRowsBuilder rowsBuilder = given_rows_builder_new(header, givenRows.size());
            for (Map<String, ? extends com.typedb.driver.api.concept.Concept> row : givenRows) {
                given_rows_builder_start_new_row(rowsBuilder);
                for (Map.Entry<String, ? extends com.typedb.driver.api.concept.Concept> variableAndEntry : row.entrySet()) {
                    if (variableAndEntry.getValue() == null) {
                        given_rows_builder_set_variable_to_empty(rowsBuilder, variableAndEntry.getKey());
                    } else {
                        com.typedb.driver.api.concept.Concept concept = variableAndEntry.getValue();
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

    public static GivenRows ofObjects(List<String> variables, List<? extends List<Object>> rows) throws TypeDBDriverException {
        List<List<com.typedb.driver.api.concept.Concept>> converted = rows.stream()
                .map(row -> row.stream()
                        .map(value -> {
                            if (value == null) return null;
                            else if (value instanceof com.typedb.driver.api.concept.Concept) return (com.typedb.driver.api.concept.Concept) value;
                            else return (com.typedb.driver.api.concept.Concept) ValueImpl.tryConvertToValue(value);
                        })
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        return GivenRowsImpl.of(variables, converted);
    }

    public static GivenRows ofObjects(List<? extends Map<String, Object>> givenRows) throws TypeDBDriverException {
        List<Map<String, com.typedb.driver.api.concept.Concept>> converted = givenRows.stream()
                .map(row -> {
                    Map<String, com.typedb.driver.api.concept.Concept> convertedRow = new HashMap<>();
                    row.forEach((variable, value) -> {
                        if (value == null) {
                            convertedRow.put(variable, null);
                        } else if (value instanceof com.typedb.driver.api.concept.Concept) {
                            convertedRow.put(variable, (com.typedb.driver.api.concept.Concept) value);
                        } else {
                            convertedRow.put(variable, ValueImpl.tryConvertToValue(value));
                        }
                    });
                    return convertedRow;
                })
                .collect(Collectors.toList());
        return GivenRowsImpl.of(converted);
    }
}
