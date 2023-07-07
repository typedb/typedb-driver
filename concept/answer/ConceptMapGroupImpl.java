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

package com.vaticle.typedb.client.concept.answer;

import com.vaticle.typedb.client.api.answer.ConceptMap;
import com.vaticle.typedb.client.api.answer.ConceptMapGroup;
import com.vaticle.typedb.client.api.concept.Concept;
import com.vaticle.typedb.client.concept.ConceptImpl;

import java.util.stream.Stream;

import static com.vaticle.typedb.client.jni.typedb_client_jni.concept_map_group_get_concept_maps;
import static com.vaticle.typedb.client.jni.typedb_client_jni.concept_map_group_equals;
import static com.vaticle.typedb.client.jni.typedb_client_jni.concept_map_group_get_owner;

public class ConceptMapGroupImpl implements ConceptMapGroup {
    private final com.vaticle.typedb.client.jni.ConceptMapGroup concept_map_group;
    private final int hash;

    public ConceptMapGroupImpl(com.vaticle.typedb.client.jni.ConceptMapGroup concept_map_group) {
        this.concept_map_group = concept_map_group;
        this.hash = toString().hashCode();
    }

    @Override
    public Concept owner() {
        return ConceptImpl.of(concept_map_group_get_owner(concept_map_group));
    }

    @Override
    public Stream<ConceptMap> conceptMaps() {
        return concept_map_group_get_concept_maps(concept_map_group).stream().map(ConceptMapImpl::new);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConceptMapGroupImpl that = (ConceptMapGroupImpl) obj;
        return concept_map_group_equals(this.concept_map_group, that.concept_map_group);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
