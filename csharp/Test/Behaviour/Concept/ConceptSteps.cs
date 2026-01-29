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
using System.Collections.Generic;
using System.Linq;
using Xunit;
using Xunit.Gherkin.Quick;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        #region Concept Kind Helpers

        /// <summary>
        /// Checks whether a concept satisfies a "kind" check (concept kind or value type).
        /// Handles both concept kinds (type, instance, entity type, etc.)
        /// and value types (boolean, integer, etc.).
        /// </summary>
        private bool CheckConceptIs(IConcept concept, string kind)
        {
            switch (kind.ToLower().Trim())
            {
                // Concept kinds
                case "type": return concept.IsType();
                case "instance": return concept.IsInstance();
                case "value": return concept.IsValue();
                case "entity type": return concept.IsEntityType();
                case "relation type": return concept.IsRelationType();
                case "attribute type": return concept.IsAttributeType();
                case "role type": return concept.IsRoleType();
                case "entity": return concept.IsEntity();
                case "relation": return concept.IsRelation();
                case "attribute": return concept.IsAttribute();
                // Value types
                case "boolean": return concept.IsBoolean();
                case "integer": return concept.IsInteger();
                case "double": return concept.IsDouble();
                case "decimal": return concept.IsDecimal();
                case "string": return concept.IsString();
                case "date": return concept.IsDate();
                case "datetime": return concept.IsDatetime();
                case "datetime-tz": return concept.IsDatetimeTZ();
                case "duration": return concept.IsDuration();
                case "struct": return concept.IsStruct();
                default:
                    throw new BehaviourTestException($"Unknown concept kind or value type: {kind}");
            }
        }

        /// <summary>
        /// Casts a concept to a specific concept kind and returns it.
        /// </summary>
        private IConcept UnwrapConceptAs(IConcept concept, string kind)
        {
            switch (kind.ToLower().Trim())
            {
                case "type": return concept.AsType();
                case "instance": return concept.AsThing();
                case "entity type": return concept.AsEntityType();
                case "relation type": return concept.AsRelationType();
                case "attribute type": return concept.AsAttributeType();
                case "role type": return concept.AsRoleType();
                case "entity": return concept.AsEntity();
                case "relation": return concept.AsRelation();
                case "attribute": return concept.AsAttribute();
                case "value": return concept.AsValue();
                default:
                    throw new BehaviourTestException($"Unknown concept kind for cast: {kind}");
            }
        }

        /// <summary>
        /// Gets a concept from a row, first casting it via the concept kind accessor.
        /// For "variable", gets by variable name directly.
        /// For specific kinds (entity, attribute type, etc.), gets by variable and casts.
        /// </summary>
        private IConcept GetConceptForKind(int rowIndex, string conceptKind, string variable)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept;
            switch (conceptKind.ToLower().Trim())
            {
                case "variable":
                    concept = GetRowGetConcept(rowIndex, variable);
                    break;
                default:
                    concept = GetRowGetConcept(rowIndex, variable);
                    if (concept != null)
                    {
                        concept = UnwrapConceptAs(concept, conceptKind);
                    }
                    break;
            }
            Assert.NotNull(concept);
            return concept!;
        }

        /// <summary>
        /// Gets the label of a concept after casting to a specific kind.
        /// </summary>
        private string GetLabelOfConceptKind(IConcept concept, string conceptKind)
        {
            switch (conceptKind.ToLower().Trim())
            {
                case "variable": return concept.GetLabel();
                case "type": return concept.AsType().GetLabel();
                case "instance": return concept.AsThing().GetLabel();
                case "entity type": return concept.AsEntityType().GetLabel();
                case "relation type": return concept.AsRelationType().GetLabel();
                case "attribute type": return concept.AsAttributeType().GetLabel();
                case "role type": return concept.AsRoleType().GetLabel();
                case "entity": return concept.AsEntity().GetLabel();
                case "relation": return concept.AsRelation().GetLabel();
                case "attribute": return concept.AsAttribute().GetLabel();
                case "value": return concept.AsValue().GetLabel();
                default:
                    throw new BehaviourTestException($"Unknown concept kind for get label: {conceptKind}");
            }
        }

        /// <summary>
        /// Try-gets the label of a concept after casting to a specific kind.
        /// </summary>
        private string? TryGetLabelOfConceptKind(IConcept concept, string conceptKind)
        {
            switch (conceptKind.ToLower().Trim())
            {
                case "variable": return concept.TryGetLabel();
                case "type": return concept.AsType().TryGetLabel();
                case "instance": return concept.AsThing().TryGetLabel();
                case "entity type": return concept.AsEntityType().TryGetLabel();
                case "relation type": return concept.AsRelationType().TryGetLabel();
                case "attribute type": return concept.AsAttributeType().TryGetLabel();
                case "role type": return concept.AsRoleType().TryGetLabel();
                case "entity": return concept.AsEntity().TryGetLabel();
                case "relation": return concept.AsRelation().TryGetLabel();
                case "attribute": return concept.AsAttribute().TryGetLabel();
                case "value": return concept.AsValue().TryGetLabel();
                default:
                    throw new BehaviourTestException($"Unknown concept kind for try get label: {conceptKind}");
            }
        }

        /// <summary>
        /// Try-gets the IID of a concept after casting to a specific kind.
        /// </summary>
        private string? TryGetIIDOfConceptKind(IConcept concept, string conceptKind)
        {
            switch (conceptKind.ToLower().Trim())
            {
                case "variable": return concept.TryGetIID();
                case "entity": return concept.AsEntity().TryGetIID();
                case "relation": return concept.AsRelation().TryGetIID();
                case "attribute": return concept.AsAttribute().TryGetIID();
                case "instance": return concept.AsThing().TryGetIID();
                default:
                    return concept.TryGetIID();
            }
        }

        /// <summary>
        /// Try-gets the value type string of a concept after casting to a specific kind.
        /// </summary>
        private string? TryGetValueTypeOfConceptKind(IConcept concept, string conceptKind)
        {
            switch (conceptKind.ToLower().Trim())
            {
                case "variable": return concept.TryGetValueType();
                case "attribute type": return concept.AsAttributeType().TryGetValueType();
                case "value": return concept.AsValue().TryGetValueType();
                default:
                    return concept.TryGetValueType();
            }
        }

        /// <summary>
        /// Try-gets the value of a concept after casting to a specific kind.
        /// </summary>
        private IValue? TryGetValueOfConceptKind(IConcept concept, string conceptKind)
        {
            switch (conceptKind.ToLower().Trim())
            {
                case "variable": return concept.TryGetValue();
                case "attribute": return concept.AsAttribute().TryGetValue();
                case "value": return concept.AsValue().TryGetValue();
                default:
                    return concept.TryGetValue();
            }
        }

        /// <summary>
        /// Gets the type of a thing concept (entity, relation, attribute, instance).
        /// </summary>
        private IConcept GetTypeOfConceptKind(IConcept concept, string conceptKind)
        {
            switch (conceptKind.ToLower().Trim())
            {
                case "instance": return concept.AsThing().Type;
                case "entity": return concept.AsEntity().Type;
                case "relation": return concept.AsRelation().Type;
                case "attribute": return concept.AsAttribute().Type;
                default:
                    throw new BehaviourTestException(
                        $"Concept kind does not have a type: {conceptKind}");
            }
        }

        #endregion

        #region Value Comparison Helpers

        /// <summary>
        /// Gets the IValue from a concept based on its kind (attribute or value).
        /// </summary>
        private IValue GetValueFromConcept(IConcept concept, string conceptKind)
        {
            switch (conceptKind.ToLower().Trim())
            {
                case "attribute": return concept.AsAttribute().Value;
                case "value": return concept.AsValue();
                default:
                    throw new BehaviourTestException(
                        $"Concept kind does not have values: {conceptKind}");
            }
        }

        /// <summary>
        /// Gets a specific value type from an IValue (e.g., getBoolean, getInteger).
        /// Returns the unwrapped value as an object.
        /// </summary>
        private object GetValueAs(IValue value, string valueType)
        {
            switch (valueType.ToLower().Trim())
            {
                case "boolean": return value.GetBoolean();
                case "integer": return value.GetInteger();
                case "double": return value.GetDouble();
                case "decimal": return value.GetDecimal();
                case "string": return value.GetString();
                case "date": return value.GetDate();
                case "datetime": return value.GetDatetime();
                case "datetime-tz": return value.GetDatetimeTZ();
                case "duration": return value.GetDuration();
                case "struct": return value.GetStruct().ToString()!;
                default:
                    throw new BehaviourTestException($"Unknown value type: {valueType}");
            }
        }

        /// <summary>
        /// Try-gets a specific value type from a concept (via IConcept.TryGet*).
        /// Returns null if the concept doesn't have that value type.
        /// </summary>
        private object? TryGetValueTypeFromConcept(IConcept concept, string valueType)
        {
            switch (valueType.ToLower().Trim())
            {
                case "boolean":
                    var b = concept.TryGetBoolean();
                    return b.HasValue ? (object)b.Value : null;
                case "integer":
                    var i = concept.TryGetInteger();
                    return i.HasValue ? (object)i.Value : null;
                case "double":
                    var d = concept.TryGetDouble();
                    return d.HasValue ? (object)d.Value : null;
                case "decimal":
                    var dec = concept.TryGetDecimal();
                    return dec.HasValue ? (object)dec.Value : null;
                case "string":
                    return concept.TryGetString();
                case "date":
                    var dt = concept.TryGetDate();
                    return dt.HasValue ? (object)dt.Value : null;
                case "datetime":
                    var dtm = concept.TryGetDatetime();
                    return dtm.HasValue ? (object)dtm.Value : null;
                case "datetime-tz":
                    var dtz = concept.TryGetDatetimeTZ();
                    return dtz.HasValue ? (object)dtz.Value : null;
                case "duration":
                    return concept.TryGetDuration();
                case "struct":
                    var s = concept.TryGetStruct();
                    return s != null ? s.ToString() : null;
                case "value":
                    return concept.TryGetValue();
                default:
                    throw new BehaviourTestException($"Unknown value type: {valueType}");
            }
        }


        #endregion

        #region Concept Kind Check Steps (is type, is entity type, is boolean, etc.)

        // Pattern: answer get row(N) get variable(VAR) is KIND: BOOL
        // KIND can be concept kind (type, instance, entity type, etc.) or value type (boolean, integer, etc.)
        [Then(@"answer get row\((\d+)\) get variable\(([^)]+)\) is (entity type|relation type|attribute type|role type|type|instance|value|entity|relation|attribute|datetime-tz|datetime|boolean|integer|double|decimal|string|date|duration|struct): (true|false)")]
        public void AnswerGetRowGetVariableIsKind(int rowIndex, string variable, string checkedKind, bool expected)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.Equal(expected, CheckConceptIs(concept!, checkedKind));
        }

        // Pattern: answer get row(N) get CONCEPT_KIND(VAR) is KIND: BOOL
        // For unwrapped concepts (entity type, attribute type, entity, attribute, value, etc.)
        [Then(@"answer get row\((\d+)\) get (entity type|relation type|attribute type|role type|type|instance|entity|relation|attribute|value)\(([^)]+)\) is (entity type|relation type|attribute type|role type|type|instance|value|entity|relation|attribute|datetime-tz|datetime|boolean|integer|double|decimal|string|date|duration|struct): (true|false)")]
        public void AnswerGetRowGetConceptKindIsKind(
            int rowIndex, string conceptKind, string variable, string checkedKind, bool expected)
        {
            CollectRowsAnswerIfNeeded();
            IConcept concept = GetConceptForKind(rowIndex, conceptKind, variable);
            Assert.Equal(expected, CheckConceptIs(concept, checkedKind));
        }

        #endregion

        #region Concept Cast Steps (as entity type, as value, etc.)

        // Pattern: answer get row(N) get variable(VAR) as CONCEPT_KIND
        [Then(@"answer get row\((\d+)\) get variable\(([^)]+)\) as (entity type|relation type|attribute type|role type|type|instance|entity|relation|attribute|value)$")]
        [When(@"answer get row\((\d+)\) get variable\(([^)]+)\) as (entity type|relation type|attribute type|role type|type|instance|entity|relation|attribute|value)$")]
        public void AnswerGetRowGetVariableAs(int rowIndex, string variable, string conceptKind)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            var result = UnwrapConceptAs(concept!, conceptKind);
            Assert.NotNull(result);
        }

        // Pattern: answer get row(N) get variable(VAR) as CONCEPT_KIND; fails with a message containing: "MSG"
        [Then(@"answer get row\((\d+)\) get variable\(([^)]+)\) as (entity type|relation type|attribute type|role type|type|instance|entity|relation|attribute|value); fails with a message containing: ""(.*)""")]
        public void AnswerGetRowGetVariableAsFailsWithMessage(
            int rowIndex, string variable, string conceptKind, string expectedMessage)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            var exception = Assert.ThrowsAny<Exception>(
                () => UnwrapConceptAs(concept!, conceptKind));
            Assert.Contains(expectedMessage, exception.Message);
        }

        #endregion

        #region Label Steps

        // Pattern: answer get row(N) get variable(VAR) get label: LABEL
        [Then(@"answer get row\((\d+)\) get variable\(([^)]+)\) get label: (.+)")]
        public void AnswerGetRowGetVariableGetLabel(int rowIndex, string variable, string label)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.Equal(label, concept!.GetLabel());
        }

        // Pattern: answer get row(N) get variable(VAR) try get label: LABEL
        // (Matches the "try get label: VALUE" form — distinct from "try get label is (not) none")
        [Then(@"answer get row\((\d+)\) get variable\(([^)]+)\) try get label: (.+)")]
        public void AnswerGetRowGetVariableTryGetLabel(int rowIndex, string variable, string label)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.Equal(label, concept!.TryGetLabel());
        }

        // Pattern: answer get row(N) get CONCEPT_KIND(VAR) get label: LABEL
        // Handles concept kinds NOT already in QuerySteps.cs: type, instance, role type, entity, relation, attribute, value
        [Then(@"answer get row\((\d+)\) get (type|instance|role type|entity|relation|attribute|value)\(([^)]+)\) get label: (.+)")]
        public void AnswerGetRowGetConceptKindGetLabel(
            int rowIndex, string conceptKind, string variable, string label)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.Equal(label, GetLabelOfConceptKind(concept!, conceptKind));
        }

        // Pattern: answer get row(N) get entity type(VAR) try get label: LABEL
        [Then(@"answer get row\((\d+)\) get (entity type|relation type|attribute type|role type|type|instance|entity|relation|attribute|value)\(([^)]+)\) try get label: (.+)")]
        public void AnswerGetRowGetConceptKindTryGetLabel(
            int rowIndex, string conceptKind, string variable, string label)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.Equal(label, TryGetLabelOfConceptKind(concept!, conceptKind));
        }

        // Pattern: answer get row(N) get instance(VAR) get type get label: LABEL
        // (entity, relation, attribute "get type get label" already exist in QuerySteps.cs)
        [Then(@"answer get row\((\d+)\) get instance\(([^)]+)\) get type get label: (.+)")]
        public void AnswerGetRowGetInstanceGetTypeGetLabel(int rowIndex, string variable, string label)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.Equal(label, concept!.AsThing().Type.GetLabel());
        }

        #endregion

        #region IID Steps

        // Pattern: answer get row(N) get variable(VAR) try get iid is none
        [Then(@"answer get row\((\d+)\) get variable\(([^)]+)\) try get iid is none")]
        public void AnswerGetRowGetVariableTryGetIIDIsNone(int rowIndex, string variable)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.Null(concept!.TryGetIID());
        }

        // Pattern: answer get row(N) get CONCEPT_KIND(VAR) try get iid is not none
        [Then(@"answer get row\((\d+)\) get (entity|relation|attribute|instance|variable)\(([^)]+)\) try get iid is not none")]
        public void AnswerGetRowGetConceptKindTryGetIIDIsNotNone(
            int rowIndex, string conceptKind, string variable)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.NotNull(TryGetIIDOfConceptKind(concept!, conceptKind));
        }

        // Pattern: answer get row(N) get CONCEPT_KIND(VAR) contains iid
        [Then(@"answer get row\((\d+)\) get (entity|relation)\(([^)]+)\) contains iid")]
        public void AnswerGetRowGetConceptKindContainsIID(
            int rowIndex, string conceptKind, string variable)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            string? iid = TryGetIIDOfConceptKind(concept!, conceptKind);
            Assert.NotNull(iid);
            Assert.True(!string.IsNullOrEmpty(iid));
        }

        #endregion

        #region Value Type Metadata Steps

        // Pattern: answer get row(N) get variable(VAR) try get value type is none
        [Then(@"answer get row\((\d+)\) get variable\(([^)]+)\) try get value type is none")]
        public void AnswerGetRowGetVariableTryGetValueTypeIsNone(int rowIndex, string variable)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.Null(concept!.TryGetValueType());
        }

        // Pattern: answer get row(N) get CONCEPT_KIND(VAR) try get value type is none
        [Then(@"answer get row\((\d+)\) get (entity type|relation type|attribute type|role type|type|instance|entity|relation|attribute|value)\(([^)]+)\) try get value type is none")]
        public void AnswerGetRowGetConceptKindTryGetValueTypeIsNone(
            int rowIndex, string conceptKind, string variable)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.Null(TryGetValueTypeOfConceptKind(concept!, conceptKind));
        }

        // Pattern: answer get row(N) get CONCEPT_KIND(VAR) try get value type: VALUE
        [Then(@"answer get row\((\d+)\) get (entity type|relation type|attribute type|role type|type|instance|entity|relation|attribute|value)\(([^)]+)\) try get value type: (.+)")]
        public void AnswerGetRowGetConceptKindTryGetValueType(
            int rowIndex, string conceptKind, string variable, string expectedValueType)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            string? actualValueType = TryGetValueTypeOfConceptKind(concept!, conceptKind);
            Assert.NotNull(actualValueType);
            Assert.Equal(expectedValueType, actualValueType);
        }

        // Pattern: answer get row(N) get CONCEPT_KIND(VAR) get value type: VALUE
        [Then(@"answer get row\((\d+)\) get (attribute type|attribute|value)\(([^)]+)\) get value type: (.+)")]
        public void AnswerGetRowGetConceptKindGetValueType(
            int rowIndex, string conceptKind, string variable, string expectedValueType)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            string actualValueType;
            switch (conceptKind.ToLower().Trim())
            {
                case "attribute type":
                    actualValueType = concept!.AsAttributeType().TryGetValueType() ?? "none";
                    break;
                case "value":
                    actualValueType = concept!.AsValue().GetValueType();
                    break;
                case "attribute":
                    actualValueType = concept!.AsAttribute().GetValueType();
                    break;
                default:
                    throw new BehaviourTestException(
                        $"Concept kind does not have value type: {conceptKind}");
            }
            Assert.Equal(expectedValueType, actualValueType);
        }

        // Pattern: answer get row(N) get attribute(VAR) get type get value type: VALUE
        [Then(@"answer get row\((\d+)\) get attribute\(([^)]+)\) get type get value type: (.+)")]
        public void AnswerGetRowGetAttributeGetTypeGetValueType(
            int rowIndex, string variable, string expectedValueType)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            string actualValueType = concept!.AsAttribute().Type.TryGetValueType() ?? "none";
            Assert.Equal(expectedValueType, actualValueType);
        }

        #endregion

        #region Try Get Value Steps (try get value is none, try get VALUE_TYPE is none)

        // Pattern: answer get row(N) get variable(VAR) try get value is none
        [Then(@"answer get row\((\d+)\) get variable\(([^)]+)\) try get value is none")]
        public void AnswerGetRowGetVariableTryGetValueIsNone(int rowIndex, string variable)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.Null(concept!.TryGetValue());
        }

        // Pattern: answer get row(N) get variable(VAR) try get VALUE_TYPE is none
        [Then(@"answer get row\((\d+)\) get variable\(([^)]+)\) try get (datetime-tz|datetime|boolean|integer|double|decimal|string|date|duration|struct) is none")]
        public void AnswerGetRowGetVariableTryGetValueTypeIsNone(
            int rowIndex, string variable, string valueType)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.Null(TryGetValueTypeFromConcept(concept!, valueType));
        }

        #endregion

        #region Value Retrieval and Comparison Steps

        // Pattern: answer get row(N) get CONCEPT_KIND(VAR) try get value is: VALUE
        [Then(@"answer get row\((\d+)\) get (attribute|value)\(([^)]+)\) try get value is: (.+)")]
        public void AnswerGetRowGetConceptKindTryGetValueIs(
            int rowIndex, string conceptKind, string variable, string expectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            IValue? value = TryGetValueOfConceptKind(concept!, conceptKind);
            Assert.NotNull(value);
            Assert.True(TestValueHelper.CompareValues(value!, expectedValue, null),
                $"Expected value '{expectedValue}' but got '{value}'");
        }

        // Pattern: answer get row(N) get CONCEPT_KIND(VAR) try get value is not: VALUE
        [Then(@"answer get row\((\d+)\) get (attribute|value)\(([^)]+)\) try get value is not: (.+)")]
        public void AnswerGetRowGetConceptKindTryGetValueIsNot(
            int rowIndex, string conceptKind, string variable, string notExpectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            IValue? value = TryGetValueOfConceptKind(concept!, conceptKind);
            Assert.NotNull(value);
            Assert.False(TestValueHelper.CompareValues(value!, notExpectedValue, null),
                $"Value should not equal '{notExpectedValue}' but it does");
        }

        // Pattern: answer get row(N) get CONCEPT_KIND(VAR) try get VALUE_TYPE is: VALUE
        [Then(@"answer get row\((\d+)\) get (attribute|value)\(([^)]+)\) try get (datetime-tz|datetime|boolean|integer|double|decimal|string|date|duration|struct) is: (.+)")]
        public void AnswerGetRowGetConceptKindTryGetValueTypeIs(
            int rowIndex, string conceptKind, string variable, string valueType, string expectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            IConcept unwrapped = UnwrapConceptAs(concept!, conceptKind);
            object? actual = TryGetValueTypeFromConcept(unwrapped, valueType);
            Assert.NotNull(actual);
            IValue? valueObj = TryGetValueOfConceptKind(unwrapped, conceptKind);
            Assert.NotNull(valueObj);
            Assert.True(TestValueHelper.CompareValues(valueObj!, expectedValue, valueType),
                $"Expected value '{expectedValue}' but got '{valueObj}'");
        }

        // Pattern: answer get row(N) get CONCEPT_KIND(VAR) try get VALUE_TYPE is not: VALUE
        [Then(@"answer get row\((\d+)\) get (attribute|value)\(([^)]+)\) try get (datetime-tz|datetime|boolean|integer|double|decimal|string|date|duration|struct) is not: (.+)")]
        public void AnswerGetRowGetConceptKindTryGetValueTypeIsNot(
            int rowIndex, string conceptKind, string variable, string valueType, string notExpectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            IConcept unwrapped = UnwrapConceptAs(concept!, conceptKind);
            object? actual = TryGetValueTypeFromConcept(unwrapped, valueType);
            Assert.NotNull(actual);
            IValue? valueObj = TryGetValueOfConceptKind(unwrapped, conceptKind);
            Assert.NotNull(valueObj);
            Assert.False(TestValueHelper.CompareValues(valueObj!, notExpectedValue, valueType),
                $"Value should not equal '{notExpectedValue}' but it does");
        }

        // Pattern: answer get row(N) get attribute(VAR) get value is: VALUE (non-quoted)
        // This handles non-string values. String values (quoted) are handled by QuerySteps.cs.
        [Then(@"answer get row\((\d+)\) get attribute\(([^)]+)\) get value is: ([^""].+)")]
        public void AnswerGetRowGetAttributeGetValueIs(
            int rowIndex, string variable, string expectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            IValue value = concept!.AsAttribute().Value;
            Assert.True(TestValueHelper.CompareValues(value, expectedValue, null),
                $"Expected value '{expectedValue}' but got '{value}'");
        }

        // Pattern: answer get row(N) get attribute(VAR) get value is not: VALUE
        [Then(@"answer get row\((\d+)\) get attribute\(([^)]+)\) get value is not: (.+)")]
        public void AnswerGetRowGetAttributeGetValueIsNot(
            int rowIndex, string variable, string notExpectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            IValue value = concept!.AsAttribute().Value;
            Assert.False(TestValueHelper.CompareValues(value, notExpectedValue, null),
                $"Value should not equal '{notExpectedValue}' but it does");
        }

        // Pattern: answer get row(N) get attribute(VAR) get VALUE_TYPE is: VALUE
        [Then(@"answer get row\((\d+)\) get attribute\(([^)]+)\) get (datetime-tz|datetime|boolean|integer|double|decimal|string|date|duration|struct) is: (.+)")]
        public void AnswerGetRowGetAttributeGetValueTypeIs(
            int rowIndex, string variable, string valueType, string expectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IValue value = GetRowGetValue(rowIndex, "attribute", variable);
            Assert.True(TestValueHelper.CompareValues(value, expectedValue, valueType),
                $"Expected value '{expectedValue}' but got '{value}'");
        }

        // Pattern: answer get row(N) get attribute(VAR) get VALUE_TYPE is not: VALUE
        [Then(@"answer get row\((\d+)\) get attribute\(([^)]+)\) get (datetime-tz|datetime|boolean|integer|double|decimal|string|date|duration|struct) is not: (.+)")]
        public void AnswerGetRowGetAttributeGetValueTypeIsNot(
            int rowIndex, string variable, string valueType, string notExpectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IValue value = GetRowGetValue(rowIndex, "attribute", variable);
            Assert.False(TestValueHelper.CompareValues(value, notExpectedValue, valueType),
                $"Value should not equal '{notExpectedValue}' but it does");
        }

        // Pattern: answer get row(N) get attribute(VAR) get VALUE_TYPE (just success, no comparison)
        [Then(@"answer get row\((\d+)\) get attribute\(([^)]+)\) get (datetime-tz|datetime|boolean|integer|double|decimal|string|date|duration|struct)$")]
        public void AnswerGetRowGetAttributeGetValueType(
            int rowIndex, string variable, string valueType)
        {
            CollectRowsAnswerIfNeeded();
            IValue value = GetRowGetValue(rowIndex, "attribute", variable);
            var result = GetValueAs(value, valueType);
            Assert.NotNull(result);
        }

        // Pattern: answer get row(N) get attribute(VAR) get VALUE_TYPE; fails with a message containing: "MSG"
        [Then(@"answer get row\((\d+)\) get attribute\(([^)]+)\) get (datetime-tz|datetime|boolean|integer|double|decimal|string|date|duration|struct); fails with a message containing: ""(.*)""")]
        public void AnswerGetRowGetAttributeGetValueTypeFails(
            int rowIndex, string variable, string valueType, string expectedMessage)
        {
            CollectRowsAnswerIfNeeded();
            var exception = Assert.ThrowsAny<Exception>(() =>
            {
                IValue value = GetRowGetValue(rowIndex, "attribute", variable);
                GetValueAs(value, valueType);
            });
            Assert.Contains(expectedMessage, exception.Message);
        }

        #endregion

        #region Value Concept Steps (get value(VAR) get is:, get VALUE_TYPE is:, etc.)

        // Pattern: answer get row(N) get value(VAR) get is: VALUE
        [Then(@"answer get row\((\d+)\) get value\(([^)]+)\) get is: (.+)")]
        public void AnswerGetRowGetValueGetIs(int rowIndex, string variable, string expectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            IValue value = concept!.AsValue();
            Assert.True(TestValueHelper.CompareValues(value, expectedValue, null),
                $"Expected value '{expectedValue}' but got '{value}'");
        }

        // Pattern: answer get row(N) get value(VAR) get is not: VALUE
        [Then(@"answer get row\((\d+)\) get value\(([^)]+)\) get is not: (.+)")]
        public void AnswerGetRowGetValueGetIsNot(int rowIndex, string variable, string notExpectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            IValue value = concept!.AsValue();
            Assert.False(TestValueHelper.CompareValues(value, notExpectedValue, null),
                $"Value should not equal '{notExpectedValue}' but it does");
        }

        // Pattern: answer get row(N) get value(VAR) try get value is: VALUE
        [Then(@"answer get row\((\d+)\) get value\(([^)]+)\) try get value is: (.+)")]
        public void AnswerGetRowGetValueTryGetValueIs(
            int rowIndex, string variable, string expectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            IValue? value = concept!.AsValue().TryGetValue();
            Assert.NotNull(value);
            Assert.True(TestValueHelper.CompareValues(value!, expectedValue, null),
                $"Expected value '{expectedValue}' but got '{value}'");
        }

        // Pattern: answer get row(N) get value(VAR) try get value is not: VALUE
        [Then(@"answer get row\((\d+)\) get value\(([^)]+)\) try get value is not: (.+)")]
        public void AnswerGetRowGetValueTryGetValueIsNot(
            int rowIndex, string variable, string notExpectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            IValue? value = concept!.AsValue().TryGetValue();
            Assert.NotNull(value);
            Assert.False(TestValueHelper.CompareValues(value!, notExpectedValue, null),
                $"Value should not equal '{notExpectedValue}' but it does");
        }

        // Pattern: answer get row(N) get value(VAR) get VALUE_TYPE is: VALUE
        [Then(@"answer get row\((\d+)\) get value\(([^)]+)\) get (datetime-tz|datetime|boolean|integer|double|decimal|string|date|duration|struct) is: (.+)")]
        public void AnswerGetRowGetValueGetValueTypeIs(
            int rowIndex, string variable, string valueType, string expectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            IValue value = concept!.AsValue();
            Assert.True(TestValueHelper.CompareValues(value, expectedValue, valueType),
                $"Expected value '{expectedValue}' but got '{value}'");
        }

        // Pattern: answer get row(N) get value(VAR) get VALUE_TYPE is not: VALUE
        [Then(@"answer get row\((\d+)\) get value\(([^)]+)\) get (datetime-tz|datetime|boolean|integer|double|decimal|string|date|duration|struct) is not: (.+)")]
        public void AnswerGetRowGetValueGetValueTypeIsNot(
            int rowIndex, string variable, string valueType, string notExpectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            IValue value = concept!.AsValue();
            Assert.False(TestValueHelper.CompareValues(value, notExpectedValue, valueType),
                $"Value should not equal '{notExpectedValue}' but it does");
        }

        // Pattern: answer get row(N) get value(VAR) try get VALUE_TYPE is: VALUE
        [Then(@"answer get row\((\d+)\) get value\(([^)]+)\) try get (datetime-tz|datetime|boolean|integer|double|decimal|string|date|duration|struct) is: (.+)")]
        public void AnswerGetRowGetValueTryGetValueTypeIs(
            int rowIndex, string variable, string valueType, string expectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            IValue value = concept!.AsValue();
            object? actual = TryGetValueTypeFromConcept(value, valueType);
            Assert.NotNull(actual);
            Assert.True(TestValueHelper.CompareValues(value, expectedValue, valueType),
                $"Expected value '{expectedValue}' but got '{value}'");
        }

        // Pattern: answer get row(N) get value(VAR) try get VALUE_TYPE is not: VALUE
        [Then(@"answer get row\((\d+)\) get value\(([^)]+)\) try get (datetime-tz|datetime|boolean|integer|double|decimal|string|date|duration|struct) is not: (.+)")]
        public void AnswerGetRowGetValueTryGetValueTypeIsNot(
            int rowIndex, string variable, string valueType, string notExpectedValue)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            IValue value = concept!.AsValue();
            object? actual = TryGetValueTypeFromConcept(value, valueType);
            Assert.NotNull(actual);
            Assert.False(TestValueHelper.CompareValues(value, notExpectedValue, valueType),
                $"Value should not equal '{notExpectedValue}' but it does");
        }

        // Pattern: answer get row(N) get value(VAR) try get value type: VALUE_TYPE
        [Then(@"answer get row\((\d+)\) get value\(([^)]+)\) try get value type: (.+)")]
        public void AnswerGetRowGetValueTryGetValueType(
            int rowIndex, string variable, string expectedValueType)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            string? actualValueType = concept!.AsValue().TryGetValueType();
            Assert.NotNull(actualValueType);
            Assert.Equal(expectedValueType, actualValueType);
        }

        // Pattern: answer get row(N) get value(VAR) get value type: VALUE_TYPE
        [Then(@"answer get row\((\d+)\) get value\(([^)]+)\) get value type: (.+)")]
        public void AnswerGetRowGetValueGetValueType(
            int rowIndex, string variable, string expectedValueType)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.Equal(expectedValueType, concept!.AsValue().GetValueType());
        }

        // Pattern: answer get row(N) get value(VAR) get label: LABEL
        [Then(@"answer get row\((\d+)\) get value\(([^)]+)\) get label: (.+)")]
        public void AnswerGetRowGetValueGetLabel(int rowIndex, string variable, string label)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            Assert.Equal(label, concept!.AsValue().GetLabel());
        }

        #endregion

        #region Type Property Steps (get type is, get type get label)

        // Pattern: answer get row(N) get CONCEPT_KIND(VAR) get type is CHECKED_KIND: BOOL
        [Then(@"answer get row\((\d+)\) get (instance|entity|relation|attribute)\(([^)]+)\) get type is (entity type|relation type|attribute type|role type|type|instance|value|entity|relation|attribute|datetime-tz|datetime|boolean|integer|double|decimal|string|date|duration|struct): (true|false)")]
        public void AnswerGetRowGetConceptKindGetTypeIsKind(
            int rowIndex, string conceptKind, string variable, string checkedKind, bool expected)
        {
            CollectRowsAnswerIfNeeded();
            IConcept? concept = GetRowGetConcept(rowIndex, variable);
            Assert.NotNull(concept);
            IConcept typeConcept = GetTypeOfConceptKind(concept!, conceptKind);
            Assert.Equal(expected, CheckConceptIs(typeConcept, checkedKind));
        }

        #endregion

        #region Answer Unwrap Error Steps

        // Pattern: answer unwraps as ok; fails
        [Then(@"answer unwraps as ok; fails")]
        public void AnswerUnwrapsAsOkFails()
        {
            Assert.ThrowsAny<Exception>(() => _queryAnswer!.AsOk());
        }

        // Pattern: answer unwraps as concept documents; fails
        [Then(@"answer unwraps as concept documents; fails")]
        public void AnswerUnwrapsAsConceptDocumentsFails()
        {
            Assert.ThrowsAny<Exception>(() => _queryAnswer!.AsConceptDocuments());
        }

        #endregion

        #region Variable Access Error Steps

        // Pattern: answer get row(N) get variable(VAR); fails with a message containing: "MSG"
        // This handles non-empty variable names that don't exist
        [Then(@"answer get row\((\d+)\) get variable\(([^)]+)\); fails with a message containing: ""(.*)""")]
        public void AnswerGetRowGetVariableFailsWithMessage(
            int rowIndex, string variable, string expectedMessage)
        {
            CollectRowsAnswerIfNeeded();
            var exception = Assert.ThrowsAny<Exception>(
                () => _collectedRows![rowIndex].Get(variable));
            Assert.Contains(expectedMessage, exception.Message);
        }

        // Pattern: answer get row(N) get variable(); fails with a message containing: "MSG"
        // Handles empty variable name
        [Then(@"answer get row\((\d+)\) get variable\(\); fails with a message containing: ""(.*)""")]
        public void AnswerGetRowGetVariableEmptyFailsWithMessage(
            int rowIndex, string expectedMessage)
        {
            CollectRowsAnswerIfNeeded();
            var exception = Assert.ThrowsAny<Exception>(
                () => _collectedRows![rowIndex].Get(""));
            Assert.Contains(expectedMessage, exception.Message);
        }

        // Pattern: answer get row(N) get variable by index(N); fails with a message containing: "MSG"
        [Then(@"answer get row\((\d+)\) get variable by index\((\d+)\); fails with a message containing: ""(.*)""")]
        public void AnswerGetRowGetVariableByIndexFailsWithMessage(
            int rowIndex, int varIndex, string expectedMessage)
        {
            CollectRowsAnswerIfNeeded();
            var exception = Assert.ThrowsAny<Exception>(
                () => _collectedRows![rowIndex].GetIndex(varIndex));
            Assert.Contains(expectedMessage, exception.Message);
        }

        #endregion

    }
}
