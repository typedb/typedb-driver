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

namespace TypeDB.Driver.Api.Analyze
{
    /// <summary>
    /// A representation of a query pipeline.
    /// </summary>
    public interface IPipeline
    {
        /// <summary>
        /// Gets the stages making up the pipeline.
        /// </summary>
        IEnumerable<IPipelineStage> Stages { get; }

        /// <summary>
        /// Gets the name of the specified variable, if it has one.
        /// </summary>
        string? GetVariableName(IVariable variable);

        /// <summary>
        /// Gets the conjunction for the specified conjunction ID.
        /// </summary>
        IConjunction? GetConjunction(IConjunctionID conjunctionID);
    }
}
