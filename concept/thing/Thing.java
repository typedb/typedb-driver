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

import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.Concept;
import grakn.client.concept.Concepts;
import grakn.client.concept.thing.impl.AttributeImpl;
import grakn.client.concept.thing.impl.EntityImpl;
import grakn.client.concept.thing.impl.RelationImpl;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.Rule;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.ThingType;
import grakn.protocol.ConceptProto;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static grakn.client.common.exception.ErrorMessage.Protocol.UNRECOGNISED_FIELD;
import static grakn.common.collection.Bytes.bytesToHexString;

/**
 * A data instance in the graph belonging to a specific Type
 * Instances represent data in the graph.
 * Every instance belongs to a Type which serves as a way of categorising them.
 * Instances can relate to one another via Relation
 */
public interface Thing extends Concept {

    /**
     * Get the unique IID associated with the Thing.
     *
     * @return The thing's unique IID.
     */
    @CheckReturnValue
    String getIID();

    /**
     * Return the Type of the Concept.
     *
     * @return A Type which is the type of this concept. This concept is an instance of that type.
     */
    @CheckReturnValue
    ThingType getType();

    /**
     * Return as an Entity, if the Thing is an Entity.
     *
     * @return An Entity if the Thing is an Entity
     */
    @CheckReturnValue
    Entity asEntity();

    /**
     * Return as a Attribute if the Thing is a Attribute.
     *
     * @return A Attribute if the Thing is an Attribute
     */
    @CheckReturnValue
    Attribute<?> asAttribute();

    /**
     * Return as a Relation if the Thing is a Relation.
     *
     * @return A Relation if the Thing is a Relation
     */
    @CheckReturnValue
    Relation asRelation();

    /**
     * Return a Thing.Remote for this Thing.
     *
     * @param concepts The transaction to use for the RPCs.
     * @return A remote concept using the given transaction to enable RPCs.
     */
    Remote asRemote(Concepts concepts);

    interface Local extends Concept.Local, Thing {

        static Thing.Local of(final ConceptProto.Thing thing) {
            switch (thing.getSchema()) {
                case ENTITY:
                    return new EntityImpl.Local(thing);
                case RELATION:
                    return new RelationImpl.Local(thing);
                case ATTRIBUTE:
                    switch (thing.getValueType()) {
                        case BOOLEAN:
                            return new AttributeImpl.Boolean.Local(thing);
                        case LONG:
                            return new AttributeImpl.Long.Local(thing);
                        case DOUBLE:
                            return new AttributeImpl.Double.Local(thing);
                        case STRING:
                            return new AttributeImpl.String.Local(thing);
                        case DATETIME:
                            return new AttributeImpl.DateTime.Local(thing);
                        default:
                        case UNRECOGNIZED:
                            throw new GraknClientException(UNRECOGNISED_FIELD.message(ConceptProto.AttributeType.VALUE_TYPE.class.getCanonicalName(), thing.getValueType()));
                    }
                default:
                case UNRECOGNIZED:
                    throw new GraknClientException(UNRECOGNISED_FIELD.message(ConceptProto.Thing.SCHEMA.class.getCanonicalName(), thing.getSchema()));
            }
        }

        @CheckReturnValue
        @Override
        default Thing.Local asThing() {
            return this;
        }

