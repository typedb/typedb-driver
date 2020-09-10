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

package grakn.client.concept.answer;

import grakn.client.Grakn.Transaction;
import grakn.client.common.exception.GraknClientException;
import grakn.protocol.AnswerProto;

import static grakn.client.common.exception.ErrorMessage.Query.BAD_ANSWER_TYPE;
import static grakn.client.common.exception.ErrorMessage.Query.MISSING_ANSWER;
import static grakn.common.util.Objects.className;

public interface Answer {

    boolean hasExplanation();

    static Answer of(final Transaction tx, final AnswerProto.Answer res) {
        switch (res.getAnswerCase()) {
            case ANSWERGROUP:
                return AnswerGroup.of(tx, res.getAnswerGroup());
            case CONCEPTMAP:
                return ConceptMap.of(tx, res.getConceptMap());
            case CONCEPTLIST:
                return ConceptList.of(res.getConceptList());
            case CONCEPTSET:
                return ConceptSet.of(res.getConceptSet());
            case CONCEPTSETMEASURE:
                return ConceptSetMeasure.of(res.getConceptSetMeasure());
            case VALUE:
                return Numeric.of(res.getValue());
            case VOID:
                return Void.of(res.getVoid());
            case ANSWER_NOT_SET:
                throw new GraknClientException(MISSING_ANSWER.message(className(AnswerProto.Answer.AnswerCase.class)));
            default:
                throw new GraknClientException(BAD_ANSWER_TYPE.message(res.getAnswerCase()));
        }
    }
}
