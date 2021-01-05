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

public class NumericGroup {
    private final Concept owner;
    private final Numeric numeric;

    private NumericGroup(Concept owner, Numeric numeric) {
        this.owner = owner;
        this.numeric = numeric;
    }

    public static NumericGroup of(AnswerProto.NumericGroup numericGroup) {
        return new NumericGroup(ConceptImpl.of(numericGroup.getOwner()), Numeric.of(numericGroup.getNumber()));
    }

    public Concept owner() {
        return this.owner;
    }

    public Numeric numeric() {
        return this.numeric;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NumericGroup a2 = (NumericGroup) obj;
        return this.owner.equals(a2.owner) &&
                this.numeric.equals(a2.numeric);
    }

    @Override
    public int hashCode() {
        int hash = owner.hashCode();
        hash = 31 * hash + numeric.hashCode();

        return hash;
    }
}
