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

import grakn.protocol.AnswerProto;

/**
 * A type of Answer object that contains a Number.
 */
public class Numeric implements Answer {

    private final Number number;

    public Numeric(Number number) {
        this.number = number;
    }

    public static Numeric of(final AnswerProto.Value res) {
        return new Numeric(AnswerMessageReader.number(res.getNumber()));
    }

    @Override
    public boolean hasExplanation() {
        return false;
    }

    public Number number() {
        return number;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Numeric a2 = (Numeric) obj;
        return this.number.toString().equals(a2.number.toString());
    }

    @Override
    public int hashCode() {
        return number.hashCode();
    }
}
