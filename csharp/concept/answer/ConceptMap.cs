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
using System.Linq;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api.Answer;
using Vaticle.Typedb.Driver.Api.Concept;
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Common.Exception;
using Vaticle.Typedb.Driver.Concept;
using Vaticle.Typedb.Driver.Util;

using ConceptError = Vaticle.Typedb.Driver.Common.Exception.Error.Concept;
using QueryError = Vaticle.Typedb.Driver.Common.Exception.Error.Query;

namespace Vaticle.Typedb.Driver.Concept.Answer
{
    public class ConceptMap : NativeObjectWrapper<Pinvoke.ConceptMap>, IConceptMap 
    {        
        private int _hash = 0;
        private Dictionary<string, IConcept>? _cachedMap = null;
        
        public ConceptMapImpl(Pinvoke.ConceptMap nativeConceptMap) 
            : base(nativeConceptMap)
        {
        }
    
        public override ICollection<string> AllVariables
        {
            get
            {
                return new NativeEnumerable<string>(
                    Pinvoke.typedb_driver.concept_map_get_variables(NativeObject))
                    .ToList();
            }
        }
    
        public override ICollection<IConcept> AllConcepts
        {
            get
            {
                return new NativeEnumerable<IConcept>(
                    Pinvoke.typedb_driver.concept_map_get_values(NativeObject))
                    .Select(obj => new Concept(obj))
                    .ToList();
            }
        }
    
        public Dictionary<string, IConcept> Map
        {
            get
            {
                if (_cachedMap == null)
                {
                    _cachedMap = AllVariables.ToDictionary(v => v, v => Get(v));
                }

                return _cachedMap!;
            }
        }
    
        public override IConcept Get(string variable)
        {
            InputChecker.NonEmptyString<TypeDBDriverException>(variable, ConceptError.MISSING_VARIABLE);

            Pinvoke.Concept concept = Pinvoke.typedb_driver.concept_map_get(NativeObject, variable);
            if (concept == null)
            {
                throw new TypeDBDriverException(QueryError.VARIABLE_DOES_NOT_EXIST, variable);
            }

            return new Concept(concept);
        }
    
        public override IExplainables AllExplainables
        {
            get { return new Explainables(Pinvoke.typedb_driver.concept_map_get_explainables(NativeObject)); }
        }
    
        public override string ToString()
        {
            return Pinvoke.typedb_driver.concept_map_to_string(NativeObject);
        }

        public override bool Equals(object obj)
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
             
            return hash;
        }
    
        private int ComputeHash()
        {
            return Map.GetHashCode();
        }
    
        public static class Explainables : NativeObjectWrapper<Pinvoke.Explainables>, IExplainables 
        {
            private int _hash = 0;
    
            Explainables(Pinvoke.Explainables nativeExplainables)
                : base(nativeExplainables)
            {
            }
    
            public override IExplainable Relation(string variable)
            {
                InputChecker.NonEmptyString<TypeDBDriverException>(variable, ConceptError.MISSING_VARIABLE);

                Pinvoke.Explainable explainable =
                    Pinvoke.typedb_driver.explainables_get_relation(NativeObject, variable);
                if (explainable == null)
                {
                    throw new TypeDBDriverException(ConceptError.NONEXISTENT_EXPLAINABLE_CONCEPT, variable);
                }

                return new Explainable(explainable);
            }
    
            public override IExplainable Attribute(string variable)
            {
                InputChecker.NonEmptyString<TypeDBDriverException>(variable, ConceptError.MISSING_VARIABLE);

                Pinvoke.Explainable explainable =
                    Pinvoke.typedb_driver.explainables_get_attribute(NativeObject, variable);
                if (explainable == null)
                {
                    throw new TypeDBDriverException(ConceptError.NONEXISTENT_EXPLAINABLE_CONCEPT, variable);
                }

                return new Explainable(explainable);
            }
    
            public override IExplainable Ownership(string , string attribute)
            {
                InputChecker.NonEmptyString<TypeDBDriverException>(owner, ConceptError.MISSING_VARIABLE);
                InputChecker.NonEmptyString<TypeDBDriverException>(attribute, ConceptError.MISSING_VARIABLE);

                Pinvoke.Explainable explainable =
                    Pinvoke.typedb_driver.explainables_get_ownership(NativeObject, owner, attribute);
                if (explainable == null)
                {
                    throw new TypeDBDriverException(ConceptError.NONEXISTENT_EXPLAINABLE_OWNERSHIP, owner, attribute);
                }

                return new Explainable(explainable);
            }
    
            public ICollection<KeyValuePair<string, IExplainable>> AllRelations
            {
                get
                {
                    return new NativeEnumerable<string>(
                        Pinvoke.typedb_driver.explainables_get_relations_keys(NativeObject))
                        .Select(key => new KeyValuePair<string, IExplainable>(key, Relation(key)))
                        .ToList();
                }
            }

            public override ICollection<KeyValuePair<string, IExplainable>> AllAttributes
            {
                get
                {
                    return new NativeEnumerable<string>(
                        Pinvoke.typedb_driver.explainables_get_attributes_keys(NativeObject))
                        .Select(key => new KeyValuePair<string, IExplainable>(key, Attribute(key)))
                        .ToList();
                }
            }
    
            public override ICollection<KeyValuePair<KeyValuePair<string, string>, IExplainable>> AllOwnerships
            {
                get
                {
                    return new NativeEnumerable<Pinvoke.StringPair>(
                        Pinvoke.typedb_driver.explainables_get_ownerships_keys(NativeObject))
                        .Select(pair =>
                        {
                            string owner = pair._0;
                            string attribute = pair._1;
                            return new KeyValuePair<string, string>(
                                new KeyValuePair<string, string>(owner, attribute),
                                Ownership(owner, attribute));
                        })
                        .ToList();
                }
            }
    
            public override string ToString()
            {
                return Pinvoke.typedb_driver.explainables_to_string(NativeObject);
            }

            public override bool Equals(object obj)
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
                return (AllRelations.ToList(),
                        AllAttributes.ToList(),
                        AllOwnerships.ToList()).GetHashCode();
                );
            }
        }
    
        public static class Explainable : NativeObjectWrapper<Pinvoke.Explainable>, IExplainable 
        {
            public ExplainableImpl(Pinvoke.Explainable nativeExplainable)
                : base(nativeExplainable)
            {
            }
    
            public override string Conjunction 
            {
                get { return Pinvoke.typedb_driver.explainable_get_conjunction(NativeObject); }
            }
    
            public override long Id
            {
                get { return Pinvoke.typedb_driver.explainable_get_id(NativeObject); }
            }
    
            public override string ToString() 
            {
                return "Explainable { id: " + Id + ", conjunction: " + Conjunction + " }";
            }

            public override bool Equals(object obj) 
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
                return (int)id();
            }
        }
    }
}
