package grakn.client.concept.thing;

import grakn.client.GraknClient;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.Role;
import grakn.client.concept.type.RelationType;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.session.ConceptProto;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class RelationImpl {
    /**
     * Client implementation of Relation
     */
    public static class Local extends ThingImpl.Local<Relation, RelationType> implements Relation.Local {

        public Local(ConceptProto.Concept concept) {
            super(concept);
        }
    }

    /**
     * Client implementation of Relation
     */
    public static class Remote extends ThingImpl.Local.Remote<Relation, RelationType> implements Relation.Remote {

        public Remote(GraknClient.Transaction tx, ConceptId id) {
            super(tx, id);
        }

        @Override
        public final RelationType.Remote type() {
            return (RelationType.Remote) super.type();
        }

        @Override
        public Relation.Remote has(Attribute<?> attribute) {
            return (Relation.Remote) super.has(attribute);
        }

        @Override
        public Relation.Remote unhas(Attribute<?> attribute) {
            return (Relation.Remote) super.unhas(attribute);
        }

        @Override
        public final Map<Role.Remote, Set<Thing.Remote<?, ?>>> rolePlayersMap() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setRelationRolePlayersMapIterReq(ConceptProto.Relation.RolePlayersMap.Iter.Req.getDefaultInstance()).build();

            Stream<ConceptProto.Relation.RolePlayersMap.Iter.Res> stream = tx().iterateConceptMethod(id(), method, ConceptProto.Method.Iter.Res::getRelationRolePlayersMapIterRes);

            Map<Role.Remote, Set<Thing.Remote<?, ?>>> rolePlayerMap = new HashMap<>();
            stream.forEach(rolePlayer -> {
                Role.Remote role = Concept.Remote.of(rolePlayer.getRole(), tx()).asRole();
                Thing.Remote<?, ?> player = Concept.Remote.of(rolePlayer.getPlayer(), tx()).asThing();
                if (rolePlayerMap.containsKey(role)) {
                    rolePlayerMap.get(role).add(player);
                } else {
                    rolePlayerMap.put(role, new HashSet<>(Collections.singletonList(player)));
                }
            });

            return rolePlayerMap;
        }

        @Override
        public final Stream<Thing.Remote<?, ?>> rolePlayers(Role... roles) {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setRelationRolePlayersIterReq(ConceptProto.Relation.RolePlayers.Iter.Req.newBuilder()
                            .addAllRoles(RequestBuilder.ConceptMessage.concepts(Arrays.asList(roles)))).build();

            return conceptStream(method, res -> res.getRelationRolePlayersIterRes().getThing()).map(Concept.Remote::asThing);
        }

        @Override
        public final Relation.Remote assign(Role role, Thing<?, ?> player) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setRelationAssignReq(ConceptProto.Relation.Assign.Req.newBuilder()
                            .setRole(RequestBuilder.ConceptMessage.from(role))
                            .setPlayer(RequestBuilder.ConceptMessage.from(player))).build();

            runMethod(method);
            return asCurrentBaseType(this);
        }

        @Override
        public final void unassign(Role role, Thing<?, ?> player) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setRelationUnassignReq(ConceptProto.Relation.Unassign.Req.newBuilder()
                            .setRole(RequestBuilder.ConceptMessage.from(role))
                            .setPlayer(RequestBuilder.ConceptMessage.from(player))).build();

            runMethod(method);
        }

        @Override
        protected final Relation.Remote asCurrentBaseType(Concept.Remote<?> other) {
            return other.asRelation();
        }

    }
}
