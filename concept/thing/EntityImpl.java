package grakn.client.concept.thing;

import grakn.client.GraknClient;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.type.EntityType;
import grakn.protocol.session.ConceptProto;

public class EntityImpl {
    /**
     * Client implementation of Entity
     */
    public static class Local extends ThingImpl.Local<Entity, EntityType> implements Entity.Local {

        public Local(ConceptProto.Concept concept) {
            super(concept);
        }
    }

    /**
     * Client implementation of Entity
     */
    public static class Remote extends ThingImpl.Local.Remote<Entity, EntityType> implements Entity.Remote {

        public Remote(GraknClient.Transaction tx, ConceptId id) {
            super(tx, id);
        }

        @Override
        public final EntityType.Remote type() {
            return (EntityType.Remote) super.type();
        }

        @Override
        public Entity.Remote has(Attribute<?> attribute) {
            return (Entity.Remote) super.has(attribute);
        }

        @Override
        public Entity.Remote unhas(Attribute<?> attribute) {
            return (Entity.Remote) super.unhas(attribute);
        }

        @Override
        protected final Entity.Remote asCurrentBaseType(Concept.Remote<?> other) {
            return other.asEntity();
        }
    }
}
