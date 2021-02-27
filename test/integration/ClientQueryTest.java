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

package grakn.client.test.integration;

import grakn.client.GraknClient;
import grakn.client.GraknClient.Session;
import grakn.client.GraknClient.Transaction;
import grakn.client.concept.answer.ConceptMap;
import grakn.common.test.server.GraknCoreRunner;
import graql.lang.Graql;
import graql.lang.common.GraqlArg;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlMatch;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static grakn.client.GraknClient.Transaction.Type.WRITE;
import static graql.lang.Graql.and;
import static graql.lang.Graql.rel;
import static graql.lang.Graql.rule;
import static graql.lang.Graql.type;
import static graql.lang.Graql.var;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("Duplicates")
public class ClientQueryTest {
    private static final Logger LOG = LoggerFactory.getLogger(ClientQueryTest.class);
    private static GraknCoreRunner grakn;
    private static GraknClient graknClient;


    @BeforeClass
    public static void setUpClass() throws InterruptedException, IOException, TimeoutException {
        grakn = new GraknCoreRunner();
        grakn.start();
        graknClient = GraknClient.core(grakn.address());
        if (graknClient.databases().contains("grakn")) graknClient.databases().get("grakn").delete();
        graknClient.databases().create("grakn");
    }

    @AfterClass
    public static void closeSession() throws Exception {
        graknClient.close();
        grakn.stop();
    }


