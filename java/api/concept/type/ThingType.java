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

package com.vaticle.typedb.client.api.concept.type;

import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.thing.Thing;
import com.vaticle.typedb.client.api.concept.value.Value;
import com.vaticle.typedb.client.common.NativeObject;

import javax.annotation.CheckReturnValue;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.jni.typedb_client.annotation_equals;
import static com.vaticle.typedb.client.jni.typedb_client.annotation_is_key;
import static com.vaticle.typedb.client.jni.typedb_client.annotation_is_unique;
import static com.vaticle.typedb.client.jni.typedb_client.annotation_new_key;
import static com.vaticle.typedb.client.jni.typedb_client.annotation_new_unique;
import static com.vaticle.typedb.client.jni.typedb_client.annotation_to_string;

public interface ThingType extends Type {
    @Override
    @CheckReturnValue
    default boolean isThingType() {
        return true;
    }

    @Override
    @CheckReturnValue
    default ThingType asThingType() {
        return this;
    }

    @Override
    @CheckReturnValue
    ThingType getSupertype(TypeDBTransaction transaction);

    @Override
    @CheckReturnValue
    Stream<? extends ThingType> getSupertypes(TypeDBTransaction transaction);

    @Override
    @CheckReturnValue
    Stream<? extends ThingType> getSubtypes(TypeDBTransaction transaction);

    @Override
    @CheckReturnValue
    Stream<? extends ThingType> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity);

    @CheckReturnValue
    Stream<? extends Thing> getInstances(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends Thing> getInstances(TypeDBTransaction transaction, Transitivity transitivity);

    void setAbstract(TypeDBTransaction transaction);

    void unsetAbstract(TypeDBTransaction transaction);

    void setPlays(TypeDBTransaction transaction, RoleType roleType);

    void setPlays(TypeDBTransaction transaction, RoleType roleType, RoleType overriddenType);

    void setOwns(TypeDBTransaction transaction, AttributeType attributeType, AttributeType overriddenType, Set<Annotation> annotations);

    void setOwns(TypeDBTransaction transaction, AttributeType attributeType, AttributeType overriddenType);

    void setOwns(TypeDBTransaction transaction, AttributeType attributeType, Set<Annotation> annotations);

    void setOwns(TypeDBTransaction transaction, AttributeType attributeType);

    @CheckReturnValue
    Stream<? extends RoleType> getPlays(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends RoleType> getPlays(TypeDBTransaction transaction, Transitivity transitivity);

    @CheckReturnValue
    RoleType getPlaysOverridden(TypeDBTransaction transaction, RoleType roleType);

    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Value.Type valueType);

    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Set<Annotation> annotations);

    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Value.Type valueType, Set<Annotation> annotations);

    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Transitivity transitivity);

    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Value.Type valueType, Transitivity transitivity);

    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Set<Annotation> annotations, Transitivity transitivity);

    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Value.Type valueType, Set<Annotation> annotations, Transitivity transitivity);

    @CheckReturnValue
    AttributeType getOwnsOverridden(TypeDBTransaction transaction, AttributeType attributeType);

    void unsetPlays(TypeDBTransaction transaction, RoleType roleType);

    void unsetOwns(TypeDBTransaction transaction, AttributeType attributeType);

    @CheckReturnValue
    String getSyntax(TypeDBTransaction transaction);

    class Annotation extends NativeObject<com.vaticle.typedb.client.jni.Annotation> {
        private final int hash;

        private Annotation(com.vaticle.typedb.client.jni.Annotation annotation) {
            super(annotation);
            this.hash = Objects.hash(isKey(), isUnique());
        }

        public static Annotation key() {
            return new Annotation(annotation_new_key());
        }

        public static Annotation unique() {
            return new Annotation(annotation_new_unique());
        }

        public boolean isKey() {
            return annotation_is_key(nativeObject);
        }

        public boolean isUnique() {
            return annotation_is_unique(nativeObject);
        }

        @Override
        public String toString() {
            return annotation_to_string(nativeObject);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Annotation that = (Annotation) obj;
            return annotation_equals(this.nativeObject, that.nativeObject);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
