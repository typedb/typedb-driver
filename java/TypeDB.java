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

import com.typedb.driver.api.Credentials;
import com.typedb.driver.api.Driver;
import com.typedb.driver.api.DriverOptions;
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.connection.DriverImpl;

import java.util.Map;
import java.util.Set;

public class TypeDB {
    public static final String DEFAULT_ADDRESS = "127.0.0.1:1729";

    /**
     * Open a TypeDB Driver to a TypeDB server available at the provided address.
     *
     * <h3>Examples</h3>
     * <pre>
     * TypeDB.driver(address);
     * </pre>
     *
     * @param address       The address of the TypeDB server
     * @param credentials   The credentials to connect with
     * @param driverOptions The driver connection options to connect with
     */
    public static Driver driver(String address, Credentials credentials, DriverOptions driverOptions) throws TypeDBDriverException {
        return new DriverImpl(address, credentials, driverOptions);
    }

    /**
     * Open a TypeDB Driver to a TypeDB cluster available at the provided addresses.
     *
     * <h3>Examples</h3>
     * <pre>
     * TypeDB.driver(address);
     * </pre>
     *
     * @param addresses     The addresses of TypeDB cluster replicas for connection
     * @param credentials   The credentials to connect with
     * @param driverOptions The driver connection options to connect with
     */
    public static Driver driver(Set<String> addresses, Credentials credentials, DriverOptions driverOptions) throws TypeDBDriverException {
        return new DriverImpl(addresses, credentials, driverOptions);
    }

    /**
     * Open a TypeDB Driver to a TypeDB cluster, using the provided address translation.
     *
     * <h3>Examples</h3>
     * <pre>
     * TypeDB.driver(addresses);
     * </pre>
     *
     * @param addressTranslation The translation of public TypeDB cluster replica addresses (keys) to server-side private addresses (values)
     * @param credentials        The credentials to connect with
     * @param driverOptions      The driver connection options to connect with
     */
    public static Driver driver(Map<String, String> addressTranslation, Credentials credentials, DriverOptions driverOptions) throws TypeDBDriverException {
        return new DriverImpl(addressTranslation, credentials, driverOptions);
    }
}