    @Test
    public void applicationTest() {
        LOG.info("clientJavaE2E() - starting client-java E2E...");

        localhostGraknTx(tx -> {
            GraqlDefine defineQuery = Graql.define(
                    type("child-bearing").sub("relation").relates("offspring").relates("child-bearer"),
                    type("mating").sub("relation").relates("male-partner").relates("female-partner").plays("child-bearing", "child-bearer"),
                    type("parentship").sub("relation").relates("parent").relates("child"),

                    type("name").sub("attribute").value(GraqlArg.ValueType.STRING),
                    type("lion").sub("entity").owns("name").plays("mating", "male-partner").plays("mating", "female-partner").plays("child-bearing", "offspring").plays("parentship", "parent").plays("parentship", "child")
            );
            GraqlDefine ruleQuery = Graql.define(rule("infer-parentship-from-mating-and-child-bearing")
                                                         .when(and(
                                                                 rel("male-partner", var("male")).rel("female-partner", var("female")).isa("mating"),
                                                                 var("childbearing").rel("child-bearer").rel("offspring", var("offspring")).isa("child-bearing")))
                                                         .then(rel("parent", var("male"))
                                                                       .rel("parent", var("female"))
                                                                       .rel("child", var("offspring")).isa("parentship")));
            LOG.info("clientJavaE2E() - define a schema...");
            LOG.info("clientJavaE2E() - '" + defineQuery + "'");
            tx.query().define(defineQuery);
            tx.query().define(ruleQuery);
            tx.commit();
            LOG.info("clientJavaE2E() - done.");
        }, Session.Type.SCHEMA);

        // TODO: re-enable when match is implemented
//        localhostGraknTx(tx -> {
//            final GraqlMatch getThingQuery = Graql.match(var("t").sub("thing"));
//            LOG.info("clientJavaE2E() - assert if schema defined...");
//            LOG.info("clientJavaE2E() - '" + getThingQuery + "'");
//            final List<String> definedSchema = tx.query().match(getThingQuery).get()
//                    .map(answer -> answer.get("t").asType().asThingType().getLabel()).collect(Collectors.toList());
//            final String[] correctSchema = new String[]{"thing", "entity", "relation", "attribute",
//                    "lion", "mating", "parentship", "child-bearing", "name"};
//            assertThat(definedSchema, hasItems(correctSchema));
//            LOG.info("clientJavaE2E() - done.");
//        });

        localhostGraknTx(tx -> {
            String[] names = lionNames();
            GraqlInsert insertLionQuery = Graql.insert(
                    var().isa("lion").has("name", names[0]),
                    var().isa("lion").has("name", names[1]),
                    var().isa("lion").has("name", names[2])
            );
            LOG.info("clientJavaE2E() - insert some data...");
            LOG.info("clientJavaE2E() - '" + insertLionQuery + "'");
            tx.query().insert(insertLionQuery);
            tx.commit();
            LOG.info("clientJavaE2E() - done.");
        });

        // TODO: uncomment when match is implemented
//        localhostGraknTx(tx -> {
//            final String[] familyMembers = lionNames();
//            LOG.info("clientJavaE2E() - inserting mating relations...");
//            final GraqlInsert insertMatingQuery = Graql.match(
//                    var("lion").isa("lion").has("name", familyMembers[0]),
//                    var("lioness").isa("lion").has("name", familyMembers[1]))
//                    .insert(rel("male-partner", "lion").rel("female-partner", var("lioness")).isa("mating"));
//            LOG.info("clientJavaE2E() - '" + insertMatingQuery + "'");
//            final long insertedMating = tx.query().insert(insertMatingQuery).get().count();
//
//            LOG.info("clientJavaE2E() - inserting child-bearing relations...");
//            final GraqlInsert insertChildBearingQuery = Graql.match(
//                    var("lion").isa("lion").has("name", familyMembers[0]),
//                    var("lioness").isa("lion").has("name", familyMembers[1]),
//                    var("offspring").isa("lion").has("name", familyMembers[2]),
//                    var("mating").rel("male-partner", var("lion")).rel("female-partner", var("lioness")).isa("mating")
//            )
//                    .insert(var("childbearing").rel("child-bearer", var("mating")).rel("offspring", var("offspring")).isa("child-bearing"));
//            LOG.info("clientJavaE2E() - '" + insertChildBearingQuery + "'");
//            final long insertedChildBearing = tx.query().insert(insertChildBearingQuery).get().count();
//
//            tx.commit();
//
//            assertEquals(1, insertedMating);
//            assertEquals(1, insertedChildBearing);
//            LOG.info("clientJavaE2E() - done.");
//        });
//
//
//        localhostGraknTx(tx -> {
//            LOG.info("clientJavaE2E() - execute match get on the lion instances...");
//            final GraqlMatch getLionQuery = Graql.match(var("p").isa("lion").has("name", var("n")));
//            LOG.info("clientJavaE2E() - '" + getLionQuery + "'");
//            final Stream<ConceptMap> insertedLions = tx.query().match(getLionQuery).get();
//            final List<String> insertedNames = insertedLions.map(answer -> answer.get("n").asThing().asAttribute().asString().getValue()).collect(Collectors.toList());
//            assertThat(insertedNames, containsInAnyOrder(lionNames()));
//
//            LOG.info("clientJavaE2E() - execute match get on the mating relations...");
//            final GraqlMatch getMatingQuery = Graql.match(var("m").isa("mating"));
//            LOG.info("clientJavaE2E() - '" + getMatingQuery + "'");
//            final long insertedMating = tx.query().match(getMatingQuery).get().count();
//            assertEquals(1, insertedMating);
//
//            LOG.info("clientJavaE2E() - execute match get on the child-bearing...");
//            final GraqlMatch getChildBearingQuery = Graql.match(var("cb").isa("child-bearing"));
//            LOG.info("clientJavaE2E() - '" + getChildBearingQuery + "'");
//            final long insertedChildBearing = tx.query().match(getChildBearingQuery).get().count();
//            assertEquals(1, insertedChildBearing);
//            LOG.info("clientJavaE2E() - done.");
//        });
//
//        localhostGraknTx(tx -> {
//            LOG.info("clientJavaE2E() - match get inferred relations...");
//            final GraqlMatch getParentship = Graql.match(
//                    var("parentship")
//                            .rel("parent", var("parent"))
//                            .rel("child", var("child"))
//                            .isa("parentship"));
//            LOG.info("clientJavaE2E() - '" + getParentship + "'");
//            final long parentship = tx.query().match(getParentship).get().count();
//            //2 answers - single answer for each parent
//            assertEquals(2, parentship);
//            LOG.info("clientJavaE2E() - done.");
//        });

        // TODO: uncomment when aggregate is implemented
//        localhostGraknTx(tx -> {
//            LOG.info("clientJavaE2E() - match aggregate...");
//            GraqlMatch.Aggregate aggregateQuery = Graql.match(var("p").isa("lion")).count();
//            LOG.info("clientJavaE2E() - '" + aggregateQuery + "'");
//            int aggregateCount = tx.query().aggregate(aggregateQuery).get().get(0).number().intValue();
//            assertThat(aggregateCount, equalTo(lionNames().length));
//            LOG.info("clientJavaE2E() - done.");
//        });

        // TODO: uncomment when compute is implemented
//        localhostGraknTx(tx -> {
//            LOG.info("clientJavaE2E() - compute count...");
//            final GraqlCompute.Statistics computeQuery = Graql.compute().count().in("lion");
//            LOG.info("clientJavaE2E() - '" + computeQuery + "'");
//            int computeCount = tx.execute(computeQuery).get().get(0).number().intValue();
//            assertThat(computeCount, equalTo(lionNames().length));
//            LOG.info("clientJavaE2E() - done.");
//        });

        // TODO: uncomment when match is implemented
//        localhostGraknTx(tx -> {
//            LOG.info("clientJavaE2E() - match delete...");
//            GraqlDelete deleteQuery = Graql.match(var("m").isa("mating")).delete(var("m").isa("mating"));
//            LOG.info("clientJavaE2E() - '" + deleteQuery + "'");
//            tx.query().delete(deleteQuery).get();
//            final long matings = tx.query().match(Graql.match(var("m").isa("mating"))).get().count();
//            assertEquals(0, matings);
//            LOG.info("clientJavaE2E() - done.");
//        });

        LOG.info("clientJavaE2E() - client-java E2E test done.");
    }

