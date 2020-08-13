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

package grakn.client.rpc;

import grakn.client.Grakn.Transaction;
import grakn.client.answer.Void;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.type.Rule;
import grakn.protocol.AnswerProto;
import grakn.protocol.ConceptProto;
import graql.lang.Graql;
import graql.lang.pattern.Pattern;
import grakn.client.answer.Answer;
import grakn.client.answer.AnswerGroup;
import grakn.client.answer.ConceptList;
import grakn.client.answer.ConceptMap;
import grakn.client.answer.ConceptSet;
import grakn.client.answer.ConceptSetMeasure;
import grakn.client.answer.Numeric;
import grakn.client.answer.Explanation;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * An RPC Response reader class to convert AnswerProto messages into Graql Answers.
 */
public class ResponseReader {

    @SuppressWarnings("unchecked")
    public static <T extends Answer> T answer(AnswerProto.Answer res, Transaction tx) {
        switch (res.getAnswerCase()) {
            case ANSWERGROUP:
                return (T) answerGroup(res.getAnswerGroup(), tx);
            case CONCEPTMAP:
                return (T) conceptMap(res.getConceptMap(), tx);
            case CONCEPTLIST:
                return (T) conceptList(res.getConceptList());
            case CONCEPTSET:
                return (T) conceptSet(res.getConceptSet());
            case CONCEPTSETMEASURE:
                return (T) conceptSetMeasure(res.getConceptSetMeasure());
            case VALUE:
                return (T) value(res.getValue());
            case VOID:
                return (T) voidAnswer(res.getVoid());
            default:
            case ANSWER_NOT_SET:
                throw new IllegalArgumentException("Unexpected " + res);
        }
    }

    public static Explanation explanation(AnswerProto.Explanation.Res res, Transaction tx) {
        List<ConceptMap> answers = new ArrayList<>();
        res.getExplanationList().forEach(explanationMap -> answers.add(conceptMap(explanationMap, tx)));
        ConceptProto.Concept ruleProto = res.getRule();
        Rule.Remote rule = res.hasRule() ? Concept.Remote.of(tx, ruleProto).asRule() : null;
        return new Explanation(answers, rule);
    }

    private static AnswerGroup<?> answerGroup(AnswerProto.AnswerGroup res, Transaction tx) {
        return new AnswerGroup<>(
                Concept.Remote.of(tx, res.getOwner()),
                res.getAnswersList().stream().map(answer -> answer(answer, tx)).collect(toList())
        );
    }

    private static ConceptMap conceptMap(AnswerProto.ConceptMap res, Transaction tx) {
        Map<String, Concept<?>> variableMap = new HashMap<>();
        res.getMapMap().forEach(
                (resVar, resConcept) -> variableMap.put(resVar, Concept.Local.of(resConcept))
        );
        boolean hasExplanation = res.getHasExplanation();
        Pattern queryPattern = res.getPattern().equals("") ? null : Graql.parsePattern(res.getPattern());
        return new ConceptMap(Collections.unmodifiableMap(variableMap), queryPattern, hasExplanation, tx);
    }

    private static ConceptList conceptList(AnswerProto.ConceptList res) {
        return new ConceptList(res.getIidsList().stream().map(ConceptIID::of).collect(toList()));
    }

    private static ConceptSet conceptSet(AnswerProto.ConceptSet res) {
        return new ConceptSet(res.getIidsList().stream().map(ConceptIID::of).collect(toSet()));
    }

    private static ConceptSetMeasure conceptSetMeasure(AnswerProto.ConceptSetMeasure res) {
        return new ConceptSetMeasure(
                res.getIidsList().stream().map(ConceptIID::of).collect(toSet()),
                number(res.getMeasurement())
        );
    }

    private static Numeric value(AnswerProto.Value res) {
        return new Numeric(number(res.getNumber()));
    }

    private static Void voidAnswer(AnswerProto.Void res) {
        return new Void(res.getMessage());
    }

    private static Number number(AnswerProto.Number res) {
        try {
            return NumberFormat.getInstance().parse(res.getValue());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
