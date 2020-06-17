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

 const ConceptFactory = require('../concept/ConceptFactory');

/**
 * Factory for Answer and Explanation objects
 * @param {Object} conceptFactory
 */
class AnswerFactory {
    constructor(conceptFactory, txService) {
        this.conceptFactory = conceptFactory;
        this.txService = txService;
    }
    createAnswer(grpcAnswer) {
        if (grpcAnswer.hasConceptmap())
            return this.createConceptmap(grpcAnswer.getConceptmap());
        if (grpcAnswer.hasAnswergroup())
            return this.createAnswergroup(grpcAnswer.getAnswergroup());
        if (grpcAnswer.hasConceptlist())
            return this.createConceptlist(grpcAnswer.getConceptlist());
        if (grpcAnswer.hasConceptset())
            return this.createConceptset(grpcAnswer.getConceptset());
        if (grpcAnswer.hasConceptsetmeasure())
            return this.createConceptsetmeasure(grpcAnswer.getConceptsetmeasure());
        if (grpcAnswer.hasValue())
            return this.createValue(grpcAnswer.getValue());
        if (grpcAnswer.hasVoid())
            return this.createVoid(grpcAnswer.getVoid());
    }
    buildExplanation(grpcExplanation) {
        const res = grpcExplanation.getExplanationRes()
        const grpcListOfConceptMaps = res.getExplanationList();
        const rule = res.hasRule() ? this.conceptFactory.createConcept(res.getRule()) : null;
        const nativeListOfConceptMaps = grpcListOfConceptMaps.map((grpcConceptMap) => this.createConceptmap(grpcConceptMap));
        return {
            getAnswers: () => nativeListOfConceptMaps,
            getRule: () => rule
        };
    }
    createConceptmap(answer) {
        const answerMap = new Map();
        answer.getMapMap().forEach((grpcConcept, key) => {
            answerMap.set(key, ConceptFactory.createLocalConcept(grpcConcept));
        });
        return {
            map: () => answerMap,
            get: (v) => answerMap.get(v),
            hasExplanation: () => answer.getHasexplanation(),
            queryPattern: () => answer.getPattern(),
            explanation: async () => {
                if (answer.getHasexplanation()) {
                    const grpcExplanation = await this.txService.explanation(answer);
                    return this.buildExplanation(grpcExplanation);
                }
                else {
                    throw "Explanation not available on concept map";
                }
            },
        };
    }
    createValue(answer) {
        return {
            number: () => Number(answer.getNumber().getValue()),
        };
    }
    createConceptlist(answer) {
        const list = answer.getList();
        return {
            list: () => list.getIdsList(),
        };
    }
    createConceptset(answer) {
        const set = answer.getSet();
        return {
            set: () => new Set(set.getIdsList()),
        };
    }
    createConceptsetmeasure(answer) {
        return {
            measurement: () => Number(answer.getMeasurement().getValue()),
            set: () => new Set(answer.getSet().getIdsList()),
        };
    }
    createAnswergroup(answer) {
        return {
            owner: () => this.conceptFactory.createConcept(answer.getOwner()),
            answers: () => answer.getAnswersList().map(a => this.createAnswer(a)),
        };
    }
    createVoid(answer) {
        return {
            message: () => answer.getMessage()
        };
    }
}

module.exports = AnswerFactory;