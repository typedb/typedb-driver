package grakn.client.concept;

import grakn.client.GraknClient;
import grakn.client.concept.remote.RemoteAttributeType;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

public interface AttributeType<D,
        AttrType extends AttributeType<D, AttrType, AttrThing>,
        AttrThing extends Attribute<D, AttrThing, AttrType>>
        extends Type<AttrType, AttrThing> {

    //------------------------------------- Accessors ---------------------------------
    /**
     * Get the data type to which instances of the AttributeType must conform.
     *
     * @return The data type to which instances of this Attribute  must conform.
     */
    @Nullable
    @CheckReturnValue
    DataType<D> dataType();

    //------------------------------------- Other ---------------------------------
    @SuppressWarnings("unchecked")
    @Deprecated
    @CheckReturnValue
    @Override
    default AttrType asAttributeType() {
        return (AttrType) this;
    }

    @SuppressWarnings("unchecked")
    @CheckReturnValue
    @Override
    default RemoteAttributeType<D> asRemote(GraknClient.Transaction tx) {
        return RemoteAttributeType.of(tx, id());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isAttributeType() {
        return true;
    }
}
