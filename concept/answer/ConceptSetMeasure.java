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

import java.util.Set;

import static grakn.client.concept.answer.AnswerMessageReader.number;
import static java.util.stream.Collectors.toSet;

/**
 * A type of Answer object that contains a Set and Number, by extending RemoteConceptSet.
 */
public class ConceptSetMeasure extends ConceptSet {

    private final Number measurement;

    public ConceptSetMeasure(Set<String> set, Number measurement) {
        super(set);
        this.measurement = measurement;
    }

    public static ConceptSetMeasure of(final AnswerProto.ConceptSetMeasure res) {
        return new ConceptSetMeasure(
                res.getIidsList().stream().map(AnswerMessageReader::iid).collect(toSet()),
                number(res.getMeasurement())
        );
    }

    public Number measurement() {
        return measurement;
    }

    @Override
    public boolean hasExplanation() {
        return false;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConceptSetMeasure a2 = (ConceptSetMeasure) obj;
        return this.set().equals(a2.set())
                && measurement.toString().equals(a2.measurement.toString());
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 31 * hash + measurement.hashCode();

        return hash;
    }
}
