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
using Vaticle.Typedb.Driver.Api.Concept.Type;
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Concept;

namespace Vaticle.Typedb.Driver.Concept.Type
{
    public abstract class Type : Concept, IType
    {
        private int _hash = 0;

        public Type(Pinvoke.Concept nativeConcept) 
            : base(nativeConcept)
        {
        }

        public abstract Promise<IType> GetSupertype(ITypeDBTransaction transaction);

        public abstract ICollection<IType> GetSupertypes(ITypeDBTransaction transaction);

        public abstract ICollection<IType> GetSubtypes(ITypeDBTransaction transaction);

        public abstract ICollection<IType> GetSubtypes(
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
            return GetLabel().GetHashCode();
        }
    }
}