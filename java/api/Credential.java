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

package com.typedb.driver.api;

import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.exception.TypeDBDriverException;

import static com.typedb.driver.jni.typedb_driver.credential_new;

/**
 * User credentials for connecting to TypeDB Server.
 *
 * <h3>Examples</h3>
 * <pre>
 * Credential credential = new Credential(username, password);
 * </pre>
 */
public class Credential extends NativeObject<com.typedb.driver.jni.Credential> {
    /**
     * @param username The name of the user to connect as
     * @param password The password for the user
     */
    public Credential(String username, String password) {
        super(newNative(username, password));
    }

    private static com.typedb.driver.jni.Credential newNative(String username, String password) {
        try {
            return credential_new(username, password);
        } catch (com.typedb.driver.jni.Error error) {
            throw new TypeDBDriverException(error);
        }
    }
}
