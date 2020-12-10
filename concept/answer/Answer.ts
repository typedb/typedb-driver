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

import AnswerProto from "grakn-protocol/protobuf/answer_pb";
import { AnswerGroup, ConceptMap, ErrorMessage, Grakn, GraknClientError, } from "../../dependencies_internal";
import Transaction = Grakn.Transaction;

export type Answer = AnswerGroup<Answer> | ConceptMap;

export namespace Answer {
    export function of(transaction: Transaction, res: AnswerProto.Answer): Answer {
        switch (res.getAnswerCase()) {
            case AnswerProto.Answer.AnswerCase.ANSWER_GROUP: return AnswerGroup.of(transaction, res.getAnswerGroup());
            case AnswerProto.Answer.AnswerCase.CONCEPT_MAP: return ConceptMap.of(res.getConceptMap());
            case AnswerProto.Answer.AnswerCase.ANSWER_NOT_SET: throw new GraknClientError(ErrorMessage.Query.MISSING_ANSWER.message(AnswerProto.Answer))
            case AnswerProto.Answer.AnswerCase.NUMBER: //FALL THROUGH
            default: throw new GraknClientError(ErrorMessage.Query.BAD_ANSWER_TYPE.message(res.getAnswerCase()))
        }
    }
}

