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

package com.vaticle.typedb.client.api;

import com.vaticle.typedb.client.common.NativeObject;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Optional;

import static com.vaticle.typedb.client.jni.typedb_client_jni.credential_new;

public class TypeDBCredential extends NativeObject {
    public final com.vaticle.typedb.client.jni.Credential credential;

    public TypeDBCredential(String username, String password, boolean tlsEnabled) {
        this(username, password, tlsEnabled, null);
    }

    public TypeDBCredential(String username, String password, Path tlsRootCA) {
        this(username, password, true, tlsRootCA.toString());
    }

    private TypeDBCredential(String username, String password, boolean tlsEnabled, @Nullable String tlsRootCA) {
        assert tlsEnabled || tlsRootCA == null;
        this.credential = credential_new(username, password, tlsRootCA, tlsEnabled);
    }
}
