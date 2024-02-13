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

using Vaticle.Typedb.Driver.Api.Concept.Thing;
using Vaticle.Typedb.Driver.Api.Concept.Type;
using Vaticle.Typedb.Driver.Api.Concept.Value;
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Common.Exception;

namespace Vaticle.Typedb.Driver.Api.Concept
{
    /**
     * Provides access for all Concept API methods.
     */
    public interface IConceptManager
    {
        /**
         * The root <code>IEntityType</code>, “entity”.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Concepts.RootEntityType
         * </pre>
         */
        IEntityType RootEntityType { get; }

        /**
         * The root <code>IRelationType</code>, “relation”.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Concepts.RootRelationType
         * </pre>
         */
        IRelationType RootRelationType { get; }

        /**
         * The root <code>IAttributeType</code>, “attribute”.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Concepts.RootAttributeType
         * </pre>
         */
        IAttributeType RootAttributeType { get; }

        /**
         * Retrieves an <code>IEntityType</code> by its label.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Concepts.GetEntityType(label).Resolve()
         * </pre>
         *
         * @param label The label of the <code>IEntityType</code> to retrieve
         */
        Promise<IEntityType> GetEntityType(string label);

        /**
         * Retrieves a <code>IRelationType</code> by its label.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Concepts.GetRelationType(label).Resolve()
         * </pre>
         *
         * @param label The label of the <code>IRelationType</code> to retrieve
         */
        Promise<IRelationType> GetRelationType(string label);

        /**
         * Retrieves an <code>IAttributeType</code> by its label.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Concepts.GetAttributeType(label).Resolve()
         * </pre>
         *
         * @param label The label of the <code>AttributeType</code> to retrieve
         */
        Promise<IAttributeType> GetAttributeType(string label);

        /**
         * Creates a new <code>IEntityType</code> if none exists with the given label,
         * otherwise retrieves the existing one.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Concepts.PutEntityType(label).Resolve()
         * </pre>
         *
         * @param label The label of the <code>IEntityType</code> to create or retrieve
         */
        Promise<IEntityType> PutEntityType(string label);

        /**
         * Creates a new <code>IRelationType</code> if none exists with the given label,
         * otherwise retrieves the existing one.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Concepts.PutRelationType(label).Resolve()
         * </pre>
         *
         * @param label The label of the <code>IRelationType</code> to create or retrieve
         */
        Promise<IRelationType> PutRelationType(string label);

        /**
         * Creates a new <code>IAttributeType</code> if none exists with the given label,
         * or retrieves the existing one.
         *
         * <h3>Examples</h3>
         * <pre>
         * await transaction.Concepts.PutAttributeType(label, valueType).Resolve()
         * </pre>
         *
         * @param label The label of the <code>IAttributeType</code> to create or retrieve
         * @param valueType The value type of the <code>IAttributeType</code> to create
         */
        Promise<IAttributeType> PutAttributeType(string label, IValue.ValueType valueType);

        /**
         * Retrieves an <code>IEntity</code> by its iid.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Concepts.GetEntity(iid).Resolve()
         * </pre>
         *
         * @param iid The iid of the <code>Entity</code> to retrieve
         */
        Promise<IEntity> GetEntity(string iid);

        /**
         * Retrieves a <code>IRelation</code> by its iid.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Concepts.GetRelation(iid).Resolve()
         * </pre>
         *
         * @param iid The iid of the <code>Relation</code> to retrieve
         */
        Promise<IRelation> GetRelation(string iid);

        /**
         * Retrieves an <code>IAttribute</code> by its iid.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Concepts.GetAttribute(iid).Resolve()
         * </pre>
         *
         * @param iid The iid of the <code>Attribute</code> to retrieve
         */
        Promise<IAttribute> GetAttribute(string iid);

        /**
         * A list of all schema exceptions for the current transaction.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Concepts.SchemaExceptions
         * </pre>
         */
        ICollection<TypeDBException> SchemaExceptions { get; }
    }
}
