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

package com.vaticle.typedb.client.concept.thing;

import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.value.Value;
import com.vaticle.typedb.client.api.concept.thing.Attribute;
import com.vaticle.typedb.client.api.concept.type.ThingType;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.concept.value.ValueImpl;
import com.vaticle.typedb.client.concept.type.AttributeTypeImpl;
import com.vaticle.typedb.client.concept.type.ThingTypeImpl;

import java.util.stream.Stream;

import static com.vaticle.typedb.client.jni.typedb_client.attribute_get_owners;
import static com.vaticle.typedb.client.jni.typedb_client.attribute_get_type;
import static com.vaticle.typedb.client.jni.typedb_client.attribute_get_value;

public class AttributeImpl extends ThingImpl implements Attribute {
    public AttributeImpl(com.vaticle.typedb.client.jni.Concept concept) {
        super(concept);
    }

    @Override
    public AttributeTypeImpl getType() {
        return new AttributeTypeImpl(attribute_get_type(nativeObject));
    }

    @Override
    public Value getValue() {
        return new ValueImpl(attribute_get_value(nativeObject));
    }

    @Override
    public final Stream<ThingImpl> getOwners(TypeDBTransaction transaction) {
        try {
            return attribute_get_owners(nativeTransaction(transaction), nativeObject, null).stream().map(ThingImpl::of);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public Stream<ThingImpl> getOwners(TypeDBTransaction transaction, ThingType ownerType) {
        try {
            return attribute_get_owners(nativeTransaction(transaction), nativeObject, ((ThingTypeImpl) ownerType).nativeObject).stream().map(ThingImpl::of);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }
}
