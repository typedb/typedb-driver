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

package grakn.client.test.behaviour;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(strict = true, plugin = "pretty", features = "external/graknlabs_behaviour/session")
public class TestConnectionKGMS {
    // ATTENTION: When you click RUN from within this class through Intellij IDE,
    // it will fail, and you can fix it by doing:
    // Go to 'Run'
    // Select 'Edit Configurations...'
    // Select 'Bazel test SessionTest'
    // Remove the line that says: '--test_filter=grakn.client.test.session.SessionTest#'
    // Hit the RUN button by selecting the test from the dropdown menu on the top bar

    @BeforeClass
    public static void setGraknKGMSProperties() {
        String address = System.getenv("GRAKN_KGMS_ADDRESS");
        String username = System.getenv("GRAKN_KGMS_USERNAME");
        String password = System.getenv("GRAKN_KGMS_PASSWORD");

        System.setProperty("grakn.client.test.behaviour.grakn", "kgms");
        System.setProperty("grakn.client.test.behaviour.address", address);
        System.setProperty("grakn.client.test.behaviour.grakn", password);
    }
}