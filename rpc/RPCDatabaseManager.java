package grakn.client.rpc;

import com.google.common.collect.ImmutableList;
import grakn.client.Grakn;
import grakn.client.common.exception.GraknClientException;
import grakn.protocol.DatabaseProto;
import grakn.protocol.GraknGrpc;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import static grakn.client.common.exception.ErrorMessage.Client.MISSING_DB_NAME;

public class RPCDatabaseManager {
    public static class Core implements Grakn.DatabaseManager {
        private final GraknGrpc.GraknBlockingStub blockingGrpcStub;

        public Core(Channel channel) {
            blockingGrpcStub = GraknGrpc.newBlockingStub(channel);
        }

        @Override
        public boolean contains(String name) {
            return request(() -> blockingGrpcStub.databaseContains(DatabaseProto.Database.Contains.Req.newBuilder().setName(nonNull(name)).build()).getContains());
        }

        @Override
        public void create(String name) {
            request(() -> blockingGrpcStub.databaseCreate(DatabaseProto.Database.Create.Req.newBuilder().setName(nonNull(name)).build()));
        }

        @Override
        public void delete(String name) {
            request(() -> blockingGrpcStub.databaseDelete(DatabaseProto.Database.Delete.Req.newBuilder().setName(nonNull(name)).build()));
        }

        @Override
        public List<String> all() {
            return request(() -> ImmutableList.copyOf(blockingGrpcStub.databaseAll(DatabaseProto.Database.All.Req.getDefaultInstance()).getNamesList()));
        }

        private String nonNull(String name) {
            if (name == null) throw new GraknClientException(MISSING_DB_NAME);
            return name;
        }

        private static <RES> RES request(Supplier<RES> req) {
            try {
                return req.get();
            } catch (StatusRuntimeException e) {
                throw new GraknClientException(e);
            }
        }
    }

    public static class Cluster implements Grakn.DatabaseManager {
        private final ConcurrentMap<Address.Cluster, RPCDatabaseManager.Core> databaseManagers;

        public Cluster(ConcurrentMap<Address.Cluster, RPCDatabaseManager.Core> databaseManagers) {
            this.databaseManagers = databaseManagers;
        }

        @Override
        public boolean contains(String name) {
            return databaseManagers.values().iterator().next().contains(name);
        }

        @Override
        public void create(String name) {
            databaseManagers.values().forEach(dbMgr -> dbMgr.create(name));
        }

        @Override
        public void delete(String name) {
            databaseManagers.values().forEach(dbMgr -> dbMgr.delete(name));
        }

        @Override
        public List<String> all() {
            return databaseManagers.values().iterator().next().all();
        }
    }
}
