package grakn.client.concept.local;

import grakn.client.concept.Concept;
import grakn.protocol.session.ConceptProto;

public interface LocalConcept<ConceptType extends LocalConcept<ConceptType>> extends Concept<ConceptType> {

    @SuppressWarnings("unchecked")
    static <ConceptType extends LocalConcept<ConceptType>>
    ConceptType of(ConceptProto.Concept concept) {
        switch (concept.getBaseType()) {
            case ENTITY:
                return (ConceptType) new EntityImpl(concept);
            case RELATION:
                return (ConceptType) new RelationImpl(concept);
            case ATTRIBUTE:
                return (ConceptType) new AttributeImpl<>(concept);
            case ENTITY_TYPE:
                return (ConceptType) new EntityTypeImpl(concept);
            case RELATION_TYPE:
                return (ConceptType) new RelationTypeImpl(concept);
            case ATTRIBUTE_TYPE:
                return (ConceptType) new AttributeTypeImpl<>(concept);
            case ROLE:
                return (ConceptType) new RoleImpl(concept);
            case RULE:
                return (ConceptType) new RuleImpl(concept);
            case META_TYPE:
                return (ConceptType) new MetaTypeImpl<>(concept);
            default:
            case UNRECOGNIZED:
                throw new IllegalArgumentException("Unrecognised " + concept);
        }
    }
}
