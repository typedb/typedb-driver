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

import static org.junit.Assert.assertNotNull;

public class GraknSetupKGMS {

    public static void setEnvironmentProperties() {
        String address = System.getenv("GRAKN_KGMS_ADDRESS");
        String username = System.getenv("GRAKN_KGMS_USERNAME");
        String password = System.getenv("GRAKN_KGMS_PASSWORD");

        assertNotNull(address);
        assertNotNull(username);
        assertNotNull(password);

        System.setProperty(GraknProperties.GRAKN_TYPE, GraknProperties.GRAKN_KGMS);
        System.setProperty(GraknProperties.GRAKN_ADDRESS, address);
        System.setProperty(GraknProperties.GRAKN_USERNAME, username);
        System.setProperty(GraknProperties.GRAKN_PASSWORD, password);
    }
}
