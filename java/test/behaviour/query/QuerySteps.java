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
import com.typedb.driver.api.concept.value.Value;
import com.typedb.driver.test.behaviour.config.Parameters;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.tx;
import static com.typedb.driver.test.behaviour.util.Util.JSONListMatches;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QuerySteps {
    private static List<ConceptRow> answerRows;
    private static List<JSON> fetchAnswers;
    private static Optional<Value> valueAnswer;

    public static List<ConceptRow> answers() {
        return answerRows;
    }

    private void clearAnswers() {
        answerRows = null;
        valueAnswer = null;
        fetchAnswers = null;
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
        // TODO: Get answers
        mayError.check(() -> tx().query(query).resolve());
    }

    @Then("answer size is: {integer}")
    public void answer_size_is(int expectedAnswers) {
        assertEquals(String.format("Expected [%d] answers, but got [%d]", expectedAnswers, answerRows.size()),
                expectedAnswers, answerRows.size());
    }

    @Then("fetch answers are")
    public void fetch_answers_are(String expectedJSON) {
        JSON expected = JSON.parse(expectedJSON);
        assertTrue("Fetch response is a list of JSON objects, but the behaviour test expects something else", expected.isArray());
        assertTrue(JSONListMatches(fetchAnswers, expected.asArray()));
    }
}
