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

using DataTable = Gherkin.Ast.DataTable;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using Xunit;
using Xunit.Gherkin.Quick;

namespace Vaticle.Typedb.Driver.Test.Behaviour
{
    public class BehaviourTestException : Exception
    {
        public BehaviourTestException(string message)
            : base(message)
        {
        }
    }

    public class Util
    {
        public static readonly int KEY_ROW_INDEX = 0;

        public static List<T> ParseDataTableToTypeList<T>(DataTable dataTable, Func<string, T> converter)
        {
            List<T> collectedTypes = new List<T>();
            foreach (var dataRow in dataTable.Rows)
            {
                foreach (var data in dataRow.Cells)
                {
                    collectedTypes.Add(converter(data.Value));
                }
            }

            return collectedTypes;
        }

        public static List<Dictionary<string, string>> ParseDataTableToMultiDictionary(DataTable dataTable)
        {
            List<Dictionary<string, string>> parsedData = new List<Dictionary<string, string>>();

            var dataRows = dataTable.Rows.ToList();
            var dataKeys = dataRows[KEY_ROW_INDEX].Cells.Select(obj => obj.Value).ToList();

            for (int i = 1, outIndex = i - 1; i < dataRows.Count(); i++, outIndex++)
            {
                var dataCells = dataRows[i].Cells.Select(obj => obj.Value).ToList();

                for (int j = 0; j < dataCells.Count(); j++)
                {
                    if (parsedData.Count <= outIndex)
                    {
                        parsedData.Add(new Dictionary<string, string>());
                    }

                    parsedData[outIndex][dataKeys[j]] = dataCells[j];
                }
            }

            return parsedData;
        }

        public static bool JsonDeepEqualsUnordered(JToken expectedToken, JToken checkedToken)
        {
            return JToken.DeepEquals(GetSortedJson(expectedToken), GetSortedJson(checkedToken));
        }

        private static JToken GetSortedJson(JToken token)
        {
            if (token == null)
            {
                return token;
            }

            var result = token;

            switch (token.Type)
            {
                case JTokenType.Object:
                    var jObject = (JObject)token;

                    if (jObject != null && jObject.HasValues)
                    {
                        var newObject = new JObject();

                        foreach (var property in jObject.Properties().OrderBy(x => x.Name).ToList())
                        {
                            var sortedValue = GetSortedJson(property.Value as JToken);
                            newObject.Add(property.Name, sortedValue);
                        }

                        return newObject;
                    }

                    break;

                case JTokenType.Array:
                    var jArray = (JArray)token;

                    if (jArray != null && jArray.Count > 0)
                    {
                        var normalizedArrayItems = jArray
                            .Select(x => GetSortedJson(x))
                            .OrderBy(x => x.ToString(), StringComparer.Ordinal);

                        result = new JArray(normalizedArrayItems);
                    }

                    break;

                default:
                    break;
            }

            return result;
        }
    }
}
