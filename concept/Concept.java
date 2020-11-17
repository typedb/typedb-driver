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

import grakn.client.Grakn;
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.Type;
import grakn.client.concept.type.impl.TypeImpl;
import grakn.protocol.ConceptProto;

import javax.annotation.CheckReturnValue;

import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static grakn.common.util.Objects.className;

public interface Concept {

    @CheckReturnValue
    Type asType();

    @CheckReturnValue
    Thing asThing();

    @CheckReturnValue
    Remote asRemote(Grakn.Transaction transaction);

    @CheckReturnValue
    boolean isRemote();

    interface Remote extends Concept {

        void delete();

        @CheckReturnValue
        boolean isDeleted();

        @Override
        Type.Remote asType();

        @Override
        Thing.Remote asThing();
    }
}
