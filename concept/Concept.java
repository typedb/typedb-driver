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

import grakn.client.GraknClient;
import grakn.client.concept.type.impl.RuleImpl;
import grakn.client.concept.thing.impl.AttributeImpl;
import grakn.client.concept.thing.impl.EntityImpl;
import grakn.client.concept.thing.impl.RelationImpl;
import grakn.client.concept.type.Rule;
import grakn.client.concept.type.Type;
import grakn.client.concept.type.impl.AttributeTypeImpl;
import grakn.client.concept.type.impl.EntityTypeImpl;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Entity;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.MetaType;
import grakn.client.concept.type.impl.MetaTypeImpl;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.impl.RelationTypeImpl;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.impl.RoleTypeImpl;
import grakn.client.concept.type.ThingType;
import grakn.protocol.ConceptProto;

import javax.annotation.CheckReturnValue;


/**
 * The base concept implementation.
 * A concept which can every object in the graph.
 * This class forms the basis of assuring the graph follows the Grakn object model.
 * It provides methods to retrieve information about the Concept, and determine if it is a Type
 * (EntityType, Role, RelationType, Rule or AttributeType)
 * or an Thing (Entity, Relation , Attribute).
 */
public interface Concept<BaseType extends Concept<BaseType>> {

    //------------------------------------- Accessors ----------------------------------

    /**
     * Get the unique IID associated with the Concept.
     *
     * @return A value the concept's unique iid.
     */
    @CheckReturnValue
    ConceptIID iid();

    //------------------------------------- Other ---------------------------------

    /**
     * Return as a SchemaConcept if the Concept is a SchemaConcept.
     *
     * @return A SchemaConcept if the Concept is a SchemaConcept
     */
    @CheckReturnValue
    default Type<?> asSchemaConcept() {
        throw GraknConceptException.invalidCasting(this, Type.class);
    }

    /**
     * Return as a Type if the Concept is a Type.
     *
     * @return A Type if the Concept is a Type
     */
    @CheckReturnValue
    default ThingType<?, ?> asType() {
        throw GraknConceptException.invalidCasting(this, ThingType.class);
    }

    /**
     * Return as an Thing if the Concept is an Thing.
     *
     * @return An Thing if the Concept is an Thing
     */
    @CheckReturnValue
    default Thing<?, ?> asThing() {
        throw GraknConceptException.invalidCasting(this, Thing.class);
    }

    /**
     * Return as an EntityType if the Concept is an EntityType.
     *
     * @return A EntityType if the Concept is an EntityType
     */
    @CheckReturnValue
    default EntityType asEntityType() {
        throw GraknConceptException.invalidCasting(this, EntityType.class);
    }

    /**
     * Return as a Role if the Concept is a Role.
     *
     * @return A Role if the Concept is a Role
     */
    @CheckReturnValue
    default RoleType asRole() {
        throw GraknConceptException.invalidCasting(this, RoleType.class);
    }

    /**
     * Return as a RelationType if the Concept is a RelationType.
     *
     * @return A RelationType if the Concept is a RelationType
     */
    @CheckReturnValue
    default RelationType asRelationType() {
        throw GraknConceptException.invalidCasting(this, RelationType.class);
    }

    /**
     * Return as a AttributeType if the Concept is a AttributeType
     *
     * @return A AttributeType if the Concept is a AttributeType
     */
    @CheckReturnValue
    default <D> AttributeType<D> asAttributeType() {
        throw GraknConceptException.invalidCasting(this, AttributeType.class);
    }

    /**
     * Return as a AttributeType if the Concept is a AttributeType
     *
     * @return A AttributeType if the Concept is a AttributeType
     */
    @CheckReturnValue
    default <D> AttributeType<D> asAttributeType(ValueType<D> valueType) {
        throw GraknConceptException.invalidCasting(this, AttributeType.class);
    }

    /**
     * Return as a Rule if the Concept is a Rule.
     *
     * @return A Rule if the Concept is a Rule
     */
    @CheckReturnValue
    default Rule asRule() {
        throw GraknConceptException.invalidCasting(this, Rule.class);
    }

    /**
     * Return as an Entity, if the Concept is an Entity Thing.
     *
     * @return An Entity if the Concept is a Thing
     */
    @CheckReturnValue
    default Entity asEntity() {
        throw GraknConceptException.invalidCasting(this, Entity.class);
    }

    /**
     * Return as a Relation if the Concept is a Relation Thing.
     *
     * @return A Relation  if the Concept is a Relation
     */
    @CheckReturnValue
    default Relation asRelation() {
        throw GraknConceptException.invalidCasting(this, Relation.class);
    }

    /**
     * Return as a Attribute  if the Concept is a Attribute Thing.
     *
     * @return A Attribute if the Concept is a Attribute
     */
    @CheckReturnValue
    default <D> Attribute<D> asAttribute() {
        throw GraknConceptException.invalidCasting(this, Attribute.class);
    }

    /**
     * Return as a Attribute  if the Concept is a Attribute Thing.
     *
     * @return A Attribute if the Concept is a Attribute
     */
    @CheckReturnValue
    default <D> Attribute<D> asAttribute(ValueType<D> valueType) {
        throw GraknConceptException.invalidCasting(this, Attribute.class);
    }

