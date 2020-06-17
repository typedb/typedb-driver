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

package grakn.client.test.integration.answer;

import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import grakn.client.answer.Explanation;
import grakn.client.exception.GraknClientException;
import grakn.client.test.setup.GraknProperties;
import grakn.client.test.setup.GraknSetup;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Variable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static grakn.client.GraknClient.Transaction.BatchSize.ALL;
import static grakn.client.GraknClient.Transaction.Options.batchSize;
import static grakn.client.GraknClient.Transaction.Options.infer;
import static grakn.client.GraknClient.Transaction.Options.explain;
import static graql.lang.Graql.Token.ValueType.STRING;
import static graql.lang.Graql.define;
import static graql.lang.Graql.insert;
import static graql.lang.Graql.match;
import static graql.lang.Graql.type;
import static graql.lang.Graql.var;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Integration Tests for Answers and Explanations
 */
public class AnswerIT {

    private static final String[] args = System.getProperty("sun.java.command").split(" ");
    private static final GraknSetup.GraknType graknType = GraknSetup.GraknType.of(args[1]);
    private static final File graknDistributionFile = new File(args[2]);
    private static GraknClient client;

    @BeforeClass
    public static void setUpClass() throws InterruptedException, IOException, TimeoutException {
        GraknSetup.bootup(graknType, graknDistributionFile);
        String address = System.getProperty(GraknProperties.GRAKN_ADDRESS);
        client = new GraknClient(address);
    }

    @AfterClass
    public static void closeSession() throws InterruptedException, TimeoutException, IOException {
        client.close();
        GraknSetup.shutdown(graknType);
    }

    @Test
    public void testExplanation() {
        GraknClient.Session session = client.session("test_rules");
        GraknClient.Transaction tx = session.transaction().write();
        tx.execute(Graql.parse(" define\n" +
                "                    object sub entity, plays owned, plays owner;\n" +
                "                    ownership sub relation, relates owned, relates owner;\n" +
                "                    transitive-ownership sub rule, when {\n" +
                "                        (owned: $x, owner: $y) isa ownership;\n" +
                "                        (owned: $y, owner: $z) isa ownership;\n" +
                "                    }, then {\n" +
                "                        (owned: $x, owner: $z) isa ownership;\n" +
                "                    };").asDefine());
        tx.execute(Graql.parse("insert\n" +
                "                    $a isa object; $b isa object; $c isa object; $d isa object; $e isa object;\n" +
                "                    (owned: $a, owner: $b) isa ownership;\n" +
                "                    (owned: $b, owner: $c) isa ownership;\n" +
                "                    (owned: $c, owner: $d) isa ownership;\n" +
                "                    (owned: $d, owner: $e) isa ownership;").asInsert());

        tx.commit();
        tx = session.transaction().write();

        List<ConceptMap> answers = tx.execute(Graql.parse("match (owner: $x, owned: $y) isa ownership; get;").asGet(), explain(true)).get();

        int hasExplanation = 0;
        int noExplanation = 0;
        for (ConceptMap answer : answers) {

            assertTrue(answer.queryPattern().toString().length() > 0);
            for (Variable var : answer.map().keySet()) {
                assertTrue(answer.queryPattern().variables().contains(var));
            }

            if (answer.hasExplanation()) {
                hasExplanation++;
                Explanation explanation = answer.explanation();
                assertEquals("transitive-ownership", explanation.getRule().label().toString());
                assertEquals("{ (owned: $x, owner: $y) isa ownership; (owned: $y, owner: $z) isa ownership; };", explanation.getRule().when().toString());
                assertEquals("{ (owned: $x, owner: $z) isa ownership; };", explanation.getRule().then().toString());
                assertNotNull(explanation);
                if (explanation.getAnswers().get(0).hasExplanation()) {
                    Explanation subExplanation = explanation.getAnswers().get(0).explanation();
                    assertNotNull(subExplanation);
                }
            } else {
                noExplanation++;
            }
        }

        assertEquals(4, noExplanation);
        assertEquals(6, hasExplanation);
    }

    @Test
    public void asynchronousWriteQueriesAreCompletedWhenTxCommit() {
        try (GraknClient.Session session = client.session("test_async")) {
            try (GraknClient.Transaction tx = session.transaction().write()) {
                tx.execute(define(
                        type("name").sub("attribute").value(STRING),
                        type("person").sub("entity").has("name")
                ));

                tx.execute(insert(
                        var("x").isa("person").has("name", "alice")
                ));

                tx.execute(insert(
                        var("x").isa("person").has("name", "bob")
                ));

                tx.commit();
            }

            try (GraknClient.Transaction tx = session.transaction().write()) {
                List<ConceptMap> answers = tx.execute(match(
                        var("x").isa("person").has("name", var("y"))
                ).get()).get();

                assertEquals(answers.size(), 2);
            }
        }
    }

    @Test
    public void writingInAReadTransactionThrows() {
        try (GraknClient.Session session = client.session("test")) {
            try (GraknClient.Transaction tx = session.transaction().read()) {
                tx.execute(Graql.parse("define newentity sub entity;").asDefine());
                tx.commit();
                fail();
            } catch (Exception ex) {
                if (!ex.getMessage().contains("is read only")) {
                    fail();
                }
            }
        }
    }

    @Test
    public void whenQueryingWithInferenceOn_inferredResultsExist() {
        try (GraknClient.Session session = client.session("infer_on")) {
            setupInferredRelations(session);

            try (GraknClient.Transaction tx = session.transaction().read()) {

                List<ConceptMap> answers = tx.execute(Graql.parse("match $x isa family; get;").asGet(),
                        infer(true)).get();

                assertEquals(1, answers.size());
            }
        }
    }

