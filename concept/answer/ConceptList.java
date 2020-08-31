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
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ConceptList implements Answer {

    // TODO: change to store List<Concept> once we are able to construct Concept without a database look up
    private final List<String> list;

    public ConceptList(List<String> list) {
        this.list = Collections.unmodifiableList(list);
    }

    public static ConceptList of(final AnswerProto.ConceptList res) {
        return new ConceptList(res.getIidsList().stream().map(AnswerProtoReader::iid).collect(toList()));
    }

    public boolean hasExplanation() {
        return false;
    }

    public List<String> list() {
        return list;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConceptList a2 = (ConceptList) obj;
        return this.list.equals(a2.list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }
}
