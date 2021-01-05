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

import grakn.client.concept.Concept;
import grakn.client.concept.impl.ConceptImpl;
import grakn.protocol.AnswerProto;

import java.util.List;
import java.util.stream.Collectors;

public class ConceptMapGroup {
    private final Concept owner;
    private final List<ConceptMap> conceptMaps;

    public ConceptMapGroup(Concept owner, List<ConceptMap> conceptMaps) {
        this.owner = owner;
        this.conceptMaps = conceptMaps;
    }

    public static ConceptMapGroup of(AnswerProto.ConceptMapGroup e) {
        Concept owner = ConceptImpl.of(e.getOwner());
        List<ConceptMap> conceptMaps = e.getConceptMapsList().stream().map(ConceptMap::of).collect(Collectors.toList());
        return new ConceptMapGroup(owner, conceptMaps);
    }

    public Concept owner() {
        return this.owner;
    }

    public List<ConceptMap> conceptMaps() {
        return this.conceptMaps;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConceptMapGroup a2 = (ConceptMapGroup) obj;
        return this.owner.equals(a2.owner) &&
                this.conceptMaps.equals(a2.conceptMaps);
    }

    @Override
    public int hashCode() {
        int hash = owner.hashCode();
        hash = 31 * hash + conceptMaps.hashCode();

        return hash;
    }
}
