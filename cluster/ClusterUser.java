package com.vaticle.typedb.client.cluster;

import com.vaticle.typedb.client.api.user.User;

public class ClusterUser implements User {

    public ClusterUser(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String name() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String password() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void password(String password) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException();
    }
}
