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
import grakn.common.collection.Pair;
import grakn.protocol.AnswerProto;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static grakn.client.common.exception.ErrorMessage.Concept.NONEXISTENT_EXPLAINABLE_CONCEPT;
import static grakn.client.common.exception.ErrorMessage.Concept.NONEXISTENT_EXPLAINABLE_OWNERSHIP;
import static grakn.client.common.exception.ErrorMessage.Query.VARIABLE_DOES_NOT_EXIST;

public class ConceptMapImpl implements ConceptMap {

    private final Map<String, Concept> map;
    private final Explainables explainables;

    public ConceptMapImpl(Map<String, Concept> map) {
        this(map, new ExplainablesImpl());
    }

    public ConceptMapImpl(Map<String, Concept> map, Explainables explainables) {
        this.map = Collections.unmodifiableMap(map);
        this.explainables = explainables;
    }

    public static ConceptMap of(AnswerProto.ConceptMap res) {
        Map<String, Concept> variableMap = new HashMap<>();
        res.getMapMap().forEach((resVar, resConcept) -> variableMap.put(resVar, ConceptImpl.of(resConcept)));
        return new ConceptMapImpl(Collections.unmodifiableMap(variableMap), of(res.getExplainables()));
    }

    private static Explainables of(AnswerProto.Explainables explainables) {
        Map<String, Explainable> explainableRelations = new HashMap<>();
        explainables.getExplainableRelationsMap().forEach((var, explainable) -> {
            explainableRelations.put(var, ExplainableImpl.of(explainable));
        });
        Map<String, Explainable> explainableAttributes = new HashMap<>();
        explainables.getExplainableAttributesMap().forEach((var, explainable) -> {
            explainableAttributes.put(var, ExplainableImpl.of(explainable));
        });
        Map<Pair<String, String>, Explainable> explainableOwnerships = new HashMap<>();
        explainables.getExplainableOwnershipsList().forEach((explainableOwnership) -> {
            explainableOwnerships.put(
                    new Pair<>(explainableOwnership.getOwner(), explainableOwnership.getAttribute()),
                    ExplainableImpl.of(explainableOwnership.getExplainable())
            );
        });
        return new ExplainablesImpl(explainableRelations, explainableAttributes, explainableOwnerships);
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
    public Explainables explainables() {
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
    public int hashCode() {
        return map.hashCode();
    }

    public static class ExplainablesImpl implements Explainables {

        Map<String, Explainable> explainableRelations;
        Map<String, Explainable> explainableAttributes;
        Map<Pair<String, String>, Explainable> explainableOwnerships;

        ExplainablesImpl() {
            this(new HashMap<>(), new HashMap<>(), new HashMap<>());
        }

        ExplainablesImpl(Map<String, Explainable> explainableRelations, Map<String, Explainable> explainableAttributes,
                         Map<Pair<String, String>, Explainable> explainableOwnerships) {
            this.explainableRelations = explainableRelations;
            this.explainableAttributes = explainableAttributes;
            this.explainableOwnerships = explainableOwnerships;
        }

        @Override
        public Explainable relation(String variable) {
            Explainable explainable = explainableRelations.get(variable);
            if (explainable == null) throw new GraknClientException(NONEXISTENT_EXPLAINABLE_CONCEPT, variable);
            return explainable;
        }

        @Override
        public Explainable attribute(String variable) {
            Explainable explainable = explainableAttributes.get(variable);
            if (explainable == null) throw new GraknClientException(NONEXISTENT_EXPLAINABLE_CONCEPT, variable);
            return explainable;
        }

        @Override
        public Explainable ownership(String owner, String attribute) {
            Explainable explainable = explainableOwnerships.get(new Pair<>(owner, attribute));
            if (explainable == null)
                throw new GraknClientException(NONEXISTENT_EXPLAINABLE_OWNERSHIP, owner, attribute);
            return explainable;
        }

        @Override
        public Map<String, Explainable> explainableRelations() {
            return this.explainableRelations;
        }

        @Override
        public Map<String, Explainable> explainableAttributes() {
            return this.explainableAttributes;
        }

        @Override
        public Map<Pair<String, String>, Explainable> explainableOwnerships() {
            return this.explainableOwnerships;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExplainablesImpl that = (ExplainablesImpl) o;
            return explainableRelations.equals(that.explainableRelations) &&
                    explainableAttributes.equals(that.explainableAttributes) &&
                    explainableOwnerships.equals(that.explainableOwnerships);
        }

        @Override
        public int hashCode() {
            return Objects.hash(explainableRelations, explainableAttributes, explainableOwnerships);
        }

    }

    static class ExplainableImpl implements Explainable {

        private final String conjunction;
        private final long id;

        ExplainableImpl(String conjunction, long id) {
            this.conjunction = conjunction;
            this.id = id;
        }

        public static Explainable of(AnswerProto.Explainable explainable) {
            return new ExplainableImpl(explainable.getConjunction(), explainable.getId());
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
            return (int) id;
        }
    }
}
