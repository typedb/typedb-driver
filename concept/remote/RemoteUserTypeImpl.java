package grakn.client.concept.remote;

import grakn.client.GraknClient;
import grakn.client.concept.ConceptId;
import grakn.client.concept.Thing;
import grakn.client.concept.UserType;

abstract class RemoteUserTypeImpl<
        SomeRemoteType extends RemoteUserType<SomeRemoteType, SomeRemoteThing, SomeType, SomeThing>,
        SomeRemoteThing extends RemoteThing<SomeRemoteThing, SomeRemoteType, SomeThing, SomeType>,
        SomeType extends UserType<SomeType, SomeThing>, SomeThing extends Thing<SomeThing, SomeType>>
        extends RemoteTypeImpl<SomeRemoteType, SomeRemoteThing, SomeType, SomeThing>
        implements UserType<SomeType, SomeThing> {

    RemoteUserTypeImpl(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }
}
