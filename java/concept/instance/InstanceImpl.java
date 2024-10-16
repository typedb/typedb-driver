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

package com.typedb.driver.concept.instance;

import com.typedb.driver.api.concept.instance.Instance;
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.concept.ConceptImpl;
import com.typedb.driver.concept.type.TypeImpl;

import static com.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.typedb.driver.jni.typedb_driver.concept_is_attribute;
import static com.typedb.driver.jni.typedb_driver.concept_is_entity;
import static com.typedb.driver.jni.typedb_driver.concept_is_relation;

public abstract class InstanceImpl extends ConceptImpl implements Instance {
    protected int hash = 0;

    InstanceImpl(com.typedb.driver.jni.Concept concept) {
        super(concept);
    }

    public static InstanceImpl of(com.typedb.driver.jni.Concept concept) {
        if (concept_is_entity(concept)) return new EntityImpl(concept);
        else if (concept_is_relation(concept)) return new RelationImpl(concept);
        else if (concept_is_attribute(concept)) return new AttributeImpl(concept);
        throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
    }

    @Override
    public abstract TypeImpl getType();

    @Override
    public InstanceImpl asInstance() {
        return this;
    }
}
