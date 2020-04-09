package grakn.client.concept.type;

import grakn.client.GraknClient;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.Label;
import grakn.client.concept.Role;
import grakn.client.concept.SchemaConceptImpl;
import grakn.client.concept.thing.Thing;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.session.ConceptProto;

import java.util.stream.Stream;

public abstract class TypeImpl {
    /**
     * Client implementation of Type
     *
     * @param <SomeType>  The exact type of this class
     */
    public abstract static class Local<
            SomeType extends Type<SomeType, SomeThing>,
            SomeThing extends Thing<SomeThing, SomeType>>
            extends SchemaConceptImpl.Local<SomeType>
            implements Type.Local<SomeType, SomeThing> {

        protected Local(ConceptProto.Concept concept) {
            super(concept);
        }
    }

    /**
     * Client implementation of Type
     *
     * @param <SomeRemoteType>  The exact type of this class
     * @param <SomeRemoteThing> the exact type of instances of this class
     */
    public abstract static class Remote<
            SomeRemoteType extends Type<SomeRemoteType, SomeRemoteThing>,
            SomeRemoteThing extends Thing<SomeRemoteThing, SomeRemoteType>>
            extends SchemaConceptImpl.Remote<SomeRemoteType>
            implements Type.Remote<SomeRemoteType, SomeRemoteThing> {

        protected Remote(GraknClient.Transaction tx, ConceptId id) {
            super(tx, id);
        }

        @Override
        public Type.Remote<SomeRemoteType, SomeRemoteThing> label(Label label) {
            return (Type.Remote<SomeRemoteType, SomeRemoteThing>) super.label(label);
        }

        @Override
        public Stream<? extends Type.Remote<SomeRemoteType, SomeRemoteThing>> sups() {
            return super.sups().map(this::asCurrentBaseType);
        }

        @Override
        public Stream<? extends Type.Remote<SomeRemoteType, SomeRemoteThing>> subs() {
            return super.subs().map(this::asCurrentBaseType);
        }

        @Override
        public Stream<? extends Thing.Remote<SomeRemoteThing, SomeRemoteType>> instances() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setTypeInstancesIterReq(ConceptProto.Type.Instances.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getTypeInstancesIterRes().getThing()).map(this::asInstance);
        }

        @Override
        public final Boolean isAbstract() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypeIsAbstractReq(ConceptProto.Type.IsAbstract.Req.getDefaultInstance()).build();

            return runMethod(method).getTypeIsAbstractRes().getAbstract();
        }

        @Override
        public Type.Remote<SomeRemoteType, SomeRemoteThing> isAbstract(Boolean isAbstract) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypeSetAbstractReq(ConceptProto.Type.SetAbstract.Req.newBuilder()
                                                   .setAbstract(isAbstract)).build();

            runMethod(method);
            return this;
        }

        @Override
        public final Stream<AttributeType.Remote<?>> keys() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setTypeKeysIterReq(ConceptProto.Type.Keys.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getTypeKeysIterRes().getAttributeType()).map(Concept.Remote::asAttributeType);
        }

        @Override
        public final Stream<AttributeType.Remote<?>> attributes() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setTypeAttributesIterReq(ConceptProto.Type.Attributes.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getTypeAttributesIterRes().getAttributeType()).map(Concept.Remote::asAttributeType);
        }

        @Override
        public final Stream<Role.Remote> playing() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setTypePlayingIterReq(ConceptProto.Type.Playing.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getTypePlayingIterRes().getRole()).map(Concept.Remote::asRole);
        }

        @Override
        public Type.Remote<SomeRemoteType, SomeRemoteThing> key(AttributeType<?> attributeType) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypeKeyReq(ConceptProto.Type.Key.Req.newBuilder()
                                           .setAttributeType(RequestBuilder.ConceptMessage.from(attributeType))).build();

            runMethod(method);
            return this;
        }

        @Override
        public Type.Remote<SomeRemoteType, SomeRemoteThing> has(AttributeType<?> attributeType) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypeHasReq(ConceptProto.Type.Has.Req.newBuilder()
                                           .setAttributeType(RequestBuilder.ConceptMessage.from(attributeType))).build();

            runMethod(method);
            return this;
        }

        @Override
        public Type.Remote<SomeRemoteType, SomeRemoteThing> plays(Role role) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypePlaysReq(ConceptProto.Type.Plays.Req.newBuilder()
                                             .setRole(RequestBuilder.ConceptMessage.from(role))).build();

            runMethod(method);
            return this;
        }

        @Override
        public Type.Remote<SomeRemoteType, SomeRemoteThing> unkey(AttributeType<?> attributeType) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypeUnkeyReq(ConceptProto.Type.Unkey.Req.newBuilder()
                                             .setAttributeType(RequestBuilder.ConceptMessage.from(attributeType))).build();

            runMethod(method);
            return this;
        }

        @Override
        public Type.Remote<SomeRemoteType, SomeRemoteThing> unhas(AttributeType<?> attributeType) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypeUnhasReq(ConceptProto.Type.Unhas.Req.newBuilder()
                                             .setAttributeType(RequestBuilder.ConceptMessage.from(attributeType))).build();

            runMethod(method);
            return this;
        }

        @Override
        public Type.Remote<SomeRemoteType, SomeRemoteThing> unplay(Role role) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypeUnplayReq(ConceptProto.Type.Unplay.Req.newBuilder()
                                              .setRole(RequestBuilder.ConceptMessage.from(role))).build();

            runMethod(method);
            return this;
        }

        protected abstract Thing.Remote<SomeRemoteThing, SomeRemoteType> asInstance(Concept.Remote<?> concept);

        @Override
        protected abstract Type.Remote<SomeRemoteType, SomeRemoteThing> asCurrentBaseType(Concept.Remote<?> other);

        protected abstract boolean equalsCurrentBaseType(Concept.Remote<?> other);
    }
}
