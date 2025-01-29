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

import com.typedb.driver.api.DriverOptions;
import com.typedb.driver.api.Credentials;
import com.typedb.driver.api.Driver;
import com.typedb.driver.connection.DriverImpl;

import java.util.Map;
import java.util.Set;

import static com.typedb.driver.common.collection.Collections.set;
import com.typedb.driver.common.exception.TypeDBDriverException;

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
     * @param driverOptions The connection settings to connect with
     */
    public static Driver coreDriver(String address, Credentials credentials, DriverOptions driverOptions) throws TypeDBDriverException {
        return new DriverImpl(address, credentials, driverOptions);
    }

    /**
     * Open a TypeDB Driver to a TypeDB Cluster server available at the provided address, using
     * the provided credential.
     *
     * <h3>Examples</h3>
     * <pre>
     * TypeDB.clusterDriver(address, credential);
     * </pre>
     *
     * @param address The address of the TypeDB server
     * @param credentials The credential to connect with
     * @param driverOptions The connection settings to connect with
     */
    public static Driver clusterDriver(String address, Credentials credentials, DriverOptions driverOptions) throws TypeDBDriverException {
        return clusterDriver(set(address), credentials, driverOptions);
    }

    /**
     * Open a TypeDB Driver to TypeDB Cluster server(s) available at the provided addresses, using
     * the provided credential.
     *
     * <h3>Examples</h3>
     * <pre>
     * TypeDB.clusterDriver(addresses, credential);
     * </pre>
     *
     * @param addresses The address(es) of the TypeDB server(s)
     * @param credentials The credential to connect with
     * @param driverOptions The connection settings to connect with
     */
    public static Driver clusterDriver(Set<String> addresses, Credentials credentials, DriverOptions driverOptions) throws TypeDBDriverException {
        return new DriverImpl(addresses, credentials, driverOptions);
    }

    /**
     * Open a TypeDB Driver to TypeDB Cluster server(s), using provided address translation, with
     * the provided credential.
     *
     * <h3>Examples</h3>
     * <pre>
     * TypeDB.clusterDriver(addressTranslation, credential);
     * </pre>
     *
     * @param addressTranslation Translation map from addresses to be used by the driver for connection
     * to addresses received from the TypeDB server(s)
     * @param credentials The credential to connect with
     * @param driverOptions The connection settings to connect with
     */
    public static Driver clusterDriver(Map<String, String> addressTranslation, Credentials credentials, DriverOptions driverOptions) throws TypeDBDriverException {
        return new DriverImpl(addressTranslation, credentials, driverOptions);
    }
}
