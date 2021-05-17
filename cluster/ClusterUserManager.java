package com.vaticle.typedb.client.cluster;

import com.vaticle.typedb.client.api.user.UserManager;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.CLUSTER_USER_DOES_NOT_EXIST;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Cluster.UserManager.containsReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Cluster.UserManager.createReq;

public class ClusterUserManager implements UserManager {

    private static final String SYSTEM_DB = "_system";

    private final ClusterClient client;

    public ClusterUserManager(ClusterClient client) {
        this.client = client;
    }

    @Override
    public ClusterUser get(String name) {
        if (contains(name)) return new ClusterUser(name);
        else throw new TypeDBClientException(CLUSTER_USER_DOES_NOT_EXIST, name);
    }

    @Override
    public boolean contains(String name) {
        return new FailsafeTask<Boolean>(client, SYSTEM_DB) {
            @Override
            Boolean rerun(ClusterDatabase.Replica replica) {
                return run(replica);
            }

            @Override
            Boolean run(ClusterDatabase.Replica replica) {
                return client.stub(replica.address())
                        .userContains(containsReq(name))
                        .getContains();
            }
        }.runPrimaryReplica();
    }

    @Override
    public void create(String name, String password) {
        new FailsafeTask<Void>(client, SYSTEM_DB) {
            @Override
            Void rerun(ClusterDatabase.Replica replica) {
                run(replica);
                return null;
            }

            @Override
            Void run(ClusterDatabase.Replica replica) {
                client.stub(replica.address())
                        .userCreate(createReq(name, password));
                return null;
            }
        }.runPrimaryReplica();
    }
}
