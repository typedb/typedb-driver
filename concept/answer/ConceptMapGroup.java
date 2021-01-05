package grakn.client.concept.answer;

import grakn.client.concept.Concept;
import grakn.client.concept.impl.ConceptImpl;
import grakn.protocol.AnswerProto;

import java.util.List;
import java.util.stream.Collectors;

public class ConceptMapGroup {
    private final Concept owner;
    private final List<ConceptMap> answers;

    public ConceptMapGroup(Concept owner, List<ConceptMap> answers) {
        this.owner = owner;
        this.answers = answers;
    }

    public static ConceptMapGroup of(AnswerProto.ConceptMapGroup e) {
        Concept owner = ConceptImpl.of(e.getOwner());
        List<ConceptMap> conceptMaps = e.getConceptMapsList().stream().map(ConceptMap::of).collect(Collectors.toList());
        return new ConceptMapGroup(owner, conceptMaps);
    }

    public Concept owner() {
        return this.owner;
    }

    public List<ConceptMap> answers() {
        return this.answers;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConceptMapGroup a2 = (ConceptMapGroup) obj;
        return this.owner.equals(a2.owner) &&
                this.answers.equals(a2.answers);
    }

    @Override
    public int hashCode() {
        int hash = owner.hashCode();
        hash = 31 * hash + answers.hashCode();

        return hash;
    }
}
