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

using System.Collections.Generic;

using TypeDB.Driver.Api;

namespace TypeDB.Driver.Api
{
    /**
     * An explanation of which rule was used for inferring the explained concept, the condition of the rule,
     * the conclusion of the rule, and the mapping of variables between the query and the rule’s conclusion.
     */
    public interface IExplanation
    {

        /**
         * Retrieves the Rule for this Explanation.
         *
         * <h3>Examples</h3>
         * <pre>
         * explanation.Rule;
         * </pre>
         */
        IRule Rule { get; }

        /**
         * Retrieves the Conclusion for this Explanation.
         *
         * <h3>Examples</h3>
         * <pre>
         * explanation.Conclusion
         * </pre>
         */
        IConceptMap Conclusion { get; }

        /**
         * Retrieves the Condition for this Explanation.
         *
         * <h3>Examples</h3>
         * <pre>
         * explanation.Condition
         * </pre>
         */
        IConceptMap Condition { get; }

        /**
         * Retrieves the query variables for this <code>Explanation</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * explanation.GetQueryVariables()
         * </pre>
         */
        ISet<string> GetQueryVariables();

        /**
         * Retrieves the rule variables corresponding to the query variable var for this <code>Explanation</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * explanation.VariableMapping(variable)
         * </pre>
         *
         * @param variable The query variable to map to rule variables.
         */
        ISet<string> QueryVariableMapping(string variable);
    }
}
