package com.vaticle.typedb.client.api.user;

import javax.annotation.CheckReturnValue;

public interface UserManager {
    @CheckReturnValue
    User get(String name);

    @CheckReturnValue
    boolean contains(String name);
    // TODO: Return type should be 'Database' but right now that would require 2 server calls in Cluster

    void create(String name, String password);
}
