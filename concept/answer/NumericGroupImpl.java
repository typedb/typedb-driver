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

package com.vaticle.typedb.client.concept.answer;

import com.vaticle.typedb.client.api.answer.Numeric;
import com.vaticle.typedb.client.api.answer.NumericGroup;
import com.vaticle.typedb.client.api.concept.Concept;
import com.vaticle.typedb.client.concept.ConceptImpl;
import com.vaticle.typedb.protocol.AnswerProto;

import java.util.Objects;

public class NumericGroupImpl implements NumericGroup {

    private final Concept owner;
    private final Numeric numeric;
    private final int hash;

    private NumericGroupImpl(Concept owner, Numeric numeric) {
        this.owner = owner;
        this.numeric = numeric;
        this.hash = Objects.hash(this.owner, this.numeric);
    }

    public static NumericGroup of(AnswerProto.NumericGroup numericGroup) {
        return new NumericGroupImpl(ConceptImpl.of(numericGroup.getOwner()), NumericImpl.of(numericGroup.getNumber()));
    }

    @Override
    public Concept owner() {
        return this.owner;
    }

    @Override
    public Numeric numeric() {
        return this.numeric;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NumericGroupImpl a2 = (NumericGroupImpl) obj;
        return this.owner.equals(a2.owner) &&
                this.numeric.equals(a2.numeric);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
