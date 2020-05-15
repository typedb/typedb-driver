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

package grakn.client.concept.type.impl;

import grakn.client.GraknClient;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.MetaType;
import grakn.client.concept.type.Type;
import grakn.protocol.session.ConceptProto;

public class MetaTypeImpl {
    public static class Local<
            SomeType extends Type<SomeType, SomeThing>,
            SomeThing extends Thing<SomeThing, SomeType>>
            extends TypeImpl.Local<SomeType, SomeThing>
            implements MetaType.Local<SomeType, SomeThing> {

        public Local(ConceptProto.Concept concept) {
            super(concept);
        }
    }

    /**
     * Client implementation of Type
     */
    public static class Remote<
            SomeRemoteType extends Type<SomeRemoteType, SomeRemoteThing>,
            SomeRemoteThing extends Thing<SomeRemoteThing, SomeRemoteType>>
            extends TypeImpl.Remote<SomeRemoteType, SomeRemoteThing>
            implements MetaType.Remote<SomeRemoteType, SomeRemoteThing> {

        public Remote(GraknClient.Transaction tx, ConceptId id) {
            super(tx, id);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Thing.Remote<SomeRemoteThing, SomeRemoteType> asInstance(Concept.Remote<?> concept) {
            return (Thing.Remote<SomeRemoteThing, SomeRemoteType>) concept.asThing();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected final Type.Remote<SomeRemoteType, SomeRemoteThing> asCurrentBaseType(Concept.Remote<?> other) {
            return (Type.Remote<SomeRemoteType, SomeRemoteThing>) other.asType();
        }

        @Override
        protected boolean equalsCurrentBaseType(
                Concept.Remote<?> other) {
            return other.isMetaType();
        }
    }
}
