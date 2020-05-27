package grakn.client;

import java.util.List;

public interface Keyspaces {
    void delete(String name);

    List<String> retrieve();
}
