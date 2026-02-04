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

using TypeDB.Driver.Api.Analyze;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Analyze
{
    public class Variable : NativeObjectWrapper<Pinvoke.Variable>, IVariable
    {
        internal Variable(Pinvoke.Variable nativeObject)
            : base(nativeObject)
        {
        }

        private long Id()
        {
            return Pinvoke.typedb_driver.variable_id_as_u32(NativeObject);
        }

        public override int GetHashCode()
        {
            return Id().GetHashCode();
        }

        public override bool Equals(object? obj)
        {
            if (ReferenceEquals(this, obj))
            {
                return true;
            }

            if (obj == null || GetType() != obj.GetType())
            {
                return false;
            }

            Variable that = (Variable)obj;
            return this.Id() == that.Id();
        }

        public override string ToString()
        {
            return Pinvoke.typedb_driver.variable_string_repr(NativeObject);
        }
    }
}
