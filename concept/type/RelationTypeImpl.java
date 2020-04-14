package grakn.client.concept.type;

import grakn.client.GraknClient;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.Label;
import grakn.client.concept.thing.Relation;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.session.ConceptProto;

import java.util.stream.Stream;

public class RelationTypeImpl {
    /**
     * Client implementation of RelationType
     */
    public static class Local extends TypeImpl.Local<RelationType, Relation> implements RelationType.Local {

        public Local(ConceptProto.Concept concept) {
            super(concept);
        }
    }

    /**
     * Client implementation of RelationType
     */
    public static class Remote extends TypeImpl.Remote<RelationType, Relation> implements RelationType.Remote {

        public Remote(GraknClient.Transaction tx, ConceptId id) {
            super(tx, id);
        }

        @Override
        public final RelationType.Remote key(AttributeType<?> attributeType) {
            return (RelationType.Remote) super.key(attributeType);
        }

        @Override
        public final RelationType.Remote has(AttributeType<?> attributeType) {
            return (RelationType.Remote) super.key(attributeType);

        }

        @Override
        public final RelationType.Remote plays(Role role) {
            return (RelationType.Remote) super.plays(role);

        }

        @Override
        public final RelationType.Remote unkey(AttributeType<?> attributeType) {
            return (RelationType.Remote) super.unkey(attributeType);

        }

        @Override
        public final RelationType.Remote unhas(AttributeType<?> attributeType) {
            return (RelationType.Remote) super.unhas(attributeType);

        }

        @Override
        public final RelationType.Remote unplay(Role role) {
            return (RelationType.Remote) super.unplay(role);
        }

        @Override
        public final RelationType.Remote isAbstract(Boolean isAbstract) {
            return (RelationType.Remote) super.isAbstract(isAbstract);
        }

        @Override
        public final Stream<Relation.Remote> instances() {
            return super.instances().map(this::asInstance);
        }

        @Override
        public final Stream<RelationType.Remote> sups() {
            return super.sups().map(this::asCurrentBaseType);
        }

        @Override
        public final Stream<RelationType.Remote> subs() {
            return super.subs().map(this::asCurrentBaseType);
        }

        @Override
        public final RelationType.Remote label(Label label) {
            return (RelationType.Remote) super.label(label);
        }

        @Override
        public final RelationType.Remote sup(RelationType type) {
            return (RelationType.Remote) super.sup(type);
        }

        @Override
        public final Relation.Remote create() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setRelationTypeCreateReq(ConceptProto.RelationType.Create.Req.getDefaultInstance()).build();

            return Concept.Remote.of(runMethod(method).getRelationTypeCreateRes().getRelation(), tx());
        }

        @Override
        public final Stream<Role.Remote> roles() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setRelationTypeRolesIterReq(ConceptProto.RelationType.Roles.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getRelationTypeRolesIterRes().getRole()).map(Concept.Remote::asRole);
        }

        @Override
        public final RelationType.Remote relates(Role role) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setRelationTypeRelatesReq(ConceptProto.RelationType.Relates.Req.newBuilder()
                                                       .setRole(RequestBuilder.ConceptMessage.from(role))).build();

            runMethod(method);
            return this;
        }

        @Override
        public final RelationType.Remote unrelate(Role role) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setRelationTypeUnrelateReq(ConceptProto.RelationType.Unrelate.Req.newBuilder()
                                                        .setRole(RequestBuilder.ConceptMessage.from(role))).build();

            runMethod(method);
            return this;
        }

        @Override
        protected final Relation.Remote asInstance(Concept.Remote<?> concept) {
            return concept.asRelation();
        }

        @Override
        protected final RelationType.Remote asCurrentBaseType(Concept.Remote<?> other) {
            return other.asRelationType();
        }

        @Override
        protected final boolean equalsCurrentBaseType(Concept.Remote<?> other) {
            return other.isRelationType();
        }

    }
}
