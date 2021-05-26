package com.vaticle.typedb.client.api.user;

import javax.annotation.CheckReturnValue;
import java.util.Set;

public interface UserManager {
    @CheckReturnValue
    User get(String name);

    @CheckReturnValue
    boolean contains(String name);

    void create(String name, String password);

    Set<User> all();
}
