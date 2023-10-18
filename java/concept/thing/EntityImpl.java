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

package com.vaticle.typedb.driver.concept.thing;

import com.vaticle.typedb.driver.api.concept.thing.Entity;
import com.vaticle.typedb.driver.common.Promise;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;
import com.vaticle.typedb.driver.concept.type.EntityTypeImpl;
import com.vaticle.typedb.driver.jni.ConceptPromise;

import javax.annotation.CheckReturnValue;

import static com.vaticle.typedb.driver.jni.typedb_driver.entity_get_type;

public class EntityImpl extends ThingImpl implements Entity {
    public EntityImpl(com.vaticle.typedb.driver.jni.Concept concept) {
        super(concept);
    }

    @CheckReturnValue
    public static Promise<EntityImpl> promise(ConceptPromise promise) {
        return new Promise<>(() -> {
            try {
                com.vaticle.typedb.driver.jni.Concept res = promise.get();
                if (res != null) return new EntityImpl(res);
                else return null;
            } catch (com.vaticle.typedb.driver.jni.Error e) {
                throw new TypeDBDriverException(e);
            }
        });
    }

    @Override
    public EntityTypeImpl getType() {
        return new EntityTypeImpl(entity_get_type(nativeObject));
    }

    @Override
    public final EntityImpl asEntity() {
        return this;
    }
}
