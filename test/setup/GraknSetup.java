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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.fail;

public class GraknSetup {

    public enum GraknType {
        GRAKN_CORE("grakn-core"),
        GRAKN_KGMS("grakn-kgms");

        private final String name;

        GraknType(String name) {
            this.name = name;
        }

        public static GraknType of(String name) {
            for (GraknType t : GraknType.values()) {
                if (t.name.equals(name)) {
                    return t;
                }
            }
            throw new IllegalArgumentException("Unrecognised Grakn Type: " + name);
        }
    }
    public static void bootup(GraknType type, File distributionFile) throws InterruptedException, TimeoutException, IOException {
        if (type.equals(GraknType.GRAKN_CORE)) {
            GraknSetupCore.bootup(distributionFile);
        } else if (type.equals(GraknType.GRAKN_KGMS)) {
            GraknSetupKGMS.setEnvironmentProperties();
        } else {
            fail("Invalid Grakn Type argument provided: " + type);
        }
    }

    public static void shutdown(GraknType graknType) throws InterruptedException, IOException, TimeoutException {
        if (graknType.equals(GraknType.GRAKN_CORE)) {
            GraknSetupCore.shutdown();
        } else {
            // TODO implement this for Grakn KGMS
            throw new UnsupportedOperationException("GraknSetup.shutdown() does not yet support Grakn KGMS");
        }
    }
}
