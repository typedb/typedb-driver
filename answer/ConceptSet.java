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

import grakn.client.concept.ConceptId;

import java.util.Collections;
import java.util.Set;

/**
 * A type of Answer object that contains a Set.
 */
public class ConceptSet extends Answer {

    // TODO: change to store Set<Concept> once we are able to construct Concept without a database look up
    private final Set<ConceptId> set;
    private final Explanation explanation;

    public ConceptSet(Set<ConceptId> set) {
        this.set = Collections.unmodifiableSet(set);
        this.explanation = new Explanation();
    }

    @Override
    public Explanation explanation() {
        return explanation;
    }

    public Set<ConceptId> set() {
        return set;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConceptSet a2 = (ConceptSet) obj;
        return this.set.equals(a2.set);
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }
}
