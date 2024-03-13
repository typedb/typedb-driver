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
using System.Linq;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

using static TypeDB.Driver.Concept.Concept;

namespace TypeDB.Driver.Concept
{
    public class ConceptMapGroup : NativeObjectWrapper<Pinvoke.ConceptMapGroup>, IConceptMapGroup
    {
        private int _hash = 0;
    
        public ConceptMapGroup(Pinvoke.ConceptMapGroup nativeConceptMapGroup) 
            : base(nativeConceptMapGroup)
        {
        }
    
        public IConcept Owner
        {
            get { return ConceptOf(Pinvoke.typedb_driver.concept_map_group_get_owner(NativeObject)); }
        }
    
        public IEnumerable<IConceptMap> ConceptMaps
        {
            get
            {
                return new NativeEnumerable<Pinvoke.ConceptMap>(
                    Pinvoke.typedb_driver.concept_map_group_get_concept_maps(NativeObject))
                    .Select(obj => new ConceptMap(obj));
            }
        }
    
        public override string ToString()
        {
            return Pinvoke.typedb_driver.concept_map_group_to_string(NativeObject);
        }

        public override bool Equals(object? obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }

            if (obj == null || this.GetType() != obj.GetType())
            {
                return false;
            }

            ConceptMapGroup that = (ConceptMapGroup)obj;

            return Pinvoke.typedb_driver.concept_map_group_equals(
                this.NativeObject, that.NativeObject);
        }
    
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
            return (Owner, ConceptMaps.ToList()).GetHashCode();
        }
    }
}
