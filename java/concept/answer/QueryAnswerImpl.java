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

package com.vaticle.typedb.driver.concept.answer;

import com.vaticle.typedb.driver.api.answer.ConceptRowsStreamQueryAnswer;
import com.vaticle.typedb.driver.api.answer.QueryAnswer;
import com.vaticle.typedb.driver.common.NativeObject;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;

import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_answer_is_concept_rows_stream;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_answer_is_concept_trees_stream;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_answer_is_ok;

public abstract class QueryAnswerImpl extends NativeObject<com.vaticle.typedb.driver.jni.QueryAnswer> implements QueryAnswer {
    protected QueryAnswerImpl(com.vaticle.typedb.driver.jni.QueryAnswer answer) {
        super(answer);
    }

    public static QueryAnswerImpl of(com.vaticle.typedb.driver.jni.QueryAnswer concept) {
        if (query_answer_is_ok(concept)) return new OkQueryAnswerImpl(concept);
        else if (query_answer_is_concept_rows_stream(concept)) return new ConceptRowsStreamQueryAnswerImpl(concept);
        else if (query_answer_is_concept_trees_stream(concept)) return new ConceptTreesStreamQueryAnswerImpl(concept);
        throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
    }
}
