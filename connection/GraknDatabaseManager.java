package grakn.client.connection;

import com.google.common.collect.ImmutableList;
import grakn.client.Grakn.DatabaseManager;
import grakn.client.exception.GraknClientException;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.GraknGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import java.util.List;

class GraknDatabaseManager implements DatabaseManager {
    private final GraknGrpc.GraknBlockingStub blockingStub;

    GraknDatabaseManager(ManagedChannel channel) {
        blockingStub = GraknGrpc.newBlockingStub(channel);
    }

    @Override
    public boolean contains(String name) {
        try {
            return blockingStub.databaseContains(RequestBuilder.DatabaseMessage.contains(name)).getContains();
        } catch (StatusRuntimeException e) {
            throw GraknClientException.create(e.getMessage(), e);
        }
    }

    @Override
    public void create(String name) {
        try {
            blockingStub.databaseCreate(RequestBuilder.DatabaseMessage.create(name));
        } catch (StatusRuntimeException e) {
            throw GraknClientException.create(e.getMessage(), e);
        }
    }

    @Override
    public void delete(String name) {
        try {
            blockingStub.databaseDelete(RequestBuilder.DatabaseMessage.delete(name));
        } catch (StatusRuntimeException e) {
            throw GraknClientException.create(e.getMessage(), e);
        }
    }

    @Override
    public List<String> all() {
        try {
            return ImmutableList.copyOf(blockingStub.databaseAll(RequestBuilder.DatabaseMessage.all()).getNamesList().iterator());
        } catch (StatusRuntimeException e) {
            throw GraknClientException.create(e.getMessage(), e);
        }
    }
}
