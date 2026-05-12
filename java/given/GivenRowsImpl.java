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

package com.typedb.driver.given;

import com.typedb.driver.api.concept.Concept;
import com.typedb.driver.api.given.GivenRows;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.concept.ConceptImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.typedb.driver.jni.typedb_driver.given_row_new;
import static com.typedb.driver.jni.typedb_driver.given_row_set_index_to_concept;
import static com.typedb.driver.jni.typedb_driver.given_row_set_index_to_empty;
import static com.typedb.driver.jni.typedb_driver.given_rows_new;
import static com.typedb.driver.jni.typedb_driver.given_rows_push;

public class GivenRowsImpl extends NativeObject<com.typedb.driver.jni.QueryGivenRows> implements GivenRows {

    private GivenRowsImpl(com.typedb.driver.jni.QueryGivenRows nativeRows) {
        super(nativeRows);
    }

    public static GivenRowsImpl build(Iterable<? extends Iterable<Optional<? extends Concept>>> rows) {
        List<List<Optional<? extends Concept>>> rowList = new ArrayList<>();
        for (Iterable<Optional<? extends Concept>> row : rows) {
            List<Optional<? extends Concept>> entries = new ArrayList<>();
            for (Optional<? extends Concept> entry : row) entries.add(entry);
            rowList.add(entries);
        }

        com.typedb.driver.jni.QueryGivenRows nativeRows = given_rows_new(rowList.size());
        for (List<Optional<? extends Concept>> entries : rowList) {
            com.typedb.driver.jni.QueryGivenRow nativeRow = given_row_new(entries.size());
            for (int i = 0; i < entries.size(); i++) {
                Optional<? extends Concept> entry = entries.get(i);
                if (entry.isEmpty()) {
                    given_row_set_index_to_empty(nativeRow, i);
                } else {
                    // released() transfers ownership to the Rust side
                    given_row_set_index_to_concept(nativeRow, i, ((ConceptImpl) entry.get()).nativeObject.released());
                }
            }
            // released() transfers ownership to the Rust side
            given_rows_push(nativeRows, nativeRow.released());
        }
        return new GivenRowsImpl(nativeRows);
    }
}
