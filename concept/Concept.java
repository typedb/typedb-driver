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
import grakn.client.concept.thing.AttributeImpl;
import grakn.client.concept.type.AttributeTypeImpl;
import grakn.client.concept.thing.EntityImpl;
import grakn.client.concept.type.EntityTypeImpl;
import grakn.client.concept.type.MetaTypeImpl;
import grakn.client.concept.thing.RelationImpl;
import grakn.client.concept.type.RelationTypeImpl;
import grakn.client.concept.remote.RemoteAttributeImpl;
import grakn.client.concept.remote.RemoteAttributeTypeImpl;
import grakn.client.concept.remote.RemoteEntityImpl;
import grakn.client.concept.remote.RemoteEntityTypeImpl;
import grakn.client.concept.remote.RemoteMetaTypeImpl;
import grakn.client.concept.remote.RemoteRelationImpl;
import grakn.client.concept.remote.RemoteRelationTypeImpl;
import grakn.client.concept.remote.RemoteRoleImpl;
import grakn.client.concept.remote.RemoteRuleImpl;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Entity;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.MetaType;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.Type;
import grakn.protocol.session.ConceptProto;

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
     * Get the unique ID associated with the Concept.
     *
     * @return A value the concept's unique id.
     */
    @CheckReturnValue
    ConceptId id();

    //------------------------------------- Other ---------------------------------

    /**
     * Return as a SchemaConcept if the Concept is a SchemaConcept.
     *
     * @return A SchemaConcept if the Concept is a SchemaConcept
     */
    @CheckReturnValue
    default SchemaConcept<?> asSchemaConcept() {
        throw GraknConceptException.invalidCasting(this, SchemaConcept.class);
    }

    /**
     * Return as a Type if the Concept is a Type.
     *
     * @return A Type if the Concept is a Type
     */
    @CheckReturnValue
    default Type<?, ?> asType() {
        throw GraknConceptException.invalidCasting(this, Type.class);
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
    default Role asRole() {
        throw GraknConceptException.invalidCasting(this, Role.class);
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
    default AttributeType<?> asAttributeType() {
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
    default Attribute<?> asAttribute() {
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
                    return (BaseType) new EntityImpl(concept);
                case RELATION:
                    return (BaseType) new RelationImpl(concept);
                case ATTRIBUTE:
                    return (BaseType) new AttributeImpl<>(concept);
                case ENTITY_TYPE:
                    return (BaseType) new EntityTypeImpl(concept);
                case RELATION_TYPE:
                    return (BaseType) new RelationTypeImpl(concept);
                case ATTRIBUTE_TYPE:
                    return (BaseType) new AttributeTypeImpl<>(concept);
                case ROLE:
                    return (BaseType) new RoleImpl(concept);
                case RULE:
                    return (BaseType) new RuleImpl(concept);
                case META_TYPE:
                    return (BaseType) new MetaTypeImpl<>(concept);
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
        RemoteType of(ConceptProto.Concept concept, GraknClient.Transaction tx) {
            ConceptId id = ConceptId.of(concept.getId());
            switch (concept.getBaseType()) {
                case ENTITY:
                    return (RemoteType) new RemoteEntityImpl(tx, id);
                case RELATION:
                    return (RemoteType) new RemoteRelationImpl(tx, id);
                case ATTRIBUTE:
                    return (RemoteType) new RemoteAttributeImpl<>(tx, id);
                case ENTITY_TYPE:
                    return (RemoteType) new RemoteEntityTypeImpl(tx, id);
                case RELATION_TYPE:
                    return (RemoteType) new RemoteRelationTypeImpl(tx, id);
                case ATTRIBUTE_TYPE:
                    return (RemoteType) new RemoteAttributeTypeImpl<>(tx, id);
                case ROLE:
                    return (RemoteType) new RemoteRoleImpl(tx, id);
                case RULE:
                    return (RemoteType) new RemoteRuleImpl(tx, id);
                case META_TYPE:
                    return (RemoteType) new RemoteMetaTypeImpl<>(tx, id);
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
        default SchemaConcept.Remote<?> asSchemaConcept() {
            throw GraknConceptException.invalidCasting(this, SchemaConcept.Remote.class);
        }

        /**
         * Return as a Type if the Concept is a Type.
         *
         * @return A Type if the Concept is a Type
         */
        @Override
        @CheckReturnValue
        default Type.Remote<?, ?> asType() {
            throw GraknConceptException.invalidCasting(this, Type.Remote.class);
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
        default Role.Remote asRole() {
            throw GraknConceptException.invalidCasting(this, Role.Remote.class);
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
        default AttributeType.Remote<?> asAttributeType() {
            throw GraknConceptException.invalidCasting(this, AttributeType.Remote.class);
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
        default Attribute.Remote<?> asAttribute() {
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