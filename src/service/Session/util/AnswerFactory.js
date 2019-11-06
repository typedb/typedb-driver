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

/**
 * Factory for Answer and Explanation objects
 * @param {Object} conceptFactory
 */
function AnswerFactory(conceptFactory, txService) {
    this.conceptFactory = conceptFactory;
    this.txService = txService;
}

AnswerFactory.prototype.createAnswer = function (grpcAnswer) {
    if (grpcAnswer.hasConceptmap()) return this.createConceptmap(grpcAnswer.getConceptmap());
    if (grpcAnswer.hasAnswergroup()) return this.createAnswergroup(grpcAnswer.getAnswergroup());
    if (grpcAnswer.hasConceptlist()) return this.createConceptlist(grpcAnswer.getConceptlist());
    if (grpcAnswer.hasConceptset()) return this.createConceptset(grpcAnswer.getConceptset());
    if (grpcAnswer.hasConceptsetmeasure()) return this.createConceptsetmeasure(grpcAnswer.getConceptsetmeasure());
    if (grpcAnswer.hasValue()) return this.createValue(grpcAnswer.getValue());
}

AnswerFactory.prototype.buildExplanation = function (grpcExplanation) {
    const grpcListOfConceptMaps = grpcExplanation.getExplanationRes().getExplanationList();
    const nativeListOfConceptMaps = grpcListOfConceptMaps.map((grpcConceptMap) => this.createConceptmap(grpcConceptMap));

    return {
        getAnswers: () => nativeListOfConceptMaps,
    }
}

AnswerFactory.prototype.createConceptmap = function (answer) {
    const answerMap = new Map();
    
    answer.getMapMap().forEach((grpcConcept, key) => {
        answerMap.set(key, this.conceptFactory.createConcept(grpcConcept));
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
            } else {
                throw "Explanation not available on concept map";
            }
        },
    };
}
AnswerFactory.prototype.createValue = function(answer){
    return {
        number: () => Number(answer.getNumber().getValue()),
    }
}
AnswerFactory.prototype.createConceptlist = function(answer){
    const list = answer.getList();
    return {
        list: () => list.getIdsList(),
    }
}
AnswerFactory.prototype.createConceptset = function(answer){
    const set = answer.getSet();
    return {
        set: () => new Set(set.getIdsList()),
    }
}
AnswerFactory.prototype.createConceptsetmeasure = function(answer){
    return {
        measurement: () => Number(answer.getMeasurement().getValue()),
        set: () => new Set(answer.getSet().getIdsList()),
    }
}
AnswerFactory.prototype.createAnswergroup = function(answer){
    return {
        owner: () => this.conceptFactory.createConcept(answer.getOwner()),
        answers: () => answer.getAnswersList().map(a => this.createAnswer(a)),
    }
}

module.exports = AnswerFactory;