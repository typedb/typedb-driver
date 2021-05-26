package com.vaticle.typedb.client.api.user;

import javax.annotation.CheckReturnValue;

public interface User {
    @CheckReturnValue
    String name();

    void delete();
}
