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

package grakn.core.test.behaviour;

import grakn.common.test.server.GraknCoreRunner;
import grakn.common.test.server.GraknSingleton;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class DebugBehaviourTest {
    // The following code is for running the Grakn Core distribution imported as an artifact.
    // If you wish to debug locally against an instance of Grakn that is already running in
    // the background, comment out all the code in this file that references 'runner'
    // and update ConnectionSteps to connect to GraknClient.DEFAULT_URI.

    private static GraknCoreRunner runner;

    @BeforeClass
    public static void setupBehaviourTests() throws InterruptedException, IOException, TimeoutException {
        runner = new GraknCoreRunner(true);
        runner.start();
        GraknSingleton.setGraknRunner(runner);
    }

    @AfterClass
    public static void tearDownBehaviourTests() throws InterruptedException, IOException, TimeoutException {
        runner.stop();
    }
}
