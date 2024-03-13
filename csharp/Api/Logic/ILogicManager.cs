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

using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Common;

namespace Vaticle.Typedb.Driver.Api
{
    /**
     * Provides methods for manipulating rules in the database.
     */
    public interface ILogicManager
    {
        /**
         * Retrieves all rules.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Logic.Rules;
         * </pre>
         */
        IEnumerable<IRule> Rules { get; }

        /**
         * Retrieves the Rule that has the given label.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Logic.GetRule(label).Resolve()
         * </pre>
         *
         * @param label The label of the Rule to create or retrieve
         */
        Promise<IRule> GetRule(string label);

        /**
         * Creates a new Rule if none exists with the given label, or replaces the existing one.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Logic.PutRule(label, when, then).Resolve()
         * </pre>
         *
         * @param label The label of the IRule to create or replace
         * @param when  The when body of the rule to create
         * @param then  The then body of the rule to create
         */
        Promise<IRule> PutRule(string label, string when, string then);
    }
}
