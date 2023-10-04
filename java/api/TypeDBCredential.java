/*
 * Copyright (C) 2022 Vaticle
 *
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

package com.vaticle.typedb.driver.api;

import com.vaticle.typedb.driver.common.NativeObject;

import javax.annotation.Nullable;
import java.nio.file.Path;

import static com.vaticle.typedb.driver.jni.typedb_driver.credential_new;

/**
 * User credentials and TLS encryption settings for connecting to TypeDB enterprise.
 *
 * <h3>Examples</h3>
 * <pre>
 * // Creates a credential as above, but the connection will be made over TLS.
 * TypeDBCredential credential = new TypeDBCredential(username, password, true);
 *
 * // Creates a credential as above, but TLS will use the specified CA to authenticate server certificates.
 * TypeDBCredential credential = new TypeDBCredential(username, password, Path.of("path/to/ca-certificate.pem"));
 * </pre>
 */
public class TypeDBCredential extends NativeObject<com.vaticle.typedb.driver.jni.Credential> {
    /**
     *
     * @param username The name of the user to connect as
     * @param password The password for the user
     * @param tlsEnabled Specify whether the connection to TypeDB Enterprise must be done over TLS
     */
    public TypeDBCredential(String username, String password, boolean tlsEnabled) {
        this(username, password, null, tlsEnabled);
    }

    /**
     *
     * @param username The name of the user to connect as
     * @param password The password for the user
     * @param tlsRootCAPath Path to the CA certificate to use for authenticating server certificates.
     */
    public TypeDBCredential(String username, String password, Path tlsRootCAPath) {
        this(username, password, tlsRootCAPath.toString(), true);
    }

    private TypeDBCredential(String username, String password, @Nullable String tlsRootCAPath, boolean tlsEnabled) {
        super(credential_new(username, password, tlsRootCAPath, tlsEnabled));
        assert tlsEnabled || tlsRootCAPath == null;
    }
}
