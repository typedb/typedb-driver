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
using TypeDB.Driver.Concept;
using TypeDB.Driver.Common.Validation;
using static TypeDB.Driver.Concept.Concept;

using ConceptError = TypeDB.Driver.Common.Error.Concept;
using QueryError = TypeDB.Driver.Common.Error.Query;

namespace TypeDB.Driver.Concept
{
    public class ConceptMap : NativeObjectWrapper<Pinvoke.ConceptMap>, IConceptMap 
    {
        private Dictionary<string, IConcept>? _cachedMap;
        private IConceptMap.IExplainables? _allExplainables;
        private int _hash = 0;

        public ConceptMap(Pinvoke.ConceptMap nativeConceptMap)
            : base(nativeConceptMap)
        {
        }

        public IEnumerable<string> GetVariables()
        {
            return new NativeEnumerable<string>(
                Pinvoke.typedb_driver.concept_map_get_variables(NativeObject));
        }
    
        public IEnumerable<IConcept> GetConcepts()
        {
            return new NativeEnumerable<Pinvoke.Concept>(
                Pinvoke.typedb_driver.concept_map_get_values(NativeObject))
                .Select(obj => ConceptOf(obj));
        }
    
        public Dictionary<string, IConcept> GetMap()
        {
            return _cachedMap ?? (_cachedMap = GetVariables().ToDictionary(v => v, v => Get(v)));
        }
    
        public IConcept Get(string variable)
        {
            Validator.NonEmptyString(variable, ConceptError.MISSING_VARIABLE);

            Pinvoke.Concept concept = Pinvoke.typedb_driver.concept_map_get(NativeObject, variable);
            if (concept == null)
            {
                throw new TypeDBDriverException(QueryError.VARIABLE_DOES_NOT_EXIST, variable);
            }

            return ConceptOf(concept);
        }
    
        public IConceptMap.IExplainables AllExplainables
        {
            get
            {
                return _allExplainables ?? (_allExplainables =
                    new Explainables(Pinvoke.typedb_driver.concept_map_get_explainables(NativeObject)));
            }
        }
    
        public override string ToString()
        {
            return Pinvoke.typedb_driver.concept_map_to_string(NativeObject);
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

            ConceptMap that = (ConceptMap)obj;

            return Pinvoke.typedb_driver.concept_map_equals(
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
            return GetMap().GetHashCode();
        }
    
        public class Explainables : NativeObjectWrapper<Pinvoke.Explainables>, IConceptMap.IExplainables
        {
            private int _hash = 0;
    
            public Explainables(Pinvoke.Explainables nativeExplainables)
                : base(nativeExplainables)
            {
            }
    
            public IConceptMap.IExplainable Relation(string variable)
            {
                Validator.NonEmptyString(variable, ConceptError.MISSING_VARIABLE);

                Pinvoke.Explainable explainable =
                    Pinvoke.typedb_driver.explainables_get_relation(NativeObject, variable);
                if (explainable == null)
                {
                    throw new TypeDBDriverException(ConceptError.NONEXISTENT_EXPLAINABLE_CONCEPT, variable);
                }

                return new Explainable(explainable);
            }
    
            public IConceptMap.IExplainable Attribute(string variable)
            {
                Validator.NonEmptyString(variable, ConceptError.MISSING_VARIABLE);

                Pinvoke.Explainable explainable =
                    Pinvoke.typedb_driver.explainables_get_attribute(NativeObject, variable);
                if (explainable == null)
                {
                    throw new TypeDBDriverException(ConceptError.NONEXISTENT_EXPLAINABLE_CONCEPT, variable);
                }

                return new Explainable(explainable);
            }
    
            public IConceptMap.IExplainable Ownership(string owner, string attribute)
            {
                Validator.NonEmptyString(owner, ConceptError.MISSING_VARIABLE);
                Validator.NonEmptyString(attribute, ConceptError.MISSING_VARIABLE);

                Pinvoke.Explainable explainable =
                    Pinvoke.typedb_driver.explainables_get_ownership(NativeObject, owner, attribute);
                if (explainable == null)
                {
                    throw new TypeDBDriverException(ConceptError.NONEXISTENT_EXPLAINABLE_OWNERSHIP, owner, attribute);
                }

                return new Explainable(explainable);
            }
    
            public IEnumerable<KeyValuePair<string, IConceptMap.IExplainable>> GetRelations()
            {
                return new NativeEnumerable<string>(
                    Pinvoke.typedb_driver.explainables_get_relations_keys(NativeObject))
                    .Select(key => new KeyValuePair<string, IConceptMap.IExplainable>(key, Relation(key)));
            }

            public IEnumerable<KeyValuePair<string, IConceptMap.IExplainable>> GetAttributes()
            {
                return new NativeEnumerable<string>(
                    Pinvoke.typedb_driver.explainables_get_attributes_keys(NativeObject))
                    .Select(key => new KeyValuePair<string, IConceptMap.IExplainable>(key, Attribute(key)));
            }
    
            public IEnumerable<KeyValuePair<KeyValuePair<string, string>, IConceptMap.IExplainable>> GetOwnerships()
            {
                return new NativeEnumerable<Pinvoke.StringPair>(
                    Pinvoke.typedb_driver.explainables_get_ownerships_keys(NativeObject))
                    .Select(pair =>
                    {
                        string owner = pair._0;
                        string attribute = pair._1;
                        return new KeyValuePair<KeyValuePair<string, string>, IConceptMap.IExplainable>(
                            new KeyValuePair<string, string>(owner, attribute),
                            Ownership(owner, attribute));
                    });
            }
    
            public override string ToString()
            {
                return Pinvoke.typedb_driver.explainables_to_string(NativeObject);
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

                Explainables that = (Explainables)obj;

                return Pinvoke.typedb_driver.explainables_equals(
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
                return (GetRelations().ToList(),
                    GetAttributes().ToList(),
                    GetOwnerships().ToList()).GetHashCode();
            }
        }
    
        public class Explainable : NativeObjectWrapper<Pinvoke.Explainable>, IConceptMap.IExplainable
        {
            private string? _conjunction;
            private long? _id;

            public Explainable(Pinvoke.Explainable nativeExplainable)
                : base(nativeExplainable)
            {
            }
    
            public string Conjunction
            {
                get
                {
                    return _conjunction ?? (_conjunction =
                        Pinvoke.typedb_driver.explainable_get_conjunction(NativeObject));
                }
            }
    
            public long Id
            {
                get
                {
                    if (!_id.HasValue)
                    {
                        _id = Pinvoke.typedb_driver.explainable_get_id(NativeObject);
                    }

                    return _id.Value;
                }
            }
    
            public override string ToString() 
            {
                return "Explainable { id: " + Id + ", conjunction: " + Conjunction + " }";
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
        
                Explainable that = (Explainable)obj;
        
                return this.Id == that.Id;
            }
    
            public override int GetHashCode() 
            {
                return (int)Id;
            }
        }
    }
}
