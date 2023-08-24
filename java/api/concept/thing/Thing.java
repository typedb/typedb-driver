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

package com.vaticle.typedb.client.api.concept.thing;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.Concept;
import com.vaticle.typedb.client.api.concept.type.AttributeType;
import com.vaticle.typedb.client.api.concept.type.RoleType;
import com.vaticle.typedb.client.api.concept.type.ThingType;
import com.vaticle.typedb.client.api.concept.type.ThingType.Annotation;

import javax.annotation.CheckReturnValue;
import java.util.Set;
import java.util.stream.Stream;

public interface Thing extends Concept {
    @CheckReturnValue
    String getIID();

    @CheckReturnValue
    ThingType getType();

    @CheckReturnValue
    boolean isInferred();

    @Override
    @CheckReturnValue
    default boolean isThing() {
        return true;
    }

    @Override
    @CheckReturnValue
    default Thing asThing() {
        return this;
    }

    @Override
    default JsonObject toJSON() {
        return Json.object().add("type", getType().getLabel().scopedName());
    }

    @CheckReturnValue
    Stream<? extends Attribute> getHas(TypeDBTransaction transaction, Set<Annotation> annotations);

    void setHas(TypeDBTransaction transaction, Attribute attribute);

    void unsetHas(TypeDBTransaction transaction, Attribute attribute);

    @CheckReturnValue
    Stream<? extends Attribute> getHas(TypeDBTransaction transaction, AttributeType... attributeTypes);

    @CheckReturnValue
    Stream<? extends Relation> getRelations(TypeDBTransaction transaction, RoleType... roleTypes);

    @CheckReturnValue
    Stream<? extends RoleType> getPlaying(TypeDBTransaction transaction);

    void delete(TypeDBTransaction transaction);

    @CheckReturnValue
    boolean isDeleted(TypeDBTransaction transaction);
}
