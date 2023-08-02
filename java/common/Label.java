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

package com.vaticle.typedb.client.common;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class Label {
    private final String scope;
    private final String name;
    private final int hash;

    private Label(@Nullable String scope, String name) {
        this.scope = scope;
        this.name = name;
        this.hash = Objects.hash(name, scope);
    }

    public static Label of(String name) {
        return new Label(null, name);
    }

    public static Label of(String scope, String name) {
        return new Label(scope, name);
    }

    public Optional<String> scope() {
        return Optional.ofNullable(scope);
    }

    public String name() {
        return name;
    }

    public String scopedName() {
        if (scope == null) return name;
        else return scope + ":" + name;
    }

    @Override
    public String toString() {
        return scopedName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Label that = (Label) o;
        return this.name.equals(that.name) && Objects.equals(this.scope, that.scope);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
