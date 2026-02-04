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

namespace TypeDB.Driver.Api.Analyze
{
    /// <summary>
    /// The answer to a TypeDB query is a set of concepts which satisfy the constraints in the query.
    /// A ConstraintVertex is either a variable, or some identifier of the concept.
    /// A Variable is a vertex the query must match and return.
    /// A Label uniquely identifies a type.
    /// A Value represents a primitive value literal in TypeDB.
    /// A NamedRole vertex is used in links and relates constraints, as multiple relations may have roles with the same name.
    /// </summary>
    public interface IConstraintVertex
    {
        /// <summary>
        /// Checks if this vertex is a variable.
        /// </summary>
        bool IsVariable { get; }

        /// <summary>
        /// Checks if this vertex is a label.
        /// </summary>
        bool IsLabel { get; }

        /// <summary>
        /// Checks if this vertex is a value.
        /// </summary>
        bool IsValue { get; }

        /// <summary>
        /// Checks if this vertex is a named role.
        /// </summary>
        bool IsNamedRole { get; }

        /// <summary>
        /// Down-casts this vertex to a variable.
        /// </summary>
        IVariable AsVariable();

        /// <summary>
        /// Down-casts this vertex to a type label.
        /// </summary>
        IType AsLabel();

        /// <summary>
        /// Down-casts this vertex to a value.
        /// </summary>
        Api.IValue AsValue();

        /// <summary>
        /// Down-casts this vertex to a named role.
        /// </summary>
        INamedRole AsNamedRole();
    }
}
