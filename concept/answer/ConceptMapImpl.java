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
import grakn.client.common.GraknClientException;
import grakn.client.concept.thing.ThingImpl;
import grakn.client.concept.type.TypeImpl;
import grakn.protocol.AnswerProto;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static grakn.client.common.ErrorMessage.Query.VARIABLE_DOES_NOT_EXIST;

public class ConceptMapImpl implements ConceptMap {

    private final Map<String, Concept> map;

    public ConceptMapImpl(Map<String, Concept> map) {
        this.map = Collections.unmodifiableMap(map);
    }

    public static ConceptMap of(AnswerProto.ConceptMap res) {
        Map<String, Concept> variableMap = new HashMap<>();
        res.getMapMap().forEach((resVar, resConcept) -> {
            Concept concept;
            if (resConcept.hasThing()) concept = ThingImpl.of(resConcept.getThing());
            else concept = TypeImpl.of(resConcept.getType());
            variableMap.put(resVar, concept);
        });
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
        ConceptMapImpl a2 = (ConceptMapImpl) obj;
        return map.equals(a2.map);
    }

    @Override
    public int hashCode() { return map.hashCode();}
}
