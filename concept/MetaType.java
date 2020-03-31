package grakn.client.concept;

import grakn.client.GraknClient;
import grakn.client.concept.remote.RemoteMetaType;
import grakn.client.concept.remote.RemoteThing;
import grakn.client.concept.remote.RemoteType;

import javax.annotation.CheckReturnValue;

public interface MetaType<
        SomeType extends MetaType<SomeType, SomeThing>,
        SomeThing extends Thing<SomeThing, SomeType>>
        extends Type<SomeType, SomeThing> {
    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default MetaType<SomeType, SomeThing> asMetaType() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    default RemoteMetaType<?, ?> asRemote(GraknClient.Transaction tx) {
        return RemoteMetaType.of(tx, id());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isMetaType() {
        return true;
    }
}
