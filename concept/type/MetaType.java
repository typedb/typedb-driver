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

package grakn.client.concept.type;

import grakn.client.GraknClient;
import grakn.client.concept.ConceptId;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.impl.MetaTypeImpl;

import javax.annotation.CheckReturnValue;

public interface MetaType<
        SomeType extends Type<SomeType, SomeThing>,
        SomeThing extends Thing<SomeThing, SomeType>>
        extends Type<SomeType, SomeThing> {
    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default MetaType<SomeType, SomeThing> asMetaType() {
        return this;
    }

    @Override
    default MetaType.Remote<SomeType, SomeThing> asRemote(GraknClient.Transaction tx) {
        return MetaType.Remote.of(tx, id());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isMetaType() {
        return true;
    }

    interface Local<
            SomeType extends Type<SomeType, SomeThing>,
            SomeThing extends Thing<SomeThing, SomeType>>
            extends Type.Local<SomeType, SomeThing>, MetaType<SomeType, SomeThing> {
    }

    /**
     * Type Class of a MetaType
     */
    interface Remote<
            SomeRemoteType extends Type<SomeRemoteType, SomeRemoteThing>,
            SomeRemoteThing extends Thing<SomeRemoteThing, SomeRemoteType>>
        extends MetaType<SomeRemoteType, SomeRemoteThing>,
            Type.Remote<SomeRemoteType, SomeRemoteThing> {

        static <SomeRemoteType extends Type<SomeRemoteType, SomeRemoteThing>,
                SomeRemoteThing extends Thing<SomeRemoteThing, SomeRemoteType>>
        MetaType.Remote<SomeRemoteType, SomeRemoteThing> of(GraknClient.Transaction tx, ConceptId id) {
            return new MetaTypeImpl.Remote<>(tx, id);
        }

        @Override
        default MetaType.Remote<SomeRemoteType, SomeRemoteThing> asMetaType() {
            return this;
        }
    }
}
