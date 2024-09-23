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

import com.vaticle.typedb.driver.api.answer.ConceptRow;
import com.vaticle.typedb.driver.api.answer.ConceptRowsStreamQueryAnswer;
import com.vaticle.typedb.driver.common.NativeIterator;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

import static com.vaticle.typedb.driver.jni.typedb_driver.query_answer_get_rows;

public class ConceptRowsStreamQueryAnswerImpl extends QueryAnswerImpl implements ConceptRowsStreamQueryAnswer {
    protected ConceptRowsStreamQueryAnswerImpl(com.vaticle.typedb.driver.jni.QueryAnswer answer) {
        super(answer);
    }

    @Override
    @CheckReturnValue
    public ConceptRowsStreamQueryAnswer asConceptRowsStream() {
        return this;
    }

    public Stream<ConceptRow> rows() {
        return new NativeIterator<>(query_answer_get_rows(nativeObject)).stream().map(ConceptRowImpl::new);
    }
}
