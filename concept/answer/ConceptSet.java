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

import grakn.protocol.AnswerProto;

import java.util.Collections;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * A type of Answer object that contains a Set.
 */
public class ConceptSet implements Answer {

    // TODO: change to store Set<Concept> once we are able to construct Concept without a database look up
    private final Set<String> set;

    public ConceptSet(Set<String> set) {
        this.set = Collections.unmodifiableSet(set);
    }

    public static ConceptSet of(final AnswerProto.ConceptSet res) {
        return new ConceptSet(res.getIidsList().stream().map(AnswerMessageReader::iid).collect(toSet()));
    }

    @Override
    public boolean hasExplanation() {
        return false;
    }

    public Set<String> set() {
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
