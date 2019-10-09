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

import grakn.client.GraknClient;

/**
 * Client implementation of Type
 */
public class MetaType extends TypeImpl<Type, Thing> {

    MetaType(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    @Override
    final Type asCurrentBaseType(Concept other) {
        return other.asType();
    }

    @Override
    boolean equalsCurrentBaseType(Concept other) {
        return other.isType();
    }

    @Override
    protected final Thing asInstance(Concept concept) {
        return concept.asThing();
    }
}
