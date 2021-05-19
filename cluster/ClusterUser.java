package com.vaticle.typedb.client.cluster;

import com.vaticle.typedb.client.api.user.User;

import static com.vaticle.typedb.client.cluster.ClusterUserManager.SYSTEM_DB;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Cluster.User.deleteReq;

public class ClusterUser implements User {

    private final ClusterClient client;
    private final String name;

    public ClusterUser(ClusterClient client, String name) {
        this.client = client;
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void password(String password) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete() {
        FailsafeTask<Void> failsafeTask = new FailsafeTask<Void>(client, SYSTEM_DB) {
            @Override
            Void run(ClusterDatabase.Replica replica) {
                client.stub(replica.address()).userDelete(deleteReq(name));
                return null;
            }
        };
        failsafeTask.runPrimaryReplica();
    }
}
