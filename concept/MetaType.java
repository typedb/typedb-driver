package grakn.client.concept;

import grakn.client.GraknClient;
import grakn.client.concept.remote.RemoteMetaType;
import grakn.client.concept.remote.RemoteThing;
import grakn.client.concept.remote.RemoteType;

import javax.annotation.CheckReturnValue;

public interface MetaType<
        SomeType extends MetaType<SomeType, SomeThing, SomeRemoteType, SomeRemoteThing>,
        SomeThing extends Thing<SomeThing, SomeType, SomeRemoteThing, SomeRemoteType>,
        SomeRemoteType extends RemoteMetaType<SomeRemoteType, SomeRemoteThing, SomeType, SomeThing>,
        SomeRemoteThing extends RemoteThing<SomeRemoteThing, SomeRemoteType, SomeThing, SomeType>>
        extends Type<SomeType, SomeThing, SomeRemoteType, SomeRemoteThing> {
    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default MetaType<SomeType, SomeThing, SomeRemoteType, SomeRemoteThing> asMetaType() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    default SomeRemoteType asRemote(GraknClient.Transaction tx) {
        return (SomeRemoteType) RemoteMetaType.of(tx, id());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isMetaType() {
        return true;
    }
}
