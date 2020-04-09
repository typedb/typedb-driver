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

package grakn.client.concept.remote;

import grakn.client.GraknClient;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.SchemaConcept;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.MetaType;

/**
 * Client implementation of Type
 */
public class RemoteMetaTypeImpl<
        SomeRemoteType extends MetaType<SomeRemoteType, SomeRemoteThing>,
        SomeRemoteThing extends Thing<SomeRemoteThing, SomeRemoteType>>
        extends RemoteTypeImpl<SomeRemoteType, SomeRemoteThing>
        implements MetaType.Remote<SomeRemoteType, SomeRemoteThing> {

    public RemoteMetaTypeImpl(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Thing.Remote<SomeRemoteThing, SomeRemoteType> asInstance(Concept.Remote<?> concept) {
        return (Thing.Remote<SomeRemoteThing, SomeRemoteType>) concept.asThing();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final MetaType.Remote<SomeRemoteType, SomeRemoteThing> asCurrentBaseType(Concept.Remote<?> other) {
        return (MetaType.Remote<SomeRemoteType, SomeRemoteThing>) other.asMetaType();
    }

    @Override
    protected boolean equalsCurrentBaseType(
            Concept.Remote<?> other) {
        return other.isMetaType();
    }
}
