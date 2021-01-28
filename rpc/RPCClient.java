package grakn.client.rpc;

import grakn.client.Grakn;
import grakn.client.GraknOptions;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

public class RPCClient implements Grakn.Client {

    private final ManagedChannel channel;
    private final RPCDatabaseManager databases;

    public RPCClient(String address) {
        channel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
        databases = new RPCDatabaseManager(channel);
    }

    @Override
    public RPCSession session(String database, Grakn.Session.Type type) {
        return session(database, type, GraknOptions.core());
    }

    @Override
    public RPCSession session(String database, Grakn.Session.Type type, GraknOptions options) {
        return new RPCSession(this, database, type, options);
    }

    @Override
    public RPCDatabaseManager databases() {
        return databases;
    }

    @Override
    public boolean isOpen() {
        return !channel.isShutdown();
    }

    @Override
    public void close() {
        try {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Channel channel() {
        return channel;
    }
}
