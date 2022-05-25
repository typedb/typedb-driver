/*
 * Copyright (C) 2021 Vaticle
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

package com.vaticle.typedb.client.logic;

import com.vaticle.typedb.client.api.answer.ConceptMap;
import com.vaticle.typedb.client.api.concept.Concept;
import com.vaticle.typedb.client.api.logic.Explanation;
import com.vaticle.typedb.client.api.logic.Rule;
import com.vaticle.typedb.client.concept.ConceptImpl;
import com.vaticle.typedb.client.concept.answer.ConceptMapImpl;
import com.vaticle.typedb.protocol.ConceptProto;
import com.vaticle.typedb.protocol.LogicProto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ExplanationImpl implements Explanation {

    private final Rule rule;
    private final Map<String, Set<String>> variableMapping;
    private final Map<String, Concept> conclusion;
    private final ConceptMap condition;

    private ExplanationImpl(Rule rule, Map<String, Set<String>> variableMapping, Map<String, Concept> conclusion, ConceptMap condition) {
        this.rule = rule;
        this.variableMapping = variableMapping;
        this.conclusion = conclusion;
        this.condition = condition;
    }

    public static Explanation of(LogicProto.Explanation explanation) {
        return new ExplanationImpl(
                RuleImpl.of(explanation.getRule()),
                variableMappingOf(explanation.getVarMappingMap()),
                conclusionOf(explanation.getConclusionMap()),
                ConceptMapImpl.of(explanation.getCondition())
        );
    }

    private static Map<String, Set<String>> variableMappingOf(Map<String, LogicProto.Explanation.VarList> varMapping) {
        Map<String, Set<String>> mapping = new HashMap<>();
        varMapping.forEach((from, tos) -> mapping.put(from, new HashSet<>(tos.getVarsList())));
        return mapping;
    }

    private static Map<String, Concept> conclusionOf(Map<String, ConceptProto.Concept> conclusionMap) {
        Map<String, Concept> conclusion = new HashMap<>();
        conclusionMap.forEach((var, concept) -> conclusion.put(var, ConceptImpl.of(concept)));
        return conclusion;
    }

    @Override
    public Rule rule() {
        return rule;
    }

    @Override
    public Map<String, Set<String>> variableMapping() {
        return variableMapping;
    }

    @Override
    public Map<String, Concept> conclusion() {
        return conclusion;
    }

    @Override
    public ConceptMap condition() {
        return condition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ExplanationImpl that = (ExplanationImpl) o;
        return rule.equals(that.rule) && variableMapping.equals(that.variableMapping) &&
                conclusion.equals(that.conclusion) && condition.equals(that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rule, variableMapping, conclusion, condition);
    }
}
