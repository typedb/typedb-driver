package com.vaticle.typedb.client.rpc.core;

import com.vaticle.typedb.client.rpc.TypeDBConnectionFactory;
import com.vaticle.typedb.client.rpc.TypeDBClientAbstract;

public class CoreClient extends TypeDBClientAbstract {
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
