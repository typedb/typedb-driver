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

using System;
using System.Collections.Generic;

using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Api
{
    public interface IType : IConcept
    {
        /**
         * The unique label of the type.
         *
         * <h3>Examples</h3>
         * <pre>
         * type.Label;
         * </pre>
         */
        Label Label { get; }

        /**
         * Checks if the type is a root type.
         *
         * <h3>Examples</h3>
         * <pre>
         * type.IsRoot();
         * </pre>
         */
        bool IsRoot();

        /**
         * Checks if the type is prevented from having data instances (i.e., <code>abstract</code>).
         *
         * <h3>Examples</h3>
         * <pre>
         * type.IsAbstract();
         * </pre>
         */
        bool IsAbstract();

        /**
         * {@inheritDoc}
         */
        bool IConcept.IsType()
        {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        IType IConcept.AsType()
        {
            return this;
        }

        /**
         * Renames the label of the type. The new label must remain unique.
         *
         * <h3>Examples</h3>
         * <pre>
         * type.SetLabel(transaction, newLabel).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param label The new <code>Label</code> to be given to the type.
         */
        VoidPromise SetLabel(ITypeDBTransaction transaction, string label);

        /**
         * Retrieves the most immediate supertype of the type.
         *
         * <h3>Examples</h3>
         * <pre>
         * type.GetSupertype(transaction).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         */
        Promise<IType> GetSupertype(ITypeDBTransaction transaction);

        /**
         * Retrieves all supertypes of the type.
         *
         * <h3>Examples</h3>
         * <pre>
         * type.GetSupertypes(transaction);
         * </pre>
         *
         * @param transaction The current transaction
         */
        IEnumerable<IType> GetSupertypes(ITypeDBTransaction transaction);

        /**
         * Retrieves all direct and indirect subtypes of the type.
         * Equivalent to <code>GetSubtypes(transaction, TRANSITIVE)</code>
         *
         * @see Type#GetSubtypes(ITypeDBTransaction, IConcept.Transitivity)
         */
        IEnumerable<IType> GetSubtypes(ITypeDBTransaction transaction);

        /**
         * Retrieves all direct and indirect (or direct only) subtypes of the type.
         *
         * <h3>Examples</h3>
         * <pre>
         * type.GetSubtypes(transaction);
         * type.GetSubtypes(transaction, EXPLICIT);
         * </pre>
         *
         * @param transaction The current transaction
         * @param transitivity <code>TRANSITIVE</code> for direct and indirect subtypes,
         *                     <code>EXPLICIT</code> for direct subtypes only
         */
        IEnumerable<IType> GetSubtypes(ITypeDBTransaction transaction, IConcept.Transitivity transitivity);

        /**
         * Deletes this type from the database.
         *
         * <h3>Examples</h3>
         * <pre>
         * type.Delete(transaction).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         */
        VoidPromise Delete(ITypeDBTransaction transaction);

        /**
         * Check if the concept has been deleted
         *
         * <h3>Examples</h3>
         * <pre>
         * type.IsDeleted(transaction).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         */
        Promise<bool> IsDeleted(ITypeDBTransaction transaction);
    }
}