    private String[] commitSHAs() {
        return new String[] {
                // queried commits
                "VladGan/console@4bdc38acb87f9fd2fbdb7cbcf2bcc93837382cab",
                "VladGan/console@b5ecd4707ce425d7d2d4d0b0d53420cb46e8ce52",
                "VladGan/console@b16788637949c6b4c2a3a4bacc8da101bf838b38",
                "VladGan/console@8e996fdf8d802d270385ac3bc7cbf5fa77ac0583",
                "VladGan/console@1ff6651afa7abf43b5bdd3b1903e489d279e3dc6",
                "VladGan/console@6d3ceda79eb3e3dc86d266095b613a53fb083d30",
                "VladGan/console@23da5b400e32805c29f41671ff3f92ef48eafcf8",
                // not queried commits
                "VladGan/console@0000000000000000000000000000000000000000",
                "VladGan/console@1111111111111111111111111111111111111111",
                "VladGan/console@2222222222222222222222222222222222222222",
        };
    }

    @Test
    public void parallelQueriesInTransactionTest() {
        localhostGraknTx(tx -> {
            GraqlDefine defineQuery = Graql.define(
                    type("symbol").sub("attribute").value(GraqlArg.ValueType.STRING),
                    type("name").sub("attribute").value(GraqlArg.ValueType.STRING),
                    type("status").sub("attribute").value(GraqlArg.ValueType.STRING),
                    type("latest").sub("attribute").value(GraqlArg.ValueType.BOOLEAN),

                    type("commit").sub("entity")
                            .owns("symbol")
                            .plays("pipeline-automation", "trigger"),
                    type("pipeline").sub("entity")
                            .owns("name")
                            .owns("latest")
                            .plays("pipeline-workflow", "pipeline")
                            .plays("pipeline-automation", "pipeline"),
                    type("workflow").sub("entity")
                            .owns("name")
                            .owns("status")
                            .owns("latest")
                            .plays("pipeline-workflow", "workflow"),

                    type("pipeline-workflow").sub("relation")
                        .relates("pipeline").relates("workflow"),
                type("pipeline-automation").sub("relation")
                        .relates("pipeline").relates("trigger")
            );

            LOG.info("clientJavaE2E() - define a schema...");
            LOG.info("clientJavaE2E() - '" + defineQuery + "'");
            tx.query().define(defineQuery);
            tx.commit();
            LOG.info("clientJavaE2E() - done.");
        }, Session.Type.SCHEMA);



        localhostGraknTx(tx -> {
            String[] commits = commitSHAs();
            GraqlInsert insertCommitQuery = Graql.insert(
                    var().isa("commit").has("symbol", commits[0]),
                    var().isa("commit").has("symbol", commits[1]),
                    var().isa("commit").has("symbol", commits[3]),
                    var().isa("commit").has("symbol", commits[4]),
                    var().isa("commit").has("symbol", commits[5]),
                    var().isa("commit").has("symbol", commits[6]),
                    var().isa("commit").has("symbol", commits[7]),
                    var().isa("commit").has("symbol", commits[8]),
                    var().isa("commit").has("symbol", commits[9])
            );

            LOG.info("clientJavaE2E() - insert commit data...");
            LOG.info("clientJavaE2E() - '" + insertCommitQuery + "'");
            tx.query().insert(insertCommitQuery);
            tx.commit();
            LOG.info("clientJavaE2E() - done.");
        });

        localhostGraknTx(tx -> {
            GraqlInsert insertWorkflowQuery = Graql.insert(
                    var().isa("workflow")
                            .has("name", "workflow-A")
                            .has("status", "running")
                            .has("latest", true),
                    var().isa("workflow")
                            .has("name", "workflow-B")
                            .has("status", "finished")
                            .has("latest", false)
            );

            LOG.info("clientJavaE2E() - insert workflow data...");
            LOG.info("clientJavaE2E() - '" + insertWorkflowQuery + "'");
            tx.query().insert(insertWorkflowQuery);
            tx.commit();
            LOG.info("clientJavaE2E() - done.");
        });

        localhostGraknTx(tx -> {
            GraqlInsert insertPipelineQuery = Graql.insert(
                    var().isa("pipeline")
                            .has("name", "pipeline-A")
                            .has("latest", true),
                    var().isa("pipeline")
                            .has("name", "pipeline-B")
                            .has("latest", false)
            );

            LOG.info("clientJavaE2E() - insert pipeline data...");
            LOG.info("clientJavaE2E() - '" + insertPipelineQuery + "'");
            tx.query().insert(insertPipelineQuery);
            tx.commit();
            LOG.info("clientJavaE2E() - done.");
        });

        localhostGraknTx(tx -> {
            String[] commitShas = commitSHAs();
            LOG.info("clientJavaE2E() - inserting pipeline-automation relations...");

            for (int i = 0; i < commitShas.length / 2; i++) {
                GraqlInsert insertPipelineAutomationQuery = Graql.match(
                        var("commit").isa("commit").has("symbol", commitShas[i]),
                        var("pipeline").isa("pipeline").has("name", "pipeline-A")
                )
                        .insert(
                                rel("pipeline", "pipeline").rel("trigger", "commit").isa("pipeline-automation")
                        );
                LOG.info("clientJavaE2E() - '" + insertPipelineAutomationQuery + "'");
                List<ConceptMap> x = tx.query().insert(insertPipelineAutomationQuery).collect(toList());
            }


            for (int i = commitShas.length / 2; i < commitShas.length; i++) {
                GraqlInsert insertPipelineAutomationQuery = Graql.match(
                        var("commit").isa("commit").has("symbol", commitShas[i]),
                        var("pipeline").isa("pipeline").has("name", "pipeline-B")
                )
                        .insert(
                                rel("pipeline", "pipeline").rel("trigger", "commit").isa("pipeline-automation")
                        );
                LOG.info("clientJavaE2E() - '" + insertPipelineAutomationQuery + "'");
                List<ConceptMap> x = tx.query().insert(insertPipelineAutomationQuery).collect(toList());
            }

            tx.commit();

            LOG.info("clientJavaE2E() - done.");
        });

        localhostGraknTx(tx -> {
            LOG.info("clientJavaE2E() - inserting pipeline-automation relations...");

            GraqlInsert insertPipelineWorkflowQuery = Graql.match(
                    var("pipelineA").isa("pipeline").has("name", "pipeline-A"),
                    var("workflowA").isa("workflow").has("name", "workflow-A"),
                    var("pipelineB").isa("pipeline").has("name", "pipeline-B"),
                    var("workflowB").isa("workflow").has("name", "workflow-B")
            )
                    .insert(
                            rel("pipeline", "pipelineA").rel("workflow", "workflowA").isa("pipeline-workflow"),
                            rel("pipeline", "pipelineB").rel("workflow", "workflowB").isa("pipeline-workflow")
                    );
            LOG.info("clientJavaE2E() - '" + insertPipelineWorkflowQuery + "'");
            List<ConceptMap> x = tx.query().insert(insertPipelineWorkflowQuery).collect(toList());

            tx.commit();

            LOG.info("clientJavaE2E() - done.");
        });

        String[] queries = {
                "match\n" +
                        "$commit isa commit, has symbol \"VladGan/console@4bdc38acb87f9fd2fbdb7cbcf2bcc93837382cab\";\n" +
                        "$_ (trigger: $commit, pipeline: $pipeline) isa pipeline-automation;\n" +
                        "$pipeline has name $pipeline-name, has latest true;\n" +
                        "$_ (pipeline: $pipeline, workflow: $workflow) isa pipeline-workflow;\n" +
                        "$workflow has name $workflow-name, has status $workflow-status, has latest true;\n" +
                        "get $pipeline-name, $workflow-name, $workflow-status;",
                "match\n" +
                        "$commit isa commit, has symbol \"VladGan/console@b5ecd4707ce425d7d2d4d0b0d53420cb46e8ce52\";\n" +
                        "$_ (trigger: $commit, pipeline: $pipeline) isa pipeline-automation;\n" +
                        "$pipeline has name $pipeline-name, has latest true;\n" +
                        "$_ (pipeline: $pipeline, workflow: $workflow) isa pipeline-workflow;\n" +
                        "$workflow has name $workflow-name, has status $workflow-status, has latest true;\n" +
                        "get $pipeline-name, $workflow-name, $workflow-status;",
                "match\n" +
                        "$commit isa commit, has symbol \"VladGan/console@b16788637949c6b4c2a3a4bacc8da101bf838b38\";\n" +
                        "$_ (trigger: $commit, pipeline: $pipeline) isa pipeline-automation;\n" +
                        "$pipeline has name $pipeline-name, has latest true;\n" +
                        "$_ (pipeline: $pipeline, workflow: $workflow) isa pipeline-workflow;\n" +
                        "$workflow has name $workflow-name, has status $workflow-status, has latest true;\n" +
                        "get $pipeline-name, $workflow-name, $workflow-status;",
                "match\n" +
                        "$commit isa commit, has symbol \"VladGan/console@8e996fdf8d802d270385ac3bc7cbf5fa77ac0583\";\n" +
                        "$_ (trigger: $commit, pipeline: $pipeline) isa pipeline-automation;\n" +
                        "$pipeline has name $pipeline-name, has latest true;\n" +
                        "$_ (pipeline: $pipeline, workflow: $workflow) isa pipeline-workflow;\n" +
                        "$workflow has name $workflow-name, has status $workflow-status, has latest true;\n" +
                        "get $pipeline-name, $workflow-name, $workflow-status;",
                "match\n" +
                        "$commit isa commit, has symbol \"VladGan/console@1ff6651afa7abf43b5bdd3b1903e489d279e3dc6\";\n" +
                        "$_ (trigger: $commit, pipeline: $pipeline) isa pipeline-automation;\n" +
                        "$pipeline has name $pipeline-name, has latest true;\n" +
                        "$_ (pipeline: $pipeline, workflow: $workflow) isa pipeline-workflow;\n" +
                        "$workflow has name $workflow-name, has status $workflow-status, has latest true;\n" +
                        "get $pipeline-name, $workflow-name, $workflow-status;",
                "match\n" +
                        "$commit isa commit, has symbol \"VladGan/console@6d3ceda79eb3e3dc86d266095b613a53fb083d30\";\n" +
                        "$_ (trigger: $commit, pipeline: $pipeline) isa pipeline-automation;\n" +
                        "$pipeline has name $pipeline-name, has latest true;\n" +
                        "$_ (pipeline: $pipeline, workflow: $workflow) isa pipeline-workflow;\n" +
                        "$workflow has name $workflow-name, has status $workflow-status, has latest true;\n" +
                        "get $pipeline-name, $workflow-name, $workflow-status;",
                "match\n" +
                        "$commit isa commit, has symbol \"VladGan/console@23da5b400e32805c29f41671ff3f92ef48eafcf8\";\n" +
                        "$_ (trigger: $commit, pipeline: $pipeline) isa pipeline-automation;\n" +
                        "$pipeline has name $pipeline-name, has latest true;\n" +
                        "$_ (pipeline: $pipeline, workflow: $workflow) isa pipeline-workflow;\n" +
                        "$workflow has name $workflow-name, has status $workflow-status, has latest true;\n" +
                        "get $pipeline-name, $workflow-name, $workflow-status;"
        };

        localhostGraknTx(tx -> {
            LOG.info("clientJavaE2E() - inserting pipeline-automation relations...");

            Stream.of(queries).parallel().forEach(x -> {
                GraqlMatch q = Graql.parseQuery(x).asMatch();
                List<ConceptMap> res = tx.query().match(q).collect(toList());
            });

            LOG.info("clientJavaE2E() - done.");
        });
    }

    private String[] lionNames() {
        return new String[]{"male-partner", "female-partner", "young-lion"};
    }

    private void localhostGraknTx(Consumer<Transaction> fn) {
        String database = "grakn";
        try (Session session = graknClient.session(database, Session.Type.DATA)) {
            try (Transaction transaction = session.transaction(WRITE)) {
                fn.accept(transaction);
            }
        }
    }

    private void localhostGraknTx(Consumer<Transaction> fn, Session.Type sessionType) {
        String database = "grakn";
        try (Session session = graknClient.session(database, sessionType)) {
            try (Transaction transaction = session.transaction(WRITE)) {
                fn.accept(transaction);
            }
        }
    }
}
