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
import com.typedb.driver.api.answer.ConceptRowIterator;
import com.typedb.driver.api.answer.JSON;
import com.typedb.driver.api.answer.QueryAnswer;
import com.typedb.driver.test.behaviour.config.Parameters;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.threadPool;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.tx;
import static com.typedb.driver.test.behaviour.util.Util.JSONListMatches;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QuerySteps {
    private static QueryAnswer queryAnswer;
    private static List<ConceptRow> collectedRows;
    private static List<JSON> collectedDocuments;
    private static List<CompletableFuture<QueryAnswer>> queryAnswersParallel = null;
    private static List<JSON> fetchAnswers;

    private void clearAnswers() {
        queryAnswer = null;
        if (queryAnswersParallel != null) {
            CompletableFuture.allOf(queryAnswersParallel.toArray(CompletableFuture[]::new)).join();
            queryAnswersParallel = null;
        }

        collectedRows = null;
        collectedDocuments = null;

        fetchAnswers = null;
    }

    private void collectAnswerIfNeeded() {
        if (collectedRows != null || collectedDocuments != null) {
            return;
        }

        if (queryAnswer.isConceptRows()) {
            collectedRows = queryAnswer.asConceptRows().stream().collect(Collectors.toList());
        } else if (queryAnswer.isConceptDocuments()) {
            collectedDocuments = queryAnswer.asConceptDocuments().stream().collect(Collectors.toList());
        } else {
            throw new AssertionError("Query answer is not collectable");
        }
    }

    public void assertAnswerSize(int expectedAnswers) {
        int answerSize;
        if (collectedRows != null) {
            answerSize = collectedRows.size();
        } else if (collectedDocuments != null) {
            answerSize = collectedDocuments.size();
        } else {
            throw new AssertionError("Query answer is not collected: the size is NULL");
        }
        assertEquals(String.format("Expected [%d] answers, but got [%d]", expectedAnswers, answerSize), expectedAnswers, answerSize);
    }

    @Given("typeql write query{may_error}")
    @Given("typeql read query{may_error}")
    @Given("typeql schema query{may_error}")
    public void typeql_query(Parameters.MayError mayError, String query) {
        clearAnswers();
        mayError.check(() -> tx().query(query).resolve());
    }

    @Given("get answers of typeql write query{may_error}")
    @Given("get answers of typeql read query{may_error}")
    @Given("get answers of typeql schema query{may_error}")
    public void get_answers_of_typeql_query(Parameters.MayError mayError, String query) {
        clearAnswers();
        mayError.check(() -> queryAnswer = tx().query(query).resolve());
    }

    @Given("concurrently get answers of typeql write query {integer} times")
    @Given("concurrently get answers of typeql read query {integer} times")
    @Given("concurrently get answers of typeql schema query {integer} times")
    public void concurrently_get_answers_of_typeql_query_count_times(int count, String query) {
        clearAnswers();
        queryAnswersParallel = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            queryAnswersParallel.add(CompletableFuture.supplyAsync(() -> tx().query(query).resolve(), threadPool));
        }
    }

    @Then("answer type {is_or_not}: {query_answer_type}")
    public void answer_type_is(Parameters.IsOrNot isOrNot, Parameters.QueryAnswerType queryAnswerType) {
        switch (queryAnswerType) {
            case OK:
                isOrNot.check(queryAnswer.isOk());
                break;
            case CONCEPT_ROWS:
                isOrNot.check(queryAnswer.isConceptRows());
                break;
            case CONCEPT_DOCUMENTS:
                isOrNot.check(queryAnswer.isConceptDocuments());
                break;
            default:
                throw new AssertionError("Unknown query answer type: " + queryAnswerType);
        }
    }

    @Then("answer size is: {integer}")
    public void answer_size_is(int expectedAnswers) {
        collectAnswerIfNeeded();
        assertAnswerSize(expectedAnswers);
    }

    @Given("concurrently process {integer} row(s) from answers{may_error}")
    public void concurrently_process_count_rows_from_answers(int count, Parameters.MayError mayError) {
        queryAnswersParallel = new ArrayList<>();

        List<CompletableFuture<Object>> jobs = queryAnswersParallel.stream()
                .map(futureAnswer -> futureAnswer.thenComposeAsync(answer -> {
                    ConceptRowIterator iterator = answer.asConceptRows();
                    List<ConceptRow> rows = new ArrayList<>();

                    for (int i = 0; i < count; i++) {
                        try {
                            rows.add(iterator.next());
                        } catch (NoSuchElementException e) {
                            mayError.check(() -> {
                                throw e;
                            });
                        }
                    }

                    return null;
                }, threadPool))
                .collect(Collectors.toList());

        CompletableFuture.allOf(jobs.toArray(new CompletableFuture[0])).join();
    }


    // TODO: Refactor
    @Then("fetch answers are")
    public void fetch_answers_are(String expectedJSON) {
        JSON expected = JSON.parse(expectedJSON);
        assertTrue("Fetch response is a list of JSON objects, but the behaviour test expects something else", expected.isArray());
        assertTrue(JSONListMatches(fetchAnswers, expected.asArray()));
    }
}
