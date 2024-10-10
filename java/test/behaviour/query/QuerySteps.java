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

package com.typedb.driver.test.behaviour.query;

import com.typedb.driver.api.answer.ConceptRow;
import com.typedb.driver.api.answer.JSON;
import com.typedb.driver.api.answer.ValueGroup;
import com.typedb.driver.api.concept.Concept;
import com.typedb.driver.api.concept.thing.Attribute;
import com.typedb.driver.api.concept.type.AttributeType;
import com.typedb.driver.api.concept.value.Value;
import com.typedb.driver.test.behaviour.config.Parameters;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.typedb.driver.test.behaviour.util.Util.JSONListMatches;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QuerySteps {
    private static List<ConceptRow> answerRows;
    private static List<JSON> fetchAnswers;
    private static Optional<Value> valueAnswer;
    private static List<ValueGroup> valueAnswerGroups;
    private Map<String, Map<String, String>> rules;

    public static List<ConceptRow> answers() {
        return answerRows;
    }

    @Given("typeql define{may_error}")
    public void typeql_define(String defineQueryStatements, Parameters.MayError mayError) {
//        TypeQLDefine typeQLQuery = TypeQL.parseQuery(String.join("\n", defineQueryStatements));
//        mayError.check(() -> tx().query(String.join("\n", defineQueryStatements)).resolve());
    }

    private void clearAnswers() {
        answerRows = null;
        valueAnswer = null;
        fetchAnswers = null;
        valueAnswerGroups = null;
    }

//    @When("get answers of typeql insert")
//    public void get_answers_of_typeql_insert(String typeQLQueryStatements) {
//        TypeQLInsert typeQLQuery = TypeQL.parseQuery(String.join("\n", typeQLQueryStatements));
//        clearAnswers();
//        answers = tx().query().insert(String.join("\n", typeQLQueryStatements)).collect(Collectors.toList());
//    }

//    @When("get answers of typeql get")
//    public void typeql_get(String typeQLQueryStatements) {
//        try {
//            TypeQLGet typeQLQuery = TypeQL.parseQuery(String.join("\n", typeQLQueryStatements)).asGet();
//            clearAnswers();
//            answers = tx().query().get(String.join("\n", typeQLQueryStatements)).collect(Collectors.toList());
//        } catch (TypeQLException e) {
//            // NOTE: We manually close transaction here, because we want to align with all non-java drivers,
//            // where parsing happens at server-side which closes transaction if they fail
//            tx().close();
//            throw e;
//        }
//    }

    @Then("answer size is: {number}")
    public void answer_quantity_assertion(int expectedAnswers) {
        assertEquals(String.format("Expected [%d] answers, but got [%d]", expectedAnswers, answerRows.size()),
                expectedAnswers, answerRows.size());
    }

    @Then("uniquely identify answer concepts")
    public void uniquely_identify_answer_concepts(List<Map<String, String>> answerConcepts) {
        assertEquals(
                String.format("The number of identifier entries (rows) should match the number of answers, but found %d identifier entries and %d answers.",
                        answerConcepts.size(), answerRows.size()),
                answerConcepts.size(), answerRows.size()
        );

        for (ConceptRow row : answerRows) {
            List<Map<String, String>> matchingIdentifiers = new ArrayList<>();

            for (Map<String, String> answerIdentifier : answerConcepts) {

                if (matchAnswerConcept(answerIdentifier, row)) {
                    matchingIdentifiers.add(answerIdentifier);
                }
            }
//            assertEquals(String.format("An identifier entry (row) should match 1-to-1 to an row, but there were %d matching identifier entries for row with variables %s.", matchingIdentifiers.size(), row.variables().collect(Collectors.toSet())), 1, matchingIdentifiers.size());
        }
    }

    @Then("order of answer concepts is")
    public void order_of_answer_concepts_is(List<Map<String, String>> answersIdentifiers) {
        assertEquals(
                String.format("The number of identifier entries (rows) should match the number of answers, but found %d identifier entries and %d answers.",
                        answersIdentifiers.size(), answerRows.size()),
                answersIdentifiers.size(), answerRows.size()
        );
        for (int i = 0; i < answerRows.size(); i++) {
            ConceptRow answer = answerRows.get(i);
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

    @Then("aggregate answer is empty")
    public void aggregate_answer_is_empty() {
        assertNotNull("The last executed query was not an aggregate query", valueAnswer);
        assertTrue(valueAnswer.isEmpty());
    }

//    @Then("answer groups are")
//    public void answer_groups_are(List<Map<String, String>> answerIdentifierTable) {
//        Set<AnswerIdentifierGroup> answerIdentifierGroups = answerIdentifierTable.stream()
//                .collect(Collectors.groupingBy(x -> x.get(AnswerIdentifierGroup.GROUP_COLUMN_NAME)))
//                .values()
//                .stream()
//                .map(AnswerIdentifierGroup::new)
//                .collect(Collectors.toSet());
//
//        assertEquals(String.format("Expected [%d] answer groups, but found [%d].",
//                        answerIdentifierGroups.size(), answerGroups.size()),
//                answerIdentifierGroups.size(), answerGroups.size()
//        );
//
//        for (AnswerIdentifierGroup answerIdentifierGroup : answerIdentifierGroups) {
//            String[] identifier = answerIdentifierGroup.ownerIdentifier.split(":", 2);
//            UniquenessCheck checker;
//            switch (identifier[0]) {
//                case "label":
//                    checker = new LabelUniquenessCheck(identifier[1]);
//                    break;
//                case "key":
//                    checker = new KeyUniquenessCheck(identifier[1]);
//                    break;
//                case "attr":
//                    checker = new AttributeValueUniquenessCheck(identifier[1]);
//                    break;
//                case "value":
//                    checker = new ValueUniquenessCheck(identifier[1]);
//                    break;
//                default:
//                    throw new IllegalStateException("Unexpected value: " + identifier[0]);
//            }
//            ConceptMapGroup answerGroup = answerGroups.stream()
//                    .filter(ag -> checker.check(ag.owner()))
//                    .findAny()
//                    .orElse(null);
//            assertNotNull(String.format("The group identifier [%s] does not match any of the answer group owners.", answerIdentifierGroup.ownerIdentifier), answerGroup);
//
//            List<Map<String, String>> answersIdentifiers = answerIdentifierGroup.answersIdentifiers;
//            answerGroup.conceptMaps().forEach(answer -> {
//                List<Map<String, String>> matchingIdentifiers = new ArrayList<>();
//
//                for (Map<String, String> answerIdentifiers : answersIdentifiers) {
//
//                    if (matchAnswerConcept(answerIdentifiers, answer)) {
//                        matchingIdentifiers.add(answerIdentifiers);
//                    }
//                }
//                assertEquals(String.format("An identifier entry (row) should match 1-to-1 to an answer, but there were [%d] matching identifier entries for answer with variables %s.", matchingIdentifiers.size(), answer.variables().collect(Collectors.toSet())), 1, matchingIdentifiers.size());
//            });
//        }
//    }

//    @Then("group aggregate values are")
//    public void group_aggregate_values_are(List<Map<String, String>> answerIdentifierTable) {
//        Map<String, Double> expectations = new HashMap<>();
//        for (Map<String, String> answerIdentifierRow : answerIdentifierTable) {
//            String groupOwnerIdentifier = answerIdentifierRow.get(AnswerIdentifierGroup.GROUP_COLUMN_NAME);
//            double expectedAnswer = Double.parseDouble(answerIdentifierRow.get("value"));
//            expectations.put(groupOwnerIdentifier, expectedAnswer);
//        }
//
//        assertEquals(String.format("Expected [%d] answer groups, but found [%d].", expectations.size(), valueAnswerGroups.size()),
//                expectations.size(), valueAnswerGroups.size()
//        );
//
//        for (Map.Entry<String, Double> expectation : expectations.entrySet()) {
//            String[] identifier = expectation.getKey().split(":", 2);
//            UniquenessCheck checker;
//            switch (identifier[0]) {
//                case "label":
//                    checker = new LabelUniquenessCheck(identifier[1]);
//                    break;
//                case "key":
//                    checker = new KeyUniquenessCheck(identifier[1]);
//                    break;
//                case "attr":
//                    checker = new AttributeValueUniquenessCheck(identifier[1]);
//                    break;
//                case "value":
//                    checker = new ValueUniquenessCheck(identifier[1]);
//                    break;
//                default:
//                    throw new IllegalStateException("Unexpected value: " + identifier[0]);
//            }
//            double expectedAnswer = expectation.getValue();
//            ValueGroup answerGroup = valueAnswerGroups.stream()
//                    .filter(ag -> checker.check(ag.owner()))
//                    .findAny()
//                    .orElse(null);
//            assertNotNull(String.format("The group identifier [%s] does not match any of the answer group owners.", expectation.getKey()), answerGroup);
//
//            Value value = answerGroup.value().get();
//            double actualAnswer = value.isDouble() ? value.asDouble() : value.asLong();
//            assertEquals(
//                    String.format("Expected answer [%f] for group [%s], but got [%f]",
//                            expectedAnswer, expectation.getKey(), actualAnswer),
//                    expectedAnswer, actualAnswer, 0.001
//            );
//        }
//    }

//    @Then("number of groups is: {int}")
//    public void number_of_groups_is(int expectedGroupCount) {
//        assertEquals(expectedGroupCount, answerGroups.size());
//    }
//
//    public static class AnswerIdentifierGroup {
//        private final String ownerIdentifier;
//        private final List<Map<String, String>> answersIdentifiers;
//
//        private static final String GROUP_COLUMN_NAME = "owner";
//
//        public AnswerIdentifierGroup(List<Map<String, String>> answerIdentifierTable) {
//            ownerIdentifier = answerIdentifierTable.get(0).get(GROUP_COLUMN_NAME);
//            answersIdentifiers = new ArrayList<>();
//            for (Map<String, String> rawAnswerIdentifiers : answerIdentifierTable) {
//                answersIdentifiers.add(rawAnswerIdentifiers.entrySet().stream()
//                        .filter(e -> !e.getKey().equals(GROUP_COLUMN_NAME))
//                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
//            }
//        }
//    }


//    @Then("group aggregate answer value is empty")
//    public void group_aggregate_answer_value_not_a_number() {
//        assertEquals("Step requires exactly 1 grouped answer", 1, valueAnswerGroups.size());
//        assertTrue(valueAnswerGroups.get(0).value().isEmpty());
//    }

    private boolean matchAnswerConcept(Map<String, String> answerIdentifiers, ConceptRow row) {
        for (Map.Entry<String, String> entry : answerIdentifiers.entrySet()) {
            String var = entry.getKey();
            String[] identifier = entry.getValue().split(":", 2);
//            switch (identifier[0]) {
//                case "label":
//                    if (!new LabelUniquenessCheck(identifier[1]).check(row.get(var))) {
//                        return false;
//                    }
//                    break;
//                case "key":
//                    if (!new KeyUniquenessCheck(identifier[1]).check(row.get(var))) {
//                        return false;
//                    }
//                    break;
//                case "attr":
//                    if (!new AttributeValueUniquenessCheck(identifier[1]).check(row.get(var))) {
//                        return false;
//                    }
//                    break;
//                case "value":
//                    if (!new ValueUniquenessCheck(identifier[1]).check(row.get(var))) {
//                        return false;
//                    }
//                    break;
//            }
        }
        return true;
    }

//    @When("get answers of typeql fetch")
//    public void typeql_fetch(String typeQLQueryStatements) {
//        try {
//            TypeQLFetch typeQLQuery = TypeQL.parseQuery(String.join("\n", typeQLQueryStatements)).asFetch();
//            clearAnswers();
//            fetchAnswers = tx().query(String.join("\n", typeQLQueryStatements)).collect(Collectors.toList());
//        } catch (TypeQLException e) {
//            // NOTE: We manually close transaction here, because we want to align with all non-java drivers,
//            // where parsing happens at server-side which closes transaction if they fail
//            tx().close();
//            throw e;
//        }
//    }

    @Then("fetch answers are")
    public void fetch_answers_are(String expectedJSON) {
        JSON expected = JSON.parse(expectedJSON);
        assertTrue("Fetch response is a list of JSON objects, but the behaviour test expects something else", expected.isArray());
        assertTrue(JSONListMatches(fetchAnswers, expected.asArray()));
    }
}
