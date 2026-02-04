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
    public class AnalyzedQuery : NativeObjectWrapper<Pinvoke.AnalyzedQuery>, IAnalyzedQuery
    {
        public AnalyzedQuery(Pinvoke.AnalyzedQuery nativeObject)
            : base(nativeObject)
        {
        }

        public IPipeline Pipeline =>
            new Pipeline(Pinvoke.typedb_driver.analyzed_query_pipeline(NativeObject));

        public IEnumerable<IFunction> Preamble =>
            new NativeEnumerable<Pinvoke.Function>(
                Pinvoke.typedb_driver.analyzed_preamble(NativeObject))
                .Select(f => new Function(f));

        public IFetch? Fetch =>
            Analyze.Fetch.Of(Pinvoke.typedb_driver.analyzed_fetch(NativeObject));
    }
}
