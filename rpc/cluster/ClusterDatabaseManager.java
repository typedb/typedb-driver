package grakn.client.rpc.cluster;

import grakn.client.Grakn;
import grakn.client.rpc.RPCDatabaseManager;

import java.util.List;
import java.util.Map;

public class ClusterDatabaseManager implements Grakn.DatabaseManager {
    private final Map<Address.Server, RPCDatabaseManager> databaseManagers;

    public ClusterDatabaseManager(Map<Address.Server, RPCDatabaseManager> databaseManagers) {
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