    /**
     * Return as a MetaType if the Concept is a MetaType.
     *
     * @return A MetaType if the Concept is a MetaType
     */
    @CheckReturnValue
    default MetaType<?, ?> asMetaType() {
        throw GraknConceptException.invalidCasting(this, MetaType.class);
    }

    /**
     * Return a RemoteConcept for this Concept.
     *
     * @param tx The transaction to use for the RPCs.
     * @return A remote concept using the given transaction to enable RPCs.
     */
    Remote asRemote(GraknClient.Transaction tx);

    /**
     * Determine if the Concept is a SchemaConcept
     *
     * @return true if theConcept concept is a SchemaConcept
     */
    @CheckReturnValue
    default boolean isSchemaConcept() {
        return false;
    }

    /**
     * Determine if the Concept is a Type.
     *
     * @return true if theConcept concept is a Type
     */
    @CheckReturnValue
    default boolean isType() {
        return false;
    }

    /**
     * Determine if the Concept is an Thing.
     *
     * @return true if the Concept is an Thing
     */
    @CheckReturnValue
    default boolean isThing() {
        return false;
    }

    /**
     * Determine if the Concept is an EntityType.
     *
     * @return true if the Concept is an EntityType.
     */
    @CheckReturnValue
    default boolean isEntityType() {
        return false;
    }

    /**
     * Determine if the Concept is a Role.
     *
     * @return true if the Concept is a Role
     */
    @CheckReturnValue
    default boolean isRole() {
        return false;
    }

    /**
     * Determine if the Concept is a RelationType.
     *
     * @return true if the Concept is a RelationType
     */
    @CheckReturnValue
    default boolean isRelationType() {
        return false;
    }

    /**
     * Determine if the Concept is a AttributeType.
     *
     * @return true if theConcept concept is a AttributeType
     */
    @CheckReturnValue
    default boolean isAttributeType() {
        return false;
    }

    /**
     * Determine if the Concept is a Rule.
     *
     * @return true if the Concept is a Rule
     */
    @CheckReturnValue
    default boolean isRule() {
        return false;
    }

    /**
     * Determine if the Concept is an Entity.
     *
     * @return true if the Concept is a Entity
     */
    @CheckReturnValue
    default boolean isEntity() {
        return false;
    }

    /**
     * Determine if the Concept is a Relation.
     *
     * @return true if the Concept is a Relation
     */
    @CheckReturnValue
    default boolean isRelation() {
        return false;
    }

    /**
     * Determine if the Concept is a Attribute.
     *
     * @return true if the Concept is a Attribute
     */
    @CheckReturnValue
    default boolean isAttribute() {
        return false;
    }

    /**
     * Determine if the Concept is a MetaType.
     *
     * @return true if the Concept is a MetaType.
     */
    default boolean isMetaType() {
        return false;
    }

    /**
     * Determine if the Concept is remote.
     *
     * @return true if the Concept is remote.
     */
    default boolean isRemote() {
        return false;
    }

    interface Local<BaseType extends Concept<BaseType>> extends Concept<BaseType> {

        @SuppressWarnings("unchecked")
        static <BaseType extends Concept<? extends BaseType>>
        BaseType of(ConceptProto.Concept concept) {
            switch (concept.getBaseType()) {
                case ENTITY:
                    return (BaseType) new EntityImpl.Local(concept);
                case RELATION:
                    return (BaseType) new RelationImpl.Local(concept);
                case ATTRIBUTE:
                    return (BaseType) new AttributeImpl.Local<>(concept);
                case ENTITY_TYPE:
                    return (BaseType) new EntityTypeImpl.Local(concept);
                case RELATION_TYPE:
                    return (BaseType) new RelationTypeImpl.Local(concept);
                case ATTRIBUTE_TYPE:
                    return (BaseType) new AttributeTypeImpl.Local<>(concept);
                case ROLE:
                    return (BaseType) new RoleTypeImpl.Local(concept);
                case RULE:
                    return (BaseType) new RuleImpl.Local(concept);
                case META_TYPE:
                    return (BaseType) new MetaTypeImpl.Local<>(concept);
                default:
                case UNRECOGNIZED:
                    throw new IllegalArgumentException("Unrecognised " + concept);
            }
        }
    }

