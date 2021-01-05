package grakn.client.concept.answer;

import grakn.client.concept.Concept;
import grakn.client.concept.impl.ConceptImpl;
import grakn.protocol.AnswerProto;

import java.util.List;
import java.util.stream.Collectors;

public class ConceptMapGroup {
    private final Concept owner;
    private final List<ConceptMap> conceptMaps;

    public ConceptMapGroup(Concept owner, List<ConceptMap> conceptMaps) {
        this.owner = owner;
        this.conceptMaps = conceptMaps;
    }

    public static ConceptMapGroup of(AnswerProto.ConceptMapGroup e) {
        Concept owner = ConceptImpl.of(e.getOwner());
        List<ConceptMap> conceptMaps = e.getConceptMapsList().stream().map(ConceptMap::of).collect(Collectors.toList());
        return new ConceptMapGroup(owner, conceptMaps);
    }

    public Concept owner() {
        return this.owner;
    }

    public List<ConceptMap> conceptMaps() {
        return this.conceptMaps;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConceptMapGroup a2 = (ConceptMapGroup) obj;
        return this.owner.equals(a2.owner) &&
                this.conceptMaps.equals(a2.conceptMaps);
    }

    @Override
    public int hashCode() {
        int hash = owner.hashCode();
        hash = 31 * hash + conceptMaps.hashCode();

        return hash;
    }
}
