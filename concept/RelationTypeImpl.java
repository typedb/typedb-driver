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

package grakn.client.concept;

import grakn.client.concept.remote.RemoteRelation;
import grakn.client.concept.remote.RemoteRelationType;
import grakn.protocol.session.ConceptProto;

/**
 * Client implementation of RelationType
 */
class RelationTypeImpl extends TypeImpl<RelationType, Relation, RemoteRelationType, RemoteRelation> implements RelationType {

    RelationTypeImpl(ConceptProto.Concept concept) {
        super(concept);
    }

    @Override
    final RelationType asCurrentBaseType(Concept<?, ?> other) {
        return other.asRelationType();
    }

    @Override
    final boolean equalsCurrentBaseType(Concept<?, ?> other) {
        return other.isRelationType();
    }

    @Override
    protected final Relation asInstance(Concept<?, ?> concept) {
        return concept.asRelation();
    }
}
