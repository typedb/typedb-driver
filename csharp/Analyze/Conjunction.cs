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
using System.Linq;

using TypeDB.Driver.Api.Analyze;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Analyze
{
    public class Conjunction : NativeObjectWrapper<Pinvoke.Conjunction>, IConjunction
    {
        internal Conjunction(Pinvoke.Conjunction nativeObject)
            : base(nativeObject)
        {
        }

        public IEnumerable<IConstraint> Constraints
        {
            get
            {
                return new NativeEnumerable<Pinvoke.ConstraintWithSpan>(
                    Pinvoke.typedb_driver.conjunction_get_constraints(NativeObject))
                    .Select(c => Constraint.Of(c));
            }
        }

        public IEnumerable<IVariable> AnnotatedVariables
        {
            get
            {
                return new NativeEnumerable<Pinvoke.Variable>(
                    Pinvoke.typedb_driver.conjunction_get_annotated_variables(NativeObject))
                    .Select(v => new Variable(v));
            }
        }

        public IVariableAnnotations GetVariableAnnotations(IVariable variable)
        {
            var varImpl = (Variable)variable;
            return new VariableAnnotations(
                Pinvoke.typedb_driver.conjunction_get_variable_annotations(NativeObject, varImpl.NativeObject));
        }
    }
}
