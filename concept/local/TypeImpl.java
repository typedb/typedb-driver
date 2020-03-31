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

package grakn.client.concept.local;

import grakn.client.concept.Concept;
import grakn.client.concept.Thing;
import grakn.client.concept.Type;
import grakn.protocol.session.ConceptProto;

/**
 * Client implementation of Type
 *
 * @param <SomeType>  The exact type of this class
 */
public abstract class TypeImpl<
        SomeType extends LocalType<SomeType, SomeThing>,
        SomeThing extends LocalThing<SomeThing, SomeType>>
        extends SchemaConceptImpl<SomeType>
        implements LocalType<SomeType, SomeThing> {

    TypeImpl(ConceptProto.Concept concept) {
        super(concept);
    }

    protected abstract SomeThing asInstance(Concept<?> concept);
}
