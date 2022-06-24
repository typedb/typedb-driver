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

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Optional;

public class TypeDBCredential {

    private final String username;
    private final String password;
    private final boolean tlsEnabled;
    @Nullable
    private final Path tlsRootCA;

    public TypeDBCredential(String username, String password, boolean tlsEnabled) {
        this(username, password, tlsEnabled, null);
    }

    public TypeDBCredential(String username, String password, Path tlsRootCA) {
        this(username, password, true, tlsRootCA);
    }

    private TypeDBCredential(String username, String password, boolean tlsEnabled, @Nullable Path tlsRootCA) {
        this.username = username;
        this.password = password;
        this.tlsEnabled = tlsEnabled;
        this.tlsRootCA = tlsRootCA;
        assert tlsEnabled || tlsRootCA == null;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public boolean tlsEnabled() {
        return tlsEnabled;
    }

    public Optional<Path> tlsRootCA() {
        return Optional.ofNullable(tlsRootCA);
    }
}
