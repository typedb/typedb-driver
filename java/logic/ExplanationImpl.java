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

package com.vaticle.typedb.client.logic;

import com.vaticle.typedb.client.api.answer.ConceptMap;
import com.vaticle.typedb.client.api.logic.Explanation;
import com.vaticle.typedb.client.api.logic.Rule;
import com.vaticle.typedb.client.common.NativeObject;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.concept.answer.ConceptMapImpl;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.MISSING_VARIABLE;
import static com.vaticle.typedb.client.jni.typedb_client.explanation_equals;
import static com.vaticle.typedb.client.jni.typedb_client.explanation_get_conclusion;
import static com.vaticle.typedb.client.jni.typedb_client.explanation_get_condition;
import static com.vaticle.typedb.client.jni.typedb_client.explanation_get_mapped_variables;
import static com.vaticle.typedb.client.jni.typedb_client.explanation_get_mapping;
import static com.vaticle.typedb.client.jni.typedb_client.explanation_get_rule;
import static com.vaticle.typedb.client.jni.typedb_client.explanation_to_string;

public class ExplanationImpl extends NativeObject<com.vaticle.typedb.client.jni.Explanation> implements Explanation {
    private int hash = 0;

    public ExplanationImpl(com.vaticle.typedb.client.jni.Explanation explanation) {
        super(explanation);
    }

    @Override
    public Rule rule() {
        return new RuleImpl(explanation_get_rule(nativeObject));
    }

    @Override
    public ConceptMap conclusion() {
        return new ConceptMapImpl(explanation_get_conclusion(nativeObject));
    }

    @Override
    public ConceptMap condition() {
        return new ConceptMapImpl(explanation_get_condition(nativeObject));
    }

    @Override
    public Set<String> queryVariables() {
        return explanation_get_mapped_variables(nativeObject).stream().collect(Collectors.toSet());
    }

    @Override
    public Set<String> queryVariableMapping(String var) {
        if (var == null || var.isEmpty()) throw new TypeDBClientException(MISSING_VARIABLE);
        return explanation_get_mapping(nativeObject, var).stream().collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final ExplanationImpl that = (ExplanationImpl) obj;
        return explanation_equals(this.nativeObject, that.nativeObject);
    }

    @Override
    public String toString() {
        return explanation_to_string(nativeObject);
    }

    @Override
    public int hashCode() {
        if (hash == 0) hash = computeHash();
        return hash;
    }

    private int computeHash() {
        return Objects.hash(rule(), conclusion(), condition());
    }
}
