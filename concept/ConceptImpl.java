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
import grakn.client.concept.api.ConceptId;
import grakn.client.concept.api.GraknConceptException;
import grakn.protocol.session.ConceptProto;

import javax.annotation.CheckReturnValue;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Client implementation of Concept
 *
 * @param <SomeConcept> represents the actual class of object to downcast to
 */
public abstract class ConceptImpl<SomeConcept extends ConceptImpl> {

    private final GraknClient.Transaction tx;
    private final ConceptId id;

    ConceptImpl(GraknClient.Transaction tx, ConceptId id) {
        if (tx == null) {
            throw new NullPointerException("Null tx");
        }
        this.tx = tx;
        if (id == null) {
            throw new NullPointerException("Null id");
        }
        this.id = id;
    }

    public static ConceptImpl of(ConceptProto.Concept concept, GraknClient.Transaction tx) {
        ConceptId id = ConceptId.of(concept.getId());

        switch (concept.getBaseType()) {
            case ENTITY:
                return new EntityImpl(tx, id);
            case RELATION:
                return new RelationImpl(tx, id);
            case ATTRIBUTE:
                return new AttributeImpl(tx, id);
            case ENTITY_TYPE:
                return new EntityTypeImpl(tx, id);
            case RELATION_TYPE:
                return new RelationType(tx, id);
            case ATTRIBUTE_TYPE:
                return new AttributeTypeImpl(tx, id);
            case ROLE:
                return new Role(tx, id);
            case RULE:
                return new Rule(tx, id);
            case META_TYPE:
                return new MetaType(tx, id);
            default:
            case UNRECOGNIZED:
                throw new IllegalArgumentException("Unrecognised " + concept);
        }
    }


    public ConceptId id() {
        return id;
    }

    public final void delete() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setConceptDeleteReq(ConceptProto.Concept.Delete.Req.getDefaultInstance())
                .build();

