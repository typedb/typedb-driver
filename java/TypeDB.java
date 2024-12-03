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

package com.typedb.driver;

import com.typedb.driver.api.ConnectionSettings;
import com.typedb.driver.api.Credentials;
import com.typedb.driver.api.Driver;
import com.typedb.driver.connection.DriverImpl;

import java.util.Map;
import java.util.Set;

import static com.typedb.driver.common.collection.Collections.set;

public class TypeDB {
    public static final String DEFAULT_ADDRESS = "localhost:1729";

    /**
     * Open a TypeDB Driver to a TypeDB Core server available at the provided address.
     *
     * <h3>Examples</h3>
     * <pre>
     * TypeDB.coreDriver(address);
     * </pre>
     *
     * @param address The address of the TypeDB server
     * @param credentials The credentials to connect with
     * @param connectionSettings The connection settings to connect with
     */
    public static Driver coreDriver(String address, Credentials credentials, ConnectionSettings connectionSettings) {
        return new DriverImpl(address, credentials, connectionSettings);
    }

    /**
     * Open a TypeDB Driver to a TypeDB Cloud server available at the provided address, using
     * the provided credential.
     *
     * <h3>Examples</h3>
     * <pre>
     * TypeDB.cloudDriver(address, credential);
     * </pre>
     *
     * @param address The address of the TypeDB server
     * @param credentials The credential to connect with
     * @param connectionSettings The connection settings to connect with
     */
    public static Driver cloudDriver(String address, Credentials credentials, ConnectionSettings connectionSettings) {
        return cloudDriver(set(address), credentials, connectionSettings);
    }

    /**
     * Open a TypeDB Driver to TypeDB Cloud server(s) available at the provided addresses, using
     * the provided credential.
     *
     * <h3>Examples</h3>
     * <pre>
     * TypeDB.cloudDriver(addresses, credential);
     * </pre>
     *
     * @param addresses The address(es) of the TypeDB server(s)
     * @param credentials The credential to connect with
     * @param connectionSettings The connection settings to connect with
     */
    public static Driver cloudDriver(Set<String> addresses, Credentials credentials, ConnectionSettings connectionSettings) {
        return new DriverImpl(addresses, credentials, connectionSettings);
    }

    /**
     * Open a TypeDB Driver to TypeDB Cloud server(s), using provided address translation, with
     * the provided credential.
     *
     * <h3>Examples</h3>
     * <pre>
     * TypeDB.cloudDriver(addressTranslation, credential);
     * </pre>
     *
     * @param addressTranslation Translation map from addresses to be used by the driver for connection
     * to addresses received from the TypeDB server(s)
     * @param credentials The credential to connect with
     * @param connectionSettings The connection settings to connect with
     */
    public static Driver cloudDriver(Map<String, String> addressTranslation, Credentials credentials, ConnectionSettings connectionSettings) {
        return new DriverImpl(addressTranslation, credentials, connectionSettings);
    }
}
