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

package com.vaticle.typedb.driver.test.behaviour.query;

import com.vaticle.typedb.driver.api.answer.ConceptMap;
import com.vaticle.typedb.driver.api.answer.ConceptMapGroup;
import com.vaticle.typedb.driver.api.answer.JSON;
import com.vaticle.typedb.driver.api.answer.ValueGroup;
import com.vaticle.typedb.driver.api.concept.Concept;
import com.vaticle.typedb.driver.api.concept.thing.Attribute;
import com.vaticle.typedb.driver.api.concept.type.AttributeType;
import com.vaticle.typedb.driver.api.concept.value.Value;
import com.vaticle.typedb.driver.common.Label;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;
import com.vaticle.typeql.lang.TypeQL;
import com.vaticle.typeql.lang.common.exception.TypeQLException;
import com.vaticle.typeql.lang.query.TypeQLDefine;
import com.vaticle.typeql.lang.query.TypeQLDelete;
import com.vaticle.typeql.lang.query.TypeQLFetch;
import com.vaticle.typeql.lang.query.TypeQLInsert;
import com.vaticle.typeql.lang.query.TypeQLGet;
import com.vaticle.typeql.lang.query.TypeQLUndefine;
import com.vaticle.typeql.lang.query.TypeQLUpdate;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.vaticle.typedb.driver.api.concept.type.ThingType.Annotation.key;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Query.VARIABLE_DOES_NOT_EXIST;
import static com.vaticle.typedb.driver.test.behaviour.connection.ConnectionStepsBase.tx;
import static com.vaticle.typedb.driver.test.behaviour.util.Util.JSONListMatches;
import static com.vaticle.typedb.driver.test.behaviour.util.Util.JSONMatches;
import static com.vaticle.typedb.driver.test.behaviour.util.Util.assertThrows;
import static com.vaticle.typedb.driver.test.behaviour.util.Util.assertThrowsWithMessage;
import static com.vaticle.typedb.common.collection.Collections.set;
import static com.vaticle.typedb.common.util.Double.equalsApproximate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QuerySteps {
    private static List<ConceptMap> answers;
    private static List<JSON> fetchAnswers;
    private static Optional<Value> valueAnswer;
    private static List<ConceptMapGroup> answerGroups;
    private static List<ValueGroup> valueAnswerGroups;
    private Map<String, Map<String, String>> rules;
    
    public static List<ConceptMap> answers() {
        return answers;
    }

    @Given("the integrity is validated")
    public void integrity_is_validated() {

        // TODO

    }

    @Given("typeql define")
    public void typeql_define(String defineQueryStatements) {
        TypeQLDefine typeQLQuery = TypeQL.parseQuery(String.join("\n", defineQueryStatements));
        tx().query().define(String.join("\n", defineQueryStatements)).resolve();
    }

    @Given("typeql define; throws exception")
    public void typeql_define_throws(String defineQueryStatements) {
        assertThrows(() -> typeql_define(defineQueryStatements));
    }

    @Given("typeql define; throws exception containing {string}")
    public void typeql_define_throws_exception(String exception, String defineQueryStatements) {
        assertThrowsWithMessage(() -> typeql_define(defineQueryStatements), exception);
    }

    @Given("typeql undefine")
    public void typeql_undefine(String undefineQueryStatements) {
        TypeQLUndefine typeQLQuery = TypeQL.parseQuery(String.join("\n", undefineQueryStatements));
        tx().query().undefine(String.join("\n", undefineQueryStatements)).resolve();
    }

    @Given("typeql undefine; throws exception")
    public void typeql_undefine_throws(String undefineQueryStatements) {
        assertThrows(() -> typeql_undefine(undefineQueryStatements));
    }

    @Given("typeql undefine; throws exception containing {string}")
    public void typeql_undefine_throws_exception(String exception, String undefineQueryStatements) {
        assertThrowsWithMessage(() -> typeql_undefine(undefineQueryStatements), exception);
    }

    @Given("typeql insert")
    public Stream<ConceptMap> typeql_insert(String insertQueryStatements) {
        TypeQLInsert typeQLQuery = TypeQL.parseQuery(String.join("\n", insertQueryStatements));
        return tx().query().insert(String.join("\n", insertQueryStatements));
    }

    @Given("typeql insert; throws exception")
    @SuppressWarnings("ReturnValueIgnored")
    public void typeql_insert_throws(String insertQueryStatements) {
        //noinspection ResultOfMethodCallIgnored
        assertThrows(() -> typeql_insert(insertQueryStatements).collect(Collectors.toList()));
    }

    @Given("typeql insert; throws exception containing {string}")
    @SuppressWarnings("ReturnValueIgnored")
    public void typeql_insert_throws_exception(String exception, String insertQueryStatements) {
        //noinspection ResultOfMethodCallIgnored
        assertThrowsWithMessage(() -> typeql_insert(insertQueryStatements).collect(Collectors.toList()), exception);
    }

    @Given("typeql delete")
    public void typeql_delete(String deleteQueryStatements) {
        TypeQLDelete typeQLQuery = TypeQL.parseQuery(String.join("\n", deleteQueryStatements));
        tx().query().delete(String.join("\n", deleteQueryStatements)).resolve();
    }

    @Given("typeql delete; throws exception")
    public void typeql_delete_throws(String deleteQueryStatements) {
        assertThrows(() -> typeql_delete(deleteQueryStatements));
    }

    @Given("typeql delete; throws exception containing {string}")
    public void typeql_delete_throws_exception(String exception, String deleteQueryStatements) {
        assertThrowsWithMessage(() -> typeql_delete(deleteQueryStatements), exception);
    }

    @Given("typeql update")
    public Stream<ConceptMap> typeql_update(String updateQueryStatements) {
        TypeQLUpdate typeQLQuery = TypeQL.parseQuery(String.join("\n", updateQueryStatements));
        return tx().query().update(String.join("\n", updateQueryStatements));
    }

    @Given("typeql update; throws exception")
    public void typeql_update_throws(String updateQueryStatements) {
        assertThrows(() -> typeql_update(updateQueryStatements).iterator().next());
    }

    @Given("typeql update; throws exception containing {string}")
    public void typeql_update_throws_exception(String exception, String updateQueryStatements) {
        assertThrowsWithMessage(() -> typeql_update(updateQueryStatements).iterator().next(), exception);
    }

    private void clearAnswers() {
        answers = null;
        valueAnswer = null;
        answerGroups = null;
        valueAnswerGroups = null;
    }

    @When("get answers of typeql insert")
    public void get_answers_of_typeql_insert(String typeQLQueryStatements) {
        TypeQLInsert typeQLQuery = TypeQL.parseQuery(String.join("\n", typeQLQueryStatements));
        clearAnswers();
        answers = tx().query().insert(String.join("\n", typeQLQueryStatements)).collect(Collectors.toList());
    }

    @When("get answers of typeql get")
    public void typeql_get(String typeQLQueryStatements) {
        try {
            TypeQLGet typeQLQuery = TypeQL.parseQuery(String.join("\n", typeQLQueryStatements)).asGet();
            clearAnswers();
            answers = tx().query().get(String.join("\n", typeQLQueryStatements)).collect(Collectors.toList());
        } catch (TypeQLException e) {
            // NOTE: We manually close transaction here, because we want to align with all non-java drivers,
            // where parsing happens at server-side which closes transaction if they fail
            tx().close();
            throw e;
        }
    }

    @When("typeql get; throws exception")
    public void typeql_get_throws_exception(String typeQLQueryStatements) {
        assertThrows(() -> typeql_get(typeQLQueryStatements));
    }

    @When("typeql get; throws exception containing {string}")
    public void typeql_get_throws_exception_containing(String error, String typeQLQueryStatements) {
        assertThrowsWithMessage(() -> typeql_get(typeQLQueryStatements), error);
    }

    @When("get answer of typeql get aggregate")
    public void typeql_get_aggregate(String typeQLQueryStatements) {
        TypeQLGet.Aggregate typeQLQuery = TypeQL.parseQuery(String.join("\n", typeQLQueryStatements)).asGetAggregate();
        clearAnswers();
        valueAnswer = Optional.ofNullable(tx().query().getAggregate(String.join("\n", typeQLQueryStatements)).resolve());
    }

    @When("typeql get aggregate; throws exception")
    public void typeql_get_aggregate_throws_exception(String typeQLQueryStatements) {
        assertThrows(() -> typeql_get_aggregate(typeQLQueryStatements));
    }

    @When("get answers of typeql get group")
    public void typeql_get_group(String typeQLQueryStatements) {
        TypeQLGet.Group typeQLQuery = TypeQL.parseQuery(String.join("\n", typeQLQueryStatements)).asGetGroup();
        clearAnswers();
        answerGroups = tx().query().getGroup(String.join("\n", typeQLQueryStatements)).collect(Collectors.toList());
    }

    @When("typeql get group; throws exception")
    public void typeql_get_group_throws_exception(String typeQLQueryStatements) {
        assertThrows(() -> typeql_get_group(typeQLQueryStatements));
    }

    @When("get answers of typeql get group aggregate")
    public void typeql_get_group_aggregate(String typeQLQueryStatements) {
        TypeQLGet.Group.Aggregate typeQLQuery = TypeQL.parseQuery(String.join("\n", typeQLQueryStatements)).asGetGroupAggregate();
        clearAnswers();
        valueAnswerGroups = tx().query().getGroupAggregate(String.join("\n", typeQLQueryStatements)).collect(Collectors.toList());
    }

    @Then("answer size is: {number}")
    public void answer_quantity_assertion(int expectedAnswers) {
        assertEquals(String.format("Expected [%d] answers, but got [%d]", expectedAnswers, answers.size()),
                expectedAnswers, answers.size());
    }

    @Then("uniquely identify answer concepts")
    public void uniquely_identify_answer_concepts(List<Map<String, String>> answerConcepts) {
        assertEquals(
                String.format("The number of identifier entries (rows) should match the number of answers, but found %d identifier entries and %d answers.",
                        answerConcepts.size(), answers.size()),
                answerConcepts.size(), answers.size()
        );

        for (ConceptMap answer : answers) {
            List<Map<String, String>> matchingIdentifiers = new ArrayList<>();

            for (Map<String, String> answerIdentifier : answerConcepts) {

                if (matchAnswerConcept(answerIdentifier, answer)) {
                    matchingIdentifiers.add(answerIdentifier);
                }
            }
            assertEquals(String.format("An identifier entry (row) should match 1-to-1 to an answer, but there were %d matching identifier entries for answer with variables %s.", matchingIdentifiers.size(), answer.variables().collect(Collectors.toSet())), 1, matchingIdentifiers.size());
        }
    }

    @Then("order of answer concepts is")
    public void order_of_answer_concepts_is(List<Map<String, String>> answersIdentifiers) {
        assertEquals(
                String.format("The number of identifier entries (rows) should match the number of answers, but found %d identifier entries and %d answers.",
                        answersIdentifiers.size(), answers.size()),
                answersIdentifiers.size(), answers.size()
        );
        for (int i = 0; i < answers.size(); i++) {
            ConceptMap answer = answers.get(i);
            Map<String, String> answerIdentifiers = answersIdentifiers.get(i);
            assertTrue(
                    String.format("The answer at index %d does not match the identifier entry (row) at index %d.", i, i),
                    matchAnswerConcept(answerIdentifiers, answer)
            );
        }
    }

    @Then("aggregate value is: {double}")
    public void aggregate_value_is(double expectedAnswer) {
        assertNotNull("The last executed query was not an aggregate query", valueAnswer);
        assertTrue("The last executed aggregate query returned NaN", valueAnswer.isPresent());
        double value = valueAnswer.get().isDouble() ? valueAnswer.get().asDouble() : valueAnswer.get().asLong();
        assertEquals(String.format("Expected answer to equal %f, but it was %f.", expectedAnswer, value),
                expectedAnswer, value, 0.001);
    }

    @Then("aggregate answer is not a number")
    public void aggregate_answer_is_not_a_number() {
        assertNotNull("The last executed query was not an aggregate query", valueAnswer);
        assertTrue(valueAnswer.isEmpty());
    }

    @Then("answer groups are")
    public void answer_groups_are(List<Map<String, String>> answerIdentifierTable) {
        Set<AnswerIdentifierGroup> answerIdentifierGroups = answerIdentifierTable.stream()
                .collect(Collectors.groupingBy(x -> x.get(AnswerIdentifierGroup.GROUP_COLUMN_NAME)))
                .values()
                .stream()
                .map(AnswerIdentifierGroup::new)
                .collect(Collectors.toSet());

        assertEquals(String.format("Expected [%d] answer groups, but found [%d].",
                answerIdentifierGroups.size(), answerGroups.size()),
                answerIdentifierGroups.size(), answerGroups.size()
        );

        for (AnswerIdentifierGroup answerIdentifierGroup : answerIdentifierGroups) {
            String[] identifier = answerIdentifierGroup.ownerIdentifier.split(":", 2);
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
                    throw new IllegalStateException("Unexpected value: " + identifier[0]);
            }
            ConceptMapGroup answerGroup = answerGroups.stream()
                    .filter(ag -> checker.check(ag.owner()))
                    .findAny()
                    .orElse(null);
            assertNotNull(String.format("The group identifier [%s] does not match any of the answer group owners.", answerIdentifierGroup.ownerIdentifier), answerGroup);

            List<Map<String, String>> answersIdentifiers = answerIdentifierGroup.answersIdentifiers;
            answerGroup.conceptMaps().forEach(answer -> {
                List<Map<String, String>> matchingIdentifiers = new ArrayList<>();

                for (Map<String, String> answerIdentifiers : answersIdentifiers) {

                    if (matchAnswerConcept(answerIdentifiers, answer)) {
                        matchingIdentifiers.add(answerIdentifiers);
                    }
                }
                assertEquals(String.format("An identifier entry (row) should match 1-to-1 to an answer, but there were [%d] matching identifier entries for answer with variables %s.", matchingIdentifiers.size(), answer.variables().collect(Collectors.toSet())), 1, matchingIdentifiers.size());
            });
        }
    }

    @Then("group aggregate values are")
    public void group_aggregate_values_are(List<Map<String, String>> answerIdentifierTable) {
        Map<String, Double> expectations = new HashMap<>();
        for (Map<String, String> answerIdentifierRow : answerIdentifierTable) {
            String groupOwnerIdentifier = answerIdentifierRow.get(AnswerIdentifierGroup.GROUP_COLUMN_NAME);
            double expectedAnswer = Double.parseDouble(answerIdentifierRow.get("value"));
            expectations.put(groupOwnerIdentifier, expectedAnswer);
        }

        assertEquals(String.format("Expected [%d] answer groups, but found [%d].", expectations.size(), valueAnswerGroups.size()),
                expectations.size(), valueAnswerGroups.size()
        );

        for (Map.Entry<String, Double> expectation : expectations.entrySet()) {
            String[] identifier = expectation.getKey().split(":", 2);
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
                    throw new IllegalStateException("Unexpected value: " + identifier[0]);
            }
            double expectedAnswer = expectation.getValue();
            ValueGroup answerGroup = valueAnswerGroups.stream()
                    .filter(ag -> checker.check(ag.owner()))
                    .findAny()
                    .orElse(null);
            assertNotNull(String.format("The group identifier [%s] does not match any of the answer group owners.", expectation.getKey()), answerGroup);

            double actualAnswer = answerGroup.value().isDouble() ? answerGroup.value().asDouble() : answerGroup.value().asLong();
            assertEquals(
                    String.format("Expected answer [%f] for group [%s], but got [%f]",
                            expectedAnswer, expectation.getKey(), actualAnswer),
                    expectedAnswer, actualAnswer, 0.001
            );
        }
    }

    @Then("number of groups is: {int}")
    public void number_of_groups_is(int expectedGroupCount) {
        assertEquals(expectedGroupCount, answerGroups.size());
    }

    public static class AnswerIdentifierGroup {
        private final String ownerIdentifier;
        private final List<Map<String, String>> answersIdentifiers;

        private static final String GROUP_COLUMN_NAME = "owner";

        public AnswerIdentifierGroup(List<Map<String, String>> answerIdentifierTable) {
            ownerIdentifier = answerIdentifierTable.get(0).get(GROUP_COLUMN_NAME);
            answersIdentifiers = new ArrayList<>();
            for (Map<String, String> rawAnswerIdentifiers : answerIdentifierTable) {
                answersIdentifiers.add(rawAnswerIdentifiers.entrySet().stream()
                        .filter(e -> !e.getKey().equals(GROUP_COLUMN_NAME))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            }
        }
    }

    private boolean matchAnswerConcept(Map<String, String> answerIdentifiers, ConceptMap answer) {
        for (Map.Entry<String, String> entry : answerIdentifiers.entrySet()) {
            String var = entry.getKey();
            String[] identifier = entry.getValue().split(":", 2);
            switch (identifier[0]) {
                case "label":
                    if (!new LabelUniquenessCheck(identifier[1]).check(answer.get(var))) {
                        return false;
                    }
                    break;
                case "key":
                    if (!new KeyUniquenessCheck(identifier[1]).check(answer.get(var))) {
                        return false;
                    }
                    break;
                case "attr":
                    if (!new AttributeValueUniquenessCheck(identifier[1]).check(answer.get(var))) {
                        return false;
                    }
                    break;
                case "value":
                    if (!new ValueUniquenessCheck(identifier[1]).check(answer.get(var))) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    @When("get answers of typeql fetch")
    public void typeql_fetch(String typeQLQueryStatements) {
        try {
            TypeQLFetch typeQLQuery = TypeQL.parseQuery(String.join("\n", typeQLQueryStatements)).asFetch();
            clearAnswers();
            fetchAnswers = tx().query().fetch(String.join("\n", typeQLQueryStatements)).collect(Collectors.toList());
        } catch (TypeQLException e) {
            // NOTE: We manually close transaction here, because we want to align with all non-java drivers,
            // where parsing happens at server-side which closes transaction if they fail
            tx().close();
            throw e;
        }
    }

    @Then("typeql fetch; throws exception")
    public void typeql_fetch_throws_exception(String typeQLQueryStatements) {
        assertThrows(() -> typeql_get(typeQLQueryStatements));
    }

    @Then("fetch answers are")
    public void fetch_answers_are(String expectedJSON) {
        JSON expected = JSON.parse(expectedJSON);
        assertTrue("Fetch response is a list of JSON objects, but the behaviour test expects something else", expected.isArray());
        assertTrue(JSONListMatches(fetchAnswers, expected.asArray()));
    }

    @Then("rules are")
    public void rules_are(Map<String, Map<String, String>> rules) {
        this.rules = rules;
    }

    @Then("rules contain: {type_label}")
    public void rules_contain(String ruleLabel) {
        assert (tx().logic().getRules().anyMatch(rule -> rule.getLabel().equals(ruleLabel)));
    }

    @Then("rules do not contain: {type_label}")
    public void rules_do_not_contain(String ruleLabel) {
        assert (tx().logic().getRules().noneMatch(rule -> rule.getLabel().equals(ruleLabel)));
    }

    @Then("answers contain explanation tree")
    public void answers_contain_explanation_tree(Map<Integer, Map<String, String>> explanationTree) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Then("each answer satisfies")
    public void each_answer_satisfies(String templatedQuery) {
        for (ConceptMap answer : answers) {
            String query = applyQueryTemplate(templatedQuery, answer);
            TypeQLGet typeQLQuery = TypeQL.parseQuery(query).asGet();
            long answerSize = tx().query().get(query).count();
            assertEquals(1, answerSize);
        }
    }

    @Then("templated typeql get; throws exception")
    public void templated_typeql_get_throws_exception(String templatedTypeQLQuery) {
        String templatedQuery = String.join("\n", templatedTypeQLQuery);
        for (ConceptMap answer : answers) {
            String queryString = applyQueryTemplate(templatedQuery, answer);
            assertThrows(() -> {
                TypeQLGet query = TypeQL.parseQuery(queryString).asGet();
                long ignored = tx().query().get(queryString).count();
            });
        }
    }

    private String applyQueryTemplate(String template, ConceptMap templateFiller) {
        // find shortest matching strings between <>
        Pattern pattern = Pattern.compile("<.+?>");
        Matcher matcher = pattern.matcher(template);

        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (matcher.find()) {
            String matched = matcher.group(0);
            String requiredVariable = variableFromTemplatePlaceholder(matched.substring(1, matched.length() - 1));

            builder.append(template, i, matcher.start());
            try {
                Concept concept = templateFiller.get(requiredVariable);
                if (!concept.isThing())
                    throw new ScenarioDefinitionException("Cannot apply IID templating to Type concepts");
                String conceptId = concept.asThing().getIID();
                builder.append(conceptId);
            } catch (TypeDBDriverException e) {
                if (e.getErrorMessage().equals(VARIABLE_DOES_NOT_EXIST)) {
                    throw new ScenarioDefinitionException(String.format("No IID available for template placeholder: %s.", matched));
                } else throw e;
            }
            i = matcher.end();
        }
        builder.append(template.substring(i));
        return builder.toString();
    }

    private String variableFromTemplatePlaceholder(String placeholder) {
        if (placeholder.endsWith(".iid")) {
            String stripped = placeholder.replace(".iid", "");
            String withoutPrefix = stripped.replace("answer.", "");
            return withoutPrefix;
        } else {
            throw new ScenarioDefinitionException("Cannot replace template not based on ID.");
        }
    }

    private static class ScenarioDefinitionException extends RuntimeException {
        ScenarioDefinitionException(String message) {
            super(message);
        }
    }

    private interface UniquenessCheck {
        boolean check(Concept concept);
    }

    public static class LabelUniquenessCheck implements UniquenessCheck {

        private final Label label;

        LabelUniquenessCheck(String scopedLabel) {
            String[] tokens = scopedLabel.split(":");
            this.label = tokens.length > 1 ? Label.of(tokens[0], tokens[1]) : Label.of(tokens[0]);
        }

        @Override
        public boolean check(Concept concept) {
            if (concept.isType()) return label.equals(concept.asType().getLabel());
            throw new ScenarioDefinitionException("Concept was checked for label uniqueness, but it is not a Type.");
        }
    }

    public static abstract class AttributeUniquenessCheck {

        protected final Label type;
        protected final String value;

        AttributeUniquenessCheck(String typeAndValue) {
            String[] s = typeAndValue.split(":", 2);
            assertEquals(
                    String.format("A check for attribute uniqueness should be given in the format \"type:value\", but received %s.", typeAndValue),
                    2, s.length
            );
            type = Label.of(s[0]);
            value = s[1];
        }
    }

    public static class AttributeValueUniquenessCheck extends AttributeUniquenessCheck implements UniquenessCheck {
        AttributeValueUniquenessCheck(String typeAndValue) {
            super(typeAndValue);
        }

        public boolean check(Concept concept) {
            if (!concept.isAttribute()) {
                return false;
            }
            Attribute attribute = concept.asAttribute();
            AttributeType attributeType = attribute.getType();
            if (attribute.getValue().isDateTime()) {
                LocalDateTime dateTime;
                try {
                    dateTime = LocalDateTime.parse(value);
                } catch (DateTimeParseException e) {
                    dateTime = LocalDate.parse(value).atStartOfDay();
                }
                return type.equals(attributeType.getLabel()) && dateTime.equals(attribute.getValue().asDateTime());
            } else return type.equals(attributeType.getLabel()) && value.equals(attribute.getValue().toString());
        }
    }

    public static class KeyUniquenessCheck extends AttributeUniquenessCheck implements UniquenessCheck {
        KeyUniquenessCheck(String typeAndValue) {
            super(typeAndValue);
        }

        @Override
        public boolean check(Concept concept) {
            if (!concept.isThing()) {
                return false;
            }

            Set<Attribute> keys = concept.asThing().getHas(tx(), set(key())).collect(Collectors.toSet());
            HashMap<Label, String> keyMap = new HashMap<>();

            for (Attribute key : keys) {
                keyMap.put(key.getType().getLabel(), key.getValue().toString());
            }
            return value.equals(keyMap.get(type));
        }
    }

    public static class ValueUniquenessCheck implements UniquenessCheck {
        private final String valueType;
        private final String value;

        ValueUniquenessCheck(String valueTypeAndValue) {
            String[] s = valueTypeAndValue.split(":", 2);
            this.valueType = s[0].toLowerCase().strip();
            this.value = s[1].strip();
        }

        public boolean check(Concept concept) {
            if (!concept.isValue()) {
                return false;
            }

            switch (concept.asValue().getType()) {
                case BOOLEAN:
                    return Boolean.valueOf(value).equals(concept.asValue().asBoolean());
                case LONG:
                    return Long.valueOf(value).equals(concept.asValue().asLong());
                case DOUBLE:
                    return equalsApproximate(Double.parseDouble(value), concept.asValue().asDouble());
                case STRING:
                    return value.equals(concept.asValue().asString());
                case DATETIME:
                    LocalDateTime dateTime;
                    try {
                        dateTime = LocalDateTime.parse(value);
                    } catch (DateTimeParseException e) {
                        dateTime = LocalDate.parse(value).atStartOfDay();
                    }
                    return dateTime.equals(concept.asValue().asDateTime());
                default:
                    throw new ScenarioDefinitionException("Unrecognised value type specified in test " + this.valueType);
            }
        }
    }
}
