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

using DocString = Gherkin.Ast.DocString;
using DataTable = Gherkin.Ast.DataTable;
using System;
using System.Collections.Generic;
using System.Linq;
using Xunit;
using Xunit.Gherkin.Quick;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Api.Answer;
using TypeDB.Driver.Answer;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        private static IQueryAnswer? _queryAnswer;
        private static List<IConceptRow>? _collectedRows;
        private static List<IJSON>? _collectedDocuments;
        private static QueryOptions? _queryOptions;

        // Transaction accessor from ConnectionStepsBase
        private static ITypeDBTransaction Tx => ConnectionStepsBase.Transactions[ConnectionStepsBase.Transactions.Count - 1];

        private void ClearAnswers()
        {
            _queryAnswer = null;
            _collectedRows = null;
            _collectedDocuments = null;
        }

        private void CollectAnswerIfNeeded()
        {
            if (_collectedRows != null || _collectedDocuments != null)
            {
                return;
            }

            if (_queryAnswer!.IsConceptRows)
            {
                _collectedRows = _queryAnswer.AsConceptRows().ToList();
            }
            else if (_queryAnswer!.IsConceptDocuments)
            {
                _collectedDocuments = _queryAnswer.AsConceptDocuments().ToList();
            }
        }

        private void CollectRowsAnswerIfNeeded()
        {
            CollectAnswerIfNeeded();
            Assert.NotNull(_collectedRows);
        }

        private void CollectDocumentsAnswerIfNeeded()
        {
            CollectAnswerIfNeeded();
            Assert.NotNull(_collectedDocuments);
        }

        private List<IConcept> GetRowGetConcepts(int rowIndex)
        {
            return _collectedRows![rowIndex].Concepts.ToList();
        }

        private IConcept? GetRowGetConcept(int rowIndex, string variable, bool byIndex = false)
        {
            IConceptRow row = _collectedRows![rowIndex];
            if (byIndex)
            {
                var columnNames = row.ColumnNames.ToList();
                int index = columnNames.IndexOf(variable);
                return row.GetIndex(index);
            }
            else
            {
                return row.Get(variable);
            }
        }

        private IValue GetRowGetValue(int rowIndex, string conceptKind, string variable, bool byIndex = false)
        {
            IConcept? concept = GetRowGetConcept(rowIndex, variable, byIndex);
            Assert.NotNull(concept);

            switch (conceptKind.ToLower())
            {
                case "attribute":
                    return concept!.AsAttribute().Value;
                case "value":
                    return concept!.AsValue();
                default:
                    throw new BehaviourTestException($"ConceptKind does not have values: {conceptKind}");
            }
        }

        #region Query Execution Steps

        [Given(@"typeql schema query")]
        [When(@"typeql schema query")]
        [Then(@"typeql schema query")]
        public void TypeqlSchemaQuery(DocString query)
        {
            ClearAnswers();
            if (_queryOptions != null)
            {
                Tx.Query(query.Content, _queryOptions);
            }
            else
            {
                Tx.Query(query.Content);
            }
        }

        [Given(@"typeql schema query; fails")]
        [When(@"typeql schema query; fails")]
        [Then(@"typeql schema query; fails")]
        public void TypeqlSchemaQueryFails(DocString query)
        {
            ClearAnswers();
            Assert.ThrowsAny<Exception>(() =>
            {
                if (_queryOptions != null)
                {
                    Tx.Query(query.Content, _queryOptions);
                }
                else
                {
                    Tx.Query(query.Content);
                }
            });
        }

        [Given(@"typeql schema query; fails with a message containing: ""(.*)""")]
        [When(@"typeql schema query; fails with a message containing: ""(.*)""")]
        [Then(@"typeql schema query; fails with a message containing: ""(.*)""")]
        public void TypeqlSchemaQueryFailsWithMessage(string expectedMessage, DocString query)
        {
            ClearAnswers();
            var exception = Assert.ThrowsAny<Exception>(() =>
            {
                if (_queryOptions != null)
                {
                    Tx.Query(query.Content, _queryOptions);
                }
                else
                {
                    Tx.Query(query.Content);
                }
            });
            Assert.Contains(expectedMessage, exception.Message);
        }

        [Given(@"typeql write query")]
        [When(@"typeql write query")]
        [Then(@"typeql write query")]
        public void TypeqlWriteQuery(DocString query)
        {
            ClearAnswers();
            if (_queryOptions != null)
            {
                Tx.Query(query.Content, _queryOptions);
            }
            else
            {
                Tx.Query(query.Content);
            }
        }

        [Given(@"typeql write query; fails")]
        [When(@"typeql write query; fails")]
        [Then(@"typeql write query; fails")]
        public void TypeqlWriteQueryFails(DocString query)
        {
            ClearAnswers();
            Assert.ThrowsAny<Exception>(() =>
            {
                if (_queryOptions != null)
                {
                    Tx.Query(query.Content, _queryOptions);
                }
                else
                {
                    Tx.Query(query.Content);
                }
            });
        }

        [Given(@"typeql write query; fails with a message containing: ""(.*)""")]
        [When(@"typeql write query; fails with a message containing: ""(.*)""")]
        [Then(@"typeql write query; fails with a message containing: ""(.*)""")]
        public void TypeqlWriteQueryFailsWithMessage(string expectedMessage, DocString query)
        {
            ClearAnswers();
            var exception = Assert.ThrowsAny<Exception>(() =>
            {
                if (_queryOptions != null)
                {
                    Tx.Query(query.Content, _queryOptions);
                }
                else
                {
                    Tx.Query(query.Content);
                }
            });
            Assert.Contains(expectedMessage, exception.Message);
        }

        [Given(@"typeql read query")]
        [When(@"typeql read query")]
        [Then(@"typeql read query")]
        public void TypeqlReadQuery(DocString query)
        {
            ClearAnswers();
            if (_queryOptions != null)
            {
                Tx.Query(query.Content, _queryOptions);
            }
            else
            {
                Tx.Query(query.Content);
            }
        }

        [Given(@"typeql read query; fails")]
        [When(@"typeql read query; fails")]
        [Then(@"typeql read query; fails")]
        public void TypeqlReadQueryFails(DocString query)
        {
            ClearAnswers();
            Assert.ThrowsAny<Exception>(() =>
            {
                if (_queryOptions != null)
                {
                    Tx.Query(query.Content, _queryOptions);
                }
                else
                {
                    Tx.Query(query.Content);
                }
            });
        }

        [Given(@"typeql read query; fails with a message containing: ""(.*)""")]
        [When(@"typeql read query; fails with a message containing: ""(.*)""")]
        [Then(@"typeql read query; fails with a message containing: ""(.*)""")]
        public void TypeqlReadQueryFailsWithMessage(string expectedMessage, DocString query)
        {
            ClearAnswers();
            var exception = Assert.ThrowsAny<Exception>(() =>
            {
                if (_queryOptions != null)
                {
                    Tx.Query(query.Content, _queryOptions);
                }
                else
                {
                    Tx.Query(query.Content);
                }
            });
            Assert.Contains(expectedMessage, exception.Message);
        }

        // "parsing fails" steps - same as "fails" since parsing errors are thrown as TypeDBDriverException
        [Given(@"typeql schema query; parsing fails")]
        [When(@"typeql schema query; parsing fails")]
        [Then(@"typeql schema query; parsing fails")]
        public void TypeqlSchemaQueryParsingFails(DocString query)
        {
            ClearAnswers();
            Assert.ThrowsAny<Exception>(() =>
            {
                if (_queryOptions != null)
                {
                    Tx.Query(query.Content, _queryOptions);
                }
                else
                {
                    Tx.Query(query.Content);
                }
            });
        }

        [Given(@"typeql write query; parsing fails")]
        [When(@"typeql write query; parsing fails")]
        [Then(@"typeql write query; parsing fails")]
        public void TypeqlWriteQueryParsingFails(DocString query)
        {
            ClearAnswers();
            Assert.ThrowsAny<Exception>(() =>
            {
                if (_queryOptions != null)
                {
                    Tx.Query(query.Content, _queryOptions);
                }
                else
                {
                    Tx.Query(query.Content);
                }
            });
        }

        [Given(@"typeql read query; parsing fails")]
        [When(@"typeql read query; parsing fails")]
        [Then(@"typeql read query; parsing fails")]
        public void TypeqlReadQueryParsingFails(DocString query)
        {
            ClearAnswers();
            Assert.ThrowsAny<Exception>(() =>
            {
                if (_queryOptions != null)
                {
                    Tx.Query(query.Content, _queryOptions);
                }
                else
                {
                    Tx.Query(query.Content);
                }
            });
        }

        [Given(@"get answers of typeql schema query")]
        [When(@"get answers of typeql schema query")]
        [Then(@"get answers of typeql schema query")]
        public void GetAnswersOfTypeqlSchemaQuery(DocString query)
        {
            ClearAnswers();
            if (_queryOptions != null)
            {
                _queryAnswer = Tx.Query(query.Content, _queryOptions);
            }
            else
            {
                _queryAnswer = Tx.Query(query.Content);
            }
        }

        [Given(@"get answers of typeql write query")]
        [When(@"get answers of typeql write query")]
        [Then(@"get answers of typeql write query")]
        public void GetAnswersOfTypeqlWriteQuery(DocString query)
        {
            ClearAnswers();
            if (_queryOptions != null)
            {
                _queryAnswer = Tx.Query(query.Content, _queryOptions);
            }
            else
            {
                _queryAnswer = Tx.Query(query.Content);
            }
        }

        [Given(@"get answers of typeql read query")]
        [When(@"get answers of typeql read query")]
        [Then(@"get answers of typeql read query")]
        public void GetAnswersOfTypeqlReadQuery(DocString query)
        {
            ClearAnswers();
            if (_queryOptions != null)
            {
                _queryAnswer = Tx.Query(query.Content, _queryOptions);
            }
            else
            {
                _queryAnswer = Tx.Query(query.Content);
            }
        }

        #endregion

        #region Query Options Steps

        [When(@"set query option include_instance_types to: (true|false)")]
        [Given(@"set query option include_instance_types to: (true|false)")]
        public void SetQueryOptionIncludeInstanceTypes(bool value)
        {
            if (_queryOptions == null)
            {
                _queryOptions = new QueryOptions();
            }
            _queryOptions.IncludeInstanceTypes = value;
        }

        [When(@"set query option include_query_structure to: (true|false)")]
        [Given(@"set query option include_query_structure to: (true|false)")]
        public void SetQueryOptionIncludeQueryStructure(bool value)
        {
            if (_queryOptions == null)
            {
                _queryOptions = new QueryOptions();
            }
            _queryOptions.IncludeQueryStructure = value;
        }

        [When(@"set query option prefetch_size to: (\d+)")]
        [Given(@"set query option prefetch_size to: (\d+)")]
        public void SetQueryOptionPrefetchSize(int value)
        {
            if (_queryOptions == null)
            {
                _queryOptions = new QueryOptions();
            }
            _queryOptions.PrefetchSize = value;
        }

        #endregion

        #region Answer Type Steps

        [Then(@"answer type is: ok")]
        public void AnswerTypeIsOk()
        {
            Assert.True(_queryAnswer!.IsOk);
        }

        [Then(@"answer type is: concept rows")]
        public void AnswerTypeIsConceptRows()
        {
            Assert.True(_queryAnswer!.IsConceptRows);
        }

        [Then(@"answer type is: concept documents")]
        public void AnswerTypeIsConceptDocuments()
        {
            Assert.True(_queryAnswer!.IsConceptDocuments);
        }

        [Then(@"answer type is not: ok")]
        public void AnswerTypeIsNotOk()
        {
            Assert.False(_queryAnswer!.IsOk);
        }

        [Then(@"answer type is not: concept rows")]
        public void AnswerTypeIsNotConceptRows()
        {
            Assert.False(_queryAnswer!.IsConceptRows);
        }

        [Then(@"answer type is not: concept documents")]
        public void AnswerTypeIsNotConceptDocuments()
        {
            Assert.False(_queryAnswer!.IsConceptDocuments);
        }

        [Then(@"answer unwraps as ok")]
        public void AnswerUnwrapsAsOk()
        {
            Assert.NotNull(_queryAnswer!.AsOk());
        }

        [Then(@"answer unwraps as concept rows")]
        public void AnswerUnwrapsAsConceptRows()
        {
            Assert.NotNull(_queryAnswer!.AsConceptRows());
        }

        [Then(@"answer unwraps as concept documents")]
        public void AnswerUnwrapsAsConceptDocuments()
        {
            Assert.NotNull(_queryAnswer!.AsConceptDocuments());
        }

        [Then(@"answer query type is: read")]
        public void AnswerQueryTypeIsRead()
        {
            Assert.Equal(QueryType.Read, _queryAnswer!.QueryType);
        }

        [Then(@"answer query type is: write")]
        public void AnswerQueryTypeIsWrite()
        {
            Assert.Equal(QueryType.Write, _queryAnswer!.QueryType);
        }

        [Then(@"answer query type is: schema")]
        public void AnswerQueryTypeIsSchema()
        {
            Assert.Equal(QueryType.Schema, _queryAnswer!.QueryType);
        }

        [Then(@"answer query type is not: read")]
        public void AnswerQueryTypeIsNotRead()
        {
            Assert.NotEqual(QueryType.Read, _queryAnswer!.QueryType);
        }

        [Then(@"answer query type is not: write")]
        public void AnswerQueryTypeIsNotWrite()
        {
            Assert.NotEqual(QueryType.Write, _queryAnswer!.QueryType);
        }

        [Then(@"answer query type is not: schema")]
        public void AnswerQueryTypeIsNotSchema()
        {
            Assert.NotEqual(QueryType.Schema, _queryAnswer!.QueryType);
        }

        #endregion

        #region Answer Size and Column Steps

        [Given(@"answer size is: (\d+)")]
        [Then(@"answer size is: (\d+)")]
        public void AnswerSizeIs(int expectedSize)
        {
            CollectAnswerIfNeeded();
            int actualSize;
            if (_collectedRows != null)
            {
                actualSize = _collectedRows.Count;
            }
            else if (_collectedDocuments != null)
            {
                actualSize = _collectedDocuments.Count;
            }
            else
            {
                throw new BehaviourTestException("Query answer is not collected: the size is NULL");
            }
            Assert.Equal(expectedSize, actualSize);
        }

        [Then(@"answer column names are:")]
        public void AnswerColumnNamesAre(DataTable names)
        {
            CollectRowsAnswerIfNeeded();
            var expectedNames = names.Rows.First().Cells.Select(c => c.Value).OrderBy(n => n).ToList();
            var actualNames = _collectedRows![0].ColumnNames.OrderBy(n => n).ToList();
            Assert.Equal(expectedNames, actualNames);
        }

        #endregion

        #region Answer Row Query Type Steps

        [Then(@"answer get row\((\d+)\) query type is: read")]
        public void AnswerGetRowQueryTypeIsRead(int rowIndex)
        {
            CollectRowsAnswerIfNeeded();
            Assert.Equal(QueryType.Read, _collectedRows![rowIndex].QueryType);
        }

        [Then(@"answer get row\((\d+)\) query type is: write")]
        public void AnswerGetRowQueryTypeIsWrite(int rowIndex)
        {
            CollectRowsAnswerIfNeeded();
            Assert.Equal(QueryType.Write, _collectedRows![rowIndex].QueryType);
        }

        [Then(@"answer get row\((\d+)\) query type is: schema")]
        public void AnswerGetRowQueryTypeIsSchema(int rowIndex)
        {
            CollectRowsAnswerIfNeeded();
            Assert.Equal(QueryType.Schema, _collectedRows![rowIndex].QueryType);
        }

        [Then(@"answer get row\((\d+)\) query type is not: read")]
        public void AnswerGetRowQueryTypeIsNotRead(int rowIndex)
        {
            CollectRowsAnswerIfNeeded();
            Assert.NotEqual(QueryType.Read, _collectedRows![rowIndex].QueryType);
        }

        [Then(@"answer get row\((\d+)\) query type is not: write")]
        public void AnswerGetRowQueryTypeIsNotWrite(int rowIndex)
        {
            CollectRowsAnswerIfNeeded();
            Assert.NotEqual(QueryType.Write, _collectedRows![rowIndex].QueryType);
        }

        [Then(@"answer get row\((\d+)\) query type is not: schema")]
        public void AnswerGetRowQueryTypeIsNotSchema(int rowIndex)
        {
            CollectRowsAnswerIfNeeded();
            Assert.NotEqual(QueryType.Schema, _collectedRows![rowIndex].QueryType);
        }

        #endregion

        #region Answer Row Concepts Steps

        [Then(@"answer get row\((\d+)\) get concepts size is: (\d+)")]
        public void AnswerGetRowGetConceptsSizeIs(int rowIndex, int size)
        {
            CollectRowsAnswerIfNeeded();
            var concepts = GetRowGetConcepts(rowIndex);
            Assert.Equal(size, concepts.Count);
        }

        [Then(@"answer get row\((\d+)\) get variable\(([^)]+)\) try get label is not none")]
        public void AnswerGetRowGetVariableTryGetLabelIsNotNone(int rowIndex, string variable)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.NotNull(concept!.TryGetLabel());
        }

        [Then(@"answer get row\((\d+)\) get variable\(([^)]+)\) try get label is none")]
        public void AnswerGetRowGetVariableTryGetLabelIsNone(int rowIndex, string variable)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.Null(concept!.TryGetLabel());
        }

        #endregion

        #region Answer Row Entity Steps

        [Then(@"answer get row\((\d+)\) get entity\(([^)]+)\) get type get label: (.+)")]
        public void AnswerGetRowGetEntityGetTypeGetLabel(int rowIndex, string variable, string label)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.True(concept!.IsEntity());
            Assert.Equal(label, concept.AsEntity().Type.GetLabel());
        }

        [Then(@"answer get row\((\d+)\) get entity by index of variable\(([^)]+)\) get type get label: (.+)")]
        public void AnswerGetRowGetEntityByIndexGetTypeGetLabel(int rowIndex, string variable, string label)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable, byIndex: true);
            Assert.NotNull(concept);
            Assert.True(concept!.IsEntity());
            Assert.Equal(label, concept.AsEntity().Type.GetLabel());
        }

        [Then(@"answer get row\((\d+)\) get entity type\(([^)]+)\) get label: (.+)")]
        public void AnswerGetRowGetEntityTypeGetLabel(int rowIndex, string variable, string label)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.True(concept!.IsEntityType());
            Assert.Equal(label, concept.AsEntityType().GetLabel());
        }

        [Then(@"answer get row\((\d+)\) get entity type by index of variable\(([^)]+)\) get label: (.+)")]
        public void AnswerGetRowGetEntityTypeByIndexGetLabel(int rowIndex, string variable, string label)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable, byIndex: true);
            Assert.NotNull(concept);
            Assert.True(concept!.IsEntityType());
            Assert.Equal(label, concept.AsEntityType().GetLabel());
        }

        #endregion

        #region Answer Row Relation Steps

        [Then(@"answer get row\((\d+)\) get relation\(([^)]+)\) get type get label: (.+)")]
        public void AnswerGetRowGetRelationGetTypeGetLabel(int rowIndex, string variable, string label)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.True(concept!.IsRelation());
            Assert.Equal(label, concept.AsRelation().Type.GetLabel());
        }

        [Then(@"answer get row\((\d+)\) get relation type\(([^)]+)\) get label: (.+)")]
        public void AnswerGetRowGetRelationTypeGetLabel(int rowIndex, string variable, string label)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.True(concept!.IsRelationType());
            Assert.Equal(label, concept.AsRelationType().GetLabel());
        }

        #endregion

        #region Answer Row Attribute Steps

        [Then(@"answer get row\((\d+)\) get attribute\(([^)]+)\) get type get label: (.+)")]
        public void AnswerGetRowGetAttributeGetTypeGetLabel(int rowIndex, string variable, string label)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.True(concept!.IsAttribute());
            Assert.Equal(label, concept.AsAttribute().Type.GetLabel());
        }

        [Then(@"answer get row\((\d+)\) get attribute by index of variable\(([^)]+)\) get type get label: (.+)")]
        public void AnswerGetRowGetAttributeByIndexGetTypeGetLabel(int rowIndex, string variable, string label)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable, byIndex: true);
            Assert.NotNull(concept);
            Assert.True(concept!.IsAttribute());
            Assert.Equal(label, concept.AsAttribute().Type.GetLabel());
        }

        [Then(@"answer get row\((\d+)\) get attribute type\(([^)]+)\) get label: (.+)")]
        public void AnswerGetRowGetAttributeTypeGetLabel(int rowIndex, string variable, string label)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.True(concept!.IsAttributeType());
            Assert.Equal(label, concept.AsAttributeType().GetLabel());
        }

        [Then(@"answer get row\((\d+)\) get attribute type by index of variable\(([^)]+)\) get label: (.+)")]
        public void AnswerGetRowGetAttributeTypeByIndexGetLabel(int rowIndex, string variable, string label)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable, byIndex: true);
            Assert.NotNull(concept);
            Assert.True(concept!.IsAttributeType());
            Assert.Equal(label, concept.AsAttributeType().GetLabel());
        }

        [Then(@"answer get row\((\d+)\) get attribute\(([^)]+)\) get value is: ""(.*)""")]
        public void AnswerGetRowGetAttributeGetValueIsString(int rowIndex, string variable, string expectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.True(concept!.IsAttribute());
            var value = concept.AsAttribute().Value;
            Assert.Equal(expectedValue, value.GetString());
        }

        [Then(@"answer get row\((\d+)\) get attribute by index of variable\(([^)]+)\) get value is: ""(.*)""")]
        public void AnswerGetRowGetAttributeByIndexGetValueIsString(int rowIndex, string variable, string expectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable, byIndex: true);
            Assert.NotNull(concept);
            Assert.True(concept!.IsAttribute());
            var value = concept.AsAttribute().Value;
            Assert.Equal(expectedValue, value.GetString());
        }

        #endregion

        #region Answer Document Steps

        [Then(@"answer contains document:")]
        public void AnswerContainsDocument(DocString expectedDocument)
        {
            CollectDocumentsAnswerIfNeeded();
            var expected = JSON.Parse(expectedDocument.Content);
            bool found = _collectedDocuments!.Any(doc => Util.JsonDeepEqualsUnordered(
                Newtonsoft.Json.Linq.JToken.Parse(doc.ToString()),
                Newtonsoft.Json.Linq.JToken.Parse(expected.ToString())));
            Assert.True(found, $"Expected document not found: {expectedDocument.Content}");
        }

        [Then(@"answer does not contain document:")]
        public void AnswerDoesNotContainDocument(DocString expectedDocument)
        {
            CollectDocumentsAnswerIfNeeded();
            var expected = JSON.Parse(expectedDocument.Content);
            bool found = _collectedDocuments!.Any(doc => Util.JsonDeepEqualsUnordered(
                Newtonsoft.Json.Linq.JToken.Parse(doc.ToString()),
                Newtonsoft.Json.Linq.JToken.Parse(expected.ToString())));
            Assert.False(found, $"Unexpected document found: {expectedDocument.Content}");
        }

        #endregion
    }
}
