package com.vaticle.typedb.client.cluster;

import com.vaticle.typedb.client.api.user.User;

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
