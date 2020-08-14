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

package grakn.client.concept.thing;

import grakn.client.Grakn.Transaction;
import grakn.client.concept.Concept;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.Rule;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.ThingType;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * A data instance in the graph belonging to a specific Type
 * Instances represent data in the graph.
 * Every instance belongs to a Type which serves as a way of categorising them.
 * Instances can relate to one another via Relation
 */
public interface Thing extends Concept {

    /**
     * Return the Type of the Concept.
     *
     * @return A Type which is the type of this concept. This concept is an instance of that type.
     */
    @CheckReturnValue
    ThingType getType();

    /**
     * Used to indicate if this Thing has been created as the result of a Rule inference.
     *
     * @return true if this Thing exists due to a rule
     * @see Rule
     */
    boolean isInferred();

    @Deprecated
    @CheckReturnValue
    @Override
    default Thing asThing() {
        return this;
    }

    @Override
    Remote asRemote(Transaction tx);

    interface Local extends Concept.Local, Thing {
    }

    /**
     * A data instance in the graph belonging to a specific Type
     * Instances represent data in the graph.
     * Every instance belongs to a Type which serves as a way of categorising them.
     * Instances can relate to one another via Relation
     */
    interface Remote extends Concept.Remote, Thing {

        /**
         * Return the Type of the Concept.
         *
         * @return A Type which is the type of this concept. This concept is an instance of that type.
         */
        @Override
        @CheckReturnValue
        ThingType.Remote getType();

        /**
         * Retrieves the Relations which the Thing takes part in, which may optionally be narrowed to a particular set
         * according to the RoleType you are interested in.
         *
         * @param roleTypes An optional parameter which allows you to specify the role type of the relations you wish to retrieve.
         * @return A set of Relations which the concept instance takes part in, optionally constrained by the RoleType.
         * @see RoleType.Remote
         * @see Relation.Remote
         */
        @CheckReturnValue
        Stream<Relation.Remote> getRelations(RoleType... roleTypes);

        /**
         * Determine the RoleTypes that this Thing is currently playing.
         *
         * @return A set of all the RoleTypes which this Thing is currently playing.
         * @see RoleType.Remote
         */
        @CheckReturnValue
        Stream<RoleType.Remote> getPlays();

        /**
         * Creates a Relation from this Thing to the provided Attribute.
         * This has the same effect as #relhas(Attribute), but returns the instance itself to allow
         * method chaining.
         *
         * @param attribute The Attribute to which a Relation is created
         * @return The instance itself
         */
        Thing.Remote setHas(Attribute attribute);

        /**
         * Retrieves a collection of Attribute attached to this Thing
         *
         * @param attributeTypes AttributeTypes of the Attributes attached to this entity
         * @return A collection of Attributes attached to this Thing.
         * @see Attribute.Remote
         */
        @CheckReturnValue
        Stream<Attribute.Remote> getHas(AttributeType... attributeTypes);

        @CheckReturnValue
        <T> Stream<Attribute.Remote> getHas(AttributeType attributeType);

        /**
         * Retrieves a collection of Attribute attached to this Thing, possibly specifying only keys.
         *
         * @param keysOnly If true, only fetch attributes which are keys.
         * @return A collection of Attributes attached to this Thing.
         * @see Attribute.Remote
         */
        @CheckReturnValue
        Stream<Attribute.Remote> getHas(boolean keysOnly);

        @CheckReturnValue
        default Stream<Attribute.Remote> getHas() {
            return getHas(false);
        }

        /**
         * Removes the provided Attribute from this Thing
         *
         * @param attribute the Attribute to be removed
         */
        void unsetHas(Attribute attribute);

        /**
         * Used to indicate if this Thing has been created as the result of a Rule inference.
         *
         * @return true if this Thing exists due to a rule
         * @see Rule.Remote
         */
        boolean isInferred();

        @Deprecated
        @CheckReturnValue
        @Override
        default Thing.Remote asThing() {
            return this;
        }

    }
}
