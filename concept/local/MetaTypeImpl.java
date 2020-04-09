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
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.MetaType;
import grakn.protocol.session.ConceptProto;

public class MetaTypeImpl<
        SomeType extends MetaType.Local<SomeType, SomeThing>,
        SomeThing extends Thing.Local<SomeThing, SomeType>>
        extends TypeImpl<SomeType, SomeThing>
        implements MetaType.Local<SomeType, SomeThing> {

    public MetaTypeImpl(ConceptProto.Concept concept) {
        super(concept);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SomeThing asInstance(Concept<?> concept) {
        return (SomeThing) concept.asThing();
    }

    @SuppressWarnings("unchecked")
    @Override
    SomeType asCurrentBaseType(Concept<?> other) {
        return (SomeType) other.asMetaType();
    }

    @Override
    boolean equalsCurrentBaseType(Concept<?> other) {
        return other.isMetaType();
    }

}
