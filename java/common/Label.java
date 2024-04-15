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

package com.vaticle.typedb.driver.common;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * A <code>Label</code> holds the uniquely identifying name of a type.
 * <p>It consists of an optional <code>scope</code>, and a <code>name</code>, represented <code>scope:name</code>.
 * The scope is used only used to distinguish between role-types of the same name declared in different relation types.</p>
 */
public class Label {
    private final String scope;
    private final String name;
    private final int hash;

    private Label(@Nullable String scope, String name) {
        this.scope = scope;
        this.name = name;
        this.hash = Objects.hash(name, scope);
    }

    /**
     * Creates a Label from a specified name.
     *
     * <h3>Examples</h3>
     * <pre>
     * Label.of("entity");
     * </pre>
     *
     * @param name Label name
     */
    public static Label of(String name) {
        return new Label(null, name);
    }

    /**
     * Creates a Label from a specified scope and name.
     *
     * <h3>Examples</h3>
     * <pre>
     * Label.of("relation", "role");
     * </pre>
     *
     * @param scope Label scope
     * @param name Label name
     */
    public static Label of(String scope, String name) {
        return new Label(scope, name);
    }

    /**
     * Returns the scope of this Label.
     *
     * <h3>Examples</h3>
     * <pre>
     * label.scope();
     * </pre>
     */
    public Optional<String> scope() {
        return Optional.ofNullable(scope);
    }

    /**
     * Returns the name of this Label.
     *
     * <h3>Examples</h3>
     * <pre>
     * label.name();
     * </pre>
     */
    public String name() {
        return name;
    }

    /**
     * Returns the string representation of the scoped name.
     *
     * <h3>Examples</h3>
     * <pre>
     * label.scopedName();
     * </pre>
     */
    public String scopedName() {
        if (scope == null) return name;
        else return scope + ":" + name;
    }

    /**
     * Returns the string representation of the scoped name.
     *
     * <h3>Examples</h3>
     * <pre>
     * label.toString();
     * </pre>
     */
    @Override
    public String toString() {
        return scopedName();
    }

    /**
     * Checks if this Label is equal to another object.
     *
     * <h3>Examples</h3>
     * <pre>
     * label.equals(obj);
     * </pre>
     *
     * @param obj Object to compare with
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Label that = (Label) obj;
        return this.name.equals(that.name) && Objects.equals(this.scope, that.scope);
    }

    /**
     * @hidden
     */
    @Override
    public int hashCode() {
        return hash;
    }
}
