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

import grakn.client.GraknClient;
import grakn.client.concept.remote.RemoteRuleImpl;
import graql.lang.pattern.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * A SchemaConcept used to model and categorise Rules.
 */
public interface Rule<RuleType extends Rule<RuleType>> extends SchemaConcept<RuleType> {
    @Deprecated
    @CheckReturnValue
    @Override
    default Rule<RuleType> asRule() {
        return this;
    }

    @Override
    default RemoteRule asRemote(GraknClient.Transaction tx) {
        return RemoteRule.of(tx, id());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isRule() {
        return true;
    }

    interface LocalRule extends Local<LocalRule>, Rule<LocalRule> {
    }

    /**
     * A SchemaConcept used to model and categorise Rules.
     */
    interface RemoteRule extends Rule<RemoteRule>, Remote<RemoteRule> {

        static RemoteRule of(GraknClient.Transaction tx, ConceptId id) {
            return new RemoteRuleImpl(tx, id);
        }

        //------------------------------------- Accessors ----------------------------------

        /**
         * Retrieves the when part of the Rule
         * When this query is satisfied the "then" part of the rule is executed.
         *
         * @return A string representing the left hand side Graql query.
         */
        @CheckReturnValue
        @Nullable
        Pattern when();

        /**
         * Retrieves the then part of the Rule.
         * This query is executed when the "when" part of the rule is satisfied
         *
         * @return A string representing the right hand side Graql query.
         */
        @CheckReturnValue
        @Nullable
        Pattern then();

        //------------------------------------- Modifiers ----------------------------------

        /**
         * Changes the Label of this Concept to a new one.
         *
         * @param label The new Label.
         * @return The Concept itself
         */
        RemoteRule label(Label label);


        /**
         * @param superRule The super of this Rule
         * @return The Rule itself
         */
        RemoteRule sup(Rule<?> superRule);

        /**
         * @return All the super-types of this this Rule
         */
        @Override
        Stream<RemoteRule> sups();

        /**
         * @return All the sub of this Rule
         */
        @Override
        Stream<RemoteRule> subs();

        //------------------------------------- Other ---------------------------------
        @Deprecated
        @CheckReturnValue
        @Override
        default RemoteRule asRule() {
            return this;
        }

        @Deprecated
        @CheckReturnValue
        @Override
        default boolean isRule() {
            return true;
        }
    }
}
