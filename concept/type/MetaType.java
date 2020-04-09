package grakn.client.concept.type;

import grakn.client.GraknClient;
import grakn.client.concept.ConceptId;
import grakn.client.concept.remote.RemoteMetaTypeImpl;
import grakn.client.concept.thing.Thing;

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

    @Override
    default Remote<?, ?> asRemote(GraknClient.Transaction tx) {
        return MetaType.Remote.of(tx, id());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isMetaType() {
        return true;
    }

    interface Local<
            SomeType extends Local<SomeType, SomeThing>,
            SomeThing extends Thing.Local<SomeThing, SomeType>>
            extends Type.Local<SomeType, SomeThing>, MetaType<SomeType, SomeThing> {
    }

    /**
     * Type Class of a MetaType
     */
    interface Remote<
            SomeRemoteType extends Remote<SomeRemoteType, SomeRemoteThing>,
            SomeRemoteThing extends Thing.Remote<SomeRemoteThing, SomeRemoteType>>
        extends MetaType<SomeRemoteType, SomeRemoteThing>,
            Type.Remote<SomeRemoteType, SomeRemoteThing> {

        static <SomeRemoteType extends Remote<SomeRemoteType, SomeRemoteThing>,
                SomeRemoteThing extends Thing.Remote<SomeRemoteThing, SomeRemoteType>>
        MetaType.Remote<SomeRemoteType, SomeRemoteThing> of(GraknClient.Transaction tx, ConceptId id) {
            return new RemoteMetaTypeImpl<>(tx, id);
        }
    }
}
