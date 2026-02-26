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
    /// A representation of the 'fetch' stage of a query.
    /// </summary>
    public interface IFetch
    {
        /// <summary>
        /// Gets the variant. One of: Leaf, List, Object
        /// </summary>
        Pinvoke.FetchVariant Variant { get; }

        /// <summary>
        /// Checks if this fetch is a leaf.
        /// </summary>
        bool IsLeaf { get; }

        /// <summary>
        /// Checks if this fetch is a list.
        /// </summary>
        bool IsList { get; }

        /// <summary>
        /// Checks if this fetch is an object.
        /// </summary>
        bool IsObject { get; }

        /// <summary>
        /// Casts this fetch to a leaf.
        /// </summary>
        IFetchLeaf AsLeaf();

        /// <summary>
        /// Casts this fetch to a list.
        /// </summary>
        IFetchList AsList();

        /// <summary>
        /// Casts this fetch to an object.
        /// </summary>
        IFetchObject AsObject();
    }

    /// <summary>
    /// A mapping of string keys to Fetch documents.
    /// </summary>
    public interface IFetchObject
    {
        /// <summary>
        /// Gets the available keys of this Fetch document.
        /// </summary>
        IEnumerable<string> Keys { get; }

        /// <summary>
        /// Gets the Fetch object for the given key.
        /// </summary>
        IFetch Get(string key);
    }

    /// <summary>
    /// A list of Fetch documents.
    /// </summary>
    public interface IFetchList
    {
        /// <summary>
        /// Gets the element of the list.
        /// </summary>
        IFetch Element { get; }
    }

    /// <summary>
    /// The leaf of a Fetch object. Holds information on the value it can hold.
    /// </summary>
    public interface IFetchLeaf
    {
        /// <summary>
        /// Gets the possible value types.
        /// </summary>
        IEnumerable<string> Annotations { get; }
    }
}
