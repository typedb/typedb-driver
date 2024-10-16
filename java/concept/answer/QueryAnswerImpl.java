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

import com.typedb.driver.api.answer.QueryAnswer;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.exception.TypeDBDriverException;

import static com.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.typedb.driver.jni.typedb_driver.query_answer_is_concept_row_stream;
import static com.typedb.driver.jni.typedb_driver.query_answer_is_concept_document_stream;
import static com.typedb.driver.jni.typedb_driver.query_answer_is_ok;

public abstract class QueryAnswerImpl extends NativeObject<com.typedb.driver.jni.QueryAnswer> implements QueryAnswer {
    protected QueryAnswerImpl(com.typedb.driver.jni.QueryAnswer answer) {
        super(answer);
    }

    public static QueryAnswerImpl of(com.typedb.driver.jni.QueryAnswer concept) {
        if (query_answer_is_ok(concept)) return new OkQueryAnswerImpl(concept);
        else if (query_answer_is_concept_row_stream(concept)) return new ConceptRowIteratorImpl(concept);
        else if (query_answer_is_concept_document_stream(concept)) return new ConceptTreeIteratorImpl(concept);
        throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
    }
}
