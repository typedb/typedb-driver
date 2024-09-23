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

package com.vaticle.typedb.driver.concept;

import com.vaticle.typedb.driver.api.concept.Concept;
import com.vaticle.typedb.driver.common.NativeObject;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;
import com.vaticle.typedb.driver.concept.thing.AttributeImpl;
import com.vaticle.typedb.driver.concept.thing.EntityImpl;
import com.vaticle.typedb.driver.concept.thing.RelationImpl;
import com.vaticle.typedb.driver.concept.type.AttributeTypeImpl;
import com.vaticle.typedb.driver.concept.type.EntityTypeImpl;
import com.vaticle.typedb.driver.concept.type.RelationTypeImpl;
import com.vaticle.typedb.driver.concept.type.RoleTypeImpl;
import com.vaticle.typedb.driver.concept.value.ValueImpl;

import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_equals;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_is_attribute;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_is_attribute_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_is_entity;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_is_entity_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_is_relation;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_is_relation_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_is_role_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_is_value;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_to_string;

public abstract class ConceptImpl extends NativeObject<com.vaticle.typedb.driver.jni.Concept> implements Concept {
    protected ConceptImpl(com.vaticle.typedb.driver.jni.Concept concept) {
        super(concept);
    }

    public static ConceptImpl of(com.vaticle.typedb.driver.jni.Concept concept) {
        if (concept_is_entity_type(concept)) return new EntityTypeImpl(concept);
        else if (concept_is_relation_type(concept)) return new RelationTypeImpl(concept);
        else if (concept_is_attribute_type(concept)) return new AttributeTypeImpl(concept);
        else if (concept_is_entity(concept)) return new EntityImpl(concept);
        else if (concept_is_relation(concept)) return new RelationImpl(concept);
        else if (concept_is_attribute(concept)) return new AttributeImpl(concept);
        else if (concept_is_value(concept)) return new ValueImpl(concept);
        else if (concept_is_role_type(concept)) return new RoleTypeImpl(concept);
        throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
    }

    @Override
    public String toString() {
        return concept_to_string(nativeObject);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConceptImpl that = (ConceptImpl) obj;
        return concept_equals(this.nativeObject, that.nativeObject);
    }

    @Override
    public abstract int hashCode();
}
