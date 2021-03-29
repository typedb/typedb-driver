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

package grakn.client.api.answer;

import grakn.client.api.concept.Concept;
import grakn.client.common.exception.GraknClientException;
import grakn.common.collection.Pair;

import javax.annotation.CheckReturnValue;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static grakn.client.common.exception.ErrorMessage.Concept.NONEXISTENT_EXPLAINABLE_CONCEPT;

public interface ConceptMap {

    @CheckReturnValue
    Map<String, Concept> map();

    @CheckReturnValue
    Collection<Concept> concepts();

    @CheckReturnValue
    Concept get(String variable);

    Explainables explainables();

    interface Explainables {

        Explainable concept(String variable);

        Explainable ownership(String owner, String attribute);

        Map<String, Explainable> explainableConcepts();

        Map<Pair<String, String>, Explainable> explainableOwnerships();

        interface Explainable {

            String conjunction();

            long id();

        }

    }
}
