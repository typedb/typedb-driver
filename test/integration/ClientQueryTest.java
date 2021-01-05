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

import grakn.client.Grakn.Client;
import grakn.client.Grakn.Session;
import grakn.client.Grakn.Transaction;
import grakn.client.GraknClient;
import grakn.common.test.server.GraknCoreRunner;
import graql.lang.Graql;
import graql.lang.common.GraqlArg;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlInsert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static grakn.client.Grakn.Transaction.Type.WRITE;
import static graql.lang.Graql.type;
import static graql.lang.Graql.var;

@SuppressWarnings("Duplicates")
public class ClientQueryTest {
    private static final Logger LOG = LoggerFactory.getLogger(ClientQueryTest.class);
    private static GraknCoreRunner grakn;
    private static Client graknClient;

    @BeforeClass
    public static void setUpClass() throws InterruptedException, IOException, TimeoutException {
        grakn = new GraknCoreRunner();
        grakn.start();
        graknClient = new GraknClient(grakn.address());
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
            final GraqlDefine defineQuery = Graql.define(
                    type("child-bearing").sub("relation").relates("offspring").relates("child-bearer"),
                    type("mating").sub("relation").relates("male-partner").relates("female-partner").plays("child-bearing", "child-bearer"),
                    type("parentship").sub("relation").relates("parent").relates("child"),

                    type("name").sub("attribute").value(GraqlArg.ValueType.STRING),
                    type("lion").sub("entity").owns("name").plays("mating", "male-partner").plays("mating", "female-partner").plays("child-bearing", "offspring").plays("parentship", "parent").plays("parentship", "child")
            );

            // TODO: re-enable when defining rules is supported
//            final GraqlDefine ruleQuery = Graql.define(type("infer-parentship-from-mating-and-child-bearing").sub("rule")
//                     .when(and(
//                             rel("male-partner", var("male")).rel("female-partner", var("female")).isa("mating"),
//                             var("childbearing").rel("child-bearer").rel("offspring", var("offspring")).isa("child-bearing")
//                     ))
//                     .then(and(
//                             rel("parent", var("male")).rel("parent", var("female")).rel("child", var("offspring")).isa("parentship")
//                     )));
            LOG.info("clientJavaE2E() - define a schema...");
            LOG.info("clientJavaE2E() - '" + defineQuery + "'");
            tx.query().define(defineQuery);
//            tx.query().define(ruleQuery);
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
            final String[] names = lionNames();
            final GraqlInsert insertLionQuery = Graql.insert(
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
