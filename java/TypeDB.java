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

public class TypeDB {
    public static final String DEFAULT_ADDRESS = "localhost:1730";

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
     * @param driverOptions The connection settings to connect with
     */
    public static Driver driver(String address, Credentials credentials, DriverOptions driverOptions) throws TypeDBDriverException {
        return new DriverImpl(address, credentials, driverOptions);
    }
}
