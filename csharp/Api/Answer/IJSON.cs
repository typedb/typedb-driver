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

using TypeDB.Driver.Common;

using ConceptError = TypeDB.Driver.Common.Error.Concept;

namespace TypeDB.Driver.Api.Answer
{
    /// <summary>
    /// Represents a JSON value returned from a fetch query.
    /// </summary>
    public interface IJSON
    {
        /// <summary>
        /// Checks if this JSON value is an object.
        /// </summary>
        /// <example>
        /// <code>
        /// json.IsObject
        /// </code>
        /// </example>
        bool IsObject => false;

        /// <summary>
        /// Checks if this JSON value is an array.
        /// </summary>
        /// <example>
        /// <code>
        /// json.IsArray
        /// </code>
        /// </example>
        bool IsArray => false;

        /// <summary>
        /// Checks if this JSON value is a number.
        /// </summary>
        /// <example>
        /// <code>
        /// json.IsNumber
        /// </code>
        /// </example>
        bool IsNumber => false;

        /// <summary>
        /// Checks if this JSON value is a string.
        /// </summary>
        /// <example>
        /// <code>
        /// json.IsString
        /// </code>
        /// </example>
        bool IsString => false;

        /// <summary>
        /// Checks if this JSON value is a boolean.
        /// </summary>
        /// <example>
        /// <code>
        /// json.IsBoolean
        /// </code>
        /// </example>
        bool IsBoolean => false;

        /// <summary>
        /// Checks if this JSON value is null.
        /// </summary>
        /// <example>
        /// <code>
        /// json.IsNull
        /// </code>
        /// </example>
        bool IsNull => false;

        /// <summary>
        /// Casts this JSON value to an object (dictionary).
        /// </summary>
        /// <returns>The JSON value as a dictionary.</returns>
        /// <exception cref="TypeDBDriverException">If the value is not an object.</exception>
        /// <example>
        /// <code>
        /// json.AsObject()
        /// </code>
        /// </example>
        IReadOnlyDictionary<string, IJSON> AsObject()
        {
            throw new TypeDBDriverException(ConceptError.INVALID_CONCEPT_CASTING,
                GetType().Name, "Object");
        }

        /// <summary>
        /// Casts this JSON value to an array.
        /// </summary>
        /// <returns>The JSON value as a list.</returns>
        /// <exception cref="TypeDBDriverException">If the value is not an array.</exception>
        /// <example>
        /// <code>
        /// json.AsArray()
        /// </code>
        /// </example>
        IReadOnlyList<IJSON> AsArray()
        {
            throw new TypeDBDriverException(ConceptError.INVALID_CONCEPT_CASTING,
                GetType().Name, "Array");
        }

        /// <summary>
        /// Casts this JSON value to a number.
        /// </summary>
        /// <returns>The JSON value as a double.</returns>
        /// <exception cref="TypeDBDriverException">If the value is not a number.</exception>
        /// <example>
        /// <code>
        /// json.AsNumber()
        /// </code>
        /// </example>
        double AsNumber()
        {
            throw new TypeDBDriverException(ConceptError.INVALID_CONCEPT_CASTING,
                GetType().Name, "Number");
        }

        /// <summary>
        /// Casts this JSON value to a string.
        /// </summary>
        /// <returns>The JSON value as a string.</returns>
        /// <exception cref="TypeDBDriverException">If the value is not a string.</exception>
        /// <example>
        /// <code>
        /// json.AsString()
        /// </code>
        /// </example>
        string AsString()
        {
            throw new TypeDBDriverException(ConceptError.INVALID_CONCEPT_CASTING,
                GetType().Name, "String");
        }

        /// <summary>
        /// Casts this JSON value to a boolean.
        /// </summary>
        /// <returns>The JSON value as a boolean.</returns>
        /// <exception cref="TypeDBDriverException">If the value is not a boolean.</exception>
        /// <example>
        /// <code>
        /// json.AsBoolean()
        /// </code>
        /// </example>
        bool AsBoolean()
        {
            throw new TypeDBDriverException(ConceptError.INVALID_CONCEPT_CASTING,
                GetType().Name, "Boolean");
        }
    }
}
