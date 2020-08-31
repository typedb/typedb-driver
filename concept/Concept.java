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
import grakn.client.common.exception.GraknException;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.Rule;
import grakn.client.concept.type.Type;
import grakn.client.concept.type.impl.TypeImpl;
import grakn.protocol.ConceptProto;

import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;

public interface Concept {

    Type asType();

    Thing asThing();

    Rule asRule();

    Remote asRemote(Grakn.Transaction transaction);

    default boolean isRemote() {
        return false;
    }

    interface Local extends Concept {

        @Override
        default Type.Local asType() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, Type.class.getSimpleName()));
        }

        @Override
        default Thing.Local asThing() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, Thing.class.getSimpleName()));
        }

        @Override
        default Rule.Local asRule() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, Rule.class.getSimpleName()));
        }
    }

    interface Remote extends Concept {

        static Concept.Remote of(final Grakn.Transaction transaction, ConceptProto.Concept concept) {
            if (concept.hasThing()) {
                return ThingImpl.Remote.of(transaction, concept.getThing());
            } else {
                return TypeImpl.Remote.of(transaction, concept.getType());
            }
        }

        void delete();

        boolean isDeleted();

        @Override
        default Type.Remote asType() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, Type.class.getSimpleName()));
        }

        @Override
        default Thing.Remote asThing() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, Thing.class.getSimpleName()));
        }

        @Override
        default Rule.Remote asRule() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, Rule.class.getSimpleName()));
        }

        @Override
        default boolean isRemote() {
            return true;
        }
    }
}
