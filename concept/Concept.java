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

import grakn.client.Grakn.Transaction;
import grakn.client.concept.type.Rule;
import grakn.client.concept.type.Type;
import grakn.client.concept.thing.Thing;
import grakn.protocol.ConceptProto;

import javax.annotation.CheckReturnValue;

/**
 * The base concept implementation.
 * A concept which can be every object in the graph.
 * This class forms the basis of assuring the graph follows the Grakn object model.
 * It provides methods to retrieve information about the Concept, and determine if it is a Type
 * (EntityType, RoleType, RelationType, Rule or AttributeType)
 * or an Thing (Entity, Relation, Attribute).
 */
public interface Concept {

    /**
     * Return as a Type if the Concept is a Type.
     *
     * @return A Type if the Concept is a Type
     */
    @CheckReturnValue
    Type asType();

    /**
     * Return as a Thing if the Concept is a Thing.
     *
     * @return A Thing if the Concept is a Thing
     */
    @CheckReturnValue
    Thing asThing();

    /**
     * Return as a Rule if the Concept is a Rule.
     *
     * @return A Rule if the Concept is a Rule
     */
    @CheckReturnValue
    Rule asRule();

    /**
     * Return a Concept.Remote for this Concept.
     *
     * @param tx The transaction to use for the RPCs.
     * @return A remote concept using the given transaction to enable RPCs.
     */
    Remote asRemote(Transaction tx);

    /**
     * Determine if the Concept is remote.
     *
     * @return true if the Concept is remote.
     */
    default boolean isRemote() {
        return false;
    }

    interface Local extends Concept {

        static Concept.Local of(ConceptProto.Concept concept) {
            if (concept.hasThing()) {
                return Thing.Local.of(concept.getThing());
            } else {
                return Type.Local.of(concept.getType());
            }
        }

        /**
         * Return as a Type if the Concept is a Type.
         *
         * @return A Type if the Concept is a Type
         */
        @CheckReturnValue
        @Override
        default Type.Local asType() {
            throw GraknConceptException.invalidCasting(this, Type.class);
        }

        /**
         * Return as a Thing if the Concept is a Thing.
         *
         * @return A Thing if the Concept is a Thing
         */
        @CheckReturnValue
        @Override
        default Thing.Local asThing() {
            throw GraknConceptException.invalidCasting(this, Thing.class);
        }

        /**
         * Return as a Rule if the Concept is a Rule.
         *
         * @return A Rule if the Concept is a Rule
         */
        @CheckReturnValue
        @Override
        default Rule.Local asRule() {
            throw GraknConceptException.invalidCasting(this, Rule.class);
        }
    }

    /**
     * The base remote concept implementation.
     *
     * Provides the basic RPCs to delete a concept and check if it is deleted.
     */
    interface Remote extends Concept {

        static Concept.Remote of(Transaction tx, ConceptProto.Concept concept) {
            if (concept.hasThing()) {
                return Thing.Remote.of(tx, concept.getThing());
            } else {
                return Type.Remote.of(tx, concept.getType());
            }
        }

        /**
         * Delete the Concept
         */
        void delete();

        /**
         * Return whether the concept has been deleted.
         */
        boolean isDeleted();

        /**
         * Return as a SchemaConcept if the Concept is a SchemaConcept.
         *
         * @return A SchemaConcept if the Concept is a SchemaConcept
         */
        @Override
        @CheckReturnValue
        default Type.Remote asType() {
            throw GraknConceptException.invalidCasting(this, Type.Remote.class);
        }

        /**
         * Return as an Thing if the Concept is an Thing.
         *
         * @return An Thing if the Concept is an Thing
         */
        @Override
        @CheckReturnValue
        default Thing.Remote asThing() {
            throw GraknConceptException.invalidCasting(this, Thing.Remote.class);
        }

        /**
         * Return as a Rule if the Concept is a Rule.
         *
         * @return A Rule if the Concept is a Rule
         */
        @Override
        @CheckReturnValue
        default Rule.Remote asRule() {
            throw GraknConceptException.invalidCasting(this, Rule.Remote.class);
        }

        @Override
        default boolean isRemote() {
            return true;
        }
    }
}
