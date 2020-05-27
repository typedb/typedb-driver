package grakn.client;

import javax.annotation.CheckReturnValue;
import java.io.Serializable;

public interface Keyspace extends Serializable {
    String DEFAULT = "grakn";

    @CheckReturnValue
    static Keyspace of(String name) {
        return new GraknClientImpl.Keyspace(name);
    }

    @CheckReturnValue
    String name();
}
