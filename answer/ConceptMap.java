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

import grakn.client.concept.api.Concept;
import grakn.client.concept.api.GraknConceptException;
import graql.lang.statement.Variable;

import javax.annotation.CheckReturnValue;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * A type of Answer object that contains a Map of Concepts.
 */
public class ConceptMap extends Answer {

    private final Map<Variable, Concept> map;
    private final Explanation explanation;

    public ConceptMap(Map<Variable, Concept> map, Explanation exp) {
        this.map = Collections.unmodifiableMap(map);
        this.explanation = exp;
    }

    @Override
    public Explanation explanation() {
        return explanation;
    }

    @CheckReturnValue
    public Map<Variable, Concept> map() {
        return map;
    }


    public Collection<Concept> concepts() {
        return map.values();
    }

    @CheckReturnValue
    public Concept get(String variable) {
        return get(new Variable(variable));
    }

    @CheckReturnValue
    public Concept get(Variable var) {
        Concept Concept = map.get(var);
        if (Concept == null) throw GraknConceptException.variableDoesNotExist(var.toString());
        return Concept;
    }

    @Override
    public String toString() {
        return map.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().name()))
                .map(e -> "[" + e.getKey() + "/" + e.getValue().id() + "]").collect(Collectors.joining());
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
