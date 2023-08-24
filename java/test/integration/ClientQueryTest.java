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

package com.vaticle.typedb.client.test.integration;

import com.vaticle.typedb.client.TypeDB;
import com.vaticle.typedb.client.api.TypeDBClient;
import com.vaticle.typedb.client.api.TypeDBOptions;
import com.vaticle.typedb.client.api.TypeDBSession;
import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.answer.ConceptMap;
import com.vaticle.typedb.client.api.concept.type.AttributeType;
import com.vaticle.typedb.client.api.concept.type.EntityType;
import com.vaticle.typedb.client.api.logic.Explanation;
import com.vaticle.typedb.common.collection.Pair;
import com.vaticle.typedb.common.test.core.TypeDBCoreRunner;
import com.vaticle.typeql.lang.TypeQL;
import com.vaticle.typeql.lang.common.TypeQLArg;
import com.vaticle.typeql.lang.query.TypeQLDefine;
import com.vaticle.typeql.lang.query.TypeQLInsert;
import com.vaticle.typeql.lang.query.TypeQLMatch;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.api.TypeDBSession.Type.DATA;
import static com.vaticle.typedb.client.api.TypeDBTransaction.Type.READ;
import static com.vaticle.typedb.client.api.TypeDBTransaction.Type.WRITE;
import static com.vaticle.typeql.lang.TypeQL.and;
import static com.vaticle.typeql.lang.TypeQL.cVar;
import static com.vaticle.typeql.lang.TypeQL.rel;
import static com.vaticle.typeql.lang.TypeQL.rule;
import static com.vaticle.typeql.lang.TypeQL.type;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("Duplicates")
public class ClientQueryTest {
    private static final Logger LOG = LoggerFactory.getLogger(ClientQueryTest.class);
    private static TypeDBCoreRunner typedb;
    private static TypeDBClient typedbClient;

    @BeforeClass
    public static void setUpClass() throws InterruptedException, IOException, TimeoutException {
        typedb = new TypeDBCoreRunner();
        typedb.start();
        typedbClient = TypeDB.coreClient(typedb.address());
        if (typedbClient.databases().contains("typedb")) typedbClient.databases().get("typedb").delete();
        typedbClient.databases().create("typedb");
    }

    @AfterClass
    public static void closeSession() {
        typedbClient.close();
        typedb.stop();
    }

    @Test
    public void applicationTest() {
        LOG.info("clientJavaE2E() - starting client-java E2E...");

        localhostTypeDBTX(tx -> {
            TypeQLDefine defineQuery = TypeQL.define(type("child-bearing").sub("relation").relates("offspring").relates("child-bearer"),
                    type("mating").sub("relation").relates("male-partner").relates("female-partner").plays("child-bearing", "child-bearer"),
                    type("parentship").sub("relation").relates("parent").relates("child"),
                    type("name").sub("attribute").value(TypeQLArg.ValueType.STRING),
                    type("lion").sub("entity").owns("name").plays("mating", "male-partner").plays("mating", "female-partner")
                            .plays("child-bearing", "offspring").plays("parentship", "parent").plays("parentship", "child"));
            TypeQLDefine ruleQuery = TypeQL.define(rule("infer-parentship-from-mating-and-child-bearing").when(
                            and(rel("male-partner", cVar("male")).rel("female-partner", cVar("female")).isa("mating"),
                                    cVar("childbearing").rel("child-bearer", cVar()).rel("offspring", cVar("offspring")).isa("child-bearing")))
                    .then(rel("parent", cVar("male")).rel("parent", cVar("female")).rel("child", cVar("offspring")).isa("parentship")));
            LOG.info("clientJavaE2E() - define a schema...");
            LOG.info("clientJavaE2E() - '" + defineQuery + "'");
            tx.query().define(defineQuery);
            tx.query().define(ruleQuery);
            tx.commit();
            LOG.info("clientJavaE2E() - done.");
        }, TypeDBSession.Type.SCHEMA);

        localhostTypeDBTX(tx -> {
            String[] names = lionNames();
            TypeQLInsert insertLionQuery = TypeQL.insert(cVar().isa("lion").has("name", names[0]), cVar().isa("lion").has("name", names[1]), cVar().isa("lion").has("name", names[2]));
            LOG.info("clientJavaE2E() - insert some data...");
            LOG.info("clientJavaE2E() - '" + insertLionQuery + "'");
            tx.query().insert(insertLionQuery);
            tx.commit();
            LOG.info("clientJavaE2E() - done.");
        }, WRITE);

        LOG.info("clientJavaE2E() - client-java E2E test done.");
    }

