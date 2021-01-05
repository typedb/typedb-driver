package grakn.client.concept.answer;

import grakn.client.concept.Concept;
import grakn.client.concept.impl.ConceptImpl;
import grakn.protocol.AnswerProto;

public class NumericGroup {
    private final Concept owner;
    private final Numeric numeric;

    private NumericGroup(Concept owner, Numeric numeric) {
        this.owner = owner;
        this.numeric = numeric;
    }

    public static NumericGroup of(AnswerProto.NumericGroup numericGroup) {
        return new NumericGroup(ConceptImpl.of(numericGroup.getOwner()), Numeric.of(numericGroup.getNumber()));
    }

    public Concept owner() {
        return this.owner;
    }

    public Numeric numeric() {
        return this.numeric;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NumericGroup a2 = (NumericGroup) obj;
        return this.owner.equals(a2.owner) &&
                this.numeric.equals(a2.numeric);
    }

    @Override
    public int hashCode() {
        int hash = owner.hashCode();
        hash = 31 * hash + numeric.hashCode();

        return hash;
    }
}
