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

package grakn.client.concept.remote;

import grakn.client.GraknClient;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.GraknConceptException;
import grakn.protocol.session.ConceptProto;

import javax.annotation.CheckReturnValue;


/**
 * The base remote concept implementation.
 *
 * Provides the basic RPCs to delete a concept and check if it is deleted.
 */
public interface RemoteConcept<
        RemoteConceptType extends RemoteConcept<RemoteConceptType, ConceptType>,
        ConceptType extends Concept<ConceptType, RemoteConceptType>>
        extends Concept<ConceptType, RemoteConceptType> {

    @SuppressWarnings("unchecked")
    static <RemoteConceptType extends RemoteConcept<RemoteConceptType, ConceptType>,
            ConceptType extends Concept<ConceptType, RemoteConceptType>> RemoteConceptType
    of(ConceptProto.Concept concept, GraknClient.Transaction tx) {
        ConceptId id = ConceptId.of(concept.getId());
        switch (concept.getBaseType()) {
            case ENTITY:
                return (RemoteConceptType) new RemoteEntityImpl(tx, id);
            case RELATION:
                return (RemoteConceptType) new RemoteRelationImpl(tx, id);
            case ATTRIBUTE:
                return (RemoteConceptType) new RemoteAttributeImpl<>(tx, id);
            case ENTITY_TYPE:
                return (RemoteConceptType) new RemoteEntityTypeImpl(tx, id);
            case RELATION_TYPE:
                return (RemoteConceptType) new RemoteRelationTypeImpl(tx, id);
            case ATTRIBUTE_TYPE:
                return (RemoteConceptType) new RemoteAttributeTypeImpl<>(tx, id);
            case ROLE:
                return (RemoteConceptType) new RemoteRoleImpl(tx, id);
            case RULE:
                return (RemoteConceptType) new RemoteRuleImpl(tx, id);
            case META_TYPE:
                return (RemoteConceptType) new RemoteMetaTypeImpl<>(tx, id);
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
    default RemoteSchemaConcept<?, ?> asSchemaConcept() {
        throw GraknConceptException.invalidCasting(this, RemoteSchemaConcept.class);
    }

    /**
     * Return as a Type if the Concept is a Type.
     *
     * @return A Type if the Concept is a Type
     */
    @Override
    @CheckReturnValue
    default RemoteType<?, ?, ?, ?> asType() {
        throw GraknConceptException.invalidCasting(this, RemoteType.class);
    }

    /**
     * Return as an Thing if the Concept is an Thing.
     *
     * @return An Thing if the Concept is an Thing
     */
    @Override
    @CheckReturnValue
    default RemoteThing<?, ?, ?, ?> asThing() {
        throw GraknConceptException.invalidCasting(this, RemoteThing.class);
    }

    /**
     * Return as an EntityType if the Concept is an EntityType.
     *
     * @return A EntityType if the Concept is an EntityType
     */
    @Override
    @CheckReturnValue
    default RemoteEntityType asEntityType() {
        throw GraknConceptException.invalidCasting(this, RemoteEntityType.class);
    }

    /**
     * Return as a Role if the Concept is a Role.
     *
     * @return A Role if the Concept is a Role
     */
    @Override
    @CheckReturnValue
    default RemoteRole asRole() {
        throw GraknConceptException.invalidCasting(this, RemoteRole.class);
    }

    /**
     * Return as a RelationType if the Concept is a RelationType.
     *
     * @return A RelationType if the Concept is a RelationType
     */
    @Override
    @CheckReturnValue
    default RemoteRelationType asRelationType() {
        throw GraknConceptException.invalidCasting(this, RemoteRelationType.class);
    }

    /**
     * Return as a AttributeType if the Concept is a AttributeType
     *
     * @return A AttributeType if the Concept is a AttributeType
     */
    @Override
    @CheckReturnValue
    default <D> RemoteAttributeType<D> asAttributeType() {
        throw GraknConceptException.invalidCasting(this, RemoteAttributeType.class);
    }

    /**
     * Return as a Rule if the Concept is a Rule.
     *
     * @return A Rule if the Concept is a Rule
     */
    @Override
    @CheckReturnValue
    default RemoteRule asRule() {
        throw GraknConceptException.invalidCasting(this, RemoteRule.class);
    }

    /**
     * Return as an Entity, if the Concept is an Entity Thing.
     *
     * @return An Entity if the Concept is a Thing
     */
    @Override
    @CheckReturnValue
    default RemoteEntity asEntity() {
        throw GraknConceptException.invalidCasting(this, RemoteEntity.class);
    }

    /**
     * Return as a Relation if the Concept is a Relation Thing.
     *
     * @return A Relation  if the Concept is a Relation
     */
    @Override
    @CheckReturnValue
    default RemoteRelation asRelation() {
        throw GraknConceptException.invalidCasting(this, RemoteRelation.class);
    }

    /**
     * Return as a Attribute  if the Concept is a Attribute Thing.
     *
     * @return A Attribute if the Concept is a Attribute
     */
    @SuppressWarnings("unchecked")
    @Override
    @CheckReturnValue
    default <D> RemoteAttribute<D> asAttribute() {
        throw GraknConceptException.invalidCasting(this, RemoteAttribute.class);
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