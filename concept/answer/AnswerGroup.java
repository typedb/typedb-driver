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

import grakn.client.Grakn.Transaction;
import grakn.client.concept.Concept;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.impl.TypeImpl;
import grakn.protocol.AnswerProto;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class AnswerGroup<T> implements Answer {

    private final Concept owner;
    private final List<T> answers;

    public AnswerGroup(Concept owner, List<T> answers) {
        this.owner = owner;
        this.answers = answers;
    }

    public static AnswerGroup<? extends Answer> of(Transaction tx, AnswerProto.AnswerGroup res) {
        Concept concept;
        if (res.getOwner().hasThing()) concept = ThingImpl.of(res.getOwner().getThing());
        else concept = TypeImpl.of(res.getOwner().getType());
        return new AnswerGroup<>(concept, res.getAnswersList().stream().map(answer -> Answer.of(tx, answer)).collect(toList()));
    }

    public Concept owner() {
        return this.owner;
    }

    public List<T> answers() {
        return this.answers;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AnswerGroup<?> a2 = (AnswerGroup<?>) obj;
        return this.owner.equals(a2.owner) &&
                this.answers.equals(a2.answers);
    }

    @Override
    public int hashCode() {
        int hash = owner.hashCode();
        hash = 31 * hash + answers.hashCode();

        return hash;
    }
}
