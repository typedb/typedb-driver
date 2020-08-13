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
import grakn.client.concept.GraknConceptException;
import grakn.client.exception.GraknClientException;
import graql.lang.pattern.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * A type of Answer object that contains a Map of Concepts.
 */
public class ConceptMap implements Answer {

    private final Map<String, Concept<?>> map;
    private final boolean hasExplanation;
    private final Transaction tx;
    private final Pattern queryPattern;

    public ConceptMap(Map<String, Concept<?>> map, Pattern queryPattern, boolean hasExplanation, Transaction tx) {
        this.map = Collections.unmodifiableMap(map);
        this.queryPattern = queryPattern;
        this.hasExplanation = hasExplanation;
        this.tx = tx;
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
            throw GraknClientException.explanationNotPresent();
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
    public Map<String, Concept<?>> map() {
        return map;
    }


    public Collection<Concept<?>> concepts() {
        return map.values();
    }

    @CheckReturnValue
    public Concept<?> get(String variable) {
        Concept<?> Concept = map.get(variable);
        if (Concept == null) throw GraknConceptException.variableDoesNotExist(variable);
        return Concept;
    }

    @Override
    public String toString() {
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> "[" + e.getKey() + "/" + e.getValue().iid() + "]").collect(Collectors.joining());
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
