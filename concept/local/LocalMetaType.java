package grakn.client.concept.local;

import grakn.client.concept.MetaType;

public interface LocalMetaType<
        SomeType extends LocalMetaType<SomeType, SomeThing>,
        SomeThing extends LocalThing<SomeThing, SomeType>>
        extends LocalType<SomeType, SomeThing>, MetaType<SomeType, SomeThing> {
}