    /**
     * The base remote concept implementation.
     *
     * Provides the basic RPCs to delete a concept and check if it is deleted.
     */
    interface Remote<BaseType extends Concept<BaseType>>
            extends Concept<BaseType> {

        @SuppressWarnings("unchecked")
        static <RemoteType extends Remote<BaseType>, BaseType extends Concept<BaseType>>
        RemoteType of(GraknClient.Transaction tx, ConceptProto.Concept concept) {
            ConceptIID iid = ConceptIID.of(concept.getIid());
            switch (concept.getBaseType()) {
                case ENTITY:
                    return (RemoteType) new EntityImpl.Remote(tx, id);
                case RELATION:
                    return (RemoteType) new RelationImpl.Remote(tx, id);
                case ATTRIBUTE:
                    return (RemoteType) new AttributeImpl.Remote<>(tx, id);
                case ENTITY_TYPE:
                    return (RemoteType) new EntityTypeImpl.Remote(tx, id);
                case RELATION_TYPE:
                    return (RemoteType) new RelationTypeImpl.Remote(tx, id);
                case ATTRIBUTE_TYPE:
                    return (RemoteType) new AttributeTypeImpl.Remote<>(tx, id);
                case ROLE:
                    return (RemoteType) new RoleTypeImpl.Remote(tx, id);
                case RULE:
                    return (RemoteType) new RuleImpl.Remote(tx, id);
                case META_TYPE:
                    return (RemoteType) new MetaTypeImpl.Remote<>(tx, id);
                default:
                case UNRECOGNIZED:
                    throw new IllegalArgumentException("Unrecognised " + concept);
            }
        }

        //------------------------------------- Other ---------------------------------

        /**
         * Return as a SchemaConcept if the Concept is a SchemaConcept.
         *
         * @return A SchemaConcept if the Concept is a SchemaConcept
         */
        @Override
        @CheckReturnValue
        default Type.Remote<?> asSchemaConcept() {
            throw GraknConceptException.invalidCasting(this, Type.Remote.class);
        }

        /**
         * Return as a Type if the Concept is a Type.
         *
         * @return A Type if the Concept is a Type
         */
        @Override
        @CheckReturnValue
        default ThingType.Remote<?, ?> asType() {
            throw GraknConceptException.invalidCasting(this, ThingType.Remote.class);
        }

        /**
         * Return as an Thing if the Concept is an Thing.
         *
         * @return An Thing if the Concept is an Thing
         */
        @Override
        @CheckReturnValue
        default Thing.Remote<?, ?> asThing() {
            throw GraknConceptException.invalidCasting(this, Thing.Remote.class);
        }

        /**
         * Return as an EntityType if the Concept is an EntityType.
         *
         * @return A EntityType if the Concept is an EntityType
         */
        @Override
        @CheckReturnValue
        default EntityType.Remote asEntityType() {
            throw GraknConceptException.invalidCasting(this, EntityType.Remote.class);
        }

        /**
         * Return as a Role if the Concept is a Role.
         *
         * @return A Role if the Concept is a Role
         */
        @Override
        @CheckReturnValue
        default RoleType.Remote asRole() {
            throw GraknConceptException.invalidCasting(this, RoleType.Remote.class);
        }

        /**
         * Return as a RelationType if the Concept is a RelationType.
         *
         * @return A RelationType if the Concept is a RelationType
         */
        @Override
        @CheckReturnValue
        default RelationType.Remote asRelationType() {
            throw GraknConceptException.invalidCasting(this, RelationType.Remote.class);
        }

        /**
         * Return as a AttributeType if the Concept is a AttributeType
         *
         * @return A AttributeType if the Concept is a AttributeType
         */
        @Override
        @CheckReturnValue
        default <D> AttributeType.Remote<D> asAttributeType() {
            throw GraknConceptException.invalidCasting(this, AttributeType.class);
        }

        /**
         * Return as a AttributeType if the Concept is a AttributeType
         *
         * @return A AttributeType if the Concept is a AttributeType
         */
        @Override
        @CheckReturnValue
        default <D> AttributeType.Remote<D> asAttributeType(ValueType<D> valueType) {
            throw GraknConceptException.invalidCasting(this, AttributeType.class);
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

        /**
         * Return as an Entity, if the Concept is an Entity Thing.
         *
         * @return An Entity if the Concept is a Thing
         */
        @Override
        @CheckReturnValue
        default Entity.Remote asEntity() {
            throw GraknConceptException.invalidCasting(this, Entity.Remote.class);
        }

        /**
         * Return as a Relation if the Concept is a Relation Thing.
         *
         * @return A Relation  if the Concept is a Relation
         */
        @Override
        @CheckReturnValue
        default Relation.Remote asRelation() {
            throw GraknConceptException.invalidCasting(this, Relation.Remote.class);
        }

        /**
         * Return as a Attribute  if the Concept is a Attribute Thing.
         *
         * @return A Attribute if the Concept is a Attribute
         */
        @Override
        @CheckReturnValue
        default <D> Attribute.Remote<D> asAttribute() {
            throw GraknConceptException.invalidCasting(this, Attribute.Remote.class);
        }

        /**
         * Return as a Attribute  if the Concept is a Attribute Thing.
         *
         * @return A Attribute if the Concept is a Attribute
         */
        @Override
        @CheckReturnValue
        default <D> Attribute.Remote<D> asAttribute(ValueType<D> valueType) {
            throw GraknConceptException.invalidCasting(this, Attribute.Remote.class);
        }

        default MetaType.Remote<?, ?> asMetaType() {
            throw GraknConceptException.invalidCasting(this, MetaType.Remote.class);
        }

        @Override
        default boolean isRemote() {
            return true;
        }

        /**
         * Delete the Concepts
         */
        void delete();

        /**
         * Return whether the concept has been deleted.
         */
        boolean isDeleted();
    }
}