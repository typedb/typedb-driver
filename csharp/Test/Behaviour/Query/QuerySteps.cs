/*
 * Copyright (C) 2022 Vaticle
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License")]; you may not use this file except in compliance
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
using DocString = Gherkin.Ast.DocString;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
using Xunit;
using Xunit.Gherkin.Quick;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Common;

using QueryError = Vaticle.Typedb.Driver.Common.Error.Query; // TODO: Change to TypeDB.Driver.Common..... everywhere.

namespace Vaticle.Typedb.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        [Given(@"typeql define")]
        [When(@"typeql define")]
        [Then(@"typeql define")]
        public void TypeqlDefine(DocString defineQueryStatements)
        {
            SingleTransaction.Query.Define(defineQueryStatements.Content).Resolve();
        }

        [Given(@"typeql define; throws exception")]
        [When(@"typeql define; throws exception")]
        [Then(@"typeql define; throws exception")]
        public void TypeqlDefineThrowsException(DocString defineQueryStatements)
        {
            Assert.Throws<TypeDBDriverException>(() => TypeqlDefine(defineQueryStatements));
        }

        [Then(@"typeql define; throws exception containing {string}")]
        public void TypeqlDefineThrowsExceptionContaining(string expectedMessage, DocString defineQueryStatements)
        {
            var exception = Assert.Throws<TypeDBDriverException>(
                () => TypeqlDefine(defineQueryStatements));

            Assert.Contains(expectedMessage, exception.Message);
        }

        [Given(@"typeql undefine")]
        [When(@"typeql undefine")]
        [Then(@"typeql undefine")]
        public void TypeqlUndefine(DocString undefineQueryStatements)
        {
            SingleTransaction.Query.Undefine(undefineQueryStatements.Content).Resolve();
        }

        [Given(@"typeql undefine; throws exception")]
        [When(@"typeql undefine; throws exception")]
        [Then(@"typeql undefine; throws exception")]
        public void TypeQlUndefineThrowsException(DocString undefineQueryStatements)
        {
            Assert.Throws<TypeDBDriverException>(() => TypeqlUndefine(undefineQueryStatements));
        }

        [Then(@"typeql undefine; throws exception containing {string}")]
        public void TypeqlUndefineThrowsExceptionContaining(string expectedMessage, DocString undefineQueryStatements)
        {
            var exception = Assert.Throws<TypeDBDriverException>(
                () => TypeqlUndefine(undefineQueryStatements));

            Assert.Contains(expectedMessage, exception.Message);
        }

        [Given(@"typeql insert")]
        [When(@"typeql insert")]
        [Then(@"typeql insert")]
        public IConceptMap[] TypeqlInsert(DocString insertQueryStatements)
        {
            return SingleTransaction.Query.Insert(insertQueryStatements.Content).ToArray();
        }

        [Given(@"typeql insert; throws exception")]
        [When(@"typeql insert; throws exception")]
        [Then(@"typeql insert; throws exception")]
        public void TypeqlInsertThrowsException(DocString insertQueryStatements)
        {
            Assert.Throws<TypeDBDriverException>(() => TypeqlInsert(insertQueryStatements));
        }

        [Then(@"typeql insert; throws exception containing {string}")]
        public void TypeqlInsertThrowsExceptionContaining(string expectedMessage, DocString insertQueryStatements)
        {
            var exception = Assert.Throws<TypeDBDriverException>(() => TypeqlInsert(insertQueryStatements));

            Assert.Contains(expectedMessage, exception.Message);
        }

        [Given(@"typeql delete")]
        [When(@"typeql delete")]
        [Then(@"typeql delete")]
        public void TypeqlDelete(DocString deleteQueryStatements)
        {
            SingleTransaction.Query.Delete(deleteQueryStatements.Content).Resolve();
        }

        [Given(@"typeql delete; throws exception")]
        [When(@"typeql delete; throws exception")]
        [Then(@"typeql delete; throws exception")]
        public void TypeqlDeleteThrowsException(DocString deleteQueryStatements)
        {
            Assert.Throws<TypeDBDriverException>(() => TypeqlDelete(deleteQueryStatements));
        }

        [Given(@"typeql delete; throws exception containing {string}")]
        [When(@"typeql delete; throws exception containing {string}")]
        public void TypeqlDeleteThrowsExceptionContaining(string expectedMessage, DocString deleteQueryStatements)
        {
            var exception = Assert.Throws<TypeDBDriverException>(
                () => TypeqlDelete(deleteQueryStatements));

            Assert.Contains(expectedMessage, exception.Message);
        }

        [Given(@"typeql update")]
        [When(@"typeql update")]
        [Then(@"typeql update")]
        public IConceptMap[] TypeqlUpdate(DocString updateQueryStatements)
        {
            return SingleTransaction.Query.Update(updateQueryStatements.Content).ToArray();
        }

        [Given(@"typeql update; throws exception")]
        [When(@"typeql update; throws exception")]
        [Then(@"typeql update; throws exception")]
        public void TypeqlUpdateThrowsException(DocString updateQueryStatements)
        {
            Assert.Throws<TypeDBDriverException>(() => TypeqlUpdate(updateQueryStatements));
        }

        [Then(@"typeql update; throws exception containing {string}")]
        public void TypeqlUpdateThrowsExceptionContaining(string expectedMessage, DocString updateQueryStatements)
        {
            var exception = Assert.Throws<TypeDBDriverException>(() => TypeqlUpdate(updateQueryStatements));

            Assert.Contains(expectedMessage, exception.Message);
        }

        [Given(@"get answers of typeql insert")]
        [When(@"get answers of typeql insert")]
        [Then(@"get answers of typeql insert")]
        public void GetAnswersOfTypeqlInsert(DocString insertQueryStatements)
        {
            ClearAnswers();
            _answers = SingleTransaction.Query.Insert(insertQueryStatements.Content).ToList();
        }

        [Given(@"get answers of typeql get")]
        [When(@"get answers of typeql get")]
        [Then(@"get answers of typeql get")]
        public void TypeqlGet(DocString getQueryStatements)
        {
            ClearAnswers();
            _answers = SingleTransaction.Query.Get(getQueryStatements.Content).ToList();
        }

        [When(@"typeql get; throws exception")]
        [Then(@"typeql get; throws exception")]
        public void TypeqlGetThrowsException(DocString getQueryStatements)
        {
            Assert.Throws<TypeDBDriverException>(() => TypeqlGet(getQueryStatements));
        }

        [When(@"typeql get; throws exception containing {string}")]
        [Then(@"typeql get; throws exception containing {string}")]
        public void TypeqlGetThrowsExceptionContaining(string expectedMessage, DocString getQueryStatements)
        {
            var exception = Assert.Throws<TypeDBDriverException>(
                () => TypeqlGet(getQueryStatements));

            Assert.Contains(expectedMessage, exception.Message);
        }

        [When(@"typeql get aggregate")]
        [Then(@"typeql get aggregate")]
        [When(@"get answer of typeql get aggregate")]
        [Then(@"get answer of typeql get aggregate")]
        public void TypeqlGetAggregate(DocString getQueryStatements)
        {
            ClearAnswers();
            _valueAnswer = SingleTransaction.Query.GetAggregate(getQueryStatements.Content).Resolve();
        }

        [When(@"typeql get aggregate; throws exception")]
        [Then(@"typeql get aggregate; throws exception")]
        public void TypeqlGetAggregateThrowsException(DocString getQueryStatements)
        {
            Assert.Throws<TypeDBDriverException>(() => TypeqlGetAggregate(getQueryStatements));
        }

        [When(@"typeql get group")]
        [Then(@"typeql get group")]
        [When(@"get answers of typeql get group")]
        [Then(@"get answers of typeql get group")]
        public void TypeqlGetGroup(DocString getQueryStatements)
        {
            ClearAnswers();
            _answerGroups = SingleTransaction.Query.GetGroup(getQueryStatements.Content).ToList();
        }

        [When(@"typeql get group; throws exception")]
        [Then(@"typeql get group; throws exception")]
        public void TypeqlGetGroupThrowsException(DocString getQueryStatements)
        {
            Assert.Throws<TypeDBDriverException>(() => TypeqlGetGroup(getQueryStatements));
        }

        [When(@"typeql get group aggregate")]
        [Then(@"typeql get group aggregate")]
        [When(@"get answers of typeql get group aggregate")]
        [Then(@"get answers of typeql get group aggregate")]
        public void TypeqlGetGroupAggregate(DocString getQueryStatements)
        {
            ClearAnswers();
            _valueAnswerGroups = SingleTransaction.Query.GetGroupAggregate(getQueryStatements.Content).ToList();
        }

        [Given(@"answer size is: {}")]
        [Then(@"answer size is: {}")]
        public void AnswerSizeIs(int expectedAnswers)
        {
            Assert.Equal(expectedAnswers, _answers.Count); // $"Expected {expectedAnswers} answers, but got {_answers.Count}"
        }

        [Given(@"uniquely identify answer concepts")]
        [Then(@"uniquely identify answer concepts")]
        public void UniquelyIdentifyAnswerConcepts(DataTable answerConcepts)
        {
            var parsedConcepts = Util.ParseDataTableToMultiDictionary(answerConcepts);
            Assert.Equal(parsedConcepts.Count, _answers.Count);

            foreach (IConceptMap answer in _answers)
            {
                List<Dictionary<string, string>> matchingIdentifiers = new List<Dictionary<string, string>>();

                foreach (Dictionary<string, string> answerIdentifier in parsedConcepts)
                {
                    if (MatchAnswerConcept(answerIdentifier, answer))
                    {
                        matchingIdentifiers.Add(answerIdentifier);
                    }
                }

                Assert.Equal(1, matchingIdentifiers.Count);
            }
        }

        [Then(@"order of answer concepts is")]
        public void OrderOfAnswerConceptsIs(DataTable answersIdentifiers)
        {
            var parsedIdentifiers = Util.ParseDataTableToMultiDictionary(answersIdentifiers);
            Assert.Equal(parsedIdentifiers.Count, _answers.Count);

            for (int i = 0; i < _answers.Count; i++)
            {
                IConceptMap answer = _answers[i];
                Dictionary<string, string> answerIdentifiers = parsedIdentifiers[i];

                Assert.True(
                    MatchAnswerConcept(answerIdentifiers, answer),
                    $"The answer at index {i} does not match the identifier entry (row) at index {i}");
            }
        }

        [Then(@"aggregate value is: {}")]
        public void AggregateValueIs(double expectedAnswer)
        {
            Assert.NotNull(_valueAnswer); // , "The last executed query was not an aggregate query"
            Assert.True(_valueAnswer != null, "The last executed aggregate query returned NaN");

            double value = _valueAnswer.IsDouble()
                ? _valueAnswer.AsDouble()
                : _valueAnswer.AsLong();

            Assert.Equal(expectedAnswer, value, 0.001);
        }

        [Then(@"aggregate answer is empty")] // TODO: Fix
        public void AggregateAnswerIsEmpty()
        {
            Assert.NotNull(_valueAnswer);
            Assert.True(_valueAnswer == null);
        }

        [Then(@"answer groups are")]
        public void AnswerGroupsAre(DataTable answerIdentifiers)
        {
            var parsedAnswerIdentifiers = Util.ParseDataTableToMultiDictionary(answerIdentifiers);

            HashSet<AnswerIdentifierGroup> answerIdentifierGroups = parsedAnswerIdentifiers
                .GroupBy(x => x[AnswerIdentifierGroup.GROUP_COLUMN_NAME])
                .Select(group => new AnswerIdentifierGroup(group.ToList()))
                .ToHashSet();

            Assert.Equal(answerIdentifierGroups.Count, _answerGroups.Count);

            foreach (AnswerIdentifierGroup answerIdentifierGroup in answerIdentifierGroups)
            {
                string[] identifier = answerIdentifierGroup.OwnerIdentifier.Split(":", 2);
                UniquenessCheck checker;

                switch (identifier[0]) {
                    case "label":
                        checker = new LabelUniquenessCheck(identifier[1]);
                        break;
                    case "key":
                        checker = new KeyUniquenessCheck(identifier[1]);
                        break;
                    case "attr":
                        checker = new AttributeValueUniquenessCheck(identifier[1]);
                        break;
                    case "value":
                        checker = new ValueUniquenessCheck(identifier[1]);
                        break;
                    default:
                        throw new BehaviourTestException("Unexpected value: " + identifier[0]);
                }

                IConceptMapGroup answerGroup = _answerGroups
                    .Where(ag => checker.Check(ag.Owner))
                    .First();

                List<Dictionary<string, string>> answersIdentifiers = answerIdentifierGroup.AnswersIdentifiers;

                foreach (var answer in answerGroup.ConceptMaps)
                {
                    List<Dictionary<string, string>> matchingIdentifiers = new List<Dictionary<string, string>>();

                    foreach (Dictionary<string, string> answerIds in answersIdentifiers)
                    {
                        if (MatchAnswerConcept(answerIds, answer))
                        {
                            matchingIdentifiers.Add(answerIds);
                        }
                    }

                    Assert.Equal(1, matchingIdentifiers.Count);
                }
            }
        }

        [Then(@"group aggregate values are")]
        public void GroupAggregateValuesAre(DataTable answerIdentifiers)
        {
            var parsedAnswerIdentifiers = Util.ParseDataTableToMultiDictionary(answerIdentifiers);
            Dictionary<string, double> expectations = new Dictionary<string, double>();

            foreach (Dictionary<string, string> answerIdentifierRow in parsedAnswerIdentifiers)
            {
                string groupOwnerIdentifier = answerIdentifierRow[AnswerIdentifierGroup.GROUP_COLUMN_NAME];
                double expectedAnswer = Double.Parse(answerIdentifierRow["value"]);
                expectations[groupOwnerIdentifier] = expectedAnswer;
            }

            Assert.Equal(expectations.Count, _valueAnswerGroups.Count);

            foreach (var (expectationKey, expectedAnswer) in expectations)
            {
                string[] identifier = expectationKey.Split(":", 2);
                UniquenessCheck checker;

                switch (identifier[0])
                {
                    case "label":
                        checker = new LabelUniquenessCheck(identifier[1]);
                        break;
                    case "key":
                        checker = new KeyUniquenessCheck(identifier[1]);
                        break;
                    case "attr":
                        checker = new AttributeValueUniquenessCheck(identifier[1]);
                        break;
                    case "value":
                        checker = new ValueUniquenessCheck(identifier[1]);
                        break;
                    default:
                        throw new BehaviourTestException("Unexpected value: " + identifier[0]);
                }

                IValueGroup answerGroup = _valueAnswerGroups
                    .Where(ag => checker.Check(ag.Owner))
                    .First();

                IValue? value = answerGroup.Value;
                Assert.NotNull(value);

                double actualAnswer = value.IsDouble() ? value.AsDouble() : value.AsLong();

                Assert.Equal(expectedAnswer, actualAnswer, 3);
            }
        }

        [Then(@"number of groups is: {int}")]
        public void NumberOfGroupsIs(int expectedGroupsCount)
        {
            Assert.Equal(expectedGroupsCount, _answerGroups.Count);
        }

        public class AnswerIdentifierGroup
        {
            public string OwnerIdentifier { get; }
            public List<Dictionary<string, string>> AnswersIdentifiers { get; }

            public static readonly string GROUP_COLUMN_NAME = "owner";

            public AnswerIdentifierGroup(List<Dictionary<string, string>> answerIdentifiers)
            {
                OwnerIdentifier = answerIdentifiers[0][GROUP_COLUMN_NAME];
                AnswersIdentifiers = new List<Dictionary<string, string>>();

                foreach (Dictionary<string, string> rawAnswerIdentifiers in answerIdentifiers)
                {
                    AnswersIdentifiers.Add(rawAnswerIdentifiers
                        .Where(entry => !entry.Key.Equals(GROUP_COLUMN_NAME))
                        .ToDictionary(entry => entry.Key, entry => entry.Value));
                }
            }
        }

        [Then(@"group aggregate answer value is empty")]
        public void GroupAggregateAnswerValueIsEmpty()
        {
            Assert.Equal(1, _valueAnswerGroups.Count);
            Assert.True(_valueAnswerGroups[0].Value == null);
        }

        [When(@"get answers of typeql fetch")]
        public void TypeqlFetch(DocString fetchQueryStatements)
        {
            ClearAnswers();
            _fetchAnswers = SingleTransaction.Query.Fetch(fetchQueryStatements.Content).ToList();
        }

        [When(@"typeql fetch; throws exception")]
        public void TypeQlFetchThrowsException(DocString fetchQueryStatements)
        {
            Assert.Throws<TypeDBDriverException>(() => TypeqlFetch(fetchQueryStatements));
        }

        public class JObjectHelper
        {
            private JObject _object;

            public JObjectHelper(JObject obj)
            {
                _object = obj;
            }

            public override bool Equals(object? obj)
            {
                if (Object.ReferenceEquals(this, obj))
                {
                    return true;
                }

                if (obj == null || this.GetType() != obj.GetType())
                {
                    return false;
                }

                return JToken.DeepEquals((((JObjectHelper)obj)._object), _object);
            }

            public override int GetHashCode() { return _object.GetHashCode(); }

            public override string ToString() { return _object.ToString();}
        }

        [Then(@"fetch answers are")]
        public void FetchAnswersAre(DocString expectedJSON)
        {
            var expected = new JArray(JArray.Parse(expectedJSON.Content).ToObject<List<JObject>>());
            var answers = new JArray(_fetchAnswers);

            Assert.True(Util.JsonDeepEqualsUnordered(expected, answers));
        }

        [Then(@"rules are")]
        public void RulesAre(DataTable rules)
        {
            throw new Exception("Rules Are is not ready! Just need to recheck its logic"); // TODO
            //_rules = Util.ParseDataTableToMultiDictionary(rules);
        }

        private bool CurrentRulesContain(string ruleLabel)
        {
            return SingleTransaction.Logic.Rules.Any(rule => rule.Label.Equals(ruleLabel));
        }

        [Then(@"rules contain: {}")]
        public void RulesContain(string ruleLabel)
        {
            Assert.True(CurrentRulesContain(ruleLabel));
        }

        [Then(@"rules do not contain: {}")]
        public void RulesDoNotContain(string ruleLabel)
        {
            Assert.False(CurrentRulesContain(ruleLabel));
        }

        [Then(@"each answer satisfies")]
        public void EachAnswerSatisfies(DocString templatedQuery)
        {
            foreach (IConceptMap answer in _answers)
            {
                string query = ApplyQueryTemplate(templatedQuery.Content, answer);
                long answerSize = SingleTransaction.Query.Get(query).ToArray().Length;
                Assert.Equal(1, answerSize);
            }
        }

        [Then(@"templated typeql get; throws exception")]
        public void TemplatedTypeqlGetThrowsException(DocString templatedQuery)
        {
            foreach (IConceptMap answer in _answers)
            {
                string query = ApplyQueryTemplate(templatedQuery.Content, answer);

                var exception = Assert.Throws<TypeDBDriverException>(() =>
                    {
                        long ignored = SingleTransaction.Query.Get(query).ToArray().Length;
                    });
            }
        }

        private string ApplyQueryTemplate(string template, IConceptMap templateFiller)
        {
            // find shortest matching strings between <>
            Regex pattern = new Regex(@"<.+?>");
            MatchCollection matches = pattern.Matches(template);

            StringBuilder builder = new StringBuilder();
            int i = 0;

            foreach (Match match in matches)
            {
                var matchedGroup = match.Groups[0];

                int valStartIndex = 1;
                string requiredVariable = VariableFromTemplatePlaceholder(
                    matchedGroup.Value.Substring(valStartIndex, matchedGroup.Length - valStartIndex - 1));

                builder.Append(template, i, matchedGroup.Index - i);

                try
                {
                    IConcept concept = templateFiller.Get(requiredVariable);
                    if (!concept.IsThing())
                    {
                        throw new BehaviourTestException("Cannot apply IID templating to Type concepts");
                    }

                    string conceptId = concept.AsThing().IID;
                    builder.Append(conceptId);
                }
                catch (TypeDBDriverException e)
                {
                    if (e.ErrorMessage.Equals(QueryError.VARIABLE_DOES_NOT_EXIST))
                    {
                        throw new BehaviourTestException($"No IID available for template placeholder: {matchedGroup}");
                    }
                    else
                    {
                        throw e;
                    }
                }

                i = matchedGroup.Index + matchedGroup.Length;
            }

            builder.Append(template, i, template.Length - i);

            return builder.ToString();
        }

        private string VariableFromTemplatePlaceholder(string placeholder)
        {
            if (placeholder.EndsWith(".iid"))
            {
                string trimmed = placeholder.Replace(".iid", "");
                string withoutPrefix = trimmed.Replace("answer.", "");
                return withoutPrefix;
            }
            else
            {
                throw new BehaviourTestException("Cannot replace template not based on ID");
            }
        }

        private interface UniquenessCheck
        {
            bool Check(IConcept concept);
        }

        public class LabelUniquenessCheck : UniquenessCheck
        {
            private Label _label { get; }

            public LabelUniquenessCheck(string scopedLabel)
            {
                string[] tokens = scopedLabel.Split(":");
                _label = tokens.Length > 1 ? new Label(tokens[0], tokens[1]) : new Label(tokens[0]);
            }

            public bool Check(IConcept concept)
            {
                if (concept.IsType())
                {
                    return _label.Equals(concept.AsType().Label);
                }

                throw new BehaviourTestException(
                    "Concept was checked for label uniqueness, but it is not a Type");
            }
        }

        public abstract class AttributeUniquenessCheck
        {
            protected Label _type { get; }
            protected string _value { get; }

            public AttributeUniquenessCheck(string typeAndValue)
            {
                string[] splittedTypeAndValue = typeAndValue.Split(":", 2);
                Assert.Equal(2, splittedTypeAndValue.Length);

                _type = new Label(splittedTypeAndValue[0]);
                _value = splittedTypeAndValue[1];
            }
        }

        public class AttributeValueUniquenessCheck : AttributeUniquenessCheck, UniquenessCheck
        {
            public AttributeValueUniquenessCheck(string typeAndValue)
                : base(typeAndValue)
            {
            }

            public bool Check(IConcept concept)
            {
                if (!concept.IsAttribute())
                {
                    return false;
                }

                IAttribute attribute = concept.AsAttribute();
                IAttributeType attributeType = (IAttributeType)attribute.Type; // TODO: Is it ok?

                if (attribute.Value.IsDateTime())
                {
                    DateTime dateTime;
//                    try // TODO?
//                    {
                        dateTime = DateTime.Parse(_value);
//                    }
//                    catch (DateTimeParseException e)
//                    {
////                        dateTime = DateTime.Parse(value).atStartOfDay();
//                    }

                    return _type.Equals(attributeType.Label)
                        && dateTime.Equals(attribute.Value.AsDateTime());
                }

                return _type.Equals(attributeType.Label)
                    && _value.Equals(attribute.Value.ToString());
            }
        }

        public class KeyUniquenessCheck : AttributeUniquenessCheck, UniquenessCheck
        {
            public KeyUniquenessCheck(string typeAndValue)
                : base(typeAndValue)
            {
            }

            public bool Check(IConcept concept)
            {
                if (!concept.IsThing())
                {
                    return false;
                }

                HashSet<IAttribute> keys = concept
                    .AsThing()
                    .GetHas(SingleTransaction, new HashSet<IThingType.Annotation>(){IThingType.Annotation.NewKey()})
                    .ToHashSet();

                Dictionary<Label, string> keyMap = new Dictionary<Label, string>();

                foreach (IAttribute key in keys)
                {
                    keyMap[key.Type.Label] = key.Value.ToString();
                }

                return _value.Equals(keyMap.ContainsKey(_type) ? keyMap[_type] : null);
            }
        }

        public class ValueUniquenessCheck : UniquenessCheck
        {
            private string _valueType { get; }
            private string _value { get; }

            public ValueUniquenessCheck(string valueTypeAndValue)
            {
                string[] splittedValueTypeAndValue = valueTypeAndValue.Split(":", 2);
                _valueType = splittedValueTypeAndValue[0].ToLower().Trim();
                _value = splittedValueTypeAndValue[1].Trim();
            }

            public bool Check(IConcept concept)
            {
                if (!concept.IsValue())
                {
                    return false;
                }

                var type = concept.AsValue().Type;

                if (type == IValue.ValueType.BOOL)
                    return bool.Parse(_value).Equals(concept.AsValue().AsBool());
                if (type == IValue.ValueType.LONG)
                    return long.Parse(_value).Equals(concept.AsValue().AsLong());
                if (type == IValue.ValueType.DOUBLE)
                    return double.Parse(_value).Equals(concept.AsValue().AsDouble());
                if (type == IValue.ValueType.STRING)
                    return _value.Equals(concept.AsValue().AsString());
                if (type == IValue.ValueType.DATETIME)
                {
                    DateTime dateTime;
//                        try // TODO?
//                        {
                            dateTime = DateTime.Parse(_value);
//                        }
//                        catch (DateTimeParseException e)
//                        {
//                            dateTime = LocalDate.Parse(_value).AtStartOfDay();
//                        }

                    return dateTime.Equals(concept.AsValue().AsDateTime());
                }

                throw new BehaviourTestException(
                    "Unrecognised value type specified in test " + _valueType);
            }
        }

        private bool MatchAnswerConcept(Dictionary<string, string> answerIdentifiers, IConceptMap answer)
        {
            foreach (var (variable, value) in answerIdentifiers)
            {
                string[] identifier = value.Split(":", 2);

                switch (identifier[0])
                {
                    case "label":
                        if (!new LabelUniquenessCheck(identifier[1]).Check(answer.Get(variable)))
                        {
                            return false;
                        }
                        break;
                    case "key":
                        if (!new KeyUniquenessCheck(identifier[1]).Check(answer.Get(variable)))
                        {
                            return false;
                        }
                        break;
                    case "attr":
                        if (!new AttributeValueUniquenessCheck(identifier[1]).Check(answer.Get(variable)))
                        {
                            return false;
                        }
                        break;
                    case "value":
                        if (!new ValueUniquenessCheck(identifier[1]).Check(answer.Get(variable)))
                        {
                            return false;
                        }
                        break;
                }
            }

            return true;
        }

        private void ClearAnswers() 
        {
            if (_answers != null)
            {
                _answers.Clear();
            }

            _valueAnswer = null;

            if (_answerGroups != null)
            {
                _answerGroups.Clear();
            }

            if (_valueAnswerGroups != null)
            {
                _valueAnswerGroups.Clear();
            }
        }

        public static List<IConceptMap> Answers
        {
            get { return _answers; }
        }

        private static List<IConceptMap> _answers;
        private static List<JObject> _fetchAnswers;
        private static IValue? _valueAnswer;
        private static List<IConceptMapGroup> _answerGroups;
        private static List<IValueGroup> _valueAnswerGroups;
        private Dictionary<string, Dictionary<string, string>> _rules;
    }
}
