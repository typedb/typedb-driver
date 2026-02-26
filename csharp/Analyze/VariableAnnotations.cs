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

using TypeDB.Driver.Api;
using TypeDB.Driver.Api.Analyze;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Analyze
{
    public class VariableAnnotations : NativeObjectWrapper<Pinvoke.VariableAnnotations>, IVariableAnnotations
    {
        internal VariableAnnotations(Pinvoke.VariableAnnotations nativeObject)
            : base(nativeObject)
        {
        }

        public Pinvoke.VariableAnnotationsVariant Variant
        {
            get { return Pinvoke.typedb_driver.variable_annotations_variant(NativeObject); }
        }

        public bool IsInstance
        {
            get { return Variant == Pinvoke.VariableAnnotationsVariant.InstanceAnnotations; }
        }

        public bool IsType
        {
            get { return Variant == Pinvoke.VariableAnnotationsVariant.TypeAnnotations; }
        }

        public bool IsValue
        {
            get { return Variant == Pinvoke.VariableAnnotationsVariant.ValueAnnotations; }
        }

        public IEnumerable<IType> AsInstance()
        {
            return new NativeEnumerable<Pinvoke.Concept>(
                Pinvoke.typedb_driver.variable_annotations_instance(NativeObject))
                .Select(c => (IType)Concept.Concept.ConceptOf(c));
        }

        public IEnumerable<IType> AsType()
        {
            return new NativeEnumerable<Pinvoke.Concept>(
                Pinvoke.typedb_driver.variable_annotations_type(NativeObject))
                .Select(c => (IType)Concept.Concept.ConceptOf(c));
        }

        public IEnumerable<string> AsValue()
        {
            return new NativeEnumerable<string>(
                Pinvoke.typedb_driver.variable_annotations_value(NativeObject));
        }
    }
}