        /**
         * Return as an Entity, if the Concept is an Entity Thing.
         *
         * @return An Entity if the Concept is a Thing
         */
        @CheckReturnValue
        @Override
        default Entity.Local asEntity() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, Entity.class.getCanonicalName()));
        }

        /**
         * Return as a Attribute  if the Concept is a Attribute Thing.
         *
         * @return A Attribute if the Concept is a Attribute
         */
        @CheckReturnValue
        @Override
        default Attribute.Local<?> asAttribute() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, Attribute.class.getCanonicalName()));
        }

        /**
         * Return as a Relation if the Concept is a Relation Thing.
         *
         * @return A Relation  if the Concept is a Relation
         */
        @CheckReturnValue
        @Override
        default Relation.Local asRelation() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, Relation.class.getCanonicalName()));
        }
    }

    /**
     * A data instance in the graph belonging to a specific Type
     * Instances represent data in the graph.
     * Every instance belongs to a Type which serves as a way of categorising them.
     * Instances can relate to one another via Relation
     */
    interface Remote extends Concept.Remote, Thing {

        static Thing.Remote of(final Concepts concepts, final ConceptProto.Thing thing) {
            final String iid = bytesToHexString(thing.getIid().toByteArray());
            switch (thing.getSchema()) {
                case ENTITY:
                    return new EntityImpl.Remote(concepts, iid);
                case RELATION:
                    return new RelationImpl.Remote(concepts, iid);
                case ATTRIBUTE:
                    switch (thing.getValueType()) {
                        case BOOLEAN:
                            return new AttributeImpl.Boolean.Remote(concepts, iid);
                        case LONG:
                            return new AttributeImpl.Long.Remote(concepts, iid);
                        case DOUBLE:
                            return new AttributeImpl.Double.Remote(concepts, iid);
                        case STRING:
                            return new AttributeImpl.String.Remote(concepts, iid);
                        case DATETIME:
                            return new AttributeImpl.DateTime.Remote(concepts, iid);
                        default:
                        case UNRECOGNIZED:
                            throw new GraknClientException(UNRECOGNISED_FIELD.message(ConceptProto.AttributeType.VALUE_TYPE.class.getCanonicalName(), thing.getValueType()));
                    }
                default:
                case UNRECOGNIZED:
                    throw new GraknClientException(UNRECOGNISED_FIELD.message(ConceptProto.Thing.SCHEMA.class.getCanonicalName(), thing.getSchema()));
            }
        }

        /**
         * Creates an ownership from this Thing to the provided Attribute.
         *
         * @param attribute The Attribute to which an ownership is created
         */
        void setHas(Attribute<?> attribute);

        /**
         * Removes the provided Attribute from this Thing
         *
         * @param attribute the Attribute to be removed
         */
        void unsetHas(Attribute<?> attribute);

        /**
         * Return the Type of the Concept.
         *
         * @return A Type which is the type of this concept. This concept is an instance of that type.
         */
        @Override
        @CheckReturnValue
        ThingType.Remote getType();

        /**
         * Used to indicate if this Thing has been created as the result of a Rule inference.
         *
         * @return true if this Thing exists due to a rule
         * @see Rule
         */
        boolean isInferred();

        /**
         * Retrieves a collection of Attribute attached to this Thing, possibly specifying only keys.
         *
         * @param onlyKey If true, only fetch attributes which are keys.
         * @return A collection of Attributes attached to this Thing.
         * @see Attribute.Remote
         */
        @CheckReturnValue
        Stream<? extends Attribute.Remote<?>> getHas(boolean onlyKey);

        @CheckReturnValue
        Stream<? extends Attribute.Boolean.Remote> getHas(AttributeType.Boolean attributeType);

        @CheckReturnValue
        Stream<? extends Attribute.Long.Remote> getHas(AttributeType.Long attributeType);

        @CheckReturnValue
        Stream<? extends Attribute.Double.Remote> getHas(AttributeType.Double attributeType);

        @CheckReturnValue
        Stream<? extends Attribute.String.Remote> getHas(AttributeType.String attributeType);

        @CheckReturnValue
        Stream<? extends Attribute.DateTime.Remote> getHas(AttributeType.DateTime attributeType);

        /**
         * Retrieves a collection of Attribute attached to this Thing
         *
         * @param attributeTypes AttributeTypes of the Attributes attached to this entity
         * @return A collection of Attributes attached to this Thing.
         * @see Attribute.Remote
         */
        @CheckReturnValue
        Stream<? extends Attribute.Remote<?>> getHas(AttributeType... attributeTypes);

        /**
         * Determine the RoleTypes that this Thing is currently playing.
         *
         * @return A set of all the RoleTypes which this Thing is currently playing.
         * @see RoleType.Remote
         */
        @CheckReturnValue
        Stream<? extends RoleType.Remote> getPlays();

        /**
         * Get all {@code Relation} instances that this {@code Thing} is playing any of the specified roles in.
         * If no roles are specified, all Relations are retrieved regardless of role.
         *
         * @param roleTypes The role types that this {@code Thing} can play
         * @return a stream of {@code Relation} that this {@code Thing} plays a specified role in
         */
        Stream<? extends Relation> getRelations(RoleType... roleTypes);

        @CheckReturnValue
        @Override
        default Thing.Remote asThing() {
            return this;
        }

        /**
         * Return as an Entity, if the Thing is an Entity.
         *
         * @return An Entity if the Thing is an Entity
         */
        @Override
        @CheckReturnValue
        default Entity.Remote asEntity() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, Entity.class.getCanonicalName()));
        }

        /**
         * Return as a Relation if the Thing is a Relation.
         *
         * @return A Relation if the Thing is a Relation
         */
        @Override
        @CheckReturnValue
        default Relation.Remote asRelation() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, Relation.class.getCanonicalName()));
        }

        /**
         * Return as a Attribute if the Thing is a Attribute.
         *
         * @return An Attribute if the Thing is an Attribute
         */
        @Override
        @CheckReturnValue
        default Attribute.Remote<?> asAttribute() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, Attribute.class.getCanonicalName()));
        }
    }
}
