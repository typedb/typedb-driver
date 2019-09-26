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

package grakn.client.test.behaviour.connection;

import grakn.client.test.setup.GraknSetup;
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
        features = "external/graknlabs_behaviour/connection/session.feature"
)
public class TestSession {
    // ATTENTION: When you click RUN from within this class through Intellij IDE,
    // it will fail, and you can fix it by doing:
    // Go to 'Run'
    // Select 'Edit Configurations...'
    // Select 'Bazel test TestConnectionCore'
    // Remove the line that says: '--test_filter=grakn.client.test.behaviour.TestConnectionCore#'
    //
    // Add the following Bazel flags:
    // --cache_test_results=no
    // --test_output=streamed
    // --subcommands
    // --sandbox_debug
    // --spawn_strategy=standalone
    //
    // Hit the RUN button by selecting the test from the dropdown menu on the top bar

    @BeforeClass
    public static void beforeClass() throws InterruptedException, TimeoutException, IOException {
        GraknSetup.bootup();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException, IOException, TimeoutException {
        GraknSetup.shutdown();
    }
}