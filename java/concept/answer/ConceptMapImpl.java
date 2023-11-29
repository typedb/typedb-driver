/*
 * Copyright (C) 2022 Vaticle
 *
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

package com.vaticle.typedb.driver.concept.answer;

import com.vaticle.typedb.driver.api.answer.ConceptMap;
import com.vaticle.typedb.driver.api.concept.Concept;
import com.vaticle.typedb.driver.common.NativeObject;
import com.vaticle.typedb.driver.common.NativeIterator;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;
import com.vaticle.typedb.driver.concept.ConceptImpl;
import com.vaticle.typedb.common.collection.Pair;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Concept.MISSING_VARIABLE;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Concept.NONEXISTENT_EXPLAINABLE_CONCEPT;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Concept.NONEXISTENT_EXPLAINABLE_OWNERSHIP;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Query.VARIABLE_DOES_NOT_EXIST;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_map_equals;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_map_get;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_map_get_explainables;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_map_get_values;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_map_get_variables;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_map_to_string;
import static com.vaticle.typedb.driver.jni.typedb_driver.explainable_get_conjunction;
import static com.vaticle.typedb.driver.jni.typedb_driver.explainable_get_id;
import static com.vaticle.typedb.driver.jni.typedb_driver.explainables_equals;
import static com.vaticle.typedb.driver.jni.typedb_driver.explainables_get_attribute;
import static com.vaticle.typedb.driver.jni.typedb_driver.explainables_get_attributes_keys;
import static com.vaticle.typedb.driver.jni.typedb_driver.explainables_get_ownership;
import static com.vaticle.typedb.driver.jni.typedb_driver.explainables_get_ownerships_keys;
import static com.vaticle.typedb.driver.jni.typedb_driver.explainables_get_relation;
import static com.vaticle.typedb.driver.jni.typedb_driver.explainables_get_relations_keys;
import static com.vaticle.typedb.driver.jni.typedb_driver.explainables_to_string;

public class ConceptMapImpl extends NativeObject<com.vaticle.typedb.driver.jni.ConceptMap> implements ConceptMap {
    private int hash = 0;
    private Map<String, Concept> cachedMap = null;

    public ConceptMapImpl(com.vaticle.typedb.driver.jni.ConceptMap concept_map) {
        super(concept_map);
    }

    @Override
    public Stream<String> variables() {
        return new NativeIterator<>(concept_map_get_variables(nativeObject)).stream();
    }

    @Override
    public Stream<Concept> concepts() {
        return new NativeIterator<>(concept_map_get_values(nativeObject)).stream().map(ConceptImpl::of);
    }

    @Override
    public Map<String, Concept> map() {
        if (cachedMap == null) cachedMap = variables().collect(Collectors.toUnmodifiableMap(v -> v, this::get));
        return cachedMap;
    }

    @Override
    public Concept get(String variable) {
        if (variable == null || variable.isEmpty()) throw new TypeDBDriverException(MISSING_VARIABLE);
        com.vaticle.typedb.driver.jni.Concept concept = concept_map_get(nativeObject, variable);
        if (concept == null) throw new TypeDBDriverException(VARIABLE_DOES_NOT_EXIST, variable);
        return ConceptImpl.of(concept);
    }

    @Override
    public Explainables explainables() {
        return new ExplainablesImpl(concept_map_get_explainables(nativeObject));
    }

    @Override
    public String toString() {
        return concept_map_to_string(nativeObject);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConceptMapImpl that = (ConceptMapImpl) obj;
        return concept_map_equals(this.nativeObject, that.nativeObject);
    }

    @Override
    public int hashCode() {
        if (hash == 0) hash = computeHash();
        return hash;
    }

    private int computeHash() {
        return map().hashCode();
    }

    public static class ExplainablesImpl extends NativeObject<com.vaticle.typedb.driver.jni.Explainables> implements Explainables {
        private int hash = 0;

        ExplainablesImpl(com.vaticle.typedb.driver.jni.Explainables explainables) {
            super(explainables);
        }

        @Override
        public Explainable relation(String variable) {
            if (variable == null || variable.isEmpty()) throw new TypeDBDriverException(MISSING_VARIABLE);
            com.vaticle.typedb.driver.jni.Explainable explainable = explainables_get_relation(nativeObject, variable);
            if (explainable == null) throw new TypeDBDriverException(NONEXISTENT_EXPLAINABLE_CONCEPT, variable);
            return new ExplainableImpl(explainable);
        }

        @Override
        public Explainable attribute(String variable) {
            if (variable == null || variable.isEmpty()) throw new TypeDBDriverException(MISSING_VARIABLE);
            com.vaticle.typedb.driver.jni.Explainable explainable = explainables_get_attribute(nativeObject, variable);
            if (explainable == null) throw new TypeDBDriverException(NONEXISTENT_EXPLAINABLE_CONCEPT, variable);
            return new ExplainableImpl(explainable);
        }

        @Override
        public Explainable ownership(String owner, String attribute) {
            if (owner == null || owner.isEmpty()) throw new TypeDBDriverException(MISSING_VARIABLE);
            if (attribute == null || attribute.isEmpty()) throw new TypeDBDriverException(MISSING_VARIABLE);
            com.vaticle.typedb.driver.jni.Explainable explainable = explainables_get_ownership(nativeObject, owner, attribute);
            if (explainable == null) throw new TypeDBDriverException(NONEXISTENT_EXPLAINABLE_OWNERSHIP, owner, attribute);
            return new ExplainableImpl(explainable);
        }

        @Override
        public Stream<Pair<String, Explainable>> relations() {
            return new NativeIterator<>(explainables_get_relations_keys(nativeObject)).stream().map(k -> new Pair<>(k, relation(k)));
        }

        @Override
        public Stream<Pair<String, Explainable>> attributes() {
            return new NativeIterator<>(explainables_get_attributes_keys(nativeObject)).stream().map(k -> new Pair<>(k, attribute(k)));
        }

        @Override
        public Stream<Pair<Pair<String, String>, Explainable>> ownerships() {
            return new NativeIterator<>(explainables_get_ownerships_keys(nativeObject)).stream().map(pair -> {
                String owner = pair.get_0();
                String attribute = pair.get_1();
                return new Pair<>(new Pair<>(owner, attribute), ownership(owner, attribute));
            });
        }

        @Override
        public String toString() {
            return explainables_to_string(nativeObject);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ExplainablesImpl that = (ExplainablesImpl) obj;
            return explainables_equals(this.nativeObject, that.nativeObject);
        }

        @Override
        public int hashCode() {
            if (hash == 0) hash = computeHash();
            return hash;
        }

        private int computeHash() {
            return Objects.hash(
                    relations().collect(Collectors.toList()),
                    attributes().collect(Collectors.toList()),
                    ownerships().collect(Collectors.toList())
            );
        }
    }

    public static class ExplainableImpl extends NativeObject<com.vaticle.typedb.driver.jni.Explainable> implements Explainable {
        public ExplainableImpl(com.vaticle.typedb.driver.jni.Explainable explainable) {
            super(explainable);
        }

        @Override
        public String conjunction() {
            return explainable_get_conjunction(nativeObject);
        }

        @Override
        public long id() {
            return explainable_get_id(nativeObject);
        }

        @Override
        public String toString() {
            return "Explainable { id: " + id() + ", conjunction: " + conjunction() + " }";
        }


        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            final ExplainableImpl that = (ExplainableImpl) obj;
            return this.id() == that.id();
        }

        @Override
        public int hashCode() {
            return (int) id();
        }
    }
}
