package grakn.client.concept.type.impl;

import grakn.client.GraknClient;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.MetaType;
import grakn.client.concept.type.Type;
import grakn.protocol.session.ConceptProto;

public class MetaTypeImpl {
    public static class Local<
            SomeType extends Type<SomeType, SomeThing>,
            SomeThing extends Thing<SomeThing, SomeType>>
            extends TypeImpl.Local<SomeType, SomeThing>
            implements MetaType.Local<SomeType, SomeThing> {

        public Local(ConceptProto.Concept concept) {
            super(concept);
        }
    }

    /**
     * Client implementation of Type
     */
    public static class Remote<
            SomeRemoteType extends Type<SomeRemoteType, SomeRemoteThing>,
            SomeRemoteThing extends Thing<SomeRemoteThing, SomeRemoteType>>
            extends TypeImpl.Remote<SomeRemoteType, SomeRemoteThing>
            implements MetaType.Remote<SomeRemoteType, SomeRemoteThing> {

        public Remote(GraknClient.Transaction tx, ConceptId id) {
            super(tx, id);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Thing.Remote<SomeRemoteThing, SomeRemoteType> asInstance(Concept.Remote<?> concept) {
            return (Thing.Remote<SomeRemoteThing, SomeRemoteType>) concept.asThing();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected final MetaType.Remote<SomeRemoteType, SomeRemoteThing> asCurrentBaseType(Concept.Remote<?> other) {
            return (MetaType.Remote<SomeRemoteType, SomeRemoteThing>) other.asMetaType();
        }

        @Override
        protected boolean equalsCurrentBaseType(
                Concept.Remote<?> other) {
            return other.isMetaType();
        }
    }
}
