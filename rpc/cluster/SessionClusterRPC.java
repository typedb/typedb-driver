/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package grakn.client.rpc.cluster;

import grakn.client.GraknClient;
import grakn.client.GraknOptions;
import grakn.client.rpc.ClientRPC;
import grakn.client.rpc.SessionRPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionClusterRPC implements GraknClient.Session {
    private static final Logger LOG = LoggerFactory.getLogger(GraknClient.Session.class);
    private final ClientClusterRPC clusterClient;
    private final GraknOptions.Cluster options;
    private ClientRPC coreClient;
    private SessionRPC coreSession;

    public SessionClusterRPC(ClientClusterRPC clusterClient, ServerAddress serverAddress, String database, GraknClient.Session.Type type, GraknOptions.Cluster options) {
        this.clusterClient = clusterClient;
        this.coreClient = clusterClient.coreClient(serverAddress);
        LOG.debug("Opening a session to '{}'", serverAddress);
        this.coreSession = coreClient.session(database, type, options);
        this.options = options;
    }

    @Override
    public GraknClient.Transaction transaction(GraknClient.Transaction.Type type) {
        return transaction(type, GraknOptions.cluster());
    }

    @Override
    public GraknClient.Transaction transaction(GraknClient.Transaction.Type type, GraknOptions options) {
        GraknOptions.Cluster clusterOpt = options.asCluster();
        if (clusterOpt.readAnyReplica().isPresent() && clusterOpt.readAnyReplica().get()) {
            return transactionAnyReplica(type, clusterOpt);
        } else {
            return transactionPrimaryReplica(type, options);
        }
    }

    private GraknClient.Transaction transactionPrimaryReplica(GraknClient.Transaction.Type type, GraknOptions options) {
        return transactionFailsafeTask(type, options).runPrimaryReplica();
    }

    private GraknClient.Transaction transactionAnyReplica(GraknClient.Transaction.Type type, GraknOptions.Cluster options) {
        return transactionFailsafeTask(type, options).runAnyReplica();
    }

    private FailsafeTask<GraknClient.Transaction> transactionFailsafeTask(GraknClient.Transaction.Type type, GraknOptions options) {
        return new FailsafeTask<GraknClient.Transaction>(clusterClient, database().name()) {

            @Override
            GraknClient.Transaction run(DatabaseClusterRPC.Replica replica) {
                return coreSession.transaction(type, options);
            }

            @Override
            GraknClient.Transaction rerun(DatabaseClusterRPC.Replica replica) {
                if (coreSession != null) coreSession.close();
                coreClient = clusterClient.coreClient(replica.address());
                coreSession = coreClient.session(database().name(), SessionClusterRPC.this.type(), SessionClusterRPC.this.options());
                return coreSession.transaction(type, options);
            }
        };
    }

    @Override
    public GraknClient.Session.Type type() {
        return coreSession.type();
    }

    @Override
    public GraknOptions.Cluster options() {
        return options;
    }

    @Override
    public boolean isOpen() {
        return coreSession.isOpen();
    }

    @Override
    public void close() {
        coreSession.close();
    }

    @Override
    public GraknClient.Database database() {
        return coreSession.database();
    }
}
