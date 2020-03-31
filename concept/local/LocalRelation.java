package grakn.client.concept.local;

import grakn.client.concept.Relation;

public interface LocalRelation extends LocalThing<LocalRelation, LocalRelationType>,
        Relation<LocalRelation, LocalRelationType> {
}
