package grakn.client.concept;

import grakn.protocol.session.ConceptProto;

abstract class UserTypeImpl<
        SomeType extends UserType<SomeType, SomeThing>,
        SomeThing extends Thing<SomeThing, SomeType>>
        extends TypeImpl<SomeType, SomeThing> implements UserType<SomeType, SomeThing> {

    UserTypeImpl(ConceptProto.Concept concept) {
        super(concept);
    }

    protected abstract SomeThing asInstance(Concept<SomeThing> concept);
}
