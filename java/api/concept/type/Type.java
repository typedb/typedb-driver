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

package com.vaticle.typedb.client.api.concept.type;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.Concept;
import com.vaticle.typedb.client.common.Label;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface Type extends Concept {
    @CheckReturnValue
    Label getLabel();

    @CheckReturnValue
    boolean isRoot();

    @CheckReturnValue
    boolean isAbstract();

    @Override
    @CheckReturnValue
    default boolean isType() {
        return true;
    }

    @Override
    @CheckReturnValue
    default Type asType() {
        return this;
    }

    @Override
    default JsonObject toJSON() {
        return Json.object().add("label", getLabel().scopedName());
    }

    void setLabel(TypeDBTransaction transaction, String label);

    @Nullable
    @CheckReturnValue
    Type getSupertype(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends Type> getSupertypes(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends Type> getSubtypes(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends Type> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity);

    void delete(TypeDBTransaction transaction);

    @CheckReturnValue
    boolean isDeleted(TypeDBTransaction transaction);
}
