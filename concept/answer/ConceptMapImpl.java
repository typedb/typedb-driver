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

import grakn.client.api.answer.ConceptMap;
import grakn.client.api.concept.Concept;
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.ConceptImpl;
import grakn.client.concept.thing.ThingImpl;
import grakn.client.concept.type.TypeImpl;
import grakn.protocol.AnswerProto;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static grakn.client.common.exception.ErrorMessage.Query.VARIABLE_DOES_NOT_EXIST;

public class ConceptMapImpl implements ConceptMap {

    private final Map<String, Concept> map;
    private final Set<Explainable> explainables;

    public ConceptMapImpl(Map<String, Concept> map, Set<Explainable> explainables) {
        this.map = Collections.unmodifiableMap(map);
        this.explainables = Collections.unmodifiableSet(explainables);
    }

    public static ConceptMap of(AnswerProto.ConceptMap res) {
        Map<String, Concept> variableMap = new HashMap<>();
        res.getMapMap().forEach((resVar, resConcept) -> {
            Concept concept = ConceptImpl.of(resConcept);
            variableMap.put(resVar, concept);
        });

        Set<Explainable> explainables = new HashSet<>();
        res.getExplainablesList().forEach((explainableProto) -> {
            explainables.add(new ExplainableImpl(explainableProto.getConjunction(), explainableProto.getId()));
        });

        return new ConceptMapImpl(variableMap, explainables);
        res.getMapMap().forEach((resVar, resConcept) -> variableMap.put(resVar, ConceptImpl.of(resConcept)));
        return new ConceptMapImpl(Collections.unmodifiableMap(variableMap));
    }

    @Override
    public Map<String, Concept> map() {
        return map;
    }

    @Override
    public Collection<Concept> concepts() {
        return map.values();
    }

    @Override
    public Concept get(String variable) {
        Concept concept = map.get(variable);
        if (concept == null) throw new GraknClientException(VARIABLE_DOES_NOT_EXIST, variable);
        return concept;
    }

    @Override
    public Set<Explainable> explainables() {
        return explainables;
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
        ConceptMapImpl a2 = (ConceptMapImpl) obj;
        return map.equals(a2.map);
    }

    @Override
    public int hashCode() { return map.hashCode();}

    static class ExplainableImpl implements Explainable {

        private final String conjunction;
        private final long id;

        ExplainableImpl(String conjunction, long id) {
            this.conjunction = conjunction;
            this.id = id;
        }

        @Override
        public String conjunction() {
            return conjunction;
        }

        @Override
        public long id() {
            return id;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final ExplainableImpl that = (ExplainableImpl) o;
            return id == that.id;
        }

        @Override
        public int hashCode() {
            return (int)id;
        }
    }
}
