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
import grakn.client.concept.ConceptId;
import grakn.client.concept.MetaType;
import grakn.client.concept.Thing;
import grakn.client.concept.Type;

/**
 * Client implementation of Type
 */
class RemoteMetaTypeImpl<
        SomeRemoteType extends RemoteType<SomeRemoteType, SomeRemoteThing, SomeType, SomeThing>,
        SomeRemoteThing extends RemoteThing<SomeRemoteThing, SomeRemoteType, SomeThing, SomeType>,
        SomeType extends Type<SomeType, SomeThing>, SomeThing extends Thing<SomeThing, SomeType>>
        extends RemoteTypeImpl<SomeRemoteType, SomeRemoteThing, SomeType, SomeThing>
        implements MetaType<SomeType, SomeThing>
         {

    RemoteMetaTypeImpl(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    @Override
    final RemoteType asCurrentBaseType(RemoteConcept other) {
        return other.asType();
    }

    @Override
    boolean equalsCurrentBaseType(RemoteConcept other) {
        return other.isType();
    }

    @Override
    protected final RemoteThing asInstance(RemoteConcept concept) {
        return concept.asThing();
    }
}
