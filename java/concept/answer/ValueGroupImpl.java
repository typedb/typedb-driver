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

package com.vaticle.typedb.driver.concept.answer;

import com.vaticle.typedb.driver.api.answer.ValueGroup;
import com.vaticle.typedb.driver.api.concept.Concept;
import com.vaticle.typedb.driver.api.concept.value.Value;
import com.vaticle.typedb.driver.common.NativeObject;
import com.vaticle.typedb.driver.concept.ConceptImpl;
import com.vaticle.typedb.driver.concept.value.ValueImpl;

import java.util.Objects;

import static com.vaticle.typedb.driver.jni.typedb_driver.value_group_equals;
import static com.vaticle.typedb.driver.jni.typedb_driver.value_group_get_value;
import static com.vaticle.typedb.driver.jni.typedb_driver.value_group_get_owner;
import static com.vaticle.typedb.driver.jni.typedb_driver.value_group_to_string;

public class ValueGroupImpl extends NativeObject<com.vaticle.typedb.driver.jni.ValueGroup> implements ValueGroup {
    private int hash = 0;

    public ValueGroupImpl(com.vaticle.typedb.driver.jni.ValueGroup valueGroup) {
        super(valueGroup);
    }

    @Override
    public Concept owner() {
        return ConceptImpl.of(value_group_get_owner(nativeObject));
    }

    @Override
    public Value value() {
        return new ValueImpl(value_group_get_value(nativeObject));
    }

    @Override
    public String toString() {
        return value_group_to_string(nativeObject);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ValueGroupImpl that = (ValueGroupImpl) obj;
        return value_group_equals(this.nativeObject, that.nativeObject);
    }

    @Override
    public int hashCode() {
        if (hash == 0) hash = computeHash();
        return hash;
    }

    private int computeHash() {
        return Objects.hash(owner(), value());
    }
}
