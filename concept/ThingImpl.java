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

import grakn.protocol.session.ConceptProto;

/**
 * Client implementation of Thing
 *
 * @param <SomeThing> The exact type of this class
 * @param <SomeType>  the type of an instance of this class
 */
public abstract class ThingImpl<SomeThing extends Thing<SomeThing, SomeType>, SomeType extends Type<SomeType, SomeThing>>
        extends ConceptImpl<SomeThing> implements Thing<SomeThing, SomeType> {

    private final SomeType type;
    private final boolean inferred;

    protected ThingImpl(ConceptProto.Concept concept) {
        super(concept);
        this.type = Concept.of(concept.getTypeRes().getType());
        this.inferred = concept.getInferredRes().getInferred();
    }

    @Override
    public final SomeType type() {
        return type;
    }

    @Override
    public final boolean isInferred() {
        return inferred;
    }

    abstract SomeType asCurrentType(Concept<SomeType> concept);
}
