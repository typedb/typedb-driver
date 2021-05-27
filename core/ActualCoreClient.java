package com.vaticle.typedb.client.core;

import com.vaticle.typedb.client.common.rpc.TypeDBConnectionFactory;

public class ActualCoreClient extends CoreClient {

    public ActualCoreClient(String address, TypeDBConnectionFactory.ActualCore core, int calculateParallelisation) {
        super(address, core, calculateParallelisation);
    }

    public static ActualCoreClient create(String address) {
        return new ActualCoreClient(address, new TypeDBConnectionFactory.ActualCore(), calculateParallelisation());
    }

    public static ActualCoreClient create(String address, int parallelisation) {
        return new ActualCoreClient(address, new TypeDBConnectionFactory.ActualCore(), parallelisation);
    }
}
