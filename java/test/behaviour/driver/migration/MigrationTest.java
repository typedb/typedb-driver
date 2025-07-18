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

package com.typedb.driver.test.behaviour.driver.migration;

import com.typedb.driver.test.behaviour.BehaviourTest;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        strict = true,
        plugin = "pretty",
        glue = "com.typedb.driver.test.behaviour",
        features = "external/typedb_behaviour/driver/migration.feature",
        tags = "not @ignore and not @ignore-typedb-driver and not @ignore-typedb-driver-java"
)
public class MigrationTest extends BehaviourTest {
    // ATTENTION:
    // When you click RUN from within this class through Intellij IDE, it will fail.
    // You can fix it by doing:
    //
    // 1) Go to 'Run'
    // 2) Select 'Edit Configurations...'
    // 3) Select 'Bazel test DefineTest'
    //
    // 4) Ensure 'Target Expression' is set correctly:
    //    a) Use '//<this>/<package>/<name>:test-community' to test against typedb (TypeDB Community Edition)
    //    b) Use '//<this>/<package>/<name>:test-cluster' to test against typedb-cluster (TypeDB Cloud / Enterprise)
    //
    // 5) Update 'Bazel Flags':
    //    a) Remove the line that says: '--test_filter=com.typedb.driver.*'
    //    b) Use the following Bazel flags:
    //       --cache_test_results=no : to make sure you're not using cache
    //       --test_output=streamed : to make sure all output is printed
    //       --subcommands : to print the low-level commands and execution paths
    //       --sandbox_debug : to keep the sandbox not deleted after test runs
    //       --spawn_strategy=standalone : if you're on Mac, tests need permission to access filesystem (to run TypeDB)
    //
    // 6) Hit the RUN button by selecting the test from the dropdown menu on the top bar
}
