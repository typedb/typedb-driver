package grakn.client.concept.thing;

import grakn.client.GraknClient;
import grakn.client.concept.ConceptId;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.DataType;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

public interface Attribute<D> extends Thing<Attribute<D>, AttributeType<D>> {
    //------------------------------------- Accessors ----------------------------------

    /**
     * Retrieves the value of the Attribute.
     *
     * @return The value itself
     */
    @CheckReturnValue
    D value();

    /**
     * Retrieves the type of the Attribute, that is, the AttributeType of which this resource is an Thing.
     *
     * @return The AttributeType of which this resource is an Thing.
     */
    @Override
    AttributeType<D> type();

    /**
     * Retrieves the data type of this Attribute's AttributeType.
     *
     * @return The data type of this Attribute's type.
     */
    @CheckReturnValue
    DataType<D> dataType();

    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default Attribute<D> asAttribute() {
        return this;
    }

    @CheckReturnValue
    @Override
    default Thing.Remote<Attribute<D>, AttributeType<D>> asRemote(GraknClient.Transaction tx) {
        return Attribute.Remote.of(tx, id());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isAttribute() {
        return true;
    }

    /**
     * Represent a literal Attribute in the graph.
     * Acts as an Thing when relating to other instances except it has the added functionality of:
     * 1. It is unique to its AttributeType based on it's value.
     * 2. It has an AttributeType.DataType associated with it which constrains the allowed values.
     *
     * @param <D> The data type of this resource type.
     *            Supported Types include: String, Long, Double, and Boolean
     */
    interface Local<D> extends Thing.Local<Attribute<D>, AttributeType<D>>, Attribute<D> {
    }

    /**
     * Represent a literal Attribute in the graph.
     * Acts as an Thing when relating to other instances except it has the added functionality of:
     * 1. It is unique to its AttributeType based on it's value.
     * 2. It has an AttributeType.DataType associated with it which constrains the allowed values.
     *
     * @param <D> The data type of this resource type.
     *            Supported Types include: String, Long, Double, and Boolean
     */
    interface Remote<D> extends Thing.Remote<Attribute<D>, AttributeType<D>>, Attribute<D> {

        static <D> Attribute.Remote<D> of(GraknClient.Transaction tx, ConceptId id) {
            return new AttributeImpl.Remote<>(tx, id);
        }

        //------------------------------------- Accessors ----------------------------------

        /**
         * Retrieves the type of the Attribute, that is, the AttributeType of which this resource is an Thing.
         *
         * @return The AttributeType of which this resource is an Thing.
         */
        @Override
        AttributeType.Remote<D> type();

        /**
         * Retrieves the set of all Instances that possess this Attribute.
         *
         * @return The list of all Instances that possess this Attribute.
         */
        @CheckReturnValue
        Stream<Thing.Remote<?, ?>> owners();

        /**
         * Creates a relation from this instance to the provided Attribute.
         *
         * @param attribute The Attribute to which a relation is created
         * @return The instance itself
         */
        @Override
        Attribute.Remote<D> has(Attribute<?> attribute);

        /**
         * Removes the provided Attribute from this Attribute
         *
         * @param attribute the Attribute to be removed
         * @return The Attribute itself
         */
        @Override
        Attribute.Remote<D> unhas(Attribute<?> attribute);

        //------------------------------------- Other ---------------------------------
        @Deprecated
        @CheckReturnValue
        @Override
        default Attribute.Remote<D> asAttribute() {
            return this;
        }

        @Deprecated
        @CheckReturnValue
        @Override
        default boolean isAttribute() {
            return true;
        }
    }
}
