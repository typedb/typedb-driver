package com.vaticle.typedb.client.cluster;

import com.vaticle.typedb.client.api.user.UserManager;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.CLUSTER_USER_DOES_NOT_EXIST;

public class ClusterUserManager implements UserManager {

    @Override
    public ClusterUser get(String name) {
        if (contains(name)) return new ClusterUser(name);
        else throw new TypeDBClientException(CLUSTER_USER_DOES_NOT_EXIST, name);
    }

    @Override
    public boolean contains(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void create(String name, String password) {
        throw new UnsupportedOperationException();
    }
}
