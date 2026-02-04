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
    public class Pipeline : NativeObjectWrapper<Pinvoke.Pipeline>, IPipeline
    {
        public Pipeline(Pinvoke.Pipeline nativeObject)
            : base(nativeObject)
        {
        }

        public IEnumerable<IPipelineStage> Stages =>
            new NativeEnumerable<Pinvoke.PipelineStage>(
                Pinvoke.typedb_driver.pipeline_stages(NativeObject))
                .Select(s => PipelineStage.Of(s));

        public string? GetVariableName(IVariable variable)
        {
            var varImpl = (Variable)variable;
            return Pinvoke.typedb_driver.variable_get_name(NativeObject, varImpl.NativeObject);
        }

        public IConjunction? GetConjunction(IConjunctionID conjunctionID)
        {
            var conjIdImpl = (ConjunctionID)conjunctionID;
            var nativeConjunction = Pinvoke.typedb_driver.pipeline_get_conjunction(NativeObject, conjIdImpl.NativeObject);
            if (nativeConjunction == null)
            {
                return null;
            }
            return new Conjunction(nativeConjunction);
        }
    }
}
