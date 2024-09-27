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

package com.typedb.driver.concept.thing;

import com.typedb.driver.api.concept.thing.Entity;
import com.typedb.driver.concept.type.EntityTypeImpl;

import static com.typedb.driver.jni.typedb_driver.entity_get_iid;
import static com.typedb.driver.jni.typedb_driver.entity_get_type;

public class EntityImpl extends ThingImpl implements Entity {
    public EntityImpl(com.typedb.driver.jni.Concept concept) {
        super(concept);
    }

    @Override
    public EntityTypeImpl getType() {
        return new EntityTypeImpl(entity_get_type(nativeObject));
    }

    @Override
    public final EntityImpl asEntity() {
        return this;
    }


    @Override
    public final String getIID() {
        return entity_get_iid(nativeObject);
    }

    @Override
    public int hashCode() {
        if (hash == 0) hash = getIID().hashCode();
        return hash;
    }
}
