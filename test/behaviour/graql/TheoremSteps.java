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

package grakn.client.test.behaviour.graql;

import com.google.common.collect.Iterators;
import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import grakn.client.test.behaviour.connection.ConnectionSteps;
import graql.lang.Graql;
import graql.lang.pattern.Pattern;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlQuery;
import graql.lang.statement.Variable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TheoremSteps {

    private static List<ConceptMap> matchAnswers;

    @Given("load file: {graql-file}")
    public void load_graql_file(Path graqlFilePath) throws IOException {
        List<String> graqlLines = Files.readAllLines(graqlFilePath, StandardCharsets.UTF_8);
        GraqlQuery graqlQuery = Graql.parse(String.join("\n", graqlLines));

        GraknClient.Session session = Iterators.getOnlyElement(ConnectionSteps.sessionsMap.values().iterator());
        GraknClient.Transaction tx = session.transaction().write();
        tx.execute(graqlQuery);
        tx.commit();
    }

    @When("match {patterns}; get {vars};")
    public void answer_generator(List<?> matchPatterns, List<?> variables) {
        GraqlGet query = Graql.match((List<Pattern>)matchPatterns).get((List<Variable>)variables);
        GraknClient.Session session = Iterators.getOnlyElement(ConnectionSteps.sessionsMap.values().iterator());
        matchAnswers = session.transaction().read().execute(query);
    }

    @Then("answers satisfy match {patterns}; get {vars};")
    public void verify_computed_answers_against_new_query(List<?> matchPatterns, List<?> variables) {
        GraknClient.Session session = Iterators.getOnlyElement(ConnectionSteps.sessionsMap.values().iterator());

        for (ConceptMap answer : matchAnswers) {
            List<Pattern> matchWithIds = new ArrayList<>(variables.size());
            for (Variable var : (List<Variable>)variables) {
                matchWithIds.add(Graql.var(var).id(answer.get(var).id().toString()));
            }
            matchWithIds.addAll((List<Pattern>)matchPatterns);
            GraqlGet query = Graql.match(matchWithIds).get((List<Variable>)variables);

            List<ConceptMap> execute = session.transaction().read().execute(query);
            assertEquals(1, execute.size());
        }
    }
}
