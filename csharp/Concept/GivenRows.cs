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
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Concept
{
    public class GivenRows : NativeObjectWrapper<Pinvoke.GivenRows>, IGivenRows
    {
        public GivenRows(Pinvoke.GivenRows nativeObject) : base(nativeObject) { }

        public static IGivenRows Of(List<string> givenVariables, List<List<IConcept?>> givenRows)
        {
            var headerBuilder = Pinvoke.typedb_driver.given_rows_header_builder_new((uint)givenVariables.Count);
            foreach (var variable in givenVariables)
                Pinvoke.typedb_driver.given_rows_header_builder_push(headerBuilder, variable);
            var header = Pinvoke.typedb_driver.given_rows_header_builder_finish(headerBuilder.Released());
            var rowsBuilder = Pinvoke.typedb_driver.given_rows_builder_new(header, (uint)givenRows.Count);
            foreach (var row in givenRows)
            {
                Pinvoke.typedb_driver.given_rows_builder_start_new_row(rowsBuilder);
                for (int i = 0; i < row.Count; i++)
                {
                    var concept = row[i];
                    if (concept == null)
                        Pinvoke.typedb_driver.given_rows_builder_set_index_to_empty(rowsBuilder, (uint)i);
                    else
                        Pinvoke.typedb_driver.given_rows_builder_set_index_to_concept(rowsBuilder, (uint)i, ((Concept)concept).NativeObject);
                }
                Pinvoke.typedb_driver.given_rows_builder_commit_row(rowsBuilder);
            }
            return new GivenRows(Pinvoke.typedb_driver.given_rows_builder_finish(rowsBuilder.Released()));
        }

        public static IGivenRows Of(List<Dictionary<string, IConcept?>> givenRows)
        {
            var variables = givenRows.SelectMany(r => r.Keys).Distinct().ToList();
            var headerBuilder = Pinvoke.typedb_driver.given_rows_header_builder_new((uint)variables.Count);
            foreach (var variable in variables)
                Pinvoke.typedb_driver.given_rows_header_builder_push(headerBuilder, variable);
            var header = Pinvoke.typedb_driver.given_rows_header_builder_finish(headerBuilder.Released());
            var rowsBuilder = Pinvoke.typedb_driver.given_rows_builder_new(header, (uint)givenRows.Count);
            foreach (var row in givenRows)
            {
                Pinvoke.typedb_driver.given_rows_builder_start_new_row(rowsBuilder);
                foreach (var (variable, concept) in row)
                {
                    if (concept == null)
                        Pinvoke.typedb_driver.given_rows_builder_set_variable_to_empty(rowsBuilder, variable);
                    else
                        Pinvoke.typedb_driver.given_rows_builder_set_variable_to_concept(rowsBuilder, variable, ((Concept)concept).NativeObject);
                }
                Pinvoke.typedb_driver.given_rows_builder_commit_row(rowsBuilder);
            }
            return new GivenRows(Pinvoke.typedb_driver.given_rows_builder_finish(rowsBuilder.Released()));
        }

        public static IGivenRows OfObjects(List<string> givenVariables, List<List<object?>> givenRows)
        {
            var converted = givenRows.Select(row =>
                row.Select(val => val == null ? null :
                                  val is IConcept c ? c :
                                  (IConcept?)Value.TryConvertToValue(val)).ToList()
            ).ToList();
            return Of(givenVariables, converted);
        }

        public static IGivenRows OfObjects(List<Dictionary<string, object?>> givenRows)
        {
            var converted = givenRows.Select(row =>
                row.ToDictionary(
                    kv => kv.Key,
                    kv => kv.Value == null ? null :
                          kv.Value is IConcept concept ? concept :
                          (IConcept?)Value.TryConvertToValue(kv.Value)
                )
            ).ToList();
            return Of(converted);
        }
    }
}
