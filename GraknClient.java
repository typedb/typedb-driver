package grakn.client;

import io.grpc.ManagedChannel;

public interface GraknClient {
    String DEFAULT_URI = "localhost:48555";

    static GraknClient open() {
        return new GraknClientImpl();
    }

    static GraknClient open(String address) {
        return new GraknClientImpl(address);
    }

    static GraknClient open(String address, String username, String password) {
        return new GraknClientImpl(address, username, password);
    }

    GraknClient overrideChannel(ManagedChannel channel);

    void close();

    boolean isOpen();

    Session session(String keyspace);

    Keyspaces keyspaces();

}
