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

package grakn.client.concept;

import grakn.protocol.session.ConceptProto;

/**
 * Client implementation of Concept
 *
 * @param <SomeConcept> represents the actual class of object to downcast to
 */
public abstract class ConceptImpl<SomeConcept extends Concept<SomeConcept>> implements Concept<SomeConcept> {

    private final ConceptId id;

    protected ConceptImpl(ConceptProto.Concept concept) {
        this.id = ConceptId.of(concept.getId());
    }

    @Override
    public ConceptId id() {
        return id;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{id=" + id + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConceptImpl<?> that = (ConceptImpl<?>) o;

        return id.equals(that.id());
    }

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= id.hashCode();
        return h;
    }

    abstract SomeConcept asCurrentBaseType(Concept<SomeConcept> other);
}
