package grakn.client.concept.local;

import grakn.client.concept.Thing;

public interface LocalThing<
        SomeThing extends LocalThing<SomeThing, SomeType>,
        SomeType extends LocalType<SomeType, SomeThing>>
        extends LocalConcept<SomeThing>, Thing<SomeThing, SomeType> {
}
