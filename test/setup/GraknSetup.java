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

package grakn.client.test.setup;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.fail;

public class GraknSetup {
    private static final String[] args = System.getProperty("sun.java.command").split(" ");
    private static final String graknType = args[1];

    public static void bootup() throws InterruptedException, TimeoutException, IOException {
        if (graknType.equals(GraknProperties.GRAKN_CORE)) {
            String distributionFile = args[2];
            GraknSetupCore.bootup(distributionFile);
        } else if (graknType.equals(GraknProperties.GRAKN_KGMS)) {
            GraknSetupKGMS.setEnvironmentProperties();
        } else {
            fail("Invalid Grakn Type argument provided: " + graknType);
        }
    }

    public static void shutdown() throws InterruptedException, IOException, TimeoutException {
        if (graknType.equals(GraknProperties.GRAKN_CORE)) {
            GraknSetupCore.shutdown();
        }
    }
}
