package grakn.client.concept;

import grakn.client.GraknClient;
import grakn.client.concept.remote.RemoteMetaType;

import javax.annotation.CheckReturnValue;

public interface MetaType<
        SomeType extends Type<SomeType, SomeThing>,
        SomeThing extends Thing<SomeThing, SomeType>>
        extends Type<SomeType, SomeThing> {
    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default MetaType<SomeType, SomeThing> asMetaType() {
        return this;
    }

    @Override
    default RemoteMetaType asRemote(GraknClient.Transaction tx) {
        return RemoteMetaType.of(tx, id());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isMetaType() {
        return true;
    }
}
