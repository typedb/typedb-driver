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

package com.typedb.driver.concept.answer;

import com.typedb.driver.api.QueryType;
import com.typedb.driver.api.answer.ConceptRow;
import com.typedb.driver.api.concept.Concept;
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.Validator;
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.concept.ConceptImpl;

import java.util.Optional;
import java.util.stream.Stream;

import static com.typedb.driver.jni.typedb_driver.concept_row_equals;
import static com.typedb.driver.jni.typedb_driver.concept_row_get;
import static com.typedb.driver.jni.typedb_driver.concept_row_get_column_names;
import static com.typedb.driver.jni.typedb_driver.concept_row_get_concepts;
import static com.typedb.driver.jni.typedb_driver.concept_row_get_index;
import static com.typedb.driver.jni.typedb_driver.concept_row_get_query_type;
import static com.typedb.driver.jni.typedb_driver.concept_row_to_string;

public class ConceptRowImpl extends NativeObject<com.typedb.driver.jni.ConceptRow> implements ConceptRow {
    private int hash = 0;

    public ConceptRowImpl(com.typedb.driver.jni.ConceptRow conceptRow) {
        super(conceptRow);
    }

    @Override
    public Stream<String> columnNames() {
        return new NativeIterator<>(concept_row_get_column_names(nativeObject)).stream();
    }

    @Override
    public QueryType getQueryType() {
        return QueryType.of(concept_row_get_query_type(nativeObject));
    }

    @Override
    public Optional<Concept> get(String columnName) throws TypeDBDriverException {
        Validator.requireNonNull(columnName, "columnName");
        try {
            com.typedb.driver.jni.Concept concept = concept_row_get(nativeObject, columnName);
            if (concept != null) {
                return Optional.of(ConceptImpl.of(concept));
            }
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Concept> getIndex(long columnIndex) throws TypeDBDriverException {
        Validator.requireNonNegative(columnIndex, "columnIndex");
        try {
            com.typedb.driver.jni.Concept concept = concept_row_get_index(nativeObject, columnIndex);
            if (concept != null) {
                return Optional.of(ConceptImpl.of(concept));
            }
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
        return Optional.empty();
    }

    @Override
    public Stream<Concept> concepts() {
        return new NativeIterator<>(concept_row_get_concepts(nativeObject)).stream().map(ConceptImpl::of);
    }

    @Override
    public String toString() {
        return concept_row_to_string(nativeObject);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConceptRowImpl that = (ConceptRowImpl) obj;
        return concept_row_equals(this.nativeObject, that.nativeObject);
    }

    @Override
    public int hashCode() {
        if (hash == 0) hash = computeHash();
        return hash;
    }

    private int computeHash() {
        return concepts().hashCode(); // TODO: Will be the same for rows with empty cells and without ones
    }
}