    @Test
    public void whenQueryingWithInferenceOff_inferredResultsDoNotExist() {
        try (GraknClient.Session session = client.session("infer_on")) {
            setupInferredRelations(session);

            try (GraknClient.Transaction tx = session.transaction().read()) {

                List<ConceptMap> answers = tx.execute(Graql.parse("match $x isa family; get;").asGet(),
                        infer(false)).get();

                assertEquals(0, answers.size());
            }
        }
    }

    @Test
    public void whenQueryingWithExplainFlag_explanationExist() {
        try (GraknClient.Session session = client.session("explain_on")) {
            setupInferredRelations(session);

            try (GraknClient.Transaction tx = session.transaction().read()) {

                List<ConceptMap> answers = tx.execute(Graql.parse("match $x isa family; get;").asGet(),
                        infer(true).explain(true)).get();

                assertEquals(1, answers.size());
                assertTrue(answers.get(0).hasExplanation());
                assertNotNull(answers.get(0).explanation());
            }
        }
    }

    @Test
    public void whenQueryingWithNoExplain_explanationDoesNotExist() {
        try (GraknClient.Session session = client.session("explain_off")) {
            setupInferredRelations(session);

            try (GraknClient.Transaction tx = session.transaction().read()) {

                List<ConceptMap> answers = tx.execute(Graql.parse("match $x isa family; get;").asGet(),
                        infer(true).explain(false)).get();

                assertEquals(1, answers.size());
                assertFalse(answers.get(0).hasExplanation());

                try {
                    answers.get(0).explanation();
                    fail();
                } catch (GraknClientException ex) {
                    // Expected
                }
            }
        }
    }

    @Test
    public void whenRequestingSubExplanationViaTransaction_subExplanationsExist() {
        try (GraknClient.Session session = client.session("sub_explanations")) {
            setupInferredRelations(session);

            try (GraknClient.Transaction tx = session.transaction().read()) {

                List<ConceptMap> answers = tx.execute(Graql.parse("match $x isa family, has family-name $n; get;").asGet(),
                        infer(true).explain(true)).get();

                assertEquals(1, answers.size());
                assertTrue(answers.get(0).hasExplanation());

                Explanation explanation = answers.get(0).explanation();

                assertNotNull(explanation);
                assertEquals(2, explanation.getAnswers().size());
                assertTrue(explanation.getAnswers().get(0).hasExplanation());
                assertNotNull(explanation.getAnswers().get(0).explanation());
            }
        }
    }

    private void setupInferredRelations(GraknClient.Session session) {
        try (GraknClient.Transaction tx = session.transaction().write()) {
            tx.execute(Graql.parse("define family-name sub attribute, value string;" +
                    "family sub relation, relates member, has family-name;" +
                    "person sub entity, has family-name, plays member;" +
                    "family-has-same-name sub rule," +
                    "when { $a isa person, has family-name $f; $b isa person, has family-name $f; $a != $b; }," +
                    "then { (member: $a, member: $b) isa family; };" +
                    "family-has-family-name sub rule," +
                    "when { $f (member: $a, member: $b) isa family; $a has family-name $n; }," +
                    "then { $f has family-name $n; };").asDefine());
            tx.execute(Graql.parse("insert $a isa person, has family-name \"bobson\";" +
                    "$b isa person, has family-name \"bobson\";").asInsert());
            tx.commit();
        }
    }

    @Test
    public void whenQueryingWithBatchSizeAll_runsCorrectly() {
        try (GraknClient.Session session = client.session("batch_size_all")) {
            setupLotsOfPeople(session, 999);

            try (GraknClient.Transaction tx = session.transaction().write()) {
                List<ConceptMap> answers = tx.execute(Graql.match(var("p").isa("person")).get(), batchSize(ALL)).get();
                assertEquals(999, answers.size());
            }
        }
    }

    @Test
    public void whenQueryingWithBatchSizeDefault_runsCorrectly() {
        try (GraknClient.Session session = client.session("batch_size_default")) {
            setupLotsOfPeople(session, 999);

            try (GraknClient.Transaction tx = session.transaction().write()) {
                List<ConceptMap> answers = tx.execute(Graql.match(var("p").isa("person")).get()).get();
                assertEquals(999, answers.size());
            }
        }
    }

    @Test
    public void whenQueryingWithBatchSizeCustom_runsCorrectly() {
        try (GraknClient.Session session = client.session("batch_size_custom")) {
            setupLotsOfPeople(session, 999);

            try (GraknClient.Transaction tx = session.transaction().write()) {
                assertEquals(999, tx.execute(Graql.match(var("p").isa("person")).get(), batchSize(20)).get().size());
                assertEquals(999, tx.execute(Graql.match(var("p").isa("person")).get(), batchSize(10000)).get().size());
                assertEquals(999, tx.execute(Graql.match(var("p").isa("person")).get(), batchSize(1)).get().size());
            }
        }
    }

    private void setupLotsOfPeople(GraknClient.Session session, int numberOfPeople) {
        try (GraknClient.Transaction tx = session.transaction().write()) {
            tx.execute(Graql.parse("define person sub entity;").asDefine());
            GraqlInsert personInsert = Graql.parse("insert $p isa person;");
            for (int i = 0; i < numberOfPeople; i++) {
                tx.execute(personInsert);
            }
            tx.commit();
        }
    }
}
