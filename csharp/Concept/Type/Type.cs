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

using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using TypeDB.Driver.Concept;

namespace TypeDB.Driver.Concept
{
    public abstract class Type : Concept, IType
    {
        private int _hash = 0;

        internal Type(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        public abstract Label Label { get; }

        public abstract bool IsRoot();

        public abstract bool IsAbstract();

        public abstract VoidPromise SetLabel(ITypeDBTransaction transaction, string label);

        public abstract VoidPromise Delete(ITypeDBTransaction transaction);

        public abstract Promise<bool> IsDeleted(ITypeDBTransaction transaction);

        public abstract Promise<IType> GetSupertype(ITypeDBTransaction transaction);

        public abstract IEnumerable<IType> GetSupertypes(ITypeDBTransaction transaction);

        public abstract IEnumerable<IType> GetSubtypes(ITypeDBTransaction transaction);

        public abstract IEnumerable<IType> GetSubtypes(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity);

        public override int GetHashCode()
        {
            if (_hash == 0)
            {
                _hash = ComputeHash();
            }

            return _hash;
        }

        private int ComputeHash()
        {
            return Label.GetHashCode();
        }
    }
}