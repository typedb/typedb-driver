package grakn.client.concept.local;

import grakn.client.concept.SchemaConcept;

public interface LocalSchemaConcept<T extends LocalSchemaConcept<T>> extends SchemaConcept<T>, LocalConcept<T> {
}
