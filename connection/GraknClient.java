package grakn.client.connection;

import grakn.client.Grakn.Client;
import grakn.client.Grakn.DatabaseManager;
import grakn.client.Grakn.Session;
import static grakn.client.Grakn.Session.Type.DATA;
import static grakn.client.Grakn.Session.Type.SCHEMA;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Entry-point which communicates with a running Grakn server using gRPC.
 */
public class GraknClient implements Client {

    public static final String DEFAULT_URI = "localhost:48555";

    private ManagedChannel channel;
    private final String username;
    private final String password;
    private final DatabaseManager databases;

    public GraknClient() {
        this(DEFAULT_URI);
    }

    public GraknClient(String address) {
        this(address, null, null);
    }

    public GraknClient(String address, String username, String password) {
        channel = ManagedChannelBuilder.forTarget(address)
                .usePlaintext().build();
        this.username = username;
        this.password = password;
        databases = new GraknDatabaseManager(channel);
    }

    @Override
    public GraknClient overrideChannel(ManagedChannel channel) {
        this.channel = channel;
        return this;
    }

    public void close() {
        channel.shutdown();
        try {
            channel.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean isOpen() {
        return !channel.isShutdown() && !channel.isTerminated();
    }

    @Override
    public Session session(String databaseName) {
        return session(databaseName, DATA);
    }

    @Override
    public Session schemaSession(String databaseName) {
        return session(databaseName, SCHEMA);
    }

    @Override
    public Session session(String databaseName, Session.Type type) {
        return new GraknSession(channel, username, password, databaseName, type);
    }

    @Override
    public DatabaseManager databases() {
        return databases;
    }
}
