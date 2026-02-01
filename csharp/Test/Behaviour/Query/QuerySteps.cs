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
using System.Threading.Tasks;
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

        // Concurrent query state
        private static List<IQueryAnswer>? _concurrentAnswers;
        private static List<IEnumerator<IConceptRow>>? _concurrentRowStreams;

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
        [Given(@"typeql write query")]
        [When(@"typeql write query")]
        [Then(@"typeql write query")]
        [Given(@"typeql read query")]
        [When(@"typeql read query")]
        [Then(@"typeql read query")]
        public void TypeqlQuery(DocString query)
        {
            ClearAnswers();
            ExecuteQuery(query.Content);
        }

        [Given(@"typeql schema query; fails")]
        [When(@"typeql schema query; fails")]
        [Then(@"typeql schema query; fails")]
        [Given(@"typeql write query; fails")]
        [When(@"typeql write query; fails")]
        [Then(@"typeql write query; fails")]
        [Given(@"typeql read query; fails")]
        [When(@"typeql read query; fails")]
        [Then(@"typeql read query; fails")]
        public void TypeqlQueryFails(DocString query)
        {
            ClearAnswers();
            Assert.ThrowsAny<Exception>(() => ExecuteQuery(query.Content));
        }

        [Given(@"typeql schema query; fails with a message containing: ""(.*)""")]
        [When(@"typeql schema query; fails with a message containing: ""(.*)""")]
        [Then(@"typeql schema query; fails with a message containing: ""(.*)""")]
        [Given(@"typeql write query; fails with a message containing: ""(.*)""")]
        [When(@"typeql write query; fails with a message containing: ""(.*)""")]
        [Then(@"typeql write query; fails with a message containing: ""(.*)""")]
        [Given(@"typeql read query; fails with a message containing: ""(.*)""")]
        [When(@"typeql read query; fails with a message containing: ""(.*)""")]
        [Then(@"typeql read query; fails with a message containing: ""(.*)""")]
        public void TypeqlQueryFailsWithMessage(string expectedMessage, DocString query)
        {
            ClearAnswers();
            var exception = Assert.ThrowsAny<Exception>(() => ExecuteQuery(query.Content));
            Assert.Contains(expectedMessage, exception.Message);
        }

        [Given(@"typeql schema query; parsing fails")]
        [When(@"typeql schema query; parsing fails")]
        [Then(@"typeql schema query; parsing fails")]
        [Given(@"typeql write query; parsing fails")]
        [When(@"typeql write query; parsing fails")]
        [Then(@"typeql write query; parsing fails")]
        [Given(@"typeql read query; parsing fails")]
        [When(@"typeql read query; parsing fails")]
        [Then(@"typeql read query; parsing fails")]
        public void TypeqlQueryParsingFails(DocString query)
        {
            ClearAnswers();
            Assert.ThrowsAny<Exception>(() => ExecuteQuery(query.Content));
        }

        private IQueryAnswer ExecuteQuery(string queryText)
        {
            if (_queryOptions != null)
                return Tx.Query(queryText, _queryOptions);
            return Tx.Query(queryText);
        }

        [Given(@"get answers of typeql schema query")]
        [When(@"get answers of typeql schema query")]
        [Then(@"get answers of typeql schema query")]
        [Given(@"get answers of typeql write query")]
        [When(@"get answers of typeql write query")]
        [Then(@"get answers of typeql write query")]
        [Given(@"get answers of typeql read query")]
        [When(@"get answers of typeql read query")]
        [Then(@"get answers of typeql read query")]
        public void GetAnswersOfTypeqlQuery(DocString query)
        {
            ClearAnswers();
            _queryAnswer = ExecuteQuery(query.Content);
        }

        [Given(@"get answers of typeql schema query; fails")]
        [When(@"get answers of typeql schema query; fails")]
        [Then(@"get answers of typeql schema query; fails")]
        [Given(@"get answers of typeql write query; fails")]
        [When(@"get answers of typeql write query; fails")]
        [Then(@"get answers of typeql write query; fails")]
        [Given(@"get answers of typeql read query; fails")]
        [When(@"get answers of typeql read query; fails")]
        [Then(@"get answers of typeql read query; fails")]
        public void GetAnswersOfTypeqlQueryFails(DocString query)
        {
            ClearAnswers();
            Assert.ThrowsAny<Exception>(() =>
            {
                _queryAnswer = ExecuteQuery(query.Content);
            });
        }

        [Given(@"get answers of typeql schema query; fails with a message containing: ""(.*)""")]
        [When(@"get answers of typeql schema query; fails with a message containing: ""(.*)""")]
        [Then(@"get answers of typeql schema query; fails with a message containing: ""(.*)""")]
        [Given(@"get answers of typeql write query; fails with a message containing: ""(.*)""")]
        [When(@"get answers of typeql write query; fails with a message containing: ""(.*)""")]
        [Then(@"get answers of typeql write query; fails with a message containing: ""(.*)""")]
        [Given(@"get answers of typeql read query; fails with a message containing: ""(.*)""")]
        [When(@"get answers of typeql read query; fails with a message containing: ""(.*)""")]
        [Then(@"get answers of typeql read query; fails with a message containing: ""(.*)""")]
        public void GetAnswersOfTypeqlQueryFailsWithMessage(string expectedMessage, DocString query)
        {
            ClearAnswers();
            var exception = Assert.ThrowsAny<Exception>(() =>
            {
                _queryAnswer = ExecuteQuery(query.Content);
            });
            Assert.Contains(expectedMessage, exception.Message);
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

        [Then(@"answer get row\((\d+)\) get variable\(([^)]+)\) is empty")]
        public void AnswerGetRowGetVariableIsEmpty(int rowIndex, string variable)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.Null(concept);
        }

        [Then(@"answer get row\((\d+)\) get variable\(([^)]+)\) is not empty")]
        public void AnswerGetRowGetVariableIsNotEmpty(int rowIndex, string variable)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
        }

        [Then(@"answer get row\((\d+)\) get variable by index of variable\(([^)]+)\) is empty")]
        public void AnswerGetRowGetVariableByIndexIsEmpty(int rowIndex, string variable)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable, byIndex: true);
            Assert.Null(concept);
        }

        [Then(@"answer get row\((\d+)\) get variable by index of variable\(([^)]+)\) is not empty")]
        public void AnswerGetRowGetVariableByIndexIsNotEmpty(int rowIndex, string variable)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable, byIndex: true);
            Assert.NotNull(concept);
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
            Assert.Equal(expectedValue.Replace("\\\"", "\""), value.GetString());
        }

        [Then(@"answer get row\((\d+)\) get attribute by index of variable\(([^)]+)\) get value is: ""(.*)""")]
        public void AnswerGetRowGetAttributeByIndexGetValueIsString(int rowIndex, string variable, string expectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable, byIndex: true);
            Assert.NotNull(concept);
            Assert.True(concept!.IsAttribute());
            var value = concept.AsAttribute().Value;
            Assert.Equal(expectedValue.Replace("\\\"", "\""), value.GetString());
        }

        #endregion

        #region Uniquely Identify Answer Concepts Steps

        /// <summary>
        /// Interface for matching concepts against expected identifiers.
        /// Based on the Node.js/Java implementation pattern.
        /// </summary>
        private interface IConceptMatcher
        {
            bool Matches(IConcept concept);
        }

        /// <summary>
        /// Matches type concepts by label (e.g., "label:person" for entity type person).
        /// </summary>
        private class TypeLabelMatcher : IConceptMatcher
        {
            private readonly string _label;

            public TypeLabelMatcher(string label)
            {
                _label = label;
            }

            public bool Matches(IConcept concept)
            {
                if (!concept.IsType())
                    return false;

                string actualLabel;
                if (concept.IsRoleType())
                {
                    // RoleType has scoped labels like "employment:employee"
                    actualLabel = concept.AsRoleType().GetLabel();
                }
                else
                {
                    actualLabel = concept.AsType().GetLabel();
                }

                return actualLabel == _label;
            }
        }

        /// <summary>
        /// Base class for attribute-based matchers with value comparison.
        /// </summary>
        private abstract class AttributeMatcherBase : IConceptMatcher
        {
            protected readonly string TypeLabel;
            protected readonly string Value;

            protected AttributeMatcherBase(string typeAndValue)
            {
                // Parse "typeLabel:value" format
                var colonIndex = typeAndValue.IndexOf(':');
                if (colonIndex < 0)
                {
                    throw new BehaviourTestException($"Invalid attribute identifier format: {typeAndValue}. Expected 'typeLabel:value'");
                }
                TypeLabel = typeAndValue.Substring(0, colonIndex);
                Value = typeAndValue.Substring(colonIndex + 1);
            }

            protected bool CheckValue(IAttribute attribute)
            {
                return TestValueHelper.CompareValues(attribute.Value, Value, null);
            }

            public abstract bool Matches(IConcept concept);
        }

        /// <summary>
        /// Matches attribute concepts by type label and value (e.g., "attr:name:Alice").
        /// </summary>
        private class AttributeValueMatcher : AttributeMatcherBase
        {
            public AttributeValueMatcher(string typeAndValue) : base(typeAndValue) { }

            public override bool Matches(IConcept concept)
            {
                if (!concept.IsAttribute())
                    return false;

                var attribute = concept.AsAttribute();
                if (attribute.Type.GetLabel() != TypeLabel)
                    return false;

                return CheckValue(attribute);
            }
        }

        /// <summary>
        /// Matches thing concepts (Entity/Relation/Attribute) by their key attribute (e.g., "key:ref:0").
        /// Uses a subquery approach since TypeDB 3.0's read-only concept API doesn't have GetHas().
        /// </summary>
        private class ThingKeyMatcher : AttributeMatcherBase
        {
            public ThingKeyMatcher(string typeAndValue) : base(typeAndValue) { }

            public override bool Matches(IConcept concept)
            {
                if (!concept.IsThing())
                    return false;

                var thing = concept.AsThing();

                // Get the IID of this thing
                var thingIid = thing.TryGetIID();
                if (thingIid == null)
                    return false;

                // Use the current transaction to run a subquery
                var tx = ConnectionStepsBase.Tx;

                // Build a query to find things with this specific key attribute value
                // Format: match $x iid <iid>, has <typeLabel> <value>;
                string valueStr = Value;
                string query;

                // Try to determine if the value is numeric or string
                if (long.TryParse(valueStr, out _))
                {
                    // Integer value
                    query = $"match $x iid {thingIid}; $x has {TypeLabel} {valueStr};";
                }
                else if (double.TryParse(valueStr, System.Globalization.NumberStyles.Any, System.Globalization.CultureInfo.InvariantCulture, out _))
                {
                    // Double value
                    query = $"match $x iid {thingIid}; $x has {TypeLabel} {valueStr};";
                }
                else if (valueStr == "true" || valueStr == "false")
                {
                    // Boolean value
                    query = $"match $x iid {thingIid}; $x has {TypeLabel} {valueStr};";
                }
                else
                {
                    // String value - needs quotes
                    query = $"match $x iid {thingIid}; $x has {TypeLabel} \"{valueStr}\";";
                }

                try
                {
                    var answer = tx.Query(query);
                    if (answer.IsConceptRows)
                    {
                        // Check if any rows are returned - if so, the thing has this key
                        var rows = answer.AsConceptRows().ToList();
                        return rows.Count > 0;
                    }
                    return false;
                }
                catch
                {
                    return false;
                }
            }
        }

        /// <summary>
        /// Matches Value concepts by value type and value (e.g., "value:long:42").
        /// </summary>
        private class ValueMatcher : IConceptMatcher
        {
            private readonly string _valueType;
            private readonly string _value;

            public ValueMatcher(string typeAndValue)
            {
                // Parse "valueType:value" format
                var colonIndex = typeAndValue.IndexOf(':');
                if (colonIndex < 0)
                {
                    throw new BehaviourTestException($"Invalid value identifier format: {typeAndValue}. Expected 'valueType:value'");
                }
                _valueType = typeAndValue.Substring(0, colonIndex);
                _value = typeAndValue.Substring(colonIndex + 1);
            }

            public bool Matches(IConcept concept)
            {
                if (!concept.IsValue())
                    return false;

                var value = concept.AsValue();
                var actualType = value.GetValueType().ToLower();

                // Map type names (some variations exist)
                var expectedType = _valueType.ToLower();
                if (expectedType == "long") expectedType = "integer";
                if (actualType == "long") actualType = "integer";

                if (actualType != expectedType)
                    return false;

                switch (actualType)
                {
                    case "boolean":
                        return value.GetBoolean() == bool.Parse(_value);
                    case "integer":
                        return value.GetInteger() == long.Parse(_value);
                    case "double":
                        return Math.Abs(value.GetDouble() - double.Parse(_value, System.Globalization.CultureInfo.InvariantCulture)) < 0.0001;
                    case "string":
                        return value.GetString() == _value;
                    default:
                        return false;
                }
            }
        }

        /// <summary>
        /// Matches null/absent concepts (represented by "none" in test tables).
        /// </summary>
        private class NoneMatcher : IConceptMatcher
        {
            public bool Matches(IConcept concept)
            {
                // This matcher should match null concepts, but the Matches method
                // receives a non-null concept, so it should always return false.
                // The check for null is done in AnswerConceptsMatch.
                return false;
            }
        }

        /// <summary>
        /// Parses a concept identifier and returns the appropriate matcher.
        /// Format: "identifierType:identifierBody"
        /// Examples: "label:person", "key:email:john@example.com", "attr:name:Alice", "value:long:42", "none"
        /// </summary>
        private IConceptMatcher ParseConceptIdentifier(string identifier)
        {
            // Special case: "none" means the concept should be null/absent
            if (identifier == "none")
            {
                return new NoneMatcher();
            }

            var colonIndex = identifier.IndexOf(':');
            if (colonIndex < 0)
            {
                throw new BehaviourTestException($"Invalid concept identifier format: {identifier}");
            }

            var identifierType = identifier.Substring(0, colonIndex);
            var identifierBody = identifier.Substring(colonIndex + 1);

            switch (identifierType)
            {
                case "label":
                    return new TypeLabelMatcher(identifierBody);
                case "key":
                    return new ThingKeyMatcher(identifierBody);
                case "attr":
                    return new AttributeValueMatcher(identifierBody);
                case "value":
                    return new ValueMatcher(identifierBody);
                default:
                    throw new BehaviourTestException($"Unknown concept identifier type: {identifierType}");
            }
        }

        /// <summary>
        /// Checks if a set of concept identifiers matches a concept row.
        /// </summary>
        private bool AnswerConceptsMatch(Dictionary<string, string> answerIdentifier, IConceptRow row)
        {
            foreach (var kv in answerIdentifier)
            {
                var variable = kv.Key;
                var conceptIdentifier = kv.Value;
                var concept = row.Get(variable);

                // Special handling for "none" - the concept should be null
                if (conceptIdentifier == "none")
                {
                    if (concept != null)
                        return false;
                    continue;
                }

                var matcher = ParseConceptIdentifier(conceptIdentifier);

                if (concept == null || !matcher.Matches(concept))
                    return false;
            }
            return true;
        }

        [Given(@"uniquely identify answer concepts")]
        [Then(@"uniquely identify answer concepts")]
        public void UniquelyIdentifyAnswerConcepts(DataTable table)
        {
            CollectRowsAnswerIfNeeded();

            // Parse table header to get variable names
            var rows = table.Rows.ToList();
            var headerRow = rows[0];
            var varNames = headerRow.Cells.Select(c => c.Value).ToList();

            // Parse expected answer identifiers from table
            var answerIdentifiers = new List<Dictionary<string, string>>();
            for (int i = 1; i < rows.Count; i++)
            {
                var row = rows[i];
                var cells = row.Cells.ToList();
                var identifier = new Dictionary<string, string>();
                for (int j = 0; j < varNames.Count; j++)
                {
                    identifier[varNames[j]] = cells[j].Value;
                }
                answerIdentifiers.Add(identifier);
            }

            // Verify the number of answers matches
            Assert.Equal(answerIdentifiers.Count, _collectedRows!.Count);

            // Track which answers match each identifier
            var resultSet = answerIdentifiers.Select(ai => (ai, new List<IConceptRow>())).ToList();

            foreach (var answer in _collectedRows!)
            {
                foreach (var (answerIdentifier, matchedAnswers) in resultSet)
                {
                    if (AnswerConceptsMatch(answerIdentifier, answer))
                    {
                        matchedAnswers.Add(answer);
                    }
                }
            }

            // Each identifier should match exactly one answer
            foreach (var (answerIdentifier, matchedAnswers) in resultSet)
            {
                var identifierStr = string.Join(", ", answerIdentifier.Select(kv => $"{kv.Key}={kv.Value}"));
                Assert.True(matchedAnswers.Count == 1,
                    $"Each answer identifier should match precisely 1 answer, but [{matchedAnswers.Count}] matched the identifier [{identifierStr}].");
            }
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

        #region Concurrent Query Steps

        private void ClearConcurrentAnswers()
        {
            if (_concurrentRowStreams != null)
            {
                foreach (var stream in _concurrentRowStreams)
                {
                    stream.Dispose();
                }
                _concurrentRowStreams = null;
            }
            _concurrentAnswers = null;
        }

        private void EnsureConcurrentRowStreams()
        {
            if (_concurrentRowStreams == null)
            {
                Assert.NotNull(_concurrentAnswers);
                _concurrentRowStreams = _concurrentAnswers!
                    .Select(a => a.AsConceptRows().GetEnumerator())
                    .ToList();
            }
        }

        [When(@"concurrently get answers of typeql read query (\d+) times")]
        [Given(@"concurrently get answers of typeql read query (\d+) times")]
        [When(@"concurrently get answers of typeql write query (\d+) times")]
        [Given(@"concurrently get answers of typeql write query (\d+) times")]
        [When(@"concurrently get answers of typeql schema query (\d+) times")]
        [Given(@"concurrently get answers of typeql schema query (\d+) times")]
        public void ConcurrentlyGetAnswersOfTypeqlQuery(int count, DocString query)
        {
            ClearConcurrentAnswers();
            ClearAnswers();

            var tasks = new Task<IQueryAnswer>[count];
            for (int i = 0; i < count; i++)
            {
                tasks[i] = Task.Run(() =>
                {
                    if (_queryOptions != null)
                        return Tx.Query(query.Content, _queryOptions);
                    else
                        return Tx.Query(query.Content);
                });
            }

            Task.WaitAll(tasks);
            _concurrentAnswers = tasks.Select(t => t.Result).ToList();
        }

        [Then(@"concurrently process (\d+) rows? from answers")]
        [Given(@"concurrently process (\d+) rows? from answers")]
        public void ConcurrentlyProcessRowsFromAnswers(int count)
        {
            EnsureConcurrentRowStreams();

            foreach (var stream in _concurrentRowStreams!)
            {
                for (int i = 0; i < count; i++)
                {
                    Assert.True(stream.MoveNext(),
                        "Expected more rows but stream was exhausted");
                }
            }
        }

        [Then(@"concurrently process (\d+) rows? from answers; fails")]
        [Given(@"concurrently process (\d+) rows? from answers; fails")]
        public void ConcurrentlyProcessRowsFromAnswersFails(int count)
        {
            EnsureConcurrentRowStreams();

            foreach (var stream in _concurrentRowStreams!)
            {
                bool exhausted = false;
                for (int i = 0; i < count; i++)
                {
                    if (!stream.MoveNext())
                    {
                        exhausted = true;
                        break;
                    }
                }
                Assert.True(exhausted,
                    "Expected row processing to fail (stream exhausted), but it succeeded");
            }
        }

        #endregion
    }
}
