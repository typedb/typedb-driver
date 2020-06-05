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
import grakn.client.concept.ValueType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.MetaType;
import grakn.client.test.setup.GraknProperties;
import grakn.client.test.setup.GraknSetup;
import graql.lang.Graql;
import graql.lang.statement.Variable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static graql.lang.Graql.Token.ValueType.STRING;
import static graql.lang.Graql.define;
import static graql.lang.Graql.insert;
import static graql.lang.Graql.match;
import static graql.lang.Graql.type;
import static graql.lang.Graql.var;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

        List<ConceptMap> answers = tx.execute(Graql.parse("match (owner: $x, owned: $y) isa ownership; get;").asGet()).get();

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
}