    private String[] commitSHAs() {
        return new String[]{
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
        localhostTypeDBTX(tx -> {
            TypeQLDefine defineQuery = TypeQL.define(
                    type("symbol").sub("attribute").value(TypeQLArg.ValueType.STRING),
                    type("name").sub("attribute").value(TypeQLArg.ValueType.STRING),
                    type("status").sub("attribute").value(TypeQLArg.ValueType.STRING),
                    type("latest").sub("attribute").value(TypeQLArg.ValueType.BOOLEAN),

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
        }, TypeDBSession.Type.SCHEMA);


        localhostTypeDBTX(tx -> {
            String[] commits = commitSHAs();
            TypeQLInsert insertCommitQuery = TypeQL.insert(cVar().isa("commit").has("symbol", commits[0]),
                    cVar().isa("commit").has("symbol", commits[1]), cVar().isa("commit").has("symbol", commits[3]),
                    cVar().isa("commit").has("symbol", commits[4]), cVar().isa("commit").has("symbol", commits[5]),
                    cVar().isa("commit").has("symbol", commits[6]), cVar().isa("commit").has("symbol", commits[7]),
                    cVar().isa("commit").has("symbol", commits[8]), cVar().isa("commit").has("symbol", commits[9]));

            LOG.info("clientJavaE2E() - insert commit data...");
            LOG.info("clientJavaE2E() - '" + insertCommitQuery + "'");
            tx.query().insert(insertCommitQuery);
            tx.commit();
            LOG.info("clientJavaE2E() - done.");
        }, WRITE);

        localhostTypeDBTX(tx -> {
            TypeQLInsert insertWorkflowQuery = TypeQL.insert(cVar().isa("workflow")
                            .has("name", "workflow-A")
                            .has("status", "running")
                            .has("latest", true), cVar().isa("workflow")
                            .has("name", "workflow-B")
                            .has("status", "finished")
                            .has("latest", false)
            );

            LOG.info("clientJavaE2E() - insert workflow data...");
            LOG.info("clientJavaE2E() - '" + insertWorkflowQuery + "'");
            tx.query().insert(insertWorkflowQuery);
            tx.commit();
            LOG.info("clientJavaE2E() - done.");
        }, WRITE);

        localhostTypeDBTX(tx -> {
            TypeQLInsert insertPipelineQuery = TypeQL.insert(cVar().isa("pipeline")
                            .has("name", "pipeline-A")
                            .has("latest", true), cVar().isa("pipeline")
                            .has("name", "pipeline-B")
                            .has("latest", false)
            );

            LOG.info("clientJavaE2E() - insert pipeline data...");
            LOG.info("clientJavaE2E() - '" + insertPipelineQuery + "'");
            tx.query().insert(insertPipelineQuery);
            tx.commit();
            LOG.info("clientJavaE2E() - done.");
        }, WRITE);

        localhostTypeDBTX(tx -> {
            String[] commitShas = commitSHAs();
            LOG.info("clientJavaE2E() - inserting pipeline-automation relations...");

            for (int i = 0; i < commitShas.length / 2; i++) {
                TypeQLInsert insertPipelineAutomationQuery = TypeQL.match(cVar("commit").isa("commit").has("symbol", commitShas[i]), cVar("pipeline").isa("pipeline").has("name", "pipeline-A"))
                        .insert(rel("pipeline", cVar("pipeline")).rel("trigger", cVar("commit")).isa("pipeline-automation")
                        );
                LOG.info("clientJavaE2E() - '" + insertPipelineAutomationQuery + "'");
                List<ConceptMap> x = tx.query().insert(insertPipelineAutomationQuery).collect(toList());
            }


            for (int i = commitShas.length / 2; i < commitShas.length; i++) {
                TypeQLInsert insertPipelineAutomationQuery = TypeQL.match(cVar("commit").isa("commit").has("symbol", commitShas[i]), cVar("pipeline").isa("pipeline").has("name", "pipeline-B"))
                        .insert(rel("pipeline", cVar("pipeline")).rel("trigger", cVar("commit")).isa("pipeline-automation")
                        );
                LOG.info("clientJavaE2E() - '" + insertPipelineAutomationQuery + "'");
                List<ConceptMap> x = tx.query().insert(insertPipelineAutomationQuery).collect(toList());
            }

            tx.commit();

            LOG.info("clientJavaE2E() - done.");
        }, WRITE);

        localhostTypeDBTX(tx -> {
            LOG.info("clientJavaE2E() - inserting pipeline-automation relations...");

            TypeQLInsert insertPipelineWorkflowQuery = TypeQL.match(cVar("pipelineA").isa("pipeline").has("name", "pipeline-A"),
                            cVar("workflowA").isa("workflow").has("name", "workflow-A"), cVar("pipelineB").isa("pipeline").has("name", "pipeline-B"),
                            cVar("workflowB").isa("workflow").has("name", "workflow-B"))
                    .insert(rel("pipeline", cVar("pipelineA")).rel("workflow", cVar("workflowA")).isa("pipeline-workflow"),
                            rel("pipeline", cVar("pipelineB")).rel("workflow", cVar("workflowB")).isa("pipeline-workflow"));
            LOG.info("clientJavaE2E() - '" + insertPipelineWorkflowQuery + "'");
            List<ConceptMap> x = tx.query().insert(insertPipelineWorkflowQuery).collect(toList());

            tx.commit();

            LOG.info("clientJavaE2E() - done.");
        }, WRITE);

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

        localhostTypeDBTX(tx -> {
            LOG.info("clientJavaE2E() - inserting pipeline-automation relations...");

            Stream.of(queries).parallel().forEach(x -> {
                TypeQLMatch q = TypeQL.parseQuery(x).asMatch();
                List<ConceptMap> res = tx.query().match(q).collect(toList());
            });

            LOG.info("clientJavaE2E() - done.");
        }, READ);
    }

    @Test
    public void testSimpleExplanation() {
        localhostTypeDBTX(tx -> {
            TypeQLDefine schema = TypeQL.parseQuery("define " +
                    "person sub entity, owns name, plays friendship:friend, plays marriage:husband, plays marriage:wife;" +
                    "name sub attribute, value string;" +
                    "friendship sub relation, relates friend;" +
                    "marriage sub relation, relates husband, relates wife;" +
                    "rule marriage-is-friendship: when {" +
                    "   $x isa person; $y isa person; (husband: $x, wife: $y) isa marriage; " +
                    "} then {" +
                    "   (friend: $x, friend: $y) isa friendship;" +
                    "};" +
                    "rule everyone-is-friends: when {" +
                    "   $x isa person; $y isa person; not { $x is $y; };" +
                    "} then {" +
                    "   (friend: $x, friend: $y) isa friendship;" +
                    "};").asDefine();
            tx.query().define(schema);
            tx.commit();
        }, TypeDBSession.Type.SCHEMA);


        localhostTypeDBTX(tx -> {
            TypeQLInsert data = TypeQL.parseQuery(
                    "insert " +
                            "$x isa person, has name 'Zack'; " +
                            "$y isa person, has name 'Yasmin'; " +
                            "(husband: $x, wife: $y) isa marriage;"
            ).asInsert();
            tx.query().insert(data);
            tx.commit();
        }, WRITE);

        localhostTypeDBTX(tx -> {
            TypeQLInsert data = TypeQL.parseQuery(
                    "insert " +
                            "$x isa person, has name 'Zack'; " +
                            "$y isa person, has name 'Yasmin'; " +
                            "(husband: $x, wife: $y) isa marriage;"
            ).asInsert();
            List<ConceptMap> answers = tx.query().match(TypeQL.parseQuery("match (friend: $p1, friend: $p2) isa friendship; $p1 has name $na;").asMatch()).collect(toList());

            assertEquals(1, answers.get(0).explainables().relations().count());
            assertEquals(1, answers.get(1).explainables().relations().count());
            List<Explanation> explanations = tx.query().explain(answers.get(0).explainables().relations().map(Pair::second).iterator().next()).collect(Collectors.toList());
            assertEquals(3, explanations.size());
            List<Explanation> explanations2 = tx.query().explain(answers.get(1).explainables().relations().map(Pair::second).iterator().next()).collect(Collectors.toList());
            assertEquals(3, explanations2.size());
        }, READ, new TypeDBOptions().infer(true).explain(true));
    }

    @Test
    public void testStreaming() {
        localhostTypeDBTX(tx -> {
            for (int i = 0; i < 51; i++) {
                tx.query().define(String.format("define person sub entity, owns name%d; name%d sub attribute, value string;", i, i));
            }
            tx.commit();
        }, TypeDBSession.Type.SCHEMA);
        localhostTypeDBTX(tx -> {
            for (int i = 0; i < 50; i++) {
                EntityType concept = tx.concepts().getEntityType("person");
                List<? extends AttributeType> attributeTypes = concept.getOwns(tx).collect(toList());
                Optional<ConceptMap> conceptMap = tx.query().match("match $x sub thing; limit 1;").findFirst();
            }
        }, READ, new TypeDBOptions().prefetch(true).prefetchSize(50));
    }

    private String[] lionNames() {
        return new String[]{"male-partner", "female-partner", "young-lion"};
    }

    private void localhostTypeDBTX(Consumer<TypeDBTransaction> fn, TypeDBTransaction.Type type) {
        localhostTypeDBTX(fn, type, new TypeDBOptions());
    }

    private void localhostTypeDBTX(Consumer<TypeDBTransaction> fn, TypeDBTransaction.Type type, TypeDBOptions options) {
        String database = "typedb";
        try (TypeDBSession session = typedbClient.session(database, DATA)) {
            try (TypeDBTransaction transaction = session.transaction(type, options)) {
                fn.accept(transaction);
            }
        }
    }

    private void localhostTypeDBTX(Consumer<TypeDBTransaction> fn, TypeDBSession.Type sessionType) {
        String database = "typedb";
        try (TypeDBSession session = typedbClient.session(database, sessionType)) {
            try (TypeDBTransaction transaction = session.transaction(WRITE)) {
                fn.accept(transaction);
            }
        }
    }
}
