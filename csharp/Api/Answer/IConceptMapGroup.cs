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

using TypeDB.Driver.Api;

namespace TypeDB.Driver.Api
{
    /**
     * Contains an element of the group query result.
     */
    public interface IConceptMapGroup
    {
        /**
         * Retrieves the concept that is the group owner.
         *
         * <h3>Examples</h3>
         * <pre>
         * conceptMapGroup.Owner;
         * </pre>
         */
        IConcept Owner { get; }

        /**
         * Retrieves the <code>IConceptMap</code>s of the group.
         *
         * <h3>Examples</h3>
         * <pre>
         * conceptMapGroup.ConceptMaps;
         * </pre>
         */
        IEnumerable<IConceptMap> ConceptMaps { get; }
    }
}