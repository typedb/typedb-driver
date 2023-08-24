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

import com.eclipsesource.json.JsonObject;
import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.value.Value;
import com.vaticle.typedb.client.api.concept.type.AttributeType;
import com.vaticle.typedb.client.api.concept.type.ThingType;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

public interface Attribute extends Thing {
    @Override
    @CheckReturnValue
    AttributeType getType();

    @Override
    @CheckReturnValue
    default boolean isAttribute() {
        return true;
    }

    @Override
    @CheckReturnValue
    default Attribute asAttribute() {
        return this;
    }

    Value getValue();

    @Override
    default JsonObject toJSON() {
        return getValue().toJSON().add("type", getType().getLabel().scopedName());
    }

    @CheckReturnValue
    Stream<? extends Thing> getOwners(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends Thing> getOwners(TypeDBTransaction transaction, ThingType ownerType);
}
