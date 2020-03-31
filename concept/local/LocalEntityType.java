package grakn.client.concept.local;

import grakn.client.concept.EntityType;

public interface LocalEntityType extends LocalType<LocalEntityType, LocalEntity>,
        EntityType<LocalEntityType, LocalEntity> {
}
