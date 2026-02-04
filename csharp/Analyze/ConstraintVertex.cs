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

using System;

using TypeDB.Driver.Api;
using TypeDB.Driver.Api.Analyze;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Analyze
{
    public class ConstraintVertex : NativeObjectWrapper<Pinvoke.ConstraintVertex>, IConstraintVertex
    {
        internal ConstraintVertex(Pinvoke.ConstraintVertex nativeObject)
            : base(nativeObject)
        {
        }

        public bool IsVariable
        {
            get { return Pinvoke.typedb_driver.constraint_vertex_variant(NativeObject) == Pinvoke.ConstraintVertexVariant.VariableVertex; }
        }

        public bool IsLabel
        {
            get { return Pinvoke.typedb_driver.constraint_vertex_variant(NativeObject) == Pinvoke.ConstraintVertexVariant.LabelVertex; }
        }

        public bool IsValue
        {
            get { return Pinvoke.typedb_driver.constraint_vertex_variant(NativeObject) == Pinvoke.ConstraintVertexVariant.ValueVertex; }
        }

        public bool IsNamedRole
        {
            get { return Pinvoke.typedb_driver.constraint_vertex_variant(NativeObject) == Pinvoke.ConstraintVertexVariant.NamedRoleVertex; }
        }

        public IVariable AsVariable()
        {
            if (!IsVariable)
            {
                throw new InvalidOperationException("Constraint vertex is not a variable");
            }
            return new Variable(Pinvoke.typedb_driver.constraint_vertex_as_variable(NativeObject));
        }

        public IType AsLabel()
        {
            if (!IsLabel)
            {
                throw new InvalidOperationException("Constraint vertex is not a label");
            }
            var nativeConcept = Pinvoke.typedb_driver.constraint_vertex_as_label(NativeObject);
            return (IType)Concept.Concept.ConceptOf(nativeConcept);
        }

        public Api.IValue AsValue()
        {
            if (!IsValue)
            {
                throw new InvalidOperationException("Constraint vertex is not a value");
            }
            var nativeConcept = Pinvoke.typedb_driver.constraint_vertex_as_value(NativeObject);
            return (Api.IValue)Concept.Concept.ConceptOf(nativeConcept);
        }

        public INamedRole AsNamedRole()
        {
            if (!IsNamedRole)
            {
                throw new InvalidOperationException("Constraint vertex is not a named role");
            }
            return new NamedRole(Pinvoke.typedb_driver.constraint_vertex_as_named_role(NativeObject));
        }
    }
}
