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

import grakn.client.GraknClient;
import grakn.client.concept.ConceptImpl;
import grakn.protocol.session.AnswerProto;
import graql.lang.Graql;
import graql.lang.pattern.Pattern;
import graql.lang.statement.Variable;
import grakn.client.answer.Answer;
import grakn.client.answer.AnswerGroup;
import grakn.client.answer.ConceptList;
import grakn.client.answer.ConceptMap;
import grakn.client.answer.ConceptSet;
import grakn.client.answer.ConceptSetMeasure;
import grakn.client.answer.Numeric;
import grakn.client.answer.Explanation;

import grakn.client.concept.api.ConceptId;
import grakn.client.concept.api.Concept;

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

    public static Answer answer(AnswerProto.Answer res, GraknClient.Transaction tx) {
        switch (res.getAnswerCase()) {
            case ANSWERGROUP:
                return answerGroup(res.getAnswerGroup(), tx);
            case CONCEPTMAP:
                return conceptMap(res.getConceptMap(), tx);
            case CONCEPTLIST:
                return conceptList(res.getConceptList());
            case CONCEPTSET:
                return conceptSet(res.getConceptSet());
            case CONCEPTSETMEASURE:
                return conceptSetMeasure(res.getConceptSetMeasure());
            case VALUE:
                return value(res.getValue());
            default:
            case ANSWER_NOT_SET:
                throw new IllegalArgumentException("Unexpected " + res);
        }
    }

    private static Explanation explanation(AnswerProto.Explanation res, GraknClient.Transaction tx) {
        List<ConceptMap> answers = new ArrayList<>();
        res.getAnswersList().forEach(answer -> answers.add(conceptMap(answer, tx)));
        Pattern explanation = res.getPattern().isEmpty() ? null : Graql.parsePattern(res.getPattern());
        return new Explanation(explanation, answers);
    }

    private static AnswerGroup<?> answerGroup(AnswerProto.AnswerGroup res, GraknClient.Transaction tx) {
        return new AnswerGroup<>(
                ConceptImpl.of(res.getOwner(), tx),
                res.getAnswersList().stream().map(answer -> answer(answer, tx)).collect(toList())
        );
    }

    private static ConceptMap conceptMap(AnswerProto.ConceptMap res, GraknClient.Transaction tx) {
        Map<Variable, Concept> answers = new HashMap<>();
        res.getMapMap().forEach(
                (resVar, resConcept) -> answers.put(new Variable(resVar), ConceptImpl.of(resConcept, tx))
        );
        return new ConceptMap(Collections.unmodifiableMap(answers), explanation(res.getExplanation(), tx));
    }

    private static ConceptList conceptList(AnswerProto.ConceptList res) {
        return new ConceptList(res.getList().getIdsList().stream().map(ConceptId::of).collect(toList()));
    }

    private static ConceptSet conceptSet(AnswerProto.ConceptSet res) {
        return new ConceptSet(res.getSet().getIdsList().stream().map(ConceptId::of).collect(toSet()));
    }

    private static ConceptSetMeasure conceptSetMeasure(AnswerProto.ConceptSetMeasure res) {
        return new ConceptSetMeasure(
                res.getSet().getIdsList().stream().map(ConceptId::of).collect(toSet()),
                number(res.getMeasurement())
        );
    }

    private static Numeric value(AnswerProto.Value res) {
        return new Numeric(number(res.getNumber()));
    }

    private static Number number(AnswerProto.Number res) {
        try {
            return NumberFormat.getInstance().parse(res.getValue());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
