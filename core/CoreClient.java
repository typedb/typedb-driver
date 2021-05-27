package com.vaticle.typedb.client.core;

import com.vaticle.typedb.client.common.rpc.TypeDBConnectionFactory;

public class CoreClient extends TypeDBClientImpl {
    public CoreClient(String address, int parallelisation) {
        super(address, new TypeDBConnectionFactory.Core(), parallelisation);
    }

    public static CoreClient create(String address) {
        return new CoreClient(address, calculateParallelisation());
    }

    public static CoreClient create(String address, int parallelisation) {
        return new CoreClient(address, parallelisation);
    }
}
