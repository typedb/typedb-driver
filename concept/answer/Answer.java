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
import grakn.client.common.exception.GraknException;
import grakn.protocol.AnswerProto;

import javax.annotation.CheckReturnValue;

import static grakn.client.common.exception.ErrorMessage.Protocol.REQUIRED_FIELD_NOT_SET;
import static grakn.client.common.exception.ErrorMessage.Protocol.UNRECOGNISED_FIELD;

/**
 * An object that contains the answer of every Graql Query.
 */
public interface Answer {

    /**
     * Whether this answer has an Explanation that can be retrieved
     */
    @CheckReturnValue
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
                throw new GraknException(REQUIRED_FIELD_NOT_SET.message(AnswerProto.Answer.AnswerCase.class.getCanonicalName()));
            default:
                throw new GraknException(UNRECOGNISED_FIELD.message(AnswerProto.Answer.AnswerCase.class.getCanonicalName(), res.getAnswerCase()));
        }
    }
}
