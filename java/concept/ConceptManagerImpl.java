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

package com.vaticle.typedb.client.concept;

import com.vaticle.typedb.client.api.concept.ConceptManager;
import com.vaticle.typedb.client.api.concept.thing.Attribute;
import com.vaticle.typedb.client.api.concept.thing.Entity;
import com.vaticle.typedb.client.api.concept.thing.Relation;
import com.vaticle.typedb.client.api.concept.type.AttributeType;
import com.vaticle.typedb.client.api.concept.type.EntityType;
import com.vaticle.typedb.client.api.concept.type.RelationType;
import com.vaticle.typedb.client.api.concept.value.Value;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.common.exception.TypeDBException;
import com.vaticle.typedb.client.concept.thing.AttributeImpl;
import com.vaticle.typedb.client.concept.thing.EntityImpl;
import com.vaticle.typedb.client.concept.thing.RelationImpl;
import com.vaticle.typedb.client.concept.type.AttributeTypeImpl;
import com.vaticle.typedb.client.concept.type.EntityTypeImpl;
import com.vaticle.typedb.client.concept.type.RelationTypeImpl;
import com.vaticle.typeql.lang.common.TypeQLToken;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.TRANSACTION_CLOSED;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.MISSING_IID;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.MISSING_LABEL;
import static com.vaticle.typedb.client.jni.typedb_client.concepts_get_attribute;
import static com.vaticle.typedb.client.jni.typedb_client.concepts_get_attribute_type;
import static com.vaticle.typedb.client.jni.typedb_client.concepts_get_entity;
import static com.vaticle.typedb.client.jni.typedb_client.concepts_get_entity_type;
import static com.vaticle.typedb.client.jni.typedb_client.concepts_get_relation;
import static com.vaticle.typedb.client.jni.typedb_client.concepts_get_relation_type;
import static com.vaticle.typedb.client.jni.typedb_client.concepts_get_schema_exceptions;
import static com.vaticle.typedb.client.jni.typedb_client.concepts_put_attribute_type;
import static com.vaticle.typedb.client.jni.typedb_client.concepts_put_entity_type;
import static com.vaticle.typedb.client.jni.typedb_client.concepts_put_relation_type;
import static com.vaticle.typedb.client.jni.typedb_client.schema_exception_code;
import static com.vaticle.typedb.client.jni.typedb_client.schema_exception_message;

public final class ConceptManagerImpl implements ConceptManager {
    final com.vaticle.typedb.client.jni.Transaction nativeTransaction;

    public ConceptManagerImpl(com.vaticle.typedb.client.jni.Transaction nativeTransaction) {
        this.nativeTransaction = nativeTransaction;
    }

    @Override
    public EntityType getRootEntityType() {
        return getEntityType(TypeQLToken.Type.ENTITY.toString());
    }

    @Override
    public RelationType getRootRelationType() {
        return getRelationType(TypeQLToken.Type.RELATION.toString());
    }

    @Override
    public AttributeType getRootAttributeType() {
        return getAttributeType(TypeQLToken.Type.ATTRIBUTE.toString());
    }

    @Override
    @Nullable
    public EntityType getEntityType(String label) {
        if (label == null || label.isEmpty()) throw new TypeDBClientException(MISSING_LABEL);
        if (!nativeTransaction.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        try {
            com.vaticle.typedb.client.jni.Concept res = concepts_get_entity_type(nativeTransaction, label);
            if (res != null) return new EntityTypeImpl(res);
            else return null;
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    @Nullable
    public RelationType getRelationType(String label) {
        if (label == null || label.isEmpty()) throw new TypeDBClientException(MISSING_LABEL);
        if (!nativeTransaction.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        try {
            com.vaticle.typedb.client.jni.Concept res = concepts_get_relation_type(nativeTransaction, label);
            if (res != null) return new RelationTypeImpl(res);
            else return null;
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    @Nullable
    public AttributeType getAttributeType(String label) {
        if (label == null || label.isEmpty()) throw new TypeDBClientException(MISSING_LABEL);
        if (!nativeTransaction.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        try {
            com.vaticle.typedb.client.jni.Concept res = concepts_get_attribute_type(nativeTransaction, label);
            if (res != null) return new AttributeTypeImpl(res);
            else return null;
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public EntityType putEntityType(String label) {
        if (label == null || label.isEmpty()) throw new TypeDBClientException(MISSING_LABEL);
        if (!nativeTransaction.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        try {
            return new EntityTypeImpl(concepts_put_entity_type(nativeTransaction, label));
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public RelationType putRelationType(String label) {
        if (label == null || label.isEmpty()) throw new TypeDBClientException(MISSING_LABEL);
        if (!nativeTransaction.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        try {
            return new RelationTypeImpl(concepts_put_relation_type(nativeTransaction, label));
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public AttributeType putAttributeType(String label, Value.Type valueType) {
        if (label == null || label.isEmpty()) throw new TypeDBClientException(MISSING_LABEL);
        if (!nativeTransaction.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        try {
            return new AttributeTypeImpl(concepts_put_attribute_type(nativeTransaction, label, valueType.nativeObject));
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    @Nullable
    public Entity getEntity(String iid) {
        if (iid == null || iid.isEmpty()) throw new TypeDBClientException(MISSING_IID);
        if (!nativeTransaction.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        try {
            com.vaticle.typedb.client.jni.Concept res = concepts_get_entity(nativeTransaction, iid);
            if (res != null) return new EntityImpl(res);
            else return null;
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    @Nullable
    public Relation getRelation(String iid) {
        if (iid == null || iid.isEmpty()) throw new TypeDBClientException(MISSING_IID);
        if (!nativeTransaction.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        try {
            com.vaticle.typedb.client.jni.Concept res = concepts_get_relation(nativeTransaction, iid);
            if (res != null) return new RelationImpl(res);
            else return null;
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    @Nullable
    public Attribute getAttribute(String iid) {
        if (iid == null || iid.isEmpty()) throw new TypeDBClientException(MISSING_IID);
        if (!nativeTransaction.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        try {
            com.vaticle.typedb.client.jni.Concept res = concepts_get_attribute(nativeTransaction, iid);
            if (res != null) return new AttributeImpl(res);
            else return null;
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public List<TypeDBException> getSchemaExceptions() {
        if (!nativeTransaction.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        try {
            return concepts_get_schema_exceptions(nativeTransaction).stream()
                    .map(e -> new TypeDBException(schema_exception_code(e), schema_exception_message(e))).collect(Collectors.toList());
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }
}
