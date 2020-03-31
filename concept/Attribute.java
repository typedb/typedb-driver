package grakn.client.concept;

import grakn.client.GraknClient;
import grakn.client.concept.remote.RemoteAttribute;

import javax.annotation.CheckReturnValue;

public interface Attribute<D,
        AttrThing extends Attribute<D, AttrThing, AttrType>,
        AttrType extends AttributeType<D, AttrType, AttrThing>>
        extends Thing<AttrThing, AttrType> {
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
    AttrType type();

    /**
     * Retrieves the data type of this Attribute's AttributeType.
     *
     * @return The data type of this Attribute's type.
     */
    @CheckReturnValue
    DataType<D> dataType();

    //------------------------------------- Other ---------------------------------
    @SuppressWarnings("unchecked")
    @Deprecated
    @CheckReturnValue
    @Override
    default AttrThing asAttribute() {
        return (AttrThing) this;
    }

    @CheckReturnValue
    @Override
    default RemoteAttribute<D> asRemote(GraknClient.Transaction tx) {
        return RemoteAttribute.of(tx, id());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isAttribute() {
        return true;
    }
}
