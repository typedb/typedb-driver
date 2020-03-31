package grakn.client.concept.local;

import grakn.client.concept.Type;

public interface LocalType<
        SomeType extends LocalType<SomeType, SomeThing>,
        SomeThing extends LocalThing<SomeThing, SomeType>>
        extends LocalSchemaConcept<SomeType>, Type<SomeType, SomeThing> {
}
