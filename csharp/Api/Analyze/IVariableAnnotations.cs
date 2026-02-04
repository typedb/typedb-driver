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
    /// Represents the type annotations inferred for a variable.
    /// </summary>
    public interface IVariableAnnotations
    {
        /// <summary>
        /// Gets the variant indicating whether this is an instance, type, or value variable.
        /// </summary>
        Pinvoke.VariableAnnotationsVariant Variant { get; }

        /// <summary>
        /// Checks if this variable is an instance variable.
        /// </summary>
        bool IsInstance { get; }

        /// <summary>
        /// Checks if this variable is a type variable.
        /// </summary>
        bool IsType { get; }

        /// <summary>
        /// Checks if this variable is a value variable.
        /// </summary>
        bool IsValue { get; }

        /// <summary>
        /// Gets the possible types of instances this variable can hold.
        /// Only valid if <see cref="IsInstance"/> is true.
        /// </summary>
        IEnumerable<IType> AsInstance();

        /// <summary>
        /// Gets the possible types this variable can hold.
        /// Only valid if <see cref="IsType"/> is true.
        /// </summary>
        IEnumerable<IType> AsType();

        /// <summary>
        /// Gets the possible value types this variable can hold.
        /// Only valid if <see cref="IsValue"/> is true.
        /// </summary>
        IEnumerable<string> AsValue();
    }
}
