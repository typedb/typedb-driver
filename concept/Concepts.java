package grakn.client.concept;

import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.Rule;
import grakn.client.concept.type.ThingType;
import grakn.protocol.ConceptProto;
import grakn.protocol.TransactionProto;
import graql.lang.pattern.Pattern;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Concepts {

    ThingType.Remote getRootType();

    EntityType.Remote getRootEntityType();

    RelationType.Remote getRootRelationType();

    AttributeType.Remote getRootAttributeType();

    RoleType.Remote getRootRoleType();

    Rule.Remote getRootRule();

    EntityType.Remote putEntityType(String label);

    @Nullable
    EntityType.Remote getEntityType(String label);

    RelationType.Remote putRelationType(String label);

    @Nullable
    RelationType.Remote getRelationType(String label);

    AttributeType.Remote putAttributeType(String label, AttributeType.ValueType valueType);

    @Nullable
    AttributeType.Remote getAttributeType(String label);

    Rule.Remote putRule(String label, Pattern when, Pattern then);

    @Nullable
    Rule.Remote getRule(String label);

    @Nullable
    grakn.client.concept.type.Type.Remote getType(String label);

    @Nullable
    grakn.client.concept.type.Type.Local getCachedType(String label);

    @Nullable
    Thing.Remote getThing(String iid);

    TransactionProto.Transaction.Res runThingMethod(String iid, ConceptProto.ThingMethod.Req thingMethod);

    TransactionProto.Transaction.Res runTypeMethod(String label, ConceptProto.TypeMethod.Req typeMethod);

    <T> Stream<T> iterateThingMethod(String iid, ConceptProto.ThingMethod.Iter.Req thingMethod, Function<ConceptProto.ThingMethod.Iter.Res, T> responseReader);

    <T> Stream<T> iterateTypeMethod(String label, ConceptProto.TypeMethod.Iter.Req typeMethod, Function<ConceptProto.TypeMethod.Iter.Res, T> responseReader);
}
