package com.vaticle.typedb.client.core;

import com.vaticle.typedb.client.common.rpc.TypeDBConnectionFactory;

public class CoreClient extends TypeDBClientImpl {

    public CoreClient(String address, TypeDBConnectionFactory.Core core, int calculateParallelisation) {
        super(address, core, calculateParallelisation);
    }

    public static CoreClient create(String address) {
        return new CoreClient(address, new TypeDBConnectionFactory.Core(), calculateParallelisation());
    }

    public static CoreClient create(String address, int parallelisation) {
        return new CoreClient(address, new TypeDBConnectionFactory.Core(), parallelisation);
    }
}
