package com.vaticle.typedb.client.cluster;

import com.vaticle.typedb.client.common.rpc.ManagedChannelFactory;
import com.vaticle.typedb.client.core.CoreClient;

import javax.annotation.Nullable;
import java.nio.file.Path;

class ClusterNodeClient extends CoreClient {
    public ClusterNodeClient(String address, ManagedChannelFactory managedChannelFactory, int parallelisation) {
        super(address, managedChannelFactory, parallelisation);
    }

    static ClusterNodeClient create(String address, boolean tlsEnabled, @Nullable Path tlsRootCA, int parallelisation) {
        ManagedChannelFactory channel;
        if (tlsEnabled) {
            channel = tlsRootCA != null ? new ManagedChannelFactory.TLS(tlsRootCA) : new ManagedChannelFactory.TLS();

        } else {
            channel = new ManagedChannelFactory.PlainText();
        }
        return new ClusterNodeClient(address, channel, parallelisation);
    }
}
