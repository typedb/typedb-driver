package grakn.client.concept.local;

import grakn.client.concept.Entity;

public interface LocalEntity extends LocalThing<LocalEntity, LocalEntityType>,
        Entity<LocalEntity, LocalEntityType> {
}
