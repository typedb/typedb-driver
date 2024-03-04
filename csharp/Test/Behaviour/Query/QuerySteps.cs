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
using System.Threading;
using System.Threading.Tasks;
using Xunit;
using Xunit.Gherkin.Quick;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Common;

using QueryError = Vaticle.Typedb.Driver.Common.Error.Query;

namespace Vaticle.Typedb.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        [Given(@"typeql define")]
        [Then(@"typeql define")]
        public void TypeqlDefine(DocString defineQueryStatements)
        {
            SingleTransaction.Query.Define(defineQueryStatements.Content).Resolve();
        }

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
        public IEnumerable<IConceptMap> TypeqlInsert(DocString insertQueryStatements)
        {
            return SingleTransaction.Query.Insert(insertQueryStatements.Content);
        }

        [Then(@"typeql insert; throws exception")]
        public void TypeqlInsertThrowsException(DocString insertQueryStatements)
        {
            Assert.Throws<TypeDBDriverException>(() => TypeqlInsert(insertQueryStatements));
        }

        [Then(@"typeql insert; throws exception containing {string}")]
        public void TypeqlInsertThrowsExceptionContaining(string expectedMessage, DocString insertQueryStatements)
        {
            var exception = Assert.Throws<TypeDBDriverException>(
                () => TypeqlInsert(insertQueryStatements).ToArray<IConceptMap>());

            Assert.Contains(expectedMessage, exception.Message);
        }

        [Given(@"typeql delete")]
        [When(@"typeql delete")]
        public void TypeqlDelete(DocString deleteQueryStatements) 
        {
            SingleTransaction.Query.Delete(deleteQueryStatements.Content).Resolve();
        }
    
        [Then(@"typeql delete; throws exception")]
        public void TypeqlDeleteThrowsException(DocString deleteQueryStatements) 
        {
            Assert.Throws<TypeDBDriverException>(() => TypeqlDelete(deleteQueryStatements));
        }
    
        [Given(@"typeql delete; throws exception containing {string}")]
        public void TypeqlDeleteThrowsExceptionContaining(string expectedMessage, DocString deleteQueryStatements)
        {
            var exception = Assert.Throws<TypeDBDriverException>(
                () => TypeqlDelete(deleteQueryStatements));

            Assert.Contains(expectedMessage, exception.Message);
        }
    
        [Given(@"typeql update")]
        [When(@"typeql update")]
        public IEnumerable<IConceptMap> TypeqlUpdate(DocString updateQueryStatements)
        {
            return SingleTransaction.Query.Update(updateQueryStatements.Content);
        }
    
        [Then(@"typeql update; throws exception")]
        public void TypeqlUpdateThrowsException(DocString updateQueryStatements)
        {
            Assert.Throws<TypeDBDriverException>(() => TypeqlUpdate(updateQueryStatements)); // [0] ?
        }
    
        [Then(@"typeql update; throws exception containing {string}")]
        public void TypeqlUpdateThrowsExceptionContaining(string expectedMessage, DocString updateQueryStatements) 
        {
            var exception = Assert.Throws<TypeDBDriverException>(
                () => TypeqlUpdate(updateQueryStatements));

            Assert.Contains(expectedMessage, exception.Message);
        }

        [Given(@"get answers of typeql insert")]
        [When(@"get answers of typeql insert")]
        public void GetAnswersOfTypeqlInsert(DocString insertQueryStatements) 
        {
            ClearAnswers();
            _answers = SingleTransaction.Query.Insert(insertQueryStatements.Content).ToList();
        }

        [Given(@"get answers of typeql get")]
        [When(@"get answers of typeql get")]
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
        [When(@"get answer of typeql get aggregate")]
        public void TypeqlGetAggregate(DocString getQueryStatements)
        {
            ClearAnswers();
            _valueAnswer = SingleTransaction.Query.GetAggregate(getQueryStatements.Content).Resolve();
        }
    
        [When(@"typeql get aggregate; throws exception")]
        public void TypeqlGetAggregateThrowsException(DocString getQueryStatements)
        {
            Assert.Throws<TypeDBDriverException>(() => TypeqlGetAggregate(getQueryStatements));
        }

        [When(@"typeql get group")]
        [When(@"get answers of typeql get group")]
        public void TypeqlGetGroup(DocString getQueryStatements)
        {
            ClearAnswers();
            _answerGroups = SingleTransaction.Query.GetGroup(getQueryStatements.Content).ToList();
        }
    
        [When(@"typeql get group; throws exception")]
        public void TypeqlGetGroupThrowsException(DocString getQueryStatements)
        {
            Assert.Throws<TypeDBDriverException>(() => TypeqlGetGroup(getQueryStatements));
        }

        [When(@"typeql get group aggregate")]
        [When(@"get answers of typeql get group aggregate")]
        public void TypeqlGetGroupAggregate(DocString getQueryStatements)
        {
            ClearAnswers();
            _valueAnswerGroups = SingleTransaction.Query.GetGroupAggregate(getQueryStatements.Content).ToList();
        }
    
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
            Assert.Equal(parsedConcepts.Count, _answers.Count); // string.format("The number of identifier entries (rows) should match the number of answers, but found %d identifier entries and %d answers.",
                                                                //  answerConcepts.Count, _answers.Count),

            foreach (IConceptMap answer in _answers)
            {
                List<Dictionary<string, string>> matchingIdentifiers = new List<Dictionary<string, string>>();

                foreach (Dictionary<string, string> answerIdentifier in parsedConcepts)
                {
                    foreach (var (key, value) in answerIdentifier)
                    {
                        Console.WriteLine($"HERE: {key}:{value}, ");
                    }
                    Console.WriteLine($"VS {answer}");
                    if (MatchAnswerConcept(answerIdentifier, answer))
                    {
                        matchingIdentifiers.Add(answerIdentifier);
                    }
                }

                Assert.Equal(1, matchingIdentifiers.Count); // "An identifier entry (row) should match 1-to-1 to an answer, but there were %d matching identifier entries for answer with variables %s.", MatchingIdentifiers.Count, answer.Variables.ToHashSet()),
            }
        }

        [Then(@"order of answer concepts is")]
        public void OrderOfAnswerConceptsIs(DataTable answersIdentifiers)
        {
            var parsedIdentifiers = Util.ParseDataTableToMultiDictionary(answersIdentifiers);
            Assert.Equal(parsedIdentifiers.Count, _answers.Count);
            // $"The number of identifier entries (rows) should match the number of answers, "
            // $"but found {answersIdentifiers.Count} identifier entries and {_answers.Count} answers."

            for (int i = 0; i < _answers.Count; i++)
            {
                IConceptMap answer = _answers[i];
                Dictionary<string, string> answerIdentifiers = parsedIdentifiers[i];

                Assert.True(
                    MatchAnswerConcept(answerIdentifiers, answer),
                    $"The answer at index {i} does not match the identifier entry (row) at index {i}.");
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
            // $"Expected answer to equal %f, but it was %f.", expectedAnswer, value
        }

        [Then(@"aggregate answer is empty")]
        public void AggregateAnswerIsEmpty()
        {
            Assert.NotNull(_valueAnswer); // , "The last executed query was not an aggregate query"
            Assert.True(_valueAnswer == null);
        }

        [Then(@"answer groups are")]
        public void AnswerGroupsAre(DataTable answerIdentifiers)
        {
            var parsedAnswerIdentifiers = Util.ParseDataTableToMultiDictionary(answerIdentifiers);
            throw new Exception("This test is not ready yet.");
            /*
            TODO:
            Input: [{owner=value:long:1000, x=key:ref:1250}, {owner=value:long:1000, x=key:ref:1750}, {owner=value:long:2000, x=key:ref:2050}, {owner=value:long:3000, x=key:ref:3000}]
            answerIdentifierGroups:
            [ownerIdentifier: value:long:2000, answersIdentifiers: [{x=key:ref:2050}], ownerIdentifier: value:long:1000, answersIdentifiers: [{x=key:ref:1250}, {x=key:ref:1750}], ownerIdentifier: value:long:3000, answersIdentifiers: [{x=key:ref:3000}]]

            */

//            HashSet<AnswerIdentifierGroup> answerIdentifierGroups = parsedAnswerIdentifiers
//                .collect(Collectors.groupingBy(x => x[AnswerIdentifierGroup.GROUP_COLUMN_NAME]))
//                .Values
//                .Select(obj => new AnswerIdentifierGroup(obj))
//                .ToHashSet();
//
//            Assert.Equal(answerIdentifierGroups.Count, answerGroups.Count);
//            //"Expected [%d] answer groups, but found [%d].", answerIdentifierGroups.Count, answerGroups.Count
//
//            foreach (AnswerIdentifierGroup answerIdentifierGroup in answerIdentifierGroups)
//            {
//                string[] identifier = answerIdentifierGroup.OwnerIdentifier.Split(":", 2);
//                UniquenessCheck checker;
//
//                switch (identifier[0]) {
//                    case "label":
//                        checker = new LabelUniquenessCheck(identifier[1]);
//                        break;
//                    case "key":
//                        checker = new KeyUniquenessCheck(identifier[1]);
//                        break;
//                    case "attr":
//                        checker = new AttributeValueUniquenessCheck(identifier[1]);
//                        break;
//                    case "value":
//                        checker = new ValueUniquenessCheck(identifier[1]);
//                        break;
//                    default:
//                        throw new IllegalStateException("Unexpected value: " + identifier[0]);
//                }
//
//                ConceptMapGroup answerGroup = answerGroups.stream()
//                    .filter(ag -> checker.Check(ag.Owner))
//                    .findAny()
//                    .orElse(null);
//                Assert.NotNull(answerGroup);
//                    // $"The group identifier {answerIdentifierGroup.ownerIdentifier} does not "
//                    // "match any of the answer group owners."
//
//                List<Dictionary<string, string>> answersIdentifiers = answerIdentifierGroup.answersIdentifiers;
//                answerGroup.ConceptMaps.forEach(answer -> {
//                    List<Dictionary<string, string>> matchingIdentifiers = new List<>();
//
//                    foreach (Dictionary<string, string> answerIdentifiers in answersIdentifiers)
//                    {
//
//                        if (MatchAnswerConcept(answerIdentifiers, answer))
//                        {
//                            matchingIdentifiers.add(answerIdentifiers);
//                        }
//                    }
//
//                    Assert.Equal(1, matchingIdentifiers.Count);
//                    // $"An identifier entry (row) should match 1-to-1 to an answer, but there were [%d] matching identifier entries for answer with variables %s.", matchingIdentifiers.Count, answer.variables().ToHashSet()
//                });
//            }
        }

        [Then(@"group aggregate values are")]
        public void GroupAggregateValuesAre(DataTable answerIdentifiers)
        {
            var parsedAnswerIdentifiers = Util.ParseDataTableToMultiDictionary(answerIdentifiers);
            Dictionary<string, double> expectations = new Dictionary<string, double>();
            throw new Exception("This test is not ready yet.");
//            foreach (Dictionary<string, string> answerIdentifierRow in parsedAnswerIdentifiers)
//            {
//                string groupOwnerIdentifier = answerIdentifierRow.get(AnswerIdentifierGroup.GROUP_COLUMN_NAME);
//                double expectedAnswer = Double.parseDouble(answerIdentifierRow.get("value")]);
//                expectations.put(groupOwnerIdentifier, expectedAnswer);
//            }
//
//            Assert.Equal(expectations.Count, _valueAnswerGroups.Count); // $"Expected {expectations.Count} answer groups, but found {valueAnswerGroups.Count}."
//
//            for (Map.Entry<string, Double> expectation : expectations.entrySet())
//            {
//                string[] identifier = expectation.getKey().Split(":", 2);
//                UniquenessCheck checker;
//
//                switch (identifier[0])
//                {
//                    case "label":
//                        checker = new LabelUniquenessCheck(identifier[1]);
//                        break;
//                    case "key":
//                        checker = new KeyUniquenessCheck(identifier[1]);
//                        break;
//                    case "attr":
//                        checker = new AttributeValueUniquenessCheck(identifier[1]);
//                        break;
//                    case "value":
//                        checker = new ValueUniquenessCheck(identifier[1]);
//                        break;
//                    default:
//                        throw new IllegalStateException("Unexpected value: " + identifier[0]);
//                }
//
//                double expectedAnswer = expectation.getValue();
//                ValueGroup answerGroup = _valueAnswerGroups.stream()
//                    .filter(ag -> checker.Check(ag.Owner))
//                    .findAny()
//                    .orElse(null);
//
//                Assert.NotNull(answerGroup);
//                    // "The group identifier {expectation.Key} does not match any of the answer group owners.");
//
//                Value value = answerGroup.value().get();
//                double actualAnswer = value.isDouble() ? value.AsDouble() : value.AsLong();
//
//                Assert.Equal(expectedAnswer, actualAnswer, 0.001);
//                // $"Expected answer {expectedAnswer} for group {expectation.Key}, but got {actualAnswer}.",
//            }
        }

        [Then(@"number of groups is: {int}")]
        public void NumberOfGroupsIs(int expectedGroupsCount)
        {
            Assert.Equal(expectedGroupsCount, _answerGroups.Count);
        }

        public class AnswerIdentifierGroup
        {
            private string _ownerIdentifier { get; }
            private List<Dictionary<string, string>> _answersIdentifiers { get; }

            public static readonly string GROUP_COLUMN_NAME = "owner";

            public AnswerIdentifierGroup(List<Dictionary<string, string>> answerIdentifiers)
            {
                _ownerIdentifier = answerIdentifiers[0][GROUP_COLUMN_NAME];
                _answersIdentifiers = new List<Dictionary<string, string>>();

                foreach (Dictionary<string, string> rawAnswerIdentifiers in answerIdentifiers)
                {
                    var filteredIdentifiers = rawAnswerIdentifiers
                        .Where(entry => !entry.Key.Equals(GROUP_COLUMN_NAME))
                        .ToDictionary(entry => entry.Key, entry => entry.Value);

//                    foreach (var filteredIdentifier in filteredIdentifiers)
//                    {
                        _answersIdentifiers.Add(filteredIdentifiers);
//                    }
                }
            }
        }

        [Then(@"group aggregate answer value is empty")]
        public void GroupAggregateAnswerValueIsEmpty()
        {
            Assert.Equal(1, _valueAnswerGroups.Count); // "Step requires exactly 1 grouped answer"
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

        [Then(@"fetch answers are")]
        public void FetchAnswersAre(DocString expectedJSON)
        {
            throw new Exception("This method is not ready");
//            JObject expected = JObject.parse(expectedJSON);
//            Assert.True(
//                expected.IsArray(),
//                "Fetch response is a list of JSON objects, but the behaviour test expects something else");
//
//            Assert.True(JSONListMatches(fetchAnswers, expected.AsArray()));
        }

//        [Then(@"rules are")]
//        public void RulesAre(DataTable rules)
//        {
//            _rules = rules; // TODO
//        }

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

                Assert.Throws<TypeDBDriverException>(() =>
                    {
                        long ignored = SingleTransaction.Query.Get(query).ToArray().Length;
                    });
            }
        }

        private string ApplyQueryTemplate(string template, IConceptMap templateFiller)
        {

            throw new Exception("This method is not ready yet");
            // find shortest matching strings between <>
//            Pattern pattern = Pattern.compile("<.+?>")];
//            Matcher matcher = pattern.matcher(template);
//
//            StringBuilder builder = new StringBuilder();
//            int i = 0;
//
//            while (matcher.find())
//            {
//                string matched = matcher.group(0);
//                string requiredVariable = variableFromTemplatePlaceholder(matched.substring(1, matched.length() - 1));
//
//                builder.append(template, i, matcher.start());
//                try
//                {
//                    Concept concept = templateFiller.get(requiredVariable);
//                    if (!concept.isThing())
//                    {
//                        throw new BehaviourTestException("Cannot apply IID templating to Type concepts")];
//                    }
//
//                    string conceptId = concept.AsThing().IID;
//                    builder.append(conceptId);
//                }
//                catch (TypeDBDriverException e)
//                {
//                    if (e.GetErrorMessage().Equals(QueryError.VARIABLE_DOES_NOT_EXIST))
//                    {
//                        throw new BehaviourTestException($"No IID available for template placeholder: {matched}."));
//                    }
//                    else
//                    {
//                        throw e;
//                    }
//                }
//
//                i = matcher.end();
//            }
//
//            builder.append(template.substring(i));
//            return builder.ToString();
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
                throw new BehaviourTestException("Cannot replace template not based on ID.");
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
                    "Concept was checked for label uniqueness, but it is not a Type.");
            }
        }

        public abstract class AttributeUniquenessCheck
        {
            protected Label _type { get; }
            protected string _value { get; }

            public AttributeUniquenessCheck(string typeAndValue)
            {
                string[] splittedTypeAndValue = typeAndValue.Split(":", 2);
                Assert.Equal(2, splittedTypeAndValue.Length); // $"A check for attribute uniqueness should be given in the format \"type:value\", but received {typeAndValue}."

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
                IAttributeType attributeType = attribute.Type;

                if (attribute.Value.IsDateTime())
                {
                    DateTime dateTime;
//                    try
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
                Console.WriteLine($"Checking Key {concept}!");
                if (!concept.IsThing())
                {
                    return false;
                }
                Console.WriteLine("Thing!");
                HashSet<IAttribute> keys = concept
                    .AsThing()
                    .GetHas(SingleTransaction, new HashSet<IThingType.Annotation>(){IThingType.Annotation.NewKey()})
                    .ToHashSet();
                Console.WriteLine($"Keys collected: {keys}!");
                Dictionary<Label, string> keyMap = new Dictionary<Label, string>();

                foreach (IAttribute key in keys)
                {
                    keyMap[key.Type.Label] = key.Value.ToString();
                }
                Console.WriteLine($"Keymap! {keyMap}! While value: {_value}!");
                return _value.Equals(keyMap[_type]);
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
                    return double.Parse(_value).Equals(concept.AsValue().AsDouble()); // TODO: Approx equals?
                if (type == IValue.ValueType.STRING)
                    return _value.Equals(concept.AsValue().AsString());
                if (type == IValue.ValueType.DATETIME)
                {
                    DateTime dateTime;
//                        try
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
            foreach (var (key, value) in answerIdentifiers)
            {
                string[] identifier = value.Split(":", 2);

                Console.WriteLine($"CHECKING {identifier[0]}! {answerIdentifiers.Count}");

                switch (identifier[0])
                {
                    case "label":
                        return new LabelUniquenessCheck(identifier[1]).Check(answer.Get(key));
                    case "key":
                        return new KeyUniquenessCheck(identifier[1]).Check(answer.Get(key));
                    case "attr":
                        return new AttributeValueUniquenessCheck(identifier[1]).Check(answer.Get(key));
                    case "value":
                        return new ValueUniquenessCheck(identifier[1]).Check(answer.Get(key));
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
