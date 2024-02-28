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
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Concept;
using Vaticle.Typedb.Driver.Util;

using ConceptError = Vaticle.Typedb.Driver.Common.Error.Concept;

namespace Vaticle.Typedb.Driver.Logic
{
    public class Explanation : NativeObjectWrapper<Pinvoke.Explanation>, IExplanation
    {
        private int _hash = 0;

        public Explanation(Pinvoke.Explanation nativeExplanation)
            : base(nativeExplanation)
        {
        }

        public IRule Rule
        {
            get { return new Rule(Pinvoke.typedb_driver.explanation_get_rule(NativeObject)); }
        }

        public IConceptMap Conclusion
        {
            get { return new ConceptMap(Pinvoke.typedb_driver.explanation_get_conclusion(NativeObject)); }
        }

        public IConceptMap Condition
        {
            get { return new ConceptMap(Pinvoke.typedb_driver.explanation_get_condition(NativeObject)); }
        }

        public ISet<string> QueryVariables
        {
            get
            {
                return new NativeEnumerable<string>(
                    Pinvoke.typedb_driver.explanation_get_mapped_variables(NativeObject))
                    .ToHashSet<string>();
            }
        }

        public ISet<string> QueryVariableMapping(string )
        {
            InputChecker.NonEmptyString(variable, ConceptError.MISSING_VARIABLE);

            return new NativeEnumerable<string>(
                Pinvoke.typedb_driver.explanation_get_mapping(NativeObject, variable))
                .ToHashSet<string>();
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

            Explanation that = (Explanation)obj;

            return Pinvoke.typedb_driver.explanation_equals(this.NativeObject, that.NativeObject);
        }

        public override string ToString() 
        {
            return Pinvoke.typedb_driver.explanation_to_string(NativeObject);
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
            return (Rule, Conclusion, Condition).GetHashCode();
        }
    }
}
