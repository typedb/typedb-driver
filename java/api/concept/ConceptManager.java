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

package com.vaticle.typedb.client.api.concept;

import com.vaticle.typedb.client.api.concept.thing.Attribute;
import com.vaticle.typedb.client.api.concept.thing.Entity;
import com.vaticle.typedb.client.api.concept.thing.Relation;
import com.vaticle.typedb.client.api.concept.type.AttributeType;
import com.vaticle.typedb.client.api.concept.type.EntityType;
import com.vaticle.typedb.client.api.concept.type.RelationType;
import com.vaticle.typedb.client.api.concept.value.Value;
import com.vaticle.typedb.client.common.exception.TypeDBException;

import java.util.List;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

public interface ConceptManager {
    @CheckReturnValue
    EntityType getRootEntityType();

    @CheckReturnValue
    RelationType getRootRelationType();

    @CheckReturnValue
    AttributeType getRootAttributeType();

    @Nullable
    @CheckReturnValue
    EntityType getEntityType(String label);

    @Nullable
    @CheckReturnValue
    RelationType getRelationType(String label);

    @Nullable
    @CheckReturnValue
    AttributeType getAttributeType(String label);

    EntityType putEntityType(String label);

    RelationType putRelationType(String label);

    AttributeType putAttributeType(String label, Value.Type valueType);

    @Nullable
    @CheckReturnValue
    Entity getEntity(String iid);

    @Nullable
    @CheckReturnValue
    Relation getRelation(String iid);

    @Nullable
    @CheckReturnValue
    Attribute getAttribute(String iid);

    @CheckReturnValue
    List<TypeDBException> getSchemaExceptions();
}
