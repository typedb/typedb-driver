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

package grakn.client.concept.type;

import grakn.client.Grakn.Transaction;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.Label;
import grakn.client.concept.type.impl.RuleImpl;
import graql.lang.pattern.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * A SchemaConcept used to model and categorise Rules.
 */
public interface Rule extends Type {
    @Deprecated
    @CheckReturnValue
    @Override
    default Rule asRule() {
        return this;
    }

    @Override
    default Remote asRemote(Transaction tx) {
        return Remote.of(tx, getIID());
    }

    interface Local extends Type.Local, Rule {
    }

    /**
     * A SchemaConcept used to model and categorise Rules.
     */
    interface Remote extends Type.Remote, Rule {

        static Rule.Remote of(Transaction tx, ConceptIID iid) {
            return new RuleImpl.Remote(tx, iid);
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
        Rule.Remote setLabel(Label label);


        /**
         * @param superRule The super of this Rule
         * @return The Rule itself
         */
        Rule.Remote setSupertype(Rule superRule);

        /**
         * @return All the super-types of this this Rule
         */
        @Override
        Stream<Rule.Remote> getSupertypes();

        /**
         * @return All the sub of this Rule
         */
        @Override
        Stream<Rule.Remote> getSubtypes();

        //------------------------------------- Other ---------------------------------
        @Deprecated
        @CheckReturnValue
        @Override
        default Rule.Remote asRule() {
            return this;
        }

    }
}
