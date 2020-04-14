package grakn.client.concept.thing.impl;

import grakn.client.GraknClient;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.DataType;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.protocol.session.ConceptProto;

import java.util.stream.Stream;

import static grakn.client.concept.DataType.staticCastValue;

public class AttributeImpl {
    /**
     * Client implementation of Attribute
     *
     * @param <D> The data type of this attribute
     */
    public static class Local<D> extends ThingImpl.Local<Attribute<D>, AttributeType<D>> implements Attribute.Local<D> {

        private final D value;

        public Local(ConceptProto.Concept concept) {
            super(concept);
            this.value = DataType.staticCastValue(concept.getValueRes().getValue());
        }

        @Override
        public final D value() {
            return value;
        }

        @Override
        public final DataType<D> dataType() {
            return type().dataType();
        }
    }

    /**
     * Client implementation of Attribute
     *
     * @param <D> The data type of this attribute
     */
    public static class Remote<D> extends ThingImpl.Local.Remote<Attribute<D>, AttributeType<D>> implements Attribute.Remote<D> {

        public Remote(GraknClient.Transaction tx, ConceptId id) {
            super(tx, id);
        }

        @Override
        public final D value() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setAttributeValueReq(ConceptProto.Attribute.Value.Req.getDefaultInstance()).build();

            ConceptProto.ValueObject value = runMethod(method).getAttributeValueRes().getValue();
            return staticCastValue(value);
        }

        @Override
        public final Stream<Thing.Remote<?, ?>> owners() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setAttributeOwnersIterReq(ConceptProto.Attribute.Owners.Iter.Req.getDefaultInstance()).build();
            return conceptStream(method, res -> res.getAttributeOwnersIterRes().getThing()).map(Concept.Remote::asThing);
        }

        @Override
        public final DataType<D> dataType() {
            return type().dataType();
        }

        @Override
        public AttributeType.Remote<D> type() {
            return (AttributeType.Remote<D>) super.type();
        }

        @Override
        public Attribute.Remote<D> has(Attribute<?> attribute) {
            return (Attribute.Remote<D>) super.has(attribute);
        }

        @Override
        public Attribute.Remote<D> unhas(Attribute<?> attribute) {
            return (Attribute.Remote<D>) super.unhas(attribute);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected final Attribute.Remote<D> asCurrentBaseType(Concept.Remote<?> other) {
            return (Attribute.Remote<D>) other.asAttribute();
        }
    }
}
