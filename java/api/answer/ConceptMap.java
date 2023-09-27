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

package com.vaticle.typedb.driver.api.answer;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.vaticle.typedb.driver.api.concept.Concept;
import com.vaticle.typedb.common.collection.Pair;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * Contains a mapping of variables to concepts.
 */
public interface ConceptMap {
    /**
     * Produces a stream over all variables in this <code>ConceptMap</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMap.variables();
     * </pre>
     */
    @CheckReturnValue
    Stream<String> variables();

    /**
     * Produces a stream over all concepts in this <code>ConceptMap</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMap.concepts();
     * </pre>
     */
    @CheckReturnValue
    Stream<Concept> concepts();

    /**
     * Retrieves a concept for a given variable name.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMap.get(variable);
     * </pre>
     *
     * @param variable The string representation of a variable
     */
    @CheckReturnValue
    Concept get(String variable);

    /**
     * Retrieves this <code>ConceptMap</code> as JSON.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMap.toJSON();
     * </pre>
     */
    @CheckReturnValue
    default JsonObject toJSON() {
        JsonObject object = Json.object();
        variables().forEach(resVar -> object.add(resVar, get(resVar).toJSON()));
        return object;
    }

    /**
     * Gets the <code>Explainables</code> object for this <code>ConceptMap</code>, exposing
     * which of the concepts in this <code>ConceptMap</code> are explainable.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMap.explainables();
     * </pre>
     */
    Explainables explainables();

    /**
     * Contains explainable objects.
     */
    interface Explainables {
        /**
         * Retrieves the explainable relation with the given variable name.
         *
         * <h3>Examples</h3>
         * <pre>
         * conceptMap.explainables().relation(variable);
         * </pre>
         *
         * @param variable The string representation of a variable
         */
        Explainable relation(String variable);

        /**
         * Retrieves the explainable attribute with the given variable name.
         *
         * <h3>Examples</h3>
         * <pre>
         * conceptMap.explainables().attribute(variable);
         * </pre>
         *
         * @param variable The string representation of a variable
         */
        Explainable attribute(String variable);

        /**
         * Retrieves the explainable attribute ownership with the pair of (owner, attribute) variable names.
         *
         * <h3>Examples</h3>
         * <pre>
         * conceptMap.explainables().ownership(owner, attribute);
         * </pre>
         *
         * @param owner The string representation of the owner variable
         * @param attribute The string representation of the attribute variable
         */
        Explainable ownership(String owner, String attribute);

        /**
         * Retrieves all of this <code>ConceptMap</code>’s explainable relations.
         *
         * <h3>Examples</h3>
         * <pre>
         * conceptMap.explainables().relations();
         * </pre>
         */
        Stream<Pair<String, Explainable>> relations();

        /**
         * Retrieves all of this <code>ConceptMap</code>’s explainable attributes.
         *
         * <h3>Examples</h3>
         * <pre>
         * conceptMap.explainables().attributes();
         * </pre>
         */
        Stream<Pair<String, Explainable>> attributes();

        /**
         * Retrieves all of this <code>ConceptMap</code>’s explainable ownerships.
         *
         * <h3>Examples</h3>
         * <pre>
         * conceptMap.explainables().ownerships();
         * </pre>
         */
        Stream<Pair<Pair<String, String>, Explainable>> ownerships();
    }

    /**
     * Contains an explainable object.
     */
    interface Explainable {
        /**
         * Retrieves the subquery of the original query that is actually being explained.
         *
         * <h3>Examples</h3>
         * <pre>
         * explainable.conjunction();
         * </pre>
         */
        String conjunction();

        /**
         * Retrieves the unique ID that identifies this <code>Explainable</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * explainable.id();
         * </pre>
         */
        long id();
    }
}
