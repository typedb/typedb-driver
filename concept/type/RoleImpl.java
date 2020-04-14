package grakn.client.concept.type;

import grakn.client.GraknClient;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.Label;
import grakn.client.concept.SchemaConceptImpl;
import grakn.protocol.session.ConceptProto;

import java.util.stream.Stream;

public class RoleImpl {
    /**
     * Client implementation of Role
     */
    public static class Local extends SchemaConceptImpl.Local<Role> implements Role.Local {

        public Local(ConceptProto.Concept concept) {
            super(concept);
        }
    }

    /**
     * Client implementation of Role
     */
    public static class Remote extends SchemaConceptImpl.Remote<Role> implements Role.Remote {

        public Remote(GraknClient.Transaction tx, ConceptId id) {
            super(tx, id);
        }

        @Override
        public final Stream<Role.Remote> sups() {
            return super.sups().map(this::asCurrentBaseType);
        }

        @Override
        public final Stream<Role.Remote> subs() {
            return super.subs().map(this::asCurrentBaseType);
        }

        @Override
        public final Role.Remote label(Label label) {
            return (Role.Remote) super.label(label);
        }

        @Override
        public Role.Remote sup(Role superRole) {
            return (Role.Remote) super.sup(superRole);
        }

        @Override
        public final Stream<RelationType.Remote> relations() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setRoleRelationsIterReq(ConceptProto.Role.Relations.Iter.Req.getDefaultInstance()).build();
            return conceptStream(method, res -> res.getRoleRelationsIterRes().getRelationType()).map(Concept.Remote::asRelationType);
        }

        @Override
        public final Stream<Type.Remote<?, ?>> players() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setRolePlayersIterReq(ConceptProto.Role.Players.Iter.Req.getDefaultInstance()).build();
            return conceptStream(method, res -> res.getRolePlayersIterRes().getType()).map(Concept.Remote::asType);
        }

        @Override
        protected final Role.Remote asCurrentBaseType(Concept.Remote<?> other) {
            return other.asRole();
        }

        @Override
        protected final boolean equalsCurrentBaseType(Concept.Remote<?> other) {
            return other.isRole();
        }

    }
}
