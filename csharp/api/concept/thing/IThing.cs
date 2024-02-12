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
using Vaticle.Typedb.Driver.Api.Concept.Type;
using Vaticle.Typedb.Driver.Api.Concept.Value;
using Vaticle.Typedb.Driver.Common;

namespace Vaticle.Typedb.Driver.Api.Concept.Thing
{
    public interface IThing : Vaticle.Typedb.Driver.Api.Concept.IConcept
    {
        /**
         * The unique id of the <code>IThing</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * thing.IID;
         * </pre>
         */
        string IID { get; }
    
        /**
         * The type which this <code>IThing</code> belongs to.
         *
         * <h3>Examples</h3>
         * <pre>
         * thing.Type;
         * </pre>
         */
        IThingType Type { get; }
    
        /**
         * Checks if this <code>IThing</code> is inferred by a [Reasoning Rule].
         *
         * <h3>Examples</h3>
         * <pre>
         * thing.IsInferred();
         * </pre>
         */
        bool IsInferred();
    
        /**
         * Checks if the concept is a <code>IThing</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * thing.IsThing();
         * </pre>
         */
        new bool IsThing() 
        {
            return true;
        }
    
        /**
         * Casts the concept to <code>IThing</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * thing.AsThing();
         * </pre>
         */
        new IThing AsThing()
        {
            return this;
        }
    
        /**
         * Assigns an <code>IAttribute</code> to be owned by this <code>IThing</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * thing.SetHas(transaction, attribute).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param attribute The <code>IAttribute</code> to be owned by this <code>IThing</code>.
         */
        VoidPromise SetHas(ITypeDBTransaction transaction, IAttribute attribute);
    
        /**
         * Unassigns an <code>IAttribute</code> from this <code>IThing</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * thing.UnsetHas(transaction, attribute).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param attribute The <code>IAttribute</code> to be disowned from this <code>IThing</code>.
         */
        VoidPromise UnsetHas(ITypeDBTransaction transaction, IAttribute attribute);
    
        /**
         * Retrieves the <code>IAttribute</code>s that this <code>IThing</code> owns,
         * optionally filtered by <code>IAttributeType</code>s.
         *
         * <h3>Examples</h3>
         * <pre>
         * thing.GetHas(transaction);
         * thing.GetHas(transaction, attributeType);
         * </pre>
         *
         * @param transaction The current transaction
         * @param attributeTypes The <code>IAttributeType</code>s to filter the attributes by
         */
        ICollection<IAttribute> GetHas(ITypeDBTransaction transaction, ICollection<IAttributeType> attributeTypes);
    
        /**
         * Retrieves the <code>IAttribute</code>s that this <code>IThing</code> owns,
         * filtered by <code>Annotation</code>s.
         *
         * <h3>Examples</h3>
         * <pre>
         * thing.GetHas(transaction);
         * thing.GetHas(transaction, new HashSet<IThingType.Annotation>(){IThingType.Annotation.NewKey()});
         * </pre>
         *
         * @param transaction The current transaction
         * @param annotations Only retrieve attributes with all given <code>Annotation</code>s
         */
        ICollection<IAttribute> GetHas(ITypeDBTransaction transaction, ICollection<IThingType.Annotation> annotations);
    
        /**
         * Retrieves all the <code>Relations</code> which this <code>IThing</code> plays a role in,
         * optionally filtered by one or more given roles.
         *
         * <h3>Examples</h3>
         * <pre>
         * thing.GetRelations(transaction, roleTypes);
         * </pre>
         *
         * @param transaction The current transaction
         * @param roleTypes The array of roles to filter the relations by.
         */
        ICollection<IRelation> GetRelations(ITypeDBTransaction transaction, ICollection<IRoleType> roleTypes);
    
        /**
         * Retrieves the roles that this <code>IThing</code> is currently playing.
         *
         * <h3>Examples</h3>
         * <pre>
         * thing.GetPlaying(transaction);
         * </pre>
         *
         * @param transaction The current transaction
         */
        ICollection<IRoleType> GetPlaying(ITypeDBTransaction transaction);
    
        /**
         * Deletes this <code>IThing</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * thing.Delete(transaction).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         */
        VoidPromise Delete(ITypeDBTransaction transaction);
    
        /**
         * Checks if this <code>IThing</code> is deleted.
         *
         * <h3>Examples</h3>
         * <pre>
         * thing.IsDeleted(transaction).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         */
        Promise<bool> IsDeleted(ITypeDBTransaction transaction);
    }
}
