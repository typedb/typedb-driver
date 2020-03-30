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

import grakn.client.concept.remote.RemoteMetaType;
import grakn.client.concept.remote.RemoteThing;
import grakn.protocol.session.ConceptProto;

class MetaTypeImpl<
        SomeType extends MetaType<SomeType, SomeThing, SomeRemoteType, SomeRemoteThing>,
        SomeThing extends Thing<SomeThing, SomeType, SomeRemoteThing, SomeRemoteType>,
        SomeRemoteType extends RemoteMetaType<SomeRemoteType, SomeRemoteThing, SomeType, SomeThing>,
        SomeRemoteThing extends RemoteThing<SomeRemoteThing, SomeRemoteType, SomeThing, SomeType>>
        extends TypeImpl<SomeType, SomeThing, SomeRemoteType, SomeRemoteThing>
        implements MetaType<SomeType, SomeThing, SomeRemoteType, SomeRemoteThing> {

    MetaTypeImpl(ConceptProto.Concept concept) {
        super(concept);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SomeThing asInstance(Concept<SomeThing, SomeRemoteThing> concept) {
        return (SomeThing) concept.asThing();
    }

    @SuppressWarnings("unchecked")
    @Override
    SomeType asCurrentBaseType(Concept<SomeType, SomeRemoteType> other) {
        return (SomeType) other.asMetaType();
    }

    @Override
    boolean equalsCurrentBaseType(Concept<SomeType, SomeRemoteType> other) {
        return other.isMetaType();
    }

}
