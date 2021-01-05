package grakn.client.concept.answer;

import grakn.client.concept.Concept;
import grakn.client.concept.impl.ConceptImpl;
import grakn.protocol.AnswerProto;

import java.util.List;
import java.util.stream.Collectors;

public class NumberGroup {
    private final Concept owner;
    private final List<String> answers;

    public NumberGroup(Concept owner, List<String> answers) {
        this.owner = owner;
        this.answers = answers;
    }

    public static NumberGroup of(AnswerProto.NumberGroup e) {
        Concept owner = ConceptImpl.of(e.getOwner());
        List<String> conceptMaps = e.getNumbersList().stream().map(AnswerProto.Number::getValue).collect(Collectors.toList());
        return new NumberGroup(owner, conceptMaps);
    }

    public Concept owner() {
        return this.owner;
    }

    public List<String> answers() {
        return this.answers;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NumberGroup a2 = (NumberGroup) obj;
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