        runMethod(method);
    }

    public final boolean isDeleted() {
        return tx().getConcept(id()) == null;
    }

    /**
     * Return as a SchemaConcept if the Concept is a SchemaConcept.
     *
     * @return A SchemaConcept if the Concept is a SchemaConcept
     */
    @CheckReturnValue
    public SchemaConcept asSchemaConcept() {
        throw GraknConceptException.invalidCasting(this, SchemaConcept.class);
    }

    /**
     * Return as a Type if the Concept is a Type.
     *
     * @return A Type if the Concept is a Type
     */
    @CheckReturnValue
    public TypeImpl asType() {
        throw GraknConceptException.invalidCasting(this, TypeImpl.class);
    }

    /**
     * Return as an Thing if the Concept is an Thing.
     *
     * @return An Thing if the Concept is an Thing
     */
    @CheckReturnValue
    public Thing asThing() {
        throw GraknConceptException.invalidCasting(this, Thing.class);
    }

    /**
     * Return as an EntityType if the Concept is an EntityType.
     *
     * @return A EntityType if the Concept is an EntityType
     */
    @CheckReturnValue
    public EntityTypeImpl asEntityType() {
        throw GraknConceptException.invalidCasting(this, EntityTypeImpl.class);
    }

    /**
     * Return as a Role if the Concept is a Role.
     *
     * @return A Role if the Concept is a Role
     */
    @CheckReturnValue
    public Role asRole() {
        throw GraknConceptException.invalidCasting(this, Role.class);
    }

    /**
     * Return as a RelationType if the Concept is a RelationType.
     *
     * @return A RelationType if the Concept is a RelationType
     */
    @CheckReturnValue
    public RelationType asRelationType() {
        throw GraknConceptException.invalidCasting(this, RelationType.class);
    }

    /**
     * Return as a AttributeType if the Concept is a AttributeType
     *
     * @return A AttributeType if the Concept is a AttributeType
     */
    @CheckReturnValue
    public <D> AttributeTypeImpl<D> asAttributeType() {
        throw GraknConceptException.invalidCasting(this, AttributeTypeImpl.class);
    }

    /**
     * Return as a Rule if the Concept is a Rule.
     *
     * @return A Rule if the Concept is a Rule
     */
    @CheckReturnValue
    public Rule asRule() {
        throw GraknConceptException.invalidCasting(this, Rule.class);
    }

    /**
     * Return as an Entity, if the Concept is an Entity Thing.
     *
     * @return An Entity if the Concept is a Thing
     */
    @CheckReturnValue
    public EntityImpl asEntity() {
        throw GraknConceptException.invalidCasting(this, EntityImpl.class);
    }

    /**
     * Return as a Relation if the Concept is a Relation Thing.
     *
     * @return A Relation  if the Concept is a Relation
     */
    @CheckReturnValue
    public RelationImpl asRelation() {
        throw GraknConceptException.invalidCasting(this, RelationImpl.class);
    }

    /**
     * Return as a Attribute  if the Concept is a Attribute Thing.
     *
     * @return A Attribute if the Concept is a Attribute
     */
    @CheckReturnValue
    public <D> AttributeImpl<D> asAttribute() {
        throw GraknConceptException.invalidCasting(this, AttributeImpl.class);
    }

    /**
     * Determine if the Concept is a SchemaConcept
     *
     * @return true if theConcept concept is a SchemaConcept
     */
    @CheckReturnValue
    public boolean isSchemaConcept() {
        return false;
    }

    /**
     * Determine if the Concept is a Type.
     *
     * @return true if theConcept concept is a Type
     */
    @CheckReturnValue
    public boolean isType() {
        return false;
    }

    /**
     * Determine if the Concept is an Thing.
     *
     * @return true if the Concept is an Thing
     */
    @CheckReturnValue
    public boolean isThing() {
        return false;
    }

    /**
     * Determine if the Concept is an EntityType.
     *
     * @return true if the Concept is an EntityType.
     */
    @CheckReturnValue
    public boolean isEntityType() {
        return false;
    }

    /**
     * Determine if the Concept is a Role.
     *
     * @return true if the Concept is a Role
     */
    @CheckReturnValue
    public boolean isRole() {
        return false;
    }

    /**
     * Determine if the Concept is a RelationType.
     *
     * @return true if the Concept is a RelationType
     */
    @CheckReturnValue
    public boolean isRelationType() {
        return false;
    }

    /**
     * Determine if the Concept is a AttributeType.
     *
     * @return true if theConcept concept is a AttributeType
     */
    @CheckReturnValue
    public boolean isAttributeType() {
        return false;
    }

    /**
     * Determine if the Concept is a Rule.
     *
     * @return true if the Concept is a Rule
     */
    @CheckReturnValue
    public boolean isRule() {
        return false;
    }

    /**
     * Determine if the Concept is an Entity.
     *
     * @return true if the Concept is a Entity
     */
    @CheckReturnValue
    public boolean isEntity() {
        return false;
    }

    /**
     * Determine if the Concept is a Relation.
     *
     * @return true if the Concept is a Relation
     */
    @CheckReturnValue
    public boolean isRelation() {
        return false;
    }

    /**
     * Determine if the Concept is a Attribute.
     *
     * @return true if the Concept is a Attribute
     */
    @CheckReturnValue
    public boolean isAttribute() {
        return false;
    }


    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{tx=" + tx + ", id=" + id + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConceptImpl<?> that = (ConceptImpl<?>) o;

        return (tx.equals(that.tx())) &&
                id.equals(that.id());
    }

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= tx.hashCode();
        h *= 1000003;
        h ^= id.hashCode();
        return h;
    }

    GraknClient.Transaction tx() {
        return tx;
    }
    abstract SomeConcept asCurrentBaseType(ConceptImpl other);

    final Stream<? extends ConceptImpl> conceptStream
            (int iteratorId, Function<ConceptProto.Method.Iter.Res, ConceptProto.Concept> conceptGetter) {

        Iterable<? extends ConceptImpl> iterable = () -> tx().iterator(
                iteratorId, res -> of(conceptGetter.apply(res.getConceptMethodIterRes()), tx())
        );

        return StreamSupport.stream(iterable.spliterator(), false);
    }

    protected final ConceptProto.Method.Res runMethod(ConceptProto.Method.Req method) {
        return runMethod(id(), method);
    }

    protected final ConceptProto.Method.Res runMethod(ConceptId id, ConceptProto.Method.Req method) {
        return tx().runConceptMethod(id, method).getConceptMethodRes().getResponse();
    }

}
