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

package com.vaticle.typedb.driver.concept;

import com.vaticle.typedb.driver.api.concept.ConceptManager;
import com.vaticle.typedb.driver.api.concept.value.Value;
import com.vaticle.typedb.driver.common.Promise;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;
import com.vaticle.typedb.driver.common.exception.TypeDBException;
import com.vaticle.typedb.driver.concept.thing.AttributeImpl;
import com.vaticle.typedb.driver.concept.thing.EntityImpl;
import com.vaticle.typedb.driver.concept.thing.RelationImpl;
import com.vaticle.typedb.driver.concept.type.AttributeTypeImpl;
import com.vaticle.typedb.driver.concept.type.EntityTypeImpl;
import com.vaticle.typedb.driver.concept.type.RelationTypeImpl;

import java.util.List;
import java.util.stream.Collectors;

import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Driver.TRANSACTION_CLOSED;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Concept.MISSING_IID;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Concept.MISSING_LABEL;
import static com.vaticle.typedb.driver.jni.typedb_driver.concepts_get_attribute;
import static com.vaticle.typedb.driver.jni.typedb_driver.concepts_get_attribute_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.concepts_get_entity;
import static com.vaticle.typedb.driver.jni.typedb_driver.concepts_get_entity_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.concepts_get_relation;
import static com.vaticle.typedb.driver.jni.typedb_driver.concepts_get_relation_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.concepts_get_root_attribute_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.concepts_get_root_entity_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.concepts_get_root_relation_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.concepts_get_schema_exceptions;
import static com.vaticle.typedb.driver.jni.typedb_driver.concepts_put_attribute_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.concepts_put_entity_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.concepts_put_relation_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.schema_exception_code;
import static com.vaticle.typedb.driver.jni.typedb_driver.schema_exception_message;

public final class ConceptManagerImpl implements ConceptManager {
    final com.vaticle.typedb.driver.jni.Transaction nativeTransaction;

    public ConceptManagerImpl(com.vaticle.typedb.driver.jni.Transaction nativeTransaction) {
        this.nativeTransaction = nativeTransaction;
    }

    @Override
    public EntityTypeImpl getRootEntityType() {
        return new EntityTypeImpl(concepts_get_root_entity_type());
    }

    @Override
    public RelationTypeImpl getRootRelationType() {
        return new RelationTypeImpl(concepts_get_root_relation_type());
    }

    @Override
    public AttributeTypeImpl getRootAttributeType() {
        return new AttributeTypeImpl(concepts_get_root_attribute_type());
    }

    @Override
    public Promise<EntityTypeImpl> getEntityType(String label) {
        if (label == null || label.isEmpty()) throw new TypeDBDriverException(MISSING_LABEL);
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        return EntityTypeImpl.promise(concepts_get_entity_type(nativeTransaction, label));
    }

    @Override
    public Promise<RelationTypeImpl> getRelationType(String label) {
        if (label == null || label.isEmpty()) throw new TypeDBDriverException(MISSING_LABEL);
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        return RelationTypeImpl.promise(concepts_get_relation_type(nativeTransaction, label));
    }

    @Override
    public Promise<AttributeTypeImpl> getAttributeType(String label) {
        if (label == null || label.isEmpty()) throw new TypeDBDriverException(MISSING_LABEL);
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        return AttributeTypeImpl.promise(concepts_get_attribute_type(nativeTransaction, label));
    }

    @Override
    public Promise<EntityTypeImpl> putEntityType(String label) {
        if (label == null || label.isEmpty()) throw new TypeDBDriverException(MISSING_LABEL);
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        return EntityTypeImpl.promise(concepts_put_entity_type(nativeTransaction, label));
    }

    @Override
    public Promise<RelationTypeImpl> putRelationType(String label) {
        if (label == null || label.isEmpty()) throw new TypeDBDriverException(MISSING_LABEL);
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        return RelationTypeImpl.promise(concepts_put_relation_type(nativeTransaction, label));
    }

    @Override
    public Promise<AttributeTypeImpl> putAttributeType(String label, Value.Type valueType) {
        if (label == null || label.isEmpty()) throw new TypeDBDriverException(MISSING_LABEL);
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        return AttributeTypeImpl.promise(concepts_put_attribute_type(nativeTransaction, label, valueType.nativeObject));
    }

    @Override
    public Promise<EntityImpl> getEntity(String iid) {
        if (iid == null || iid.isEmpty()) throw new TypeDBDriverException(MISSING_IID);
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        return EntityImpl.promise(concepts_get_entity(nativeTransaction, iid));
    }

    @Override
    public Promise<RelationImpl> getRelation(String iid) {
        if (iid == null || iid.isEmpty()) throw new TypeDBDriverException(MISSING_IID);
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        return RelationImpl.promise(concepts_get_relation(nativeTransaction, iid));
    }

    @Override
    public Promise<AttributeImpl> getAttribute(String iid) {
        if (iid == null || iid.isEmpty()) throw new TypeDBDriverException(MISSING_IID);
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        return AttributeImpl.promise(concepts_get_attribute(nativeTransaction, iid));
    }

    @Override
    public List<TypeDBException> getSchemaExceptions() {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        try {
            return concepts_get_schema_exceptions(nativeTransaction).stream()
                    .map(e -> new TypeDBException(schema_exception_code(e), schema_exception_message(e))).collect(Collectors.toList());
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }
}
