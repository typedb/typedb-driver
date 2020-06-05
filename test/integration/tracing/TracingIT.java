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

package grakn.client.test.integration.tracing;

import grabl.tracing.client.GrablTracingThreadStatic;
import grabl.tracing.client.GrablTracingThreadStatic.ThreadContext;
import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import grakn.client.test.setup.GraknProperties;
import grakn.client.test.setup.GraknSetup;
import graql.lang.Graql;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static grabl.tracing.client.GrablTracing.tracingNoOp;
import static grabl.tracing.client.GrablTracingThreadStatic.contextOnThread;
import static grabl.tracing.client.GrablTracingThreadStatic.openGlobalAnalysis;
import static grabl.tracing.client.GrablTracingThreadStatic.setGlobalTracingClient;
import static org.junit.Assert.assertEquals;

public class TracingIT {
    private static final String[] args = System.getProperty("sun.java.command").split(" ");
    private static final GraknSetup.GraknType graknType = GraknSetup.GraknType.of(args[1]);
    private static final File graknDistributionFile = new File(args[2]);
    private static GraknClient client;

    @BeforeClass
    public static void setUpClass() throws InterruptedException, IOException, TimeoutException {
        setGlobalTracingClient(tracingNoOp());
        openGlobalAnalysis("owner", "repo", "commit");
        GraknSetup.bootup(graknType, graknDistributionFile);
        String address = System.getProperty(GraknProperties.GRAKN_ADDRESS);
        client = new GraknClient(address);
    }

    @AfterClass
    public static void closeSession() throws Exception {
        client.close();
        GraknSetup.shutdown(graknType);
        GrablTracingThreadStatic.getGrablTracing().close();
    }

    @Test
    public void testWithTracing() {
        try (ThreadContext ignored = contextOnThread("tracker", 1)) {
            try (GraknClient.Session session = client.session("test_tracing")) {
                try (GraknClient.Transaction tx = session.transaction().write()) {
                    tx.execute(Graql.parse("define\n" +
                            "name sub attribute, value string;\n" +
                            "person sub entity, has name;").asDefine());

                    tx.execute(Graql.parse("insert $x isa person, has name \"bob\";").asInsert());

                    Stream<ConceptMap> answers = tx.stream(Graql.parse("match $x isa person, has name \"bob\"; get $x;").asGet()).get();

                    assertEquals(1, answers.count());
                }
            }
        }
    }
}
