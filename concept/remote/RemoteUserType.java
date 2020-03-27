package grakn.client.concept.remote;

import grakn.client.concept.UserType;
import grakn.client.concept.Thing;

public interface RemoteUserType<
        SomeRemoteType extends RemoteUserType<SomeRemoteType, SomeRemoteThing, SomeType, SomeThing>,
        SomeRemoteThing extends RemoteThing<SomeRemoteThing, SomeRemoteType, SomeThing, SomeType>,
        SomeType extends UserType<SomeType, SomeThing>, SomeThing extends Thing<SomeThing, SomeType>>
        extends UserType<SomeType, SomeThing>, RemoteType<SomeRemoteType, SomeRemoteThing, SomeType, SomeThing> {

    /**
     * Creates and returns a new instance, whose direct type will be this type.
     *
     * @return a new empty instance.
     */
    SomeRemoteThing create();

    /**
     * Sets the supertype of the Type to be the Type specified.
     *
     * @param type The supertype of this Type
     * @return This type itself.
     */
    SomeRemoteType sup(SomeType type);
}
