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

using System.Collections.Generic;

using Vaticle.Typedb.Driver.Api.Concept;

namespace Vaticle.Typedb.Driver.Api.Answer
{
    /**
     * Contains a mapping of variables to concepts.
     */
    public interface IConceptMap
    {
        /**
         * Returns a collection of all variables in this <code>IConceptMap</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * conceptMap.AllVariables;
         * </pre>
         */
        ICollection<string> AllVariables { get; }
    
        /**
         * Returns a collection of all concepts in this <code>IConceptMap</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * conceptMap.AllConcepts;
         * </pre>
         */
        ICollection<IConcept> AllConcepts { get; }
    
        /**
         * Returns the inner <code>Dictionary</code> where keys are query variables, and values are concepts.
         *
         * <h3>Examples</h3>
         * <pre>
         * conceptMap.Dictionary;
         * </pre>
         */
        Dictionary<string, IConcept> Dictionary { get; }
    
        /**
         * Retrieves a concept for a given variable name.
         *
         * <h3>Examples</h3>
         * <pre>
         * conceptMap.Get(variable);
         * </pre>
         *
         * @param variable The string representation of a variable
         */
        IConcept Get(string variable);
    
        /**
         * Gets the <code>IExplainables</code> object for this <code>IConceptMap</code>, exposing
         * which of the concepts in this <code>IConceptMap</code> are explainable.
         *
         * <h3>Examples</h3>
         * <pre>
         * conceptMap.AllExplainables;
         * </pre>
         */
        IExplainables AllExplainables { get; }
    
        /**
         * Contains explainable objects.
         */
        interface IExplainables 
        {
            /**
             * Retrieves the explainable relation with the given variable name.
             *
             * <h3>Examples</h3>
             * <pre>
             * conceptMap.Explainables.Relation(variable);
             * </pre>
             *
             * @param variable The string representation of a variable
             */
            IExplainable Relation(string variable);
    
            /**
             * Retrieves the explainable attribute with the given variable name.
             *
             * <h3>Examples</h3>
             * <pre>
             * conceptMap.Explainables.Attribute(variable);
             * </pre>
             *
             * @param variable The string representation of a variable
             */
            IExplainable Attribute(string variable);
    
            /**
             * Retrieves the explainable attribute ownership with the pair of (owner, attribute) variable names.
             *
             * <h3>Examples</h3>
             * <pre>
             * conceptMap.Explainables.Ownership(owner, attribute);
             * </pre>
             *
             * @param owner The string representation of the owner variable
             * @param attribute The string representation of the attribute variable
             */
            IExplainable Ownership(string owner, string attribute);
    
            /**
             * Retrieves all of this <code>IConceptMap</code>’s explainable relations.
             *
             * <h3>Examples</h3>
             * <pre>
             * conceptMap.Explainables.AllRelations;
             * </pre>
             */
            ICollection<KeyValuePair<string, IExplainable>> AllRelations { get; }
    
            /**
             * Retrieves all of this <code>IConceptMap</code>’s explainable attributes.
             *
             * <h3>Examples</h3>
             * <pre>
             * conceptMap.Explainables.AllAttributes;
             * </pre>
             */
            ICollection<KeyValuePair<string, IExplainable>> AllAttributes { get; }
    
            /**
             * Retrieves all of this <code>IConceptMap</code>’s explainable ownerships.
             *
             * <h3>Examples</h3>
             * <pre>
             * conceptMap.Explainables.AllOwnerships;
             * </pre>
             */
            ICollection<KeyValuePair<KeyValuePair<string, string>, IExplainable>> AllOwnerships { get; }
        }
    
        /**
         * Contains an explainable object.
         */
        interface IExplainable 
        {
            /**
             * Retrieves the subquery of the original query that is actually being explained.
             *
             * <h3>Examples</h3>
             * <pre>
             * explainable.Conjunction;
             * </pre>
             */
            string Conjunction { get; }
    
            /**
             * Retrieves the unique ID that identifies this <code>IExplainable</code>.
             *
             * <h3>Examples</h3>
             * <pre>
             * explainable.Id;
             * </pre>
             */
            long Id { get; }
        }
    }
}
