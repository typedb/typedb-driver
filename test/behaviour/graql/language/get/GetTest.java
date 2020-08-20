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

package grakn.client.test.behaviour.graql.language.get;

import grakn.common.test.server.GraknCoreRunner;
import grakn.common.test.server.GraknSingleton;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Cucumber.class)
@CucumberOptions(
        strict = true,
        plugin = "pretty",
        glue = "grakn.client.test.behaviour",
        features = "external/graknlabs_verification/behaviour/graql/language/get.feature",
        tags = "not @ignore and not @ignore-client-java"
)
public class GetTest {
    // ATTENTION:
    // When you click RUN from within this class through Intellij IDE, it will fail.
    // You can fix it by doing:
    //
    // 1) Go to 'Run'
    // 2) Select 'Edit Configurations...'
    // 3) Select 'Bazel test GetTest'
    //
    // 4) Ensure 'Target Expression' is set correctly:
    //    a) Use '//<this>/<package>/<name>:test-core' to test against grakn-core
    //    b) Use '//<this>/<package>/<name>:test-kgms' to test against grakn-kgms
    //
    // 5) Update 'Bazel Flags':
    //    a) Remove the line that says: '--test_filter=grakn.client.*'
    //    b) Use the following Bazel flags:
    //       --cache_test_results=no : to make sure you're not using cache
    //       --test_output=streamed : to make sure all output is printed
    //       --subcommands : to print the low-level commands and execution paths
    //       --sandbox_debug : to keep the sandbox not deleted after test runs
    //       --spawn_strategy=standalone : if you're on Mac, tests need permission to access filesystem (to run Grakn)
    //
    // 6) Hit the RUN button by selecting the test from the dropdown menu on the top bar

    private static GraknCoreRunner runner;

    @BeforeClass
    public static void beforeClass() throws InterruptedException, IOException, TimeoutException {
        runner = new GraknCoreRunner();
        runner.start();
        GraknSingleton.setGraknRunner(runner);
    }
    @AfterClass
    public static void afterClass() throws InterruptedException, IOException, TimeoutException {
        runner.stop();
    }
}
