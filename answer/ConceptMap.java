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

package grakn.client.answer;

import grakn.client.Grakn.Transaction;
import grakn.client.concept.Concept;
import grakn.client.common.exception.GraknClientException;
import grakn.protocol.AnswerProto;
import graql.lang.Graql;
import graql.lang.pattern.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static grakn.client.common.exception.ErrorMessage.Query.NO_EXPLANATION;
import static grakn.client.common.exception.ErrorMessage.Query.VARIABLE_DOES_NOT_EXIST;


/**
 * A type of Answer object that contains a Map of Concepts.
 */
public class ConceptMap implements Answer {

    private final Map<String, Concept> map;
    private final boolean hasExplanation;
    private final Transaction tx;
    private final Pattern queryPattern;

    public ConceptMap(Map<String, Concept> map, Pattern queryPattern, boolean hasExplanation, Transaction tx) {
        this.map = Collections.unmodifiableMap(map);
        this.queryPattern = queryPattern;
        this.hasExplanation = hasExplanation;
        this.tx = tx;
    }

    public static ConceptMap of(final Transaction tx, final AnswerProto.ConceptMap res) {
        final Map<String, Concept> variableMap = new HashMap<>();
        res.getMapMap().forEach((resVar, resConcept) -> variableMap.put(resVar, Concept.Local.of(resConcept)));
        boolean hasExplanation = res.getHasExplanation();
        Pattern queryPattern = res.getPattern().equals("") ? null : Graql.parsePattern(res.getPattern());
        return new ConceptMap(Collections.unmodifiableMap(variableMap), queryPattern, hasExplanation, tx);
    }

    /**
     * @return all explanations taking part in the derivation of this answer
     */
    @Nullable
    @CheckReturnValue
    public Set<Explanation> explanations() {
        if (this.explanation() == null) return Collections.emptySet();
        Set<Explanation> explanations = new HashSet<>();
        explanations.add(this.explanation());
        this.explanation().getAnswers().forEach(conceptMap -> {
            Set<Explanation> subexplanations = conceptMap.explanations();
            if (subexplanations != null) {
                explanations.addAll(subexplanations);
            }
        });
        return explanations;
    }

    @CheckReturnValue
    public Explanation explanation() {
        if (hasExplanation) {
            return tx.getExplanation(this);
        } else {
            throw new GraknClientException(NO_EXPLANATION);
        }
    }

    @CheckReturnValue
    public Pattern queryPattern() {
        return queryPattern;
    }

    @Override
    public boolean hasExplanation() {
        return hasExplanation;
    }

    @CheckReturnValue
    public Map<String, Concept> map() {
        return map;
    }

    @CheckReturnValue
    public Collection<Concept> concepts() {
        return map.values();
    }

    @CheckReturnValue
    public Concept get(String variable) {
        Concept concept = map.get(variable);
        if (concept == null) throw new GraknClientException(VARIABLE_DOES_NOT_EXIST.message(variable));
        return concept;
    }

    @Override
    public String toString() {
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> "[" + e.getKey() + "/" + e.getValue() + "]").collect(Collectors.joining());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConceptMap a2 = (ConceptMap) obj;
        return map.equals(a2.map);
    }

    @Override
    public int hashCode() { return map.hashCode();}
